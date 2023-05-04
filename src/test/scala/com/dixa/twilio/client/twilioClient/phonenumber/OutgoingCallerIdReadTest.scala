package com.dixa.twilio.client.twilioClient.phonenumber

import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.{
  OutgoingCallerIdReadRequest,
  OutgoingCallerIdReadRequestFilter
}
import com.dixa.twilio.client.phonenumber.{
  OutgoingCallerIdReadRequestExecutor,
  TwilioClientPhoneNumber
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo}
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.scalatest.matchers.should.Matchers

import java.util.{HashMap => JavaMap}

final class OutgoingCallerIdReadTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientPhoneNumber].getSimpleName when {

    "outgoingCallerIdRead" should {
      "safely list a single outgoing caller id when filter applied" in {
        val f = new Fixture()
        import f._

        val filter = OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequestFilter(
          Some(PhoneNumberE164.unsafe("+141586753096"))
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds.json"
              )
            )
            .withQueryParams(filterMapBuilder(filter))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber
        val req =
          OutgoingCallerIdReadRequest(
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            filter
          )

        val resultFut =
          instance.outgoingCallerIdList.source(twilioConnectionSetting, req).runWith(Sink.seq)

        resultFut.flatMap { seq =>
          assert(seq === Seq(Right(outgoingCallerId)))
        }
      }
    }
  }

  // noinspection TypeAnnotation
  class Fixture {
    val outgoingCallerId = OutgoingCallerId(
      sid = OutgoingCallerId.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
      friendlyName = OutgoingCallerId.FriendlyName("(415) 867-5309"),
      phoneNumber = PhoneNumberE164.unsafe("+141586753096")
    )

    val twilioResponse1 =
      """
        |{
        |  "end": 0,
        |  "first_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds.json?PageSize=50&Page=0",
        |  "next_page_uri": null,
        |  "outgoing_caller_ids": [
        |    {
        |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        |      "date_created": "Fri, 21 Aug 2009 00:11:24 +0000",
        |      "date_updated": "Fri, 21 Aug 2009 00:11:24 +0000",
        |      "friendly_name": "(415) 867-5309",
        |      "phone_number": "+141586753096",
        |      "sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
        |    }
        |  ],
        |  "page": 0,
        |  "page_size": 50,
        |  "previous_page_uri": null,
        |  "start": 0,
        |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds.json?PageSize=50&Page=0"
        |}
        |""".stripMargin

    def filterMapBuilder(
        filter: OutgoingCallerIdReadRequestFilter
    ): JavaMap[String, StringValuePattern] = {
      val filterMap = new JavaMap[String, StringValuePattern]()
      filter.friendlyName.map { friendlyName =>
        filterMap.put("FriendlyName", equalTo(friendlyName.toString))
      }
      filter.phoneNumber.map { phoneNumber =>
        filterMap.put("PhoneNumber", equalTo(phoneNumber.toString))
      }
      filterMap.put("PageSize", equalTo(filter.pageSize.toString))
      filterMap
    }
  }
}
