package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.{StringUtil, TwilioStringValue}

import scala.collection.immutable
import scala.collection.mutable.ListBuffer

/** Represent a element in TwiML (everything within <>) */
sealed trait TwimlElement extends TwilioStringValue {

  override final def hashCode(): Int = xmlCompact.hashCode

  override final def equals(obj: Any): Boolean = obj match {
    case other: TwimlElement => xmlCompact == other.xmlCompact
    case _                   => false
  }

  /** Specify what the name of the tag this TwimlElement represent.
    *
    * This is used when building the XML of the TwiMLElement.
    */
  protected def tagName: String

  /** Specify what attributes the tag this TwiML element represent has.
    *
    * This is used when building the XML of the TwiMLElement.
    *
    * @see
    *   [[TwimlElement.TagAttributeBuilder]] for an easy way to create this value in
    *   implementations.
    */
  protected def tagAttributes: immutable.Seq[(String, String)]

  /** Specify the sub elements the tag this TwiML element represent has.
    *
    * This is used when building the XML of the TwiMLElement.
    */
  protected def tagSubElements: immutable.Seq[TwimlElement]

  /** Specify the value the tag this TwiML element represent has.
    *
    * This is used when building the XML of the TwiMLElement.
    */
  protected def tagValue: Option[String]

  def xmlCompact: String
  def xmlPretty: String

}

object TwimlElement {

  trait NoneRoot extends TwimlElement {

    private lazy val xmlBuilder = new XmlBuilder(tagName, tagAttributes, tagSubElements, tagValue)

    override final lazy val xmlCompact: String = xmlBuilder.compactPrint

    override final lazy val xmlPretty: String = xmlBuilder.prettyPrint

    override final lazy val twilioString: String = xmlCompact
  }

  trait Verb extends NoneRoot
  trait Noun extends NoneRoot

  /** Abstraction over the root element of TwiML.
    *
    * Response is the only valid root element of TwiML, and therefore also the only existing and
    * allowed implementation of Root.
    */
  abstract class Root private[twiml] () extends TwimlElement {

    private lazy val xmlBuilder = new XmlBuilder(tagName, tagAttributes, tagSubElements, tagValue)

    // It's deliberate that xmlCompact and xmlPretty are not final here, but final in the NoneRoot trait.
    // This is because NoneRoot can be extended by clients, and we want to enforce that they let our system
    // generate the xml, so that xml is both consistent, and guaranteed to be escaped properly. However Root has
    // a package private constructor, and hence cannot be extended by client, and it's necessary for the
    // Response.UnverifiedFromStringImpl to be able to override these.
    override def xmlCompact: String =
      """<?xml version="1.0" encoding="UTF-8"?>""" + xmlBuilder.compactPrint

    override def xmlPretty: String =
      """<?xml version="1.0" encoding="UTF-8"?>""" + System.lineSeparator() + xmlBuilder.prettyPrint

    override def twilioString: String = xmlCompact
  }

  /** Builder class for making it easy to build the list for [[TwimlElement.tagAttributes]]
    *
    * Note that this builder is not immutable, and therefore not thread-safe, but it is also only
    * meant to be used as short lived local scope builder.
    */
  final class TagAttributeBuilder(
      attributes: ListBuffer[(String, String)] = ListBuffer.empty
  ) {

    def add(key: String, valueOpt: Option[TwilioStringValue]): TagAttributeBuilder = {
      valueOpt.foreach(value => attributes.append(key -> value.twilioString))
      this
    }

    def addString(key: String, valueOpt: Option[String]): TagAttributeBuilder = {
      valueOpt.foreach(value => attributes.append(key -> value))
      this
    }

    def addString(key: String, value: String): TagAttributeBuilder = {
      attributes.append(key -> value)
      this
    }

    def addInt(key: String, valueOpt: Option[Int]): TagAttributeBuilder = {
      valueOpt.foreach(value => attributes.append(key -> value.toString))
      this
    }

    def addInt(key: String, value: Int): TagAttributeBuilder = {
      attributes.append(key -> value.toString)
      this
    }

    def addLong(key: String, valueOpt: Option[Long]): TagAttributeBuilder = {
      valueOpt.foreach(value => attributes.append(key -> value.toString))
      this
    }

    def addLong(key: String, value: Long): TagAttributeBuilder = {
      attributes.append(key -> value.toString)
      this
    }

    def addBoolean(key: String, valueOpt: Option[Boolean]): TagAttributeBuilder = {
      valueOpt.foreach(value => attributes.append(key -> value.toString))
      this
    }

    def addBoolean(key: String, value: Boolean): TagAttributeBuilder = {
      attributes.append(key -> value.toString)
      this
    }

    def build: immutable.Seq[(String, String)] = attributes.toList
  }

  private final class XmlBuilder(
      tagName: String,
      attributes: Seq[(String, String)],
      nestedElements: Seq[TwimlElement],
      tagValue: Option[String]
  ) {

    private def attParams = attributes
      .map(att => s""" ${StringUtil.xmlEscape(att._1)}="${StringUtil.xmlEscape(att._2)}"""")
      .mkString

    private val tagNameEscaped: String = StringUtil.xmlEscape(tagName)
    private val tagStart               = s"""<$tagNameEscaped$attParams"""

    lazy val prettyPrint: String =
      if (nestedElements.isEmpty && tagValue.isEmpty) s"""$tagStart />"""
      else if (nestedElements.isEmpty) {
        s"""$tagStart>${tagValue.map(StringUtil.xmlEscape).getOrElse("")}</$tagNameEscaped>"""
      } else {
        val nestedAsXmlList =
          nestedElements.map(v => StringUtil.indentEveryLineWith2Spaces(v.xmlPretty))
        s"""$tagStart>${tagValue.map(StringUtil.xmlEscape).getOrElse("")}
           |${nestedAsXmlList.mkString(System.lineSeparator())}
           |</$tagNameEscaped>""".stripMargin
      }

    lazy val compactPrint: String =
      if (nestedElements.isEmpty && tagValue.isEmpty) s"""$tagStart/>"""
      else {
        val tagValueXml     = StringUtil.xmlEscape(tagValue.getOrElse(""))
        val nestedAsXmlList = nestedElements.map(_.xmlCompact).mkString
        s"""$tagStart>$tagValueXml$nestedAsXmlList</$tagNameEscaped>"""
      }
  }
}
