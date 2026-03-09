package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.client.iam.{AccessTokenCreateRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Region
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioGrant.VoiceGrant

import scala.concurrent.duration._

final class AccessTokenCreateTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to create an access token" should {

      "Return a valid token string" in {
        val instance     = TwilioClient.defaultImpl().iam.accessTokenCreate
        val connSettings = TwilioTestConstants.connSettings(8080)

        val request = AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.Builder.empty
          .withIdentity("user123")
          .withRegion(Region.Ireland1)
          .addGrant(
            VoiceGrant(
              incomingAllow = true,
              outgoingAppSid = Some(Application.Sid.unsafe("APaaaabbbbccccdddd1111222233334444"))
            )
          )
          .build()

        instance.run(connSettings, request).map { resultEither =>
          val result = resultEither.toTry.get
          assert(result.token.nonEmpty)
          val parts = result.token.split('.')
          assert(parts.length === 3)
          // Header, Payload, Signature are all base64url encoded
        }
      }

      "Return an error if ttl is invalid" in {
        val instance     = TwilioClient.defaultImpl().iam.accessTokenCreate
        val connSettings = TwilioTestConstants.connSettings(8080)

        val request = AccessTokenCreateRequestExecutor.AccessTokenCreateRequest.Builder.empty
          .withIdentity("user123")
          .withTtl(0.seconds)
          .build()

        instance.run(connSettings, request).map {
          case Left(AccessTokenCreateRequestExecutor.AccessTokenCreateException.InvalidTtl(0)) =>
            // Success
            succeed
          case other =>
            fail(s"Expected InvalidTtl exception, got $other")
        }
      }
    }
  }
}
