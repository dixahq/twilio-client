package com.dixa.twilio.client.impl

import cats.implicits.toBifunctorOps
import io.circe.Decoder
import io.circe.parser.decode

import scala.reflect.{classTag, ClassTag}

private[client] final case class HttpEntityString(override val toString: String) {

  import HttpEntityString._

  /** Parse this entity into Specified type using Circe, throwing exception on error.
    */
  def parseUnsafe[A: ClassTag: Decoder](): A = decode[A](this.toString)
    .leftMap { error =>
      JsonParsingException(classTag[A], this, error)
    }
    .toTry
    .get

  /** Parse this entity into Specified type using Circe, throwing exception on error.
    */
  def parse[A: ClassTag: Decoder](): Either[JsonParsingException, A] = decode[A](this.toString)
    .leftMap { error =>
      JsonParsingException(classTag[A], this, error)
    }
}

private[client] object HttpEntityString {

  private[client] final case class JsonParsingException(
      targetClass: ClassTag[_],
      entity: HttpEntityString,
      cause: Exception
  ) extends RuntimeException(
        s"Error parsing following json as ${targetClass.runtimeClass.getSimpleName}: $entity",
        cause
      )
}
