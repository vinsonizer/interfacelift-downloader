interfacelift-downloader
========================

You may want to change your file path in the main method of:
 
 - src/main/scala/com/shemasoft/interfacelift/downloader/InterfaceliftDownloader.scala

To run on cygwin:

 - `sbt run && for i in ``cat files.txt``; do cygstart $(cygpath -u $i); done`
