package com.shemasoft.interfacelift.downloader

import java.io.{BufferedOutputStream, File, FileOutputStream, InputStream, FileWriter}
import java.net.{URL, URLConnection}
import java.util
import java.util.Comparator

import scala.xml.{Node, NodeSeq, Text }
import org.xml.sax.InputSource

class InterfaceliftDownloader {

  val parser = new InterfaceliftHtmlParser
  
  def getImages(targetFolder: String, baseUrl: String) = {
    println("Downloading from " + baseUrl + " to " + targetFolder)
    val xml = getUrlContent(baseUrl)
    //val pageNums = getPageNums(xml)
    val pageNums = 10
    val imageUrls = Range(1, pageNums+1, 1).map(idx => {getImageUrls(getUrlContent(baseUrl + "index" + idx + ".html"))}).flatten
    val imagesProcessed = imageUrls.filter(_.endsWith(".jpg")).filter(_.startsWith("/wallpaper")).distinct.map(downloadImage(_,
      targetFolder)).filter(_.length > 0)
    writeCompletionFile(targetFolder, imagesProcessed)
    println(imagesProcessed.size + " of " + imagesProcessed.size + " downloaded")
  }

  def writeCompletionFile(targetFolder: String, files: Seq[String]) = {
    val output = new FileWriter("files.txt", false)
    files.foreach(x => { output.write(targetFolder + File.separatorChar + x + "\n") })
    output.flush
    output.close
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
      fileStream.flush
      fileStream.close
      imgUrl.substring(imgUrl.lastIndexOf("/"))
    } else {
      ""
    }
  }
  
  def getImageUrls(xml: NodeSeq) = {
    (findAttrBy((xml \\ "div"), "class", _.equals("download")) \\ "a").map(_.attribute("href").getOrElse(Text(""))).map(_.text)
  }
  
  def getPageNums(xml: Node) = {
    val pageLinks = (findAttrBy((xml \\ "div"), "class", _.equals("pagenums_bottom")) \\ "a").toArray
    pageLinks(pageLinks.size - 2).text.toInt
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
    inputStream.close
    result
  }

  def trimOld(directory: String, amount: Int) = {
    val imgDirFiles = new File(directory).listFiles()
    util.Arrays.sort(imgDirFiles, new Comparator[File]() {
      override def compare(o1: File, o2: File): Int = {
        return o1.lastModified().compareTo(o2.lastModified());
      }
    })
    imgDirFiles.take(amount).foreach(_.delete())
  }

}

object InterfaceliftDownloader {

  def main(args: Array[String]) = {
    val d = new InterfaceliftDownloader
    val target ="""C:\Users\jv\Pictures\InterfaceLift"""
    d.trimOld(target, 20)
    //d.getImages("/home/jason/Pictures/InterfaceLift","http://interfacelift.com/wallpaper/downloads/rating/widescreen/1440x900/")
    d.getImages(target,"http://interfacelift.com/wallpaper/downloads/rating/widescreen/1600x900/")
  }

}
