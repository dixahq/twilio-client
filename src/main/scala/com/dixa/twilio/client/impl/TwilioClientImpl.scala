package com.dixa.twilio.client.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import com.dixa.twilio.client.iam.TwilioClientIam
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.TwilioClient
import com.dixa.twilio.client.impl.iam.TwilioClientIamImpl
import com.dixa.twilio.client.impl.messaging.TwilioClientMessagingImpl
import com.dixa.twilio.client.impl.phonenumber.TwilioClientPhoneNumberImpl
import com.dixa.twilio.client.impl.voice.TwilioClientVoiceImpl
import com.dixa.twilio.client.voice.TwilioClientVoice

/** Default implementation of a TwilioClient. */
private[client] final class TwilioClientImpl()(
    implicit actorSystem: ActorSystem
) extends TwilioClient {

  import actorSystem.dispatcher

  private implicit val http: HttpExt = Http()

  override val iam: TwilioClientIam = new TwilioClientIamImpl()

  override val voice: TwilioClientVoice = new TwilioClientVoiceImpl()

  override val messaging: TwilioClientMessaging = new TwilioClientMessagingImpl()

  override def phoneNumber: TwilioClientPhoneNumber = new TwilioClientPhoneNumberImpl()
}
