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
import com.dixa.twilio.model.Iso8601DateTime.{After, Before}
import com.dixa.twilio.model.{Iso8601DateTime, TwilioStringValue}

import java.net.URLEncoder

/** Helper class for easy building of multiple query params, into one single String, that can just
  * be added to the end of a url
  */
private[impl] final class QueryParamBuilder private (private val paramStrings: List[String]) {

  def withParam(key: String, value: String): QueryParamBuilder = {
    val encodedKey   = URLEncoder.encode(key, "utf-8")
    val encodedValue = URLEncoder.encode(value, "utf-8")
    val toAdd        = s"$encodedKey=$encodedValue"
    new QueryParamBuilder(toAdd +: paramStrings)
  }

  def withParam(key: String, value: TwilioStringValue): QueryParamBuilder =
    withParam(key, value.twilioString)

  def withOptionalParam(
      key: String,
      valueOpt: Option[TwilioStringValue]
  ): QueryParamBuilder = valueOpt match {
    case Some(value) => withParam(key, value.twilioString)
    case None        => this
  }

  def withOptionalStringParam(
      key: String,
      valueOpt: Option[String]
  ): QueryParamBuilder = valueOpt match {
    case Some(value) => withParam(key, value)
    case None        => this
  }

  def withCollectionParam(
      key: String,
      valueSet: Iterable[TwilioStringValue]
  ): QueryParamBuilder = if (valueSet.iterator.hasNext) {
    withParam(key, valueSet.map(_.twilioString).mkString(" "))
  } else {
    this
  }

  def withOptionalBooleanParam(
      key: String,
      valueOpt: Option[Boolean]
  ): QueryParamBuilder = valueOpt match {
    case Some(value) => withParam(key, value.toString)
    case None        => this
  }

  def withOptionalDateParam(
      key: String,
      valueOpt: Option[Iso8601DateTime]
  ): QueryParamBuilder = valueOpt match {
    case Some(before: Before) => withParam(s"$key<", s"${before.instant}")
    case Some(after: After)   => withParam(s"$key>", s"${after.instant}")
    case _                    => this
  }

  def build: String = paramStrings match {
    case Nil                            => ""
    case ::(head, tail) if tail.isEmpty => s"?$head"
    case ::(head, tail)                 => s"?$head${tail.mkString("&", "&", "")}"
  }

  def buildForPostParams: String = paramStrings match {
    case Nil                            => ""
    case ::(head, tail) if tail.isEmpty => s"$head"
    case ::(head, tail)                 => s"$head${tail.mkString("&", "&", "")}"
  }
}

private[impl] object QueryParamBuilder {
  val empty = new QueryParamBuilder(List.empty)
}
