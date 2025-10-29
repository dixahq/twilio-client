package com.dixa.twilio.client.twilioClient.messaging

import org.apache.pekko.stream.scaladsl.Sink
import com.dixa.twilio.client.impl.messaging.MediaResourceUrlFactory
import com.dixa.twilio.client.messaging.{
  MessageMediaResourceReadRequestExecutor,
  TwilioClientMessaging
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{Media, MediaResourceReference, MediaResourceUrl, Message}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._

final class MessageMediaResourceReadTest extends TwilioClientTest with Matchers {

  private val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
  private val messageSid   = Message.Sid.unsafe("SM9c8a124127702f0c7084b373cb06157a")
  private val sid          = Media.Sid.unsafe("ME9ec380c03268689d63e8fc5e97bba86e")
  private val req = MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest(
    messageSid = messageSid
  )
  private val path = MediaResourceUrlFactory.buildMediaResourcePath(
    connSettings.accountSid,
    messageSid
  )

  private val createdAt = "Tue, 01 Feb 2022 13:44:20 +0000"
  private val updatedAt = "Wed, 02 Feb 2022 15:42:20 +0000"

  private val createdAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2022, 2, 1), LocalTime.of(13, 44, 20)),
      ZoneOffset.UTC
    )
  )
  private val updatedAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2022, 2, 2), LocalTime.of(15, 42, 20)),
      ZoneOffset.UTC
    )
  )

  classOf[TwilioClientMessaging].getSimpleName when {

    "mediaResourceReadV2" should {

      "no media resources should turn into an empty list" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(mediaResourceListResp(connSettings.accountSid, messageSid, List.empty))
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result   =
          instance.mediaResourceRead.source(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.isEmpty shouldBe true
        }
      }

      "lists a single media resource" in {
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(mediaResourceListResp(connSettings.accountSid, messageSid, List(sid)))
            )
        )

        val expected = MediaResourceReference(
          sid,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          MediaResourceUrl(
            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid"
          )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result   =
          instance.mediaResourceRead.source(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 1
          assert(result.head.isRight)
          result.head shouldBe Right(expected)
          assert(result.head.map(_.dateCreated.toString) === Right("2022-02-01T13:44:20Z"))
          assert(result.head.map(_.dateUpdated.toString) === Right("2022-02-02T15:42:20Z"))
        }
      }

      "lists multiple of media resources" in {
        val sid2 = Media.Sid.unsafe("MEXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
        val sid3 = Media.Sid.unsafe("MEXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3")
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path)
            )
            .withBasicAuth(connSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  mediaResourceListResp(connSettings.accountSid, messageSid, List(sid, sid2, sid3))
                )
            )
        )

        val expected = MediaResourceReference(
          sid,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          MediaResourceUrl(
            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid"
          )
        )

        val expected2 = MediaResourceReference(
          sid2,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          MediaResourceUrl(
            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/$sid2"
          )
        )

        val expected3 = MediaResourceReference(
          sid3,
          connSettings.accountSid,
          messageSid,
          contentType = "image/jpeg",
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          MediaResourceUrl(
            s"http://localhost/2010-04-01/Accounts/${connSettings.accountSid}/Messages/$messageSid/Media/MEXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"
          )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result   =
          instance.mediaResourceRead.source(connSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3).map(Right(_))
        }
      }
    }
  }

  def mediaResourceReferenceResp(
      accountSid: TwilioAccount.Sid,
      messageSid: Message.Sid,
      sid: Media.Sid
  ): String = {
    s"""{
       |             "sid": "$sid",
       |             "account_sid": "$accountSid",
       |             "parent_sid": "$messageSid",
       |             "content_type": "image/jpeg",
       |             "date_created": "$createdAt",
       |             "date_updated": "$updatedAt",
       |             "uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media/$sid.json"
       |         }""".stripMargin
  }

  private def mediaResourceListResp(
      accountSid: TwilioAccount.Sid,
      messageSid: Message.Sid,
      sids: List[Media.Sid]
  ) =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "media_list": [
       |         ${sids.map(mediaResourceReferenceResp(accountSid, messageSid, _)).mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Messages/$messageSid/Media.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
