package com.dixa.twilio.model.voice

final case class Queue(
    sid: Queue.Sid
) {}

object Queue {

  final case class Sid private (override val toString: String) extends AnyVal

  object Sid {

    def apply(input: String): Sid = safe(input).toTry.get

    def safe(input: String): Either[CreationException, Sid] = {
      if (input.isEmpty) Left(Sid.ArgumentEmptyException())
      else if (!input.startsWith("QU")) Left(Sid.ArgumentMissingCaPrefixException(input))
      else if (input.length != 34) Left(Sid.ArgumentLengthException(input))
      else Right(new Sid(input))
    }

    sealed trait CreationException extends RuntimeException

    final case class ArgumentEmptyException()
        extends IllegalArgumentException(s"Empty string does not conform to: $conformToString")
        with CreationException

    final case class ArgumentMissingCaPrefixException(argument: String)
        extends IllegalArgumentException(
          s"$conformToString does not start with QU and therefore not conform to: $conformToString"
        )
        with CreationException

    final case class ArgumentLengthException(argument: String)
        extends IllegalArgumentException(
          s"$argument has length not conforming to: $conformToString"
        )
        with CreationException

    private val conformToString = "Queue.Sid is a 34 character string that starts with QU"
  }
}
