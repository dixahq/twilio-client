package com.dixa.twilio.client.model

import cats.effect.IO
import org.http4s.circe.jsonOf
import io.circe.generic.auto._

object UpdateRecordingResponse {
  implicit val decoder = jsonOf[IO, UpdateRecordingResponse]
}
case class UpdateRecordingResponse(status: String)
