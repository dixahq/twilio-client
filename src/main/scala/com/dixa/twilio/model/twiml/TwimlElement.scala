package com.dixa.twilio.model.twiml

/** Represent a element in TwiML (everything within <>) */
sealed trait TwimlElement {

  override final def hashCode(): Int = xmlCompact.hashCode

  override final def equals(obj: Any): Boolean = obj match {
    case other: TwimlElement => xmlCompact == other.xmlCompact
    case _                   => false
  }

  def xmlCompact: String
  def xmlPretty: String
}

object TwimlElement {
  trait Verb extends TwimlElement
  trait Noun extends TwimlElement

  /** Abstraction over the root element of TwiML.
    *
    * Response is the only valid root element of TwiML, and therefore also the only existing and
    * allowed implementation of Root.
    */
  abstract class Root private[twiml] () extends TwimlElement
}
