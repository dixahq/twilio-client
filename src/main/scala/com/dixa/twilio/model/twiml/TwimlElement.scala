package com.dixa.twilio.model.twiml

/** Represent a element in TwiML (everything within <>) */
trait TwimlElement {

  def xmlCompact: String
  def xmlPretty: String
}

object TwimlElement {
  trait Verb extends TwimlElement
}
