package com.dixa.twilio.client

import akka.actor.ActorSystem
import com.dixa.twilio.client.iam.TwilioClientIam
import com.dixa.twilio.client.impl.TwilioClientImpl
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.voice.TwilioClientVoice

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
