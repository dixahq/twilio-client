package com.dixa.twilio.model.twiml

/** Represent a element in TwiML (everything within <>) */
sealed trait TwimlElement {

  def xmlCompact: String
  def xmlPretty: String
}

object TwimlElement {
  trait Verb                            extends TwimlElement
  trait Noun                            extends TwimlElement
  abstract class Root private[twiml] () extends TwimlElement
}
