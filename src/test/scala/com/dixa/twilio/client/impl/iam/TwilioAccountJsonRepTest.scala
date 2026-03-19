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

package com.dixa.twilio.client.impl.iam

import org.scalatest.wordspec.AnyWordSpec

final class TwilioAccountJsonRepTest extends AnyWordSpec {

  classOf[TwilioAccountJsonRep].getSimpleName should {

    "Should not expose the auth token in its toString, to ensure that it does not by " +
      "mistake ends up in a log somewhere" in {

        val authTokenString = "testAuthToken"
        val instance        = TwilioAccountJsonRep(
          "testStatus",
          "Wed, 23 Feb 2022 17:13:40 +0000",
          authTokenString,
          "testFreindlyName",
          "testOwnerSid",
          "testSid",
          "Wed, 23 Feb 2022 17:13:40 +0000",
          "Full"
        )
        assert(!instance.toString.contains(authTokenString))
      }
  }
}
