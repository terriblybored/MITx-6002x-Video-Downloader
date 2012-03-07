libraryDependencies ~= { seq =>
  val vers = "0.8.7"
  seq ++ Seq(
    "net.databinder" %% "dispatch-core" % vers,
    "net.databinder" %% "dispatch-http" % vers,
    "net.databinder" %% "dispatch-jsoup" % vers
  )
}


initialCommands := "import dispatch._"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

mainClass := Some("downloader")


