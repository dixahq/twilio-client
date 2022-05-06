package com.dixa.twilio.model.twiml

trait TwimlElement {

  def xmlCompact: String
  def xmlPretty: String
}

object TwimlElement {
  trait Verb extends TwimlElement
}
