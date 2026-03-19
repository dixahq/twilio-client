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

import java.time.format.DateTimeFormatter

private[impl] object Formatter {

  /** Formatter for the typical datetimes Twilio use in there older APIs:
    *
    * Format looks a bit like: "EEE, d MMM yyyy HH:mm:ss Z", however the actual formatter used is
    * the pre created RFC_1123_DATE_TIME formatter, as using the above format directly, seems to
    * flaky, as it only works on some machines. My guess is that is is dependent on the running JVM
    * for some reason. The RFC_1123_DATE_TIME seems to also conform to this pattern, but works on
    * all JVM.
    */
  val dateTime: DateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

  /** Formatter for the typical dates used in the newer Twilio apis.
    *
    * This is the standard [[DateTimeFormatter.ISO_DATE_TIME]] formatter.
    */
  val newApiDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
}
