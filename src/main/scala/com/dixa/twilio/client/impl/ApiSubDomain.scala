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

package com.dixa.twilio.client.impl

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** Twilio use different subdomains for different APIs. This enum specifies them. */
private[client] sealed abstract class ApiSubDomain(
    override val toString: String,
    val pagingStyle: PagingStyle
) extends EnumEntry

private[client] object ApiSubDomain extends Enum[ApiSubDomain] {

  override val values: immutable.IndexedSeq[ApiSubDomain] = findValues

  case object Accounts  extends ApiSubDomain("accounts", PagingStyle.NoPaging)
  case object Api       extends ApiSubDomain("api", PagingStyle.PagingAttributesInRootJson)
  case object Iam       extends ApiSubDomain("iam", PagingStyle.MetaObject)
  case object Messaging extends ApiSubDomain("messaging", PagingStyle.MetaObject)
  case object Preview   extends ApiSubDomain("preview", PagingStyle.MetaObject)
}
