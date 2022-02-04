package com.dixa.twilio.client

import akka.actor.ActorSystem
import com.dixa.twilio.client.impl.TwilioClientImpl

trait TwilioClient {

  def iam: TwilioClientIam

  def voice: TwilioClientVoice

  def messaging: TwilioClientMessaging

  def phoneNumber: TwilioClientPhoneNumber
}

object TwilioClient {
  def defaultImpl()(
      implicit actorSystem: ActorSystem
  ): TwilioClient = new TwilioClientImpl()
}
