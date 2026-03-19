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

package com.dixa.twilio.client.twilioClient

import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.matching.{MatchResult, RequestMatcherExtension}

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Custom wiremock matcher, that can match on application/x-www-form-urlencoded body params.
  *
  * This matcher does not require a specific order of parameters.
  *
  * This class has quickly been hacked together, to help match the POST request we send to twilio,
  * as wiremock seem to have no build in way of matching on these type of parameters, without
  * relying on things like request, or doing an exact match, that would lock down the order of
  * params.
  *
  * We could consider trying to push this upstream to wiremock, as application/x-www-form-urlencoded
  * common used standard (what is used in typical browser post request). However it would require a
  * rewrite to java, some tests, and properly a more thoroughly look into the specification of
  * application/x-www-form-urlencoded, as this class as mention is jus ta quick implementation to be
  * good enough for the need of this library.
  *
  * @param expectedParams
  *   the map of params expected.
  */
final class WiremockPostParamMatcher(expectedParams: Map[String, String])
    extends RequestMatcherExtension {

  override def getName: String = "application/x-www-form-urlencoded"

  override def `match`(request: Request, parameters: Parameters): MatchResult = {
    val bodyAsString =
      try {
        new String(request.getBody, StandardCharsets.UTF_8)
      } catch {
        case e: Exception => return MatchResult.noMatch()
      }
    val bodyParamsAsArray = bodyAsString.split('&').filter(_.nonEmpty)
    if (bodyParamsAsArray.exists(x => x.count(_ == '=') != 1 || x.startsWith("=")))
      // This is expected to be a key value pair, so needs exactly one =, that is not at the start of the line because
      // then there no key, however no value is OK, so = as last char is allowed.
      return MatchResult.noMatch()
    val bodyParamsAsMap = bodyParamsAsArray.map { keyValueAsString =>
      val keyValueAsArray =
        keyValueAsString
          .split('=')
          .map(URLDecoder.decode(_, StandardCharsets.UTF_8.name()))
      keyValueAsArray.length match {
        case 1 => (keyValueAsArray(0), "")
        case 2 => (keyValueAsArray(0), keyValueAsArray(1))
      }
    }.toMap
    MatchResult.of(bodyParamsAsMap == expectedParams)
  }
}
