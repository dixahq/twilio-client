package com.dixa.twilio.client.model.messaging

final case class MessageNumSegments(segments: Int) {
  override def toString = segments.toString
}
