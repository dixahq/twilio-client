package com.dixa.twilio.model.twiml

sealed trait Response extends TwimlElement

object Response {

  sealed abstract class FromModel(val verbs: Seq[TwimlElement.Verb]) extends TwimlElement {

    def canEqual(other: Any): Boolean = other.isInstanceOf[FromModel]

    override def equals(other: Any): Boolean = other match {
      case that: FromModel =>
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
      s"""<?xml version="1.0" encoding="UTF-8"?><Response>${
        verbs.map(_.xmlCompact).mkString("")
      }</Response>"""
    // format: on

    override def xmlPretty: String =
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |${verbs.map(v => s"  ${v.xmlPretty}").mkString(System.lineSeparator())}
         |</Response>""".stripMargin
  }

  final class Verified private[Response] (v: Seq[TwimlElement.Verb]) extends FromModel(v)
  sealed trait Unverified                                            extends Response

  final class UnverifiedFromModel private[Response] (v: Seq[TwimlElement.Verb]) extends FromModel(v)

  final class UnverifiedFromString private[Response] (val suppliedTwiml: String)
      extends Unverified() {

    override def toString = s"Response.${getClass.getSimpleName}($suppliedTwiml)"

    override def xmlCompact: String = suppliedTwiml

    override def xmlPretty: String = suppliedTwiml

    override def equals(other: Any): Boolean = other match {
      case that: UnverifiedFromString =>
        suppliedTwiml == that.suppliedTwiml
      case _ => false
    }

    override def hashCode(): Int = {
      val state = Seq(suppliedTwiml)
      state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
    }
  }

  final class Builder[B <: PhantomTypes.Buildable, V <: PhantomTypes.Verified] private[Response] (
      verbs: Vector[TwimlElement.Verb]
  ) {

    def addSay(fun: Say.BuildFunction): Builder[PhantomTypes.BuildableTrue, V] =
      new Builder[PhantomTypes.BuildableTrue, V](verbs :+ Say.build(fun))

    def addCustomVerb(
        verb: TwimlElement.Verb
    ): Builder[PhantomTypes.BuildableTrue, PhantomTypes.VerifiedFalse] =
      new Builder[PhantomTypes.BuildableTrue, PhantomTypes.VerifiedFalse](verbs :+ verb)

    def buildVerified()(
        implicit evB: B =:= PhantomTypes.BuildableTrue,
        evV: V =:= PhantomTypes.VerifiedTrue
    ): Response.Verified = new Verified(verbs)

    def buildUnverified()(
        implicit evB: B =:= PhantomTypes.BuildableTrue,
        evV: V =:= PhantomTypes.VerifiedFalse
    ): Response.UnverifiedFromModel = new UnverifiedFromModel(verbs)
  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse, PhantomTypes.VerifiedTrue]
  type BuildFunction[A <: FromModel] = BuilderStartState => A

  def build[A <: FromModel](fun: BuildFunction[A]): A = fun(
    new BuilderStartState(Vector.empty)
  )

  def fromString(suppliedTwiml: String): UnverifiedFromString = new UnverifiedFromString(
    suppliedTwiml
  )

}
