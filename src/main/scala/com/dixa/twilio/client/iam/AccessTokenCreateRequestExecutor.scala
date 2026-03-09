package com.dixa.twilio.client.iam

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class AccessTokenCreateRequestExecutor {

  // TODO UVA all the keys, secrets and such will come from connection settings of executor
  //  conn settings will be provided by telephony when calling this executor

//  val voiceGrants = Map(
//    "voice" -> Map(
//      "incoming" -> Map("allow" -> true),
//      "outgoing" -> Map("application_sid" -> appSid)
//    )
//  )

  sealed trait TwilioRegion {
    def value: String
  }

  object TwilioRegion {
    case object US1 extends TwilioRegion { val value = "us1" }
    case object IE1 extends TwilioRegion { val value = "ie1" }
    case object AU1 extends TwilioRegion { val value = "au1" }
  }

  sealed trait TwilioGrant {
    def grantKey: String
    def toJson: String
  }

  object TwilioGrant {

    case class VoiceGrant(
        incomingAllow: Boolean = true,
        outgoingAppSid: Option[String] = None // TwiML app
    ) extends TwilioGrant {
      val grantKey       = "voice"
      def toJson: String = {
        val incoming = s""""incoming":{"allow":$incomingAllow}"""
        val outgoing = outgoingAppSid
          .map(sid => s""","outgoing":{"application_sid":"$sid"}""")
          .getOrElse("")
        s"{$incoming$outgoing}"
      }
    }

    case class ChatGrant(
        serviceSid: String
    ) extends TwilioGrant {
      val grantKey       = "chat"
      def toJson: String =
        s"""{"service_sid":"$serviceSid"}"""
    }

    case class SyncGrant(
        serviceSid: String
    ) extends TwilioGrant {
      val grantKey       = "sync"
      def toJson: String =
        s"""{"service_sid":"$serviceSid"}"""
    }

    case class VideoGrant(
        room: Option[String] = None
    ) extends TwilioGrant {
      val grantKey       = "video"
      def toJson: String =
        room.map(r => s"""{"room":"$r"}""").getOrElse("{}")
    }

    case class RawGrant(
        grantKey: String,
        json: String
    ) extends TwilioGrant {
      def toJson: String = json
    }
  }

  def generateAccessToken(
      identity: String,
      region: Option[TwilioRegion],
      grants: Seq[TwilioGrant],
      ttl: Long
  ): String = {

    // TODO UVA convert to a proper error returned to client
    require(ttl > 0 && ttl <= 86400, s"ttl must be between 1 and 86400 seconds, got $ttl")

    val now     = System.currentTimeMillis() / 1000
    val encoder = Base64.getUrlEncoder.withoutPadding()

    def base64(s: String): String =
      encoder.encodeToString(s.getBytes("UTF-8"))

    val regionValue = region.getOrElse(TwilioRegion.US1).value
    val regionField = s""""region":"$regionValue","""

    val grantsFields = grants
      .map(g => s""""${g.grantKey}":${g.toJson}""")
      .mkString(",")

    val grantsJson = s""""identity":"$identity",$grantsFields"""

    val rawPayload =
      s"""{""" +
        s""""jti":"$apiKeySid-$now",""" +
        s""""iss":"$apiKeySid",""" +
        s""""sub":"$accountSid",""" +
        s""""iat":$now,""" +
        s""""exp":${now + ttl},""" +
        regionField +
        s""""grants":{$grantsJson}""" +
        s"""}"""

    val header  = base64("""{"typ":"JWT","alg":"HS256","cty":"twilio-fpa;v=1"}""")
    val payload = base64(rawPayload)

    val signingInput = s"$header.$payload"

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(apiKeySecret.getBytes("UTF-8"), "HmacSHA256"))
    val signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes("UTF-8")))

    s"$signingInput.$signature"
  }

}
