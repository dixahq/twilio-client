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

package com.dixa.twilio.model.voice

import org.scalatest.wordspec.AnyWordSpec

final class ClientIdentityTest extends AnyWordSpec {

  private val length256Identity = "a" * 256
  private val length257Identity = "a" * 257

  classOf[ClientIdentity].getSimpleName should {

    "return an instance if created with 256 chars, the maximum Twilio allows" in {
      assert(
        ClientIdentity.safe(length256Identity).map(_.twilioString) == Right(length256Identity)
      )
    }

    "return a Left if created with 257 chars" in {
      assert(
        ClientIdentity.safe(length257Identity) ==
          Left(ClientIdentity.ToLongException(length257Identity, 256))
      )
    }

    "not allow instances to be created with the default apply method of case classes" in {
      assertTypeError("""ClientIdentity("joey")""")
    }
  }
}
