import dispatch._
import dispatch.jsoup.JSoupHttp._
import scala.actors.Actor._
import java.io.{File, FileWriter, FileOutputStream}
import scala.collection.JavaConversions._
import scala.util.parsing.json._

object downloader {

  def main(args: Array[String]) {

    var email = ""
    var password = ""

    if (args.length >= 2) {
      email = args(0)
      password = args(1)
    }
    else {
      println("downloader EMAIL PASSWORD \"Week n\"")
      println("e.g. downloader example@example.com password \"Week 1\"")
      println("Omit last argument to download all")
      println("e.g. downloader example@example.com password")
      println("Use ? in place of \"Week n\" to get list of downloadable week names")
      System.exit(-1)
    }

    collection.parallel.ForkJoinTasks.defaultForkJoinPool.setParallelism(30)
    val parsed = new MITx6002x(email, password)
    if (args.length == 3 && args(2) == "?") {
      println("List of downloadable weeks as follows: ")
      parsed.weekList.foreach(x => println(x.name))
    }
    else {
        val filterFunc = if (args.length == 3) {x: Week => x.name == args(2)} else {x: Week => true}
      parsed.weekList.filter(filterFunc).par.foreach(week => week.lectureSequence.par.foreach(lecSeq => (lecSeq.lectures.par.foreach(_.download))))
    }
  }
  

}

object LoginFailure extends Exception

case class Week(name: String, lectureSequence: Seq[LectureSequence])
case class LectureSequence(name: String, lectures: Seq[YoutubeLink])
class MITx6002x(val email: String, val password: String) {
  val http = new Http with thread.Safety
  val site = :/("6002x.mitx.mit.edu").secure

  //grab cookies and token
  val tokenRegex = """(?<=csrftoken=)[\w]*""".r
  val token = http(site >:> {x => x("Set-Cookie").headOption match {
    case Some(cookie) => tokenRegex.findFirstIn(cookie).get
    case _ => {println("Failed to login. Check password or username, or update program"); System.exit(-1); throw LoginFailure}
  }})

  //login
  http(site / "login"
    <:< Map(("Accept"-> "application/json, text/javascript, */*; q=0.01"),("X-Requested-With"->"XMLHttpRequest"),("X-CSRFToken", token))
    << List(("email"->email),("password"->password),("remember"->"false")) 
    >- {x => 
      if (x == """{"success": true}""") {
        println("Successfully logged in")
        }
      else {
        println("Failure to login")
        println("Site returned " + x)
        throw LoginFailure
      }
    })

  //get links to video pages
  val streamRegex = """(?<=var.streams=)\{.*?\}""".r
  val videoNameRegex = """(?<=titles=)\[.*?\]""".r  //Extracts the video titles array from webpage
  val courseNumbersRegex = """S([\d]+)V([\d]+)""".r //Extracts course numbering
  val weekList = http(site / "courseware" 
    </> {x => 
      val weekNames = x.select("div#accordion > h3").map(_.text)
      val rawWeekList = x.select("div#accordion > ul")
      for ((weekName, weekLinks) <- weekNames.zip(rawWeekList).par) yield {
        val names = weekLinks.select("li > a > p").not("p[class]").map(_.text)
        
        val linkType = weekLinks.select("li > a > p[class=subtitle]").map(_.text)
        val links = weekLinks.select("li > a").map(_.attr("href"))
        val videoSequenceLinks = links.zip(linkType).filter(_._2 == "Lecture Sequence").map{_._1}
        
        val lectureSeq = for ((link,name) <- videoSequenceLinks.zip(names).par) yield {
          http(site / link >- {videoPage =>
            val streams = streamRegex.findAllIn(videoPage).flatMap(x => JSON.parseFull(x)).map(x=>x.asInstanceOf[Map[String,String]])
            val ids = streams.map(stream => stream("1.0")).toList //Speed is set over here
            val videoNames = videoNameRegex.findAllIn(videoPage).flatMap(x => JSON.parseFull(x)).next.asInstanceOf[List[String]].filter({x => !"""^S[\d]+V[\d]""".r.findFirstIn(x).isEmpty})  //Grab title names, and filter out those which don't meet SxxVxx

            val videoNamesRenamed = videoNames.map({x => //Normalize numbering
              val originalNumberingString = courseNumbersRegex.findFirstIn(x).get //Let's hope the string really exists!! Validate first (as done in videoNames)!
              val courseNumbersRegex(s, v) = originalNumberingString
              val newNumberingString = "S%02dV%02d".format(s.toInt,v.toInt)
              x.replaceFirst(originalNumberingString,newNumberingString)})

            new LectureSequence(name, videoNamesRenamed.zip(ids).map({ x => new YoutubeLink(x._2,x._1,http)}))
            })
        }
        new Week(weekName, lectureSeq.toList)
      }
    })
    
}

class YoutubeLink(val id: String, val filename: String, val http: HttpExecutor) {
  val downloadLinkRegex = """(?<="url_encoded_fmt_stream_map": ")[\S]*(?=")""".r //Matches Youtube video url list block
  val formatMapRegex = """(?<="fmt_list": ")[\S]*(?=")""".r //Matches the block of urls
  val individualDownloadLinksRegex = """(?<=url=).*?(?=\\u0026)""".r //Matches all the links in different formats
  val invalidChars = """[^\w\.-]""".r //Filename invalid chars

  val preferenceList = List("18","34","5") //Youtube formats that are most preferred in order, h.264 baseline mp4, and then flv for the last two (modify with extensionMap to prefer other formats)
  val extensionMap = Map(("18"->"mp4"),("34"->"flv"),("5"->"flv")) //Denotes file extension that should go with each YouTube format
  def download() {
    try {
      val formatLink = http(url("http://www.youtube.com/watch?v=" + id) >- {x =>
        val formatList = formatMapRegex.findFirstIn(x).get.split(",").map(str => """[\d]+""".r.findFirstIn(str).get)
        val links = individualDownloadLinksRegex.findAllIn(downloadLinkRegex.findFirstIn(x).get).map(_.toString).toList
        val formatLinks = formatList.zip(links)
        val formatValue = preferenceList.filter({x=> formatList.contains(x)}).head
        

        formatLinks.filter(_._1 == formatValue).map(x => (x._1, Request.decode_%(x._2).toString)).head
        }).asInstanceOf[(String,String)]
      http(url(formatLink._2) >> {input =>
          val file = new File(invalidChars.replaceAllIn(filename, " ") + "." + extensionMap(formatLink._1))
          val out = new FileOutputStream(file)
          var buffer = new Array[Byte](512000)
          Iterator.continually(input.read(buffer)).takeWhile(_ != -1).foreach({x => out.write(buffer,0,x)})
          out.close()
      })
    }
    catch {
      case _ => println("WARNING: DOWNLOAD OF " + filename + " FAILED")
    }
  }
}
