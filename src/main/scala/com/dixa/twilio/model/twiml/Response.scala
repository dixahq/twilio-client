package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.twiml.TwimlConstraints.{
  Buildable,
  BuildableFalse,
  BuildableTrue,
  LastAddedVerbProhibitMoreVerbs,
  LastAddedVerbProhibitMoreVerbsFalse,
  LastAddedVerbProhibitMoreVerbsTrue,
  VerifiedFalse,
  VerifiedTrue
}
import com.dixa.twilio.model.twiml.verb.{DialVerb, RedirectVerb, SayVerb}

// format: off
/** Class represent the TwiML Response element (the root element of TwiML)
  *
  * There are multiple subtypes to this trait, used to make a distinction between:
  *
  *   - TwiML that guarantied to be correct (verified) vs TwiML that we cannot prove correct at
  *      compile time.
  *   - TwiML that is build using the moddeling system provided for it vs TwiML that was just
  *      constructed from a String
  *
  * This is modelled by having the classes that can be seen in this diagram:
  * [[https://plantuml.cirque-udv.dk/svg/oymhIIrAIqnELGXABIx8pojEvKfCAYufIamkKN0hoi_rpKz9pU5ApaaiBbO8IotAJCjCJU7AX6iApIk32KBK80JGTQFA19SKPUQbSt71R5MmgT7LHR8Hpe98mAr6qu1aFnU2ZIw7qrXiIWYO0t4u0000]]
  *
  * It is strongly recommended, that you build you Response instance, by using the
  * [[Response.build]] method, and avoid adding custom Verbs to it, so you are allowed to call
  * [[Response.Builder.buildVerified]] on the builder, to retrieve a [[Response.Verified]] instance.
  * Doing so you can check compile time, that you Response instance will produce valid formatted TwiML. 
  * You can do so by doing:
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
  * this library instead, and make it support building the needed TwiMl in a typesafe way.
  * 
  * It may seem like an extra unnecessary step, that the build method takes a function, that it then
  * provides the builder to. But as many of the things added to the builder are them self objects 
  * that needs to be build using another builder, which provides a pleasant syntax for clients.
  * Instead of them needing to find the correct builder to create 
  * and provide, they can just provide a function, give the argument (the builder) a name, and
  * start using it. This works really well with autocompletion in editors, after calling
  * [[Response.build]], autocompletion can show all of the possibilities, without clients needing
  * to look up anything elsewhere.
  */
// format: on
sealed trait Response extends TwimlElement.Root

object Response {

  sealed trait FromModel extends Response {

    def verbs: Seq[TwimlElement.Verb]

    override final def toString = s"Response.${getClass.getSimpleName}($verbs)"

    // format: off
    override lazy val xmlCompact: String =
      s"""<?xml version="1.0" encoding="UTF-8"?><Response>${verbs.map(_.xmlCompact).mkString("")}</Response>"""
    // format: on

    override lazy val xmlPretty: String = {
      val verbsAsXmlList = verbs.map(v => StringUtil.indentEveryLineWith2Spaces(v.xmlPretty))
      s"""<?xml version="1.0" encoding="UTF-8"?>
         |<Response>
         |${verbsAsXmlList.mkString(System.lineSeparator())}
         |</Response>""".stripMargin
    }
  }

  sealed trait Verified extends FromModel

  private final case class VerifiedImpl(
      override val verbs: Seq[TwimlElement.Verb]
  ) extends Verified

  sealed trait Unverified extends Response

  sealed trait UnverifiedFromModel extends FromModel with Unverified

  private final case class UnverifiedFromModelImpl(
      override val verbs: Seq[TwimlElement.Verb]
  ) extends UnverifiedFromModel

  sealed trait UnverifiedFromString extends Unverified {
    def suppliedTwiml: String
  }

  private final case class UnverifiedFromStringImpl(suppliedTwiml: String)
      extends UnverifiedFromString() {

    override def toString = s"Response.${getClass.getSimpleName}($suppliedTwiml)"

    override def xmlCompact: String = suppliedTwiml

    override def xmlPretty: String = suppliedTwiml
  }

  final class Builder[
      B <: Buildable,
      V <: TwimlConstraints.Verified,
      L <: LastAddedVerbProhibitMoreVerbs
  ] private[Response] (
      verbs: Vector[TwimlElement.Verb]
  ) {

    def addDial(fun: DialVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] =
      new Builder(verbs :+ DialVerb.build(fun))

    /** Adds a redirect verb
      *
      * Calling this, will prevent you from adding more verbs to builder, as it makes no sense to
      * have anything after a redirect in TwiML.
      */
    def addRedirect(fun: RedirectVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, LastAddedVerbProhibitMoreVerbsTrue] =
      new Builder(verbs :+ RedirectVerb.build(fun))

    def addSay(fun: SayVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] =
      new Builder(verbs :+ SayVerb.build(fun))

    /** A a custom Verb to the builder (not recommended)
      *
      * This will allow you to add you own custom implemented [[TwimlElement.Verb]], but as soon as
      * you do that, then the builder can no longer guaranty to produce a verified response, and as
      * such the generated [[Response]] may generate TwiML that is not valid, without detecting it
      * compile time.
      */
    def addCustomVerb(
        verb: TwimlElement.Verb
    )(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, VerifiedFalse, L] =
      new Builder(verbs :+ verb)

    /** Build a verified [[Response]]
      *
      * By verified we mean an instance of a [[Response]], that is guaranteed to produce valid
      * TwiML.
      *
      * To call this method you must have:
      *   1. Added at least one verb.
      *   1. Added no custom verb - [[Response.Builder.addCustomVerb]].
      */
    def buildVerified()(
        implicit evB: B =:= TwimlConstraints.BuildableTrue,
        evV: V =:= TwimlConstraints.VerifiedTrue
    ): Response.Verified = VerifiedImpl(verbs)

    /** Build a unverified [[Response]]
      *
      * By unverified we mean, that we cannot guaranteed it to produce valid TwiML.
      *
      * To call this method you must have:
      *   1. Added at least one verb
      *   1. Added a custom vert via [[Response.Builder.addCustomVerb]]
      */
    def buildUnverified()(
        implicit evB: B =:= TwimlConstraints.BuildableTrue,
        evV: V =:= TwimlConstraints.VerifiedFalse
    ): Response.UnverifiedFromModel = UnverifiedFromModelImpl(verbs)
  }

  type BuilderStartState =
    Builder[BuildableFalse, VerifiedTrue, LastAddedVerbProhibitMoreVerbsFalse]
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
    *
    * There will be no manipulation of the supplied TwiML. So returned Response will return it
    * exactly as is, both when `xmlCompact` and `xmlCompact` is called.
    */
  def fromString(suppliedTwiml: String): UnverifiedFromString = UnverifiedFromStringImpl(
    suppliedTwiml
  )

}
