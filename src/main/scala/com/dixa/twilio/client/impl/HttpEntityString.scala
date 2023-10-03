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
