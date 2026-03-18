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

package com.dixa.twilio.model.iam

import com.dixa.twilio.CommonFixtures
import org.scalatest.wordspec.AnyWordSpec

final class TwilioAccountTest extends AnyWordSpec {

  classOf[TwilioAccount].getSimpleName when {

    "isActive is called" should {
      "say true if status is active" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Active)
        assert(instance.isActive)
      }
      "say false if status is closed" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Closed)
        assert(!instance.isActive)
      }
      "say false if status is suspended" in {
        val f = new Fixture
        import f._
        val instance = account1.copy(status = TwilioAccount.Status.Suspended)
        assert(!instance.isActive)
      }
    }
  }

  private final class Fixture extends CommonFixtures.Account
}
