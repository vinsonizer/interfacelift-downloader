package com.shemasoft.interfacelift.downloader

import org.xml.sax.InputSource

/**
  * @author jv
  *
  * A class to abstract away the HTML parsing.  Internally uses tagsoup parser to clean up potentially malformed html
  */
class InterfaceLiftHtmlParser {

  val parserFactory = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
  val parser = parserFactory.newSAXParser()
  val source = new org.xml.sax.InputSource("http://www.scala-lang.org")
  val adapter = new scala.xml.parsing.NoBindingFactoryAdapter

  /**
    * Converts HTML org.xml.sax.InputSource to XML NodeSeq
    * @param source InputSource of HTML
    * @return XML NodeSeq representation of HTML for parsing
    */
  def parseHtml(source: InputSource) = {
    adapter.loadXML(source, parser)
  }
}
