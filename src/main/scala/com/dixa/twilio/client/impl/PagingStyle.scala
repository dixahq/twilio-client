// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** The Different Twilio APIs have different ways of doing paging. This enum specifies them. */
private[client] sealed abstract class PagingStyle extends EnumEntry

private[client] object PagingStyle extends Enum[PagingStyle] {
  override val values: immutable.IndexedSeq[PagingStyle] = findValues

  /** Paging is not supported or expected in this API. */
  case object NoPaging extends PagingStyle

  /** Paging is provided by having the paging attributes directly in the root json returned
    *
    * Example:
    * {{{
    * {
    *   "first_page_uri": "/2010-04-01/Accounts.json?FriendlyName=friendly_name&Status=active&PageSize=50&Page=0",
    *   "end": 0,
    *   "previous_page_uri": "/2010-04-01/Accounts.json?FriendlyName=friendly_name&Status=active&PageSize=50&Page=0",
    *   "accounts": [],
    *   "uri": "/2010-04-01/Accounts.json?FriendlyName=friendly_name&Status=active&PageSize=50&Page=0",
    *   "page_size": 50,
    *   "start": 0,
    *   "next_page_uri": "/2010-04-01/Accounts.json?FriendlyName=friendly_name&Status=active&PageSize=50&Page=50",
    *   "page": 0
    * }
    * }}}
    */
  case object PagingAttributesInRootJson extends PagingStyle

  /** Paging information is provided in a meta object.
    *
    * Example:
    * {{{
    * {
    *   "services": [
    *     {
    *       "sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    *       "url": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    *     }
    *   ],
    *   "meta": {
    *     "page": 1,
    *     "page_size": 2,
    *     "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
    *     "previous_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0&PageToken=PTMGd8410e59416697cb4455c87eba98a6d0",
    *     "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=1&PageToken=PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd",
    *     "next_page_url": null,
    *     "key": "services"
    *   }
    * }
    * }}}
    */
  case object MetaObject extends PagingStyle
}
