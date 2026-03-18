// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.stunTurn

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.stunTurn.TokenCreateRequestExecutor
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.PositiveInteger
import com.dixa.twilio.model.stunTurn.Token
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class TokenCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an Token" should {
      "ask twilio to create it, and return the usage trigger it gets back from Twilio" in {

        val request = TokenCreateRequestExecutor.TokenCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Tokens.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = Token(
          Token.Username("dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269"),
          Token.Password("tE2DajzSJwnsSbc123"),
          PositiveInteger.unsafe(86400),
          CommonFixtures.accountSid1,
          Seq(
            Token.IceServer(Token.IceServerUrl("stun:global.stun.twilio.com:3478"), None, None),
            Token.IceServer(
              Token.IceServerUrl("turn:global.turn.twilio.com:3478?transport=udp"),
              Some(
                Token.IceServerUsername(
                  "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269"
                )
              ),
              Some(Token.IceServerCredential("tE2DajzSJwnsSbc123"))
            ),
            Token.IceServer(
              Token.IceServerUrl("turn:global.turn.twilio.com:3478?transport=tcp"),
              Some(
                Token.IceServerUsername(
                  "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269"
                )
              ),
              Some(Token.IceServerCredential("tE2DajzSJwnsSbc123"))
            ),
            Token.IceServer(
              Token.IceServerUrl("turn:global.turn.twilio.com:443?transport=tcp"),
              Some(
                Token.IceServerUsername(
                  "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269"
                )
              ),
              Some(Token.IceServerCredential("tE2DajzSJwnsSbc123"))
            )
          ),
          Instant.from(ZonedDateTime.of(2020, 5, 1, 1, 42, 57, 0, ZoneOffset.UTC)),
          Instant.from(ZonedDateTime.of(2020, 5, 1, 1, 42, 57, 0, ZoneOffset.UTC))
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TokenCreateRequestExecutor =
          TwilioClient.defaultImpl().stunTurn.tokenCreate
        val resultFut: Future[
          Either[TokenCreateRequestExecutor.TokenCreateException, Token]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === expected)
        }
      }
    }
  }

  private def twilioResponse1 =
    s"""{
       |  "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
       |  "ice_servers": [
       |    {
       |      "url": "stun:global.stun.twilio.com:3478"
       |    },
       |    {
       |      "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
       |      "credential": "tE2DajzSJwnsSbc123",
       |      "url": "turn:global.turn.twilio.com:3478?transport=udp"
       |    },
       |    {
       |      "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
       |      "credential": "tE2DajzSJwnsSbc123",
       |      "url": "turn:global.turn.twilio.com:3478?transport=tcp"
       |    },
       |    {
       |      "username": "dc2d2894d5a9023620c467b0e71cfa6a35457e6679785ed6ae9856fe5bdfa269",
       |      "credential": "tE2DajzSJwnsSbc123",
       |      "url": "turn:global.turn.twilio.com:443?transport=tcp"
       |    }
       |  ],
       |  "date_updated": "Fri, 01 May 2020 01:42:57 +0000",
       |  "account_sid": "${CommonFixtures.accountSid1}",
       |  "ttl": "86400",
       |  "date_created": "Fri, 01 May 2020 01:42:57 +0000",
       |  "password": "tE2DajzSJwnsSbc123"
       |}
       |""".stripMargin
}
