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

/** Custom Upickle Pickler settings that in generel suit this client
  */
private[client] object TwilioClientPickler extends upickle.AttributeTagged {

  /** The default is to treat option as json collections of max 1 element. However twilio sends us a
    * lot of attributes where the values could be null, and we would like to read these in as
    * options, instead of relying on attributes actually being set to null (the upickle default for
    * null values).
    */
  override implicit def OptionReader[T: Reader]: Reader[Option[T]] = {
    new Reader.Delegate[Any, Option[T]](implicitly[Reader[T]].map(Some(_))) {
      override def visitNull(index: Int) = None

    }
  }

  override implicit def OptionWriter[T: Writer]: Writer[Option[T]] = {
    implicitly[Writer[T]].comap[Option[T]] {
      case Some(value) => value
      case None        => null.asInstanceOf[T]
    }
  }
}
