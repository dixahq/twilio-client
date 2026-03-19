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

package com.dixa.twilio.model.video

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{SidAbstract, TwilioStringValue}

/** Represent a Room SID or Unique Name.
  *
  * When used in a Video Grant, it can be either a Room SID (starts with RM) or a Unique Name.
  *
  * @see
  *   https://www.twilio.com/docs/video/api/rooms-resource#room-sid
  */
sealed trait Room extends TwilioStringValue

object Room {

  /** Represent a Twilio Room SID
    *
    * It is a 34 character string that starts with RM.
    */
  final case class Sid private[Room] (override val toString: String) extends SidAbstract with Room

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("RM")), new Sid(_))

  /** Represent a Twilio Room Unique Name
    */
  final case class UniqueName private[Room] (override val toString: String) extends Room

  /** Construct a Room from a string.
    *
    * If the string is a valid Room SID, it will be a Sid instance. Otherwise, it will be a
    * UniqueName instance.
    */
  def apply(value: String): Room = {
    Sid.safe(value).getOrElse(UniqueName(value))
  }
}
