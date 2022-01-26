package com.dixa.twilio.client.implDetails

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import com.dixa.twilio.client.{TwilioClient, TwilioClientIam, TwilioClientVoice}

/** Default implementation of a TwilioClient.
  *
  * To not make this class way to big and cluttered, each method is implemented in its own object.
  * So all that this class has to do, is to find the right object and call it. See the scaladoc on
  * the package object of [[com.dixa.twilio.client.implDetails.request]]
  */
private[client] final class TwilioClientImpl()(
    implicit actorSystem: ActorSystem
) extends TwilioClient {

  import actorSystem.dispatcher

  private implicit val http: HttpExt = Http()

  override val iam: TwilioClientIam = new TwilioClientIamImpl()

  override val voice: TwilioClientVoice = new TwilioClientVoiceImpl()
}
