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

package com.dixa.twilio.model.callback

import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import org.scalatest.wordspec.AnyWordSpec

import java.net.URL

final class CallbackUrlTest extends AnyWordSpec {

  classOf[CallbackUrl].getSimpleName should {

    "support extracting url as String in pattern matching" in {

      val s        = "http://localhost/some/callback/url"
      val instance = CallbackUrl(s)
      instance match {
        case CallbackUrl(s2) =>
          assert(s2 == s)
        case _ => fail("wrong type")
      }
    }

    "toString should just return the represented url" in {
      val s        = "http://localhost/some/callback/url"
      val instance = CallbackUrl(s)
      assert(instance.toString == s)
    }

    "MessagingStatusCallback subtype should print the urls string representation in it's toString" in {
      val urlAsString = "http://localhost:8347/CallbackPath"
      val a           = MessageStatusCallback(new URL(urlAsString))
      assert(a.toString === urlAsString)
    }
  }

}
