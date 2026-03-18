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

/** small utility methods, helpfully when parsing the Twilio json responses */
private[impl] object JsonParsingUtil {

  /** Convert Option[String] into a None, if it contains an empty String
    *
    * Some Twilio API are a bit confused, when it comes to representing unset values, where they mix
    * the use of null and empty strings in there output. This method can help in such cases, if you
    * have you json representation map such fields as option, so that a null value will be mapped
    * into None, and then call this method on it, so that an empty string would also be mapped into
    * a None.
    */
  def emptyStringToNone(x: Option[String]): Option[String] =
    x.flatMap(s => if (s.isEmpty) None else x)
}
