package com.dixa.twilio.model.twiml

// format: off
/** Class represent the TwiML Response element (the root element of TwiML)
  *
  * There are multiple subtypes to this trait, used to make a distinction between:
  *
  *   1. TwiML that guarantied to be correct (verified) vs TwiML that we cannot prove correct at
  *      compile time.
  *   1. TwiML that is build using the moddeling system provided for it vs TwiML that was just
  *      constructed from a String
  *
  * This is moddeling by having the classes that can be seen in this diagram:
  * [[https://plantuml.cirque-udv.dk/svg/oymhIIrAIqnELGXABIx8pojEvKfCAYufIamkKN0hoi_rpKz9pU5ApaaiBbO8IotAJCjCJU7AX6iApIk32KBK80JGTQFA19SKPUQbSt71R5MmgT7LHR8Hpe98mAr6qu1aFnU2ZIw7qrXiIWYO0t4u0000]]
  *
  * It is strongly recommended, that you build you Response instance, by using the
  * [[Response.build]] method, and avoid adding customVerbs to it, so you are allowed to call
  * [[Response.Builder.buildVerified]] on the builder, to retrieve a [[Response.Verified]] instance.
  * Doing so you can garenty compile time, that you Response instance will produce valid TwiML. You
  * can do so by doing:
  * {{{
  *    val result: Response.Verified = Response.build { responseBuilder =>
  *      responseBuilder
  *        .addSay { sayBuilder =>
  *          sayBuilder.withText(textToSay).build
  *        }
  *        .buildVerified()
  *    }
  * }}}
  * [[Response.Builder.buildVerified]] can only be called, as long as you have not called
  * [[Response.Builder.addCustomVerb]]. If you find you self building official TwiML, but still
  * need to use a custom Verb, or create a Response from a String, then please contribute to
  * this library instead, and make the it support building the needed TwiMl in a typesafe way.
  * 
  * It may seem like an extra unnecessary step, that the build method takes a function, that it then
  * provides the builder to. But as many of the things added to the builder, are them self objects 
  * that needs to be build using another builder, I found that this was what provided the most
  * pleasant syntax for clients. Instead of them needing to find the correct builder to create 
  * and provide, you can just provide a function, give the argument (the builder) a name, and
  * start using it. This works really well with autocompletion in editors, after calling
  * [[Response.build]], autocompletion can show all of the possibilities, without clients needing
  * to look up anything elsewhere.
  */
// format: on
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

    /** A a custom Verb to the builder (not recommended)
      *
      * This will allow you to add you own custom implemented [[TwimlElement.Verb]], but as soon as
      * you do that, then the builder can no longer guaranty to produce a verified response, and as
      * such the generated [[Response]] may generate TwiML that is not valid, without detecting it
      * compile time.
      */
    def addCustomVerb(
        verb: TwimlElement.Verb
    ): Builder[PhantomTypes.BuildableTrue, PhantomTypes.VerifiedFalse] =
      new Builder[PhantomTypes.BuildableTrue, PhantomTypes.VerifiedFalse](verbs :+ verb)

    /** Build a verified [[Response]]
      *
      * By verified we mean an instance of a [[Response]], that is garentied to produce valid TwiML.
      *
      * This method cannot be called, if you have added custom verbs to the builder via
      * [[Response.Builder.addCustomVerb]]
      */
    def buildVerified()(
        implicit evB: B =:= PhantomTypes.BuildableTrue,
        evV: V =:= PhantomTypes.VerifiedTrue
    ): Response.Verified = new Verified(verbs)

    /** Build a unverified [[Response]]
      *
      * By unverified we mean, that we cannot garenty it to produce valid TwiML.
      *
      * This method can only be called, if you have added custom verbs to the builder via
      * [[Response.Builder.addCustomVerb]]
      */
    def buildUnverified()(
        implicit evB: B =:= PhantomTypes.BuildableTrue,
        evV: V =:= PhantomTypes.VerifiedFalse
    ): Response.UnverifiedFromModel = new UnverifiedFromModel(verbs)
  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse, PhantomTypes.VerifiedTrue]
  type BuildFunction[A <: FromModel] = BuilderStartState => A

  /** Build an instance of a Response using builder.
    *
    * See the documentation on the [[Response]] class, for an example of how to use this.
    */
  def build[A <: FromModel](fun: BuildFunction[A]): A = fun(
    new BuilderStartState(Vector.empty)
  )

  /** Build a Response element out of the supplied String
    *
    * It is highly recommended to use [[Response.build]] instead.
    */
  def fromString(suppliedTwiml: String): UnverifiedFromString = new UnverifiedFromString(
    suppliedTwiml
  )

}
