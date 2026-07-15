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

package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.MessageSendRequest
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.content.ContentTemplate
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import org.scalatest.wordspec.AnyWordSpec

import java.net.URL

final class MessageSendRequestBuilderTest extends AnyWordSpec {

  classOf[MessageSendRequest].getSimpleName when {

    "building a request" should {

      "succeed with body and media urls (mms)" in {
        MessageSendRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
            .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
            .withBody(MessageBody("Hi there"))
            .withMediaUrls(Seq(MediaResourceUrl("https://example.com/media/abc.jpg")))
            .build()
        )
      }

      "succeed with content sid and content variables" in {
        MessageSendRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
            .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
            .withContentSid(ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
            .withContentVariables(Map("1" -> "Jose"))
            .build()
        )
      }

      "not compile when neither body nor content sid is set" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .build()
             )"""
        )
      }

      "not compile when content sid is set after body" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .withBody(MessageBody("Hi there"))
                 .withContentSid(ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
                 .build()
             )"""
        )
      }

      "not compile when body is set after content sid" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .withContentSid(ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
                 .withBody(MessageBody("Hi there"))
                 .build()
             )"""
        )
      }

      "not compile when media urls are set after content sid" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .withContentSid(ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
                 .withMediaUrls(Seq(MediaResourceUrl("https://example.com/media/abc.jpg")))
                 .build()
             )"""
        )
      }

      "not compile when content sid is set after media urls" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .withMediaUrls(Seq(MediaResourceUrl("https://example.com/media/abc.jpg")))
                 .withContentSid(ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
                 .build()
             )"""
        )
      }

      "not compile when content variables are set without content sid" in {
        assertDoesNotCompile(
          """MessageSendRequest.build(
               _.withAccountSid(TwilioTestConstants.accountSid)
                 .withFrom(MessageSender.E164(PhoneNumberE164.unsafe("+12015550123")))
                 .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe("+4532123456")))
                 .withStatusCallback(MessageStatusCallback(new URL("http://example.com/v1/sms/status")))
                 .withContentVariables(Map("1" -> "Jose"))
                 .build()
             )"""
        )
      }
    }
  }
}
