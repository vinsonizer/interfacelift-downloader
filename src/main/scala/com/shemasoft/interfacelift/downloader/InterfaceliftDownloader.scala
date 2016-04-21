package com.shemasoft.interfacelift.downloader

import java.io.{BufferedOutputStream, File, FileOutputStream, FileWriter, InputStream}
import java.net.{URL, URLConnection}
import java.util
import java.util.{Comparator, Properties}

import scala.xml.{Node, NodeSeq, Text}
import org.xml.sax.InputSource

import scala.io.Source

class InterfaceliftDownloader {

  val parser = new InterfaceliftHtmlParser

  def readProperties() : Properties = {
    val props = new Properties()
    props.load(Source.fromFile("src/main/resources/downloader.properties").reader())
    props
  }

  val configProperties = readProperties()
  
  def downLoadImages() = {
    trimOld()
    val targetFolder = configProperties.getProperty("targetFolder")
    val baseUrl = configProperties.getProperty("baseUrl")
    val numberOfPages = configProperties.getProperty("numPages").toInt
    println("Downloading from " + baseUrl + " to " + targetFolder)
    val imageUrls = Range(1, numberOfPages+1, 1).flatMap(idx => {getImageUrls(getUrlContent(baseUrl + "index" + idx + ".html"))})

    val imagesProcessed = imageUrls
      .filter(_.endsWith(".jpg"))
      .filter(_.startsWith("/wallpaper"))
      .distinct.map(downloadImage(_, targetFolder))
      .filter(_.length > 0)

    writeCompletionFile(targetFolder, imagesProcessed)
    println(imagesProcessed.size + " of " + imagesProcessed.size + " downloaded")
  }

  def writeCompletionFile(targetFolder: String, files: Seq[String]) = {
    val output = new FileWriter("files.txt", false)
    files.foreach(x => { output.write(targetFolder + File.separatorChar + x + "\n") })
    output.flush()
    output.close()
  }
  
  def downloadImage(imgUrl: String, targetFolder: String) = {
    println("Downloading from " + imgUrl + " to " + targetFolder)
    val outputFile = new File(targetFolder + File.separatorChar + imgUrl.substring(imgUrl.lastIndexOf("/"), imgUrl.length))
    if(!outputFile.exists) {
      println("downloading " + imgUrl)
      val fileStream = new BufferedOutputStream(new FileOutputStream(outputFile))
      useStream(new URL("http://interfacelift.com" + imgUrl).openConnection) (stream => {
        val buffer:Array[Byte] = new Array[Byte](1024)
	    Iterator.continually(stream.read(buffer)).takeWhile(_ != -1).foreach(n => fileStream.write(buffer,0,n))
      })
      fileStream.flush()
      fileStream.close()
      imgUrl.substring(imgUrl.lastIndexOf("/"))
    } else {
      ""
    }
  }
  
  def getImageUrls(xml: NodeSeq) = {
    (findAttrBy(xml \\ "div", "class", _.equals("download")) \\ "a").map(_.attribute("href").getOrElse(Text(""))).map(_.text)
  }
  
  def getPageNums(xml: Node) = {
    val pageLinks = (findAttrBy(xml \\ "div", "class", _.equals("pagenums_bottom")) \\ "a").toArray
    pageLinks(pageLinks.length - 2).text.toInt
  }
  
  def findAttrBy(xml: NodeSeq, label: String, check: String => Boolean) = {
    xml.filter(x => {(x \\ ("@"+label)).exists(y => {check(y.text)})})
  }

  def getUrlContent(url: String) = {
    println("Getting content for " + url)
    useStream(new URL(url).openConnection) (stream => {
	    val inputSource = new InputSource
	    inputSource.setByteStream(stream)
	    parser.parseHtml(inputSource)      
    })    
  }
  
  def useStream[ReturnType](connection: URLConnection)(func: InputStream => ReturnType) : ReturnType = {
    connection.setRequestProperty("user-agent", "Mozilla")
    val inputStream = connection.getInputStream
    val result = func(inputStream)
    inputStream.close()
    result
  }

  def trimOld() = {
    val directory = configProperties.getProperty("targetFolder")
    val amount = configProperties.getProperty("trimFactor").toInt
    val imgDirFiles = new File(directory).listFiles()
    util.Arrays.sort(imgDirFiles, new Comparator[File]() {
      override def compare(o1: File, o2: File): Int = {
        o1.lastModified().compareTo(o2.lastModified())
      }
    })
    imgDirFiles.take(amount).foreach(_.delete())
  }

}

object InterfaceliftDownloader {

  def main(args: Array[String]) = {
    val d = new InterfaceliftDownloader
    d.downLoadImages()
  }

}
