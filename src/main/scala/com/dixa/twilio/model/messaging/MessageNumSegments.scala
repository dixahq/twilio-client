package com.dixa.twilio.model.messaging

final case class MessageNumSegments(segments: Int) {
  override def toString: String = segments.toString
}
