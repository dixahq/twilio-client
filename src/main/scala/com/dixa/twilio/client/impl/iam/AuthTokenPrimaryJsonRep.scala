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

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import java.time.Instant

/** Representation of the Json twilio uses for auth tokens */
private[iam] final case class AuthTokenPrimaryJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    auth_token: String
) {

  def toModel: AuthToken.AuthTokenAndMetaData[AuthToken.Primary] = {
    val token    = AuthToken.Primary(auth_token)
    val metadata = AuthToken.MetaData(
      TwilioAccount.Sid.unsafe(account_sid),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_created)),
      Instant.from(Formatter.newApiDateTimeFormatter.parse(date_updated))
    )
    AuthToken.AuthTokenAndMetaData(token, metadata)
  }

  override def toString =
    s"AuthTokenJsonRep(account_sid=$account_sid, date_created=$date_created, date_updated=$date_updated, auth_token=***)"
}

private[impl] object AuthTokenPrimaryJsonRep {

  implicit val upickleRW: Reader[AuthTokenPrimaryJsonRep] = macroR[AuthTokenPrimaryJsonRep]
}
