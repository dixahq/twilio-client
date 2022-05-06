package com.dixa.twilio.model.twiml

sealed abstract class Response(val verbs: Seq[TwimlElement.Verb]) extends TwimlElement {

  def canEqual(other: Any): Boolean = other.isInstanceOf[Response]

  override def equals(other: Any): Boolean = other match {
    case that: Response =>
      (that canEqual this) &&
      verbs == that.verbs
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(verbs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Response.${getClass.getSimpleName}($verbs)"

  // format: off
  override def xmlCompact: String =
    s"""<?xml version="1.0" encoding="UTF-8"?><Response>${verbs.map(_.xmlCompact).mkString("")}</Response>"""
  // format: on

  override def xmlPretty: String =
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<Response>
       |${verbs.map(v => s"  ${v.xmlPretty}").mkString(System.lineSeparator())}
       |</Response>""".stripMargin
}

object Response {

  final class Verified private[Response] (v: Seq[TwimlElement.Verb]) extends Response(v)

  final class Builder[B <: PhantomTypes.Buildable, V <: PhantomTypes.Verified] private[Response] (
      verbs: Vector[TwimlElement.Verb]
  ) {

    def addSay(fun: Say.BuildFunction): Builder[PhantomTypes.BuildableTrue, V] =
      new Builder[PhantomTypes.BuildableTrue, V](verbs :+ Say.build(fun))

    def buildVerified()(
        implicit evB: B =:= PhantomTypes.BuildableTrue,
        evV: V =:= PhantomTypes.VerifiedTrue
    ): Response.Verified = new Verified(verbs)
  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse, PhantomTypes.VerifiedTrue]
  type BuildFunction[A <: Response] = BuilderStartState => A

  def build[A <: Response](fun: BuildFunction[A]): A = fun(
    new BuilderStartState(Vector.empty)
  )

}
