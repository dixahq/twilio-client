package com.dixa.twilio.client

import akka.actor.ActorSystem
import com.dixa.twilio.client.implDetails.TwilioClientImpl

import scala.concurrent.ExecutionContext

trait TwilioClient {

  def account: TwilioClientAccount

  def conference: TwilioClientConference
}

object TwilioClient {
  def defaultImpl()(
      implicit actorSystem: ActorSystem
  ): TwilioClient = new TwilioClientImpl()
}
