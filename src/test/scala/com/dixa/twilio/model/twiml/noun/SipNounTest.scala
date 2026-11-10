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

package com.dixa.twilio.model.twiml.noun

import org.scalatest.wordspec.AnyWordSpec

final class SipNounTest extends AnyWordSpec {

  private val length254Uri = "sip:" + "a" * 250
  private val length255Uri = "sip:" + "a" * 251

  classOf[SipNoun.Uri].getSimpleName should {

    "return an instance if created with 254 chars, as Twilio requires a URI under 255 chars" in {
      assert(SipNoun.Uri.safe(length254Uri).map(_.twilioString) == Right(length254Uri))
    }

    "return a Left if created with 255 chars" in {
      assert(SipNoun.Uri.safe(length255Uri) == Left(SipNoun.Uri.ToLongException(length255Uri, 254)))
    }

    "keep the URI out of the message and the toString of a too-long error, so it is not logged by accident" in {
      val error = SipNoun.Uri.safe(length255Uri).swap.toOption.get
      assert(error.getMessage == "A value of length 255 does not conform to maxLength of 254")
      assert(!error.toString.contains(length255Uri))
    }

    "not allow instances to be created with the default apply method of case classes" in {
      assertTypeError("""SipNoun.Uri("sip:jack@example.com")""")
    }
  }

  classOf[SipNoun.Password].getSimpleName should {

    "hide the password in toString, so it is not logged by accident" in {
      val password = SipNoun.Password("secret-password")
      assert(!password.toString.contains("secret-password"))
    }

    "give the password itself as the value sent to Twilio" in {
      assert(SipNoun.Password("secret-password").twilioString == "secret-password")
    }
  }

  classOf[SipNoun.Credentials].getSimpleName should {

    "hide the password in toString, so it is not logged by accident" in {
      val credentials =
        SipNoun.Credentials(SipNoun.Username("trunk-user"), SipNoun.Password("secret-password"))
      assert(!credentials.toString.contains("secret-password"))
    }
  }
}
