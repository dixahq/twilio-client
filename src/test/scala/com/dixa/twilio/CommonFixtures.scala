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

package com.dixa.twilio

import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneOffset}

object CommonFixtures {

  trait AccountSid {
    val accountSid1: TwilioAccount.Sid = CommonFixtures.accountSid1
  }

  trait Account extends AccountSid {
    val account1: TwilioAccount = TwilioAccount(
      name = TwilioAccount.Name("CommonFixtures.Account.account1 friendly name"),
      sid = this.accountSid1,
      status = TwilioAccount.Status.Active,
      ownerAccountSid = TwilioAccount.Sid.unsafe("AC5fc6c53ce58165d0712d4a56fa29e23a"),
      authToken = AuthToken.Primary("AVerySecretValueThatShouldBeXXXX"),
      accountType = TwilioAccount.Type.Full,
      timeCreated = Instant.from(
        OffsetDateTime.of(
          LocalDateTime.of(LocalDate.of(2015, 10, 26), LocalTime.of(11, 40, 54)),
          ZoneOffset.UTC
        )
      ),
      timeUpdated = Instant.from(
        OffsetDateTime.of(
          LocalDateTime.of(LocalDate.of(2022, 2, 23), LocalTime.of(17, 13, 40)),
          ZoneOffset.UTC
        )
      ),
    )
  }

  val accountSid1: TwilioAccount.Sid =
    TwilioAccount.Sid.unsafe("ACf6c9aa4f2754c258aa45a6d2637cfa15")
}
