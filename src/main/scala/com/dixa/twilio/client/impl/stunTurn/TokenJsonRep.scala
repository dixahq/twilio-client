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

package com.dixa.twilio.client.impl.stunTurn

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.PositiveInteger
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.stunTurn.Token

import java.time.Instant

/** Json representation of a Token */
private[stunTurn] case class TokenJsonRep(
    username: String,
    ice_servers: Seq[TokenJsonRep.IceServerJsonRep],
    date_updated: String,
    account_sid: String,
    ttl: String,
    date_created: String,
    password: String
) {

  def toModel: Token = Token(
    Token.Username(username),
    Token.Password(password),
    PositiveInteger.unsafe(ttl.toInt),
    TwilioAccount.Sid.unsafe(account_sid),
    ice_servers.map(_.toModel),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[stunTurn] object TokenJsonRep {

  private[stunTurn] final case class IceServerJsonRep(
      url: String,
      username: Option[String] = None,
      credential: Option[String] = None
  ) {
    def toModel: Token.IceServer = Token.IceServer(
      Token.IceServerUrl(url),
      emptyStringToNone(username).map(Token.IceServerUsername),
      emptyStringToNone(credential).map(Token.IceServerCredential)
    )
  }

  private implicit val iceServerReader: Reader[IceServerJsonRep] = macroR[IceServerJsonRep]

  implicit val upickleReader: Reader[TokenJsonRep] =
    macroR[TokenJsonRep]
}
