package com.dixa.twilio.client.model

import cats.effect.IO
import org.http4s.circe.jsonOf
import io.circe.generic.auto._
import org.http4s.EntityDecoder

object TConference {
  implicit val decoder: EntityDecoder[IO, TConference] = jsonOf[IO, TConference]
}
case class TConference(subresource_uris: SubResourceListing, sid: String)
