package com.shemasoft.interfacelift.downloader

import org.xml.sax.InputSource

class InterfaceliftHtmlParser {

  val parserFactory = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
  val parser = parserFactory.newSAXParser()
  val source = new org.xml.sax.InputSource("http://www.scala-lang.org")
  val adapter = new scala.xml.parsing.NoBindingFactoryAdapter

  def parseHtml(source: InputSource) = {
    adapter.loadXML(source, parser)
  }
}
