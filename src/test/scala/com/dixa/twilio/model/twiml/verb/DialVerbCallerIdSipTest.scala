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

package com.dixa.twilio.model.twiml.verb

import org.scalatest.wordspec.AnyWordSpec

final class DialVerbCallerIdSipTest extends AnyWordSpec {

  classOf[DialVerb.CallerId.Sip].getSimpleName should {

    "return an instance if created with letters, digits and the chars + - _ ." in {
      val result = DialVerb.CallerId.Sip.safe("Dixa_Support-1.0+")
      assert(result.map(_.twilioString) == Right("Dixa_Support-1.0+"))
    }

    "throw an exception if created with a space" in {
      intercept[DialVerb.CallerId.Sip.InvalidCharException](
        DialVerb.CallerId.Sip.unsafe("Dixa Support")
      )
    }

    "throw an exception if created with a char that is not alphanumeric or one of + - _ ." in {
      intercept[DialVerb.CallerId.Sip.InvalidCharException](
        DialVerb.CallerId.Sip.unsafe("support@dixa")
      )
    }

    "not allow instances to be created with the default apply method of case classes" in {
      assertTypeError("""DialVerb.CallerId.Sip("Dixa")""")
    }
  }
}
