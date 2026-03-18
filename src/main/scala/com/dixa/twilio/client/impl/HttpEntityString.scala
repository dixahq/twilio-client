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

import com.dixa.twilio.client.impl.TwilioClientPickler.{read, Reader}

import scala.reflect.{classTag, ClassTag}
import scala.util.Try

private[client] final case class HttpEntityString(override val toString: String) {

  import HttpEntityString._

  /** Parse this entity into Specified type using Circe, throwing exception on error.
    */
  def parseUnsafe[A: Reader](): A = read[A](this.toString)

  /** Parse this entity into Specified type using Circe, throwing exception on error.
    */
  def parse[A: ClassTag: Reader](): Either[JsonParsingException, A] =
    Try(parseUnsafe()).toEither.left.map(JsonParsingException(classTag[A], this, _))
}

private[client] object HttpEntityString {

  private[client] final case class JsonParsingException(
      targetClass: ClassTag[_],
      entity: HttpEntityString,
      cause: Throwable
  ) extends RuntimeException(
        s"Error parsing following json as ${targetClass.runtimeClass.getSimpleName}: $entity",
        cause
      )
}
