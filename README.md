interfacelift-downloader
========================

You may want to change your file path in src/main/resources/downloader.properties
 
To run on cygwin:

`sbt run && for i in $(cat files.txt); do cygstart $(cygpath -u $i); done`

or with maven:

`mvn compile exec:java -Dexec.mainClass=com.shemasoft.interfacelift.downloader.InterfaceliftDownloader`
