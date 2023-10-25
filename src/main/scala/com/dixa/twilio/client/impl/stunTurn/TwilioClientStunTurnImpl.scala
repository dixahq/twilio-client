package com.dixa.twilio.client.impl.stunTurn

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.iam._
import com.dixa.twilio.client.stunTurn.{TokenCreateRequestExecutor, TwilioClientStunTurn}

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientStunTurnImpl()(
    implicit executionContext: ExecutionContext,
    materializer: Materializer,
    httpExt: HttpExt
) extends TwilioClientStunTurn {

  override val tokenCreate: TokenCreateRequestExecutor =
}
