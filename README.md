#MITx 6002x downloader
Binaries removed, not maintained

##Introduction
Automatically searches for and downloads youtube videos through MITx. Requires java, which you can get from [here](https://www.java.com/en/download/index.jsp)

##Prerequisites
* [Java](https://www.java.com/en/download/index.jsp)

##Usage
You cannot double click on the jar file directly. You must open up the command line, navigate to the folder the jar file is in, and type the following commands.

Protip: On Windows 7 (at least), you can hold down the shift key and right click in explorer to "Open command window here"

Also, most shells allow you to hit the tab key to autocomplete half typed out file names. So for me, I usually type "java -jar default-e8 TAB", before filling in the rest of the arguments.

To use, first type (and autocomplete)

	java -jar default-e89b08_2.9.1-0.1-SNAPSHOT-one-jar.jar EMAIL PASSWORD ?

to view available weeks, replacing the email and password with your own.

After a while, you should see the program exit. Listed will be the weeks available to download. Let us pick Week 1 to download. Hit the up key to display the last command. Erase the "?" and add the week argument. You should get

	java -jar default-e89b08_2.9.1-0.1-SNAPSHOT-one-jar.jar EMAIL PASSWORD "Week 1"

Please remember to retain the quotes around "Week 1", or it will fail. You should replace the week with any week you want (that is listed). All videos in the lecture sequences in that week will be downloaded.

To download HD 720p versions of the lectures, type

	java -jar default-e89b08_2.9.1-0.1-SNAPSHOT-one-jar.jar EMAIL PASSWORD "Week 1" -hd

Note that a "-hd" has been tacked on at the end.

##More (advanced?) options
* If you leave the week argument blank, all videos available will be downloaded
* Tacking on "-hd" anywhere will cause the downloader to prefer to download 720p videos instead (with a fallback to whatevers remaining)

##Possible questions
###Are subtitles downloaded?
Yes they are (in the current version).

###How might I view a changelog?
Click on the commits tab, or click [here](https://github.com/terriblybored/MITx-6002x-Video-Downloader/commits/master), which brings you to the same place. I might update the program once in a while to add more features. Newer features include the ability to download hd videos (see above), and the automatic downloading of subtitles together with the video.

###How might I compile this program for myself?
Download a [zip](https://github.com/terriblybored/MITx-6002x-Video-Downloader/zipball/master) of source code, or clone this repo. 
Download and setup sbt, following the instructions [here](https://github.com/harrah/xsbt/wiki/Getting-Started-Setup)
Modify whatever you want in /src/main/scala
Launch sbt, and type "one-jar" to build the jar, which will now reside in the target\scala-2.9.1 folder.

###Might this program be malicious? :(
The full source is supplied, so you can compile the program yourself. If required, you can also verify that the compiled executable I supply matches that in the repo (and raise hell in the forums if it doesn't).

###Why do you need my username and password?
I need it to login under your account to access the "Courseware" page. This allows me to get all the YouTube links present.

###I got an exception?!
Try rerunning the program. It gets a bit fickle as it will fail on any server error. If you still get problems, email me at the address listed below.

###What language is this program written in?
Scala. I had decided to start programming randomly. This has resulted in horrifically messy code. I hope to learn something out of this.

##Libraries used (you should not need to manually download them)
* [One-](https://github.com/sbt/sbt-onejar)[Jar](http://one-jar.sourceforge.net/)
* [Dispatch](https://github.com/dispatch/dispatch)

Binaries removed, not maintained
====================

If you have any further problems, feel free to email me at terriblybored gmail com or post in the issues page.