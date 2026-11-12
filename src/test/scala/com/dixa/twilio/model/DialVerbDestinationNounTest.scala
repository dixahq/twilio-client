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

package com.dixa.twilio.model

import org.scalatest.wordspec.AnyWordSpec

/** Lives outside the twiml package, because code inside the twiml package may extend
  * DestinationNoun.
  *
  * The snippets are anonymous classes. For a class definition, assertTypeError does not report the
  * error that the constructor cannot be accessed, so that check would pass with any base class.
  */
final class DialVerbDestinationNounTest extends AnyWordSpec {

  "DestinationNoun" should {

    "compile a custom noun that extends TwimlElement.Noun, so the next case fails only on DestinationNoun" in {
      assertCompiles(
        """new com.dixa.twilio.model.twiml.TwimlElement.Noun {
          |  override protected def tagName: String = "Custom"
          |  override protected def tagAttributes: scala.collection.immutable.Seq[(String, String)] = Nil
          |  override protected def tagSubElements: scala.collection.immutable.Seq[com.dixa.twilio.model.twiml.TwimlElement] = Nil
          |  override protected def tagValue: Option[String] = None
          |}""".stripMargin
      )
    }

    "not allow code outside the twiml package to extend it" in {
      assertTypeError(
        """new com.dixa.twilio.model.twiml.verb.DialVerb.DestinationNoun {
          |  override protected def tagName: String = "Custom"
          |  override protected def tagAttributes: scala.collection.immutable.Seq[(String, String)] = Nil
          |  override protected def tagSubElements: scala.collection.immutable.Seq[com.dixa.twilio.model.twiml.TwimlElement] = Nil
          |  override protected def tagValue: Option[String] = None
          |}""".stripMargin
      )
    }
  }
}
