package com.dixa.twilio.client.twilioClient.voice

import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor.MessageResourcesReadRequestFilter
import com.dixa.twilio.client.messaging.{MessageResourceReadRequestExecutor, TwilioClientMessaging}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor.ConferenceReadRequest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.voice.Conference
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo}
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.scalatest.matchers.should.Matchers

import java.time._
import java.util.{HashMap => JavaMap}

final class ConferenceReadTest extends TwilioClientTest with Matchers {

  import ConferenceReadTest._

  classOf[TwilioClientMessaging].getSimpleName when {

    val connectionSettings = connSettings(wireMockServer.port())
    val accountSid         = connectionSettings.accountSid

    "messageResourceList" should {

      "no message resources should turn into an empty list" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(messageResourceListResp(accountSid, List.empty))
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead.source(connectionSettings, req(accountSid)).runWith(Sink.seq)
        result.map { result =>
          result.isEmpty shouldBe true
        }
      }

      "lists a single message resource" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withQueryParams(filterMapBuilder(req(accountSid).filter))
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(messageResourceListResp(accountSid, List(messageResource(accountSid))))
            )
        )

        val expected = messageResource(accountSid)

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead.source(connectionSettings, req(accountSid)).runWith(Sink.seq)
        result.map { result =>
          result.head.left.map(ex => println(ex.getMessage))
          result.size shouldBe 1
          result.head.isRight shouldBe true
          result.head.right.get shouldBe expected
          result.head.right.get.dateCreated
            .map { _.toString }
            .getOrElse("") shouldBe "2022-02-01T13:44:20Z"
          result.head.right.get.dateUpdated
            .map { _.toString }
            .getOrElse("") shouldBe "2022-02-02T15:42:20Z"
          result.head.right.get.body.toString shouldBe "testing"
        }
      }

      "lists multiple of message resources" in {
        val expected  = messageResource(accountSid)
        val expected2 = messageResource(accountSid).copy(to = receiver2)
        val expected3 = messageResource(accountSid).copy(to = receiver3)

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  messageResourceListResp(
                    connectionSettings.accountSid,
                    List(expected, expected2, expected3)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().messaging
        val result =
          instance.messageResourceRead.source(connectionSettings, req(accountSid)).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3).map {
            Right(_)
          }
        }
      }

      "lists multiple of message resources, with filter parameters" in {
        val expected  = messageResource(accountSid)
        val expected2 = messageResource(accountSid).copy(to = receiver2)
        val expected3 = messageResource(accountSid).copy(to = receiver3)

        val filter = MessageResourceReadRequestExecutor.MessageResourcesReadRequestFilter(
          to = Some(expected.to),
          from = Some(PhoneNumberE164(expected.from.asString)),
          dateSentAfter = Some(createdAtInstant),
          dateSentBefore = Some(updatedAtInstant)
        )
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(path(accountSid))
            )
            .withQueryParams(filterMapBuilder(filter))
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  messageResourceListResp(
                    connectionSettings.accountSid,
                    List(expected, expected2, expected3)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val result =
          instance.conferenceRead
            .source(connectionSettings, req(accountSid, filter))
            .runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3).map {
            Right(_)
          }
        }
      }
    }
  }
}

private object ConferenceReadTest {
  private def connSettings(port: Int) = TwilioTestConstants.connSettings(port)

  private def path(accountSid: TwilioAccount.Sid) =
    s"/2010-04-01/Accounts/$accountSid/Conference.json"

  private def req(
      accountSid: TwilioAccount.Sid,
  ) =
    ConferenceReadRequestExecutor.ConferenceReadRequest.builder(
      _.withAccountSid(accountSid)
        .build()
    )

  private val createdAt = "Tue, 01 Feb 2022 13:44:20 +0000"
  private val updatedAt = "Wed, 02 Feb 2022 15:42:20 +0000"
  private val dateSent  = "Wed, 02 Feb 2022 16:42:20 +0000"

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
  private val dateSentDateTime = OffsetDateTime.of(
    LocalDateTime.of(LocalDate.of(2022, 2, 2), LocalTime.of(16, 42, 20)),
    ZoneOffset.UTC
  )

  private val dateSentInstant = Instant.from(dateSentDateTime)

  private def conference(accountSid: TwilioAccount.Sid) = Conference.DefaultImpl(
    accountSid = accountSid,
    sid = conferenceSid,
    status = Conference.Status.InProgress,
    friendlyName = friendlyName,
//    sid = messageSid,
//    dateCreated = Some(createdAtInstant),
//    dateUpdated = Some(updatedAtInstant),
//    dateSent = Some(dateSentInstant),
//    accountSid = accountSid,
//    to = receiver,
//    from = sender,
//    messagingServiceSid = Some(serviceSid),
//    body = MessageBody("testing"),
//    status = MessageStatus.Sent,
//    numSegments = numberSegments,
//    numMedia = 0,
//    direction = direction,
//    price = Some(price),
//    error = None
  )

  def messageResourceReferenceResp(
      accountSid: TwilioAccount.Sid,
      messageResource: MessageResource
  ): String = {
    s"""{
       |  "account_sid": "${messageResource.accountSid}",
       |  "api_version": "2010-04-01",
       |  "body": "${messageResource.body}",
       |  "date_created": "$createdAt",
       |  "date_sent": "$dateSent",
       |  "date_updated": "$updatedAt",
       |  "direction": "${messageResource.direction.twilioString}",
       |  "from": "${messageResource.from.asString}",
       |  "messaging_service_sid": "${messageResource.messagingServiceSid.getOrElse("")}",
       |  "num_media": "${messageResource.numMedia}",
       |  "num_segments": "${messageResource.numSegments}",
       |  "price": "${messageResource.price.map(_.amount).getOrElse("null")}",
       |  "price_unit": "${messageResource.price.map(_.unit).getOrElse("null")}",
       |  "sid": "${messageResource.sid}",
       |  "status": "${messageResource.status.twilioString}",
       |  "to": "${messageResource.to.asString}",
       |  "uri": "/2010-04-01/Accounts/$accountSid/Messages/${messageResource.sid}.json"
       |}""".stripMargin
  }

  private def messageResourceListResp(
      accountSid: TwilioAccount.Sid,
      messages: List[MessageResource]
  ): String =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Messages.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "messages": [
       |         ${messages.map(messageResourceReferenceResp(accountSid, _)).mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Messages.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
