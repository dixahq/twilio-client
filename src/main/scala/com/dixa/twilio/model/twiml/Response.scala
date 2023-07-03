package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.twiml.TwimlConstraints._
import com.dixa.twilio.model.twiml.verb._

import scala.collection.immutable

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
  * It is strongly recommended that you build your Response instance by using the
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
  * [[Response.Builder.addCustomVerbs]].
  *
  * If you instead need to build a Response from a string, you simply just use call
  * [[Response.fromString]]. This will give you an instance of [[Response.UnverifiedFromString]].
  * In Contrast you will get a [[Response.Verified]] if you are building it via the [[Response.build]]
  * method without adding a custom [[TwimlElement.Verb]]. If you do add a custom verb, you end up
  * with a [[Response.UnverifiedFromModel]].
  *
  * Note that getting a [[Response.Verified]] is only guaranteeing that the TwiML is formatted
  * correctly, and is following the schema rules of Twiml. However we cannot guarantee that
  * the TwiML will not produce an error in Twilio at runtime, as a lot of TwiML elements can point
  * to external resources, that we have no way of checking at compile time. An example of this is Play,
  * that can point to external downloadable files.
  *
  * It may seem like an extra unnecessary step, that the build method takes a function, that it then
  * provides the builder to. But as many of the things added to the builder are objects themselves
  * that need to be build using another builder, which provides a pleasant syntax for clients.
  * Instead of them needing to find the correct builder to create
  * and provide, they can just provide a function, give the argument (the builder) a name, and
  * start using it. This works really well with autocompletion in editors, after calling
  * [[Response.build]], autocompletion can show all of the possibilities, without clients needing
  * to look up anything elsewhere.
  */
// format: on
sealed trait Response extends TwimlElement.Root {

  override protected def tagName: String = "Response"

  override protected def tagAttributes: immutable.Seq[(String, String)] = Nil

  override protected def tagValue: Option[String] = None
}

object Response {

  sealed trait FromModel extends Response {

    def verbs: immutable.Seq[TwimlElement.Verb]

    override protected def tagSubElements: immutable.Seq[TwimlElement] = verbs

    override final def toString = s"Response.${getClass.getSimpleName}($verbs)"

  }

  sealed trait Verified extends FromModel

  private final case class VerifiedImpl(
      override val verbs: immutable.Seq[TwimlElement.Verb]
  ) extends Verified

  sealed trait Unverified extends Response

  sealed trait UnverifiedFromModel extends FromModel with Unverified

  private final case class UnverifiedFromModelImpl(
      override val verbs: immutable.Seq[TwimlElement.Verb]
  ) extends UnverifiedFromModel

  sealed trait UnverifiedFromString extends Unverified {
    def suppliedTwiml: String
    override protected def tagSubElements: immutable.Seq[TwimlElement] = Nil
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

    /** Add a Say verb to the response.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/say
      */
    def addSay(fun: SayVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] =
      new Builder(verbs :+ SayVerb.build(fun))

    /** Add a Pause verb to the response.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/pause
      */
    def addPause(fun: PauseVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] = new Builder(verbs :+ PauseVerb.build(fun))

    /** Add a Play verb to the response.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/play
      */
    def addPlay(fun: PlayVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] =
      new Builder(verbs :+ PlayVerb.build(fun))

    /** Add any Verb to the builder (try to avoid this, unless you have good reasons not to)
      *
      * This will allow you to add you own custom implemented [[TwimlElement.Verb]], or to add pre
      * created verbs, but as soon as you do that, then the builder can no longer guaranty to
      * produce a verified response, and as such the generated [[Response]] may generate TwiML that
      * is not valid, without detecting it compile time.
      *
      * For the above reason, it is recommended to add the verbs you need via the respective `addX`
      * methods, because then most mistakes will be caught compile time. However in some situations
      * this may not be feasible, for example in cases where you construct Twiml based on very
      * dynamic input values, that you don't have control of compile time. In such cases you can use
      * this method instead.
      *
      * You can also use this for adding you own completely custom verbs, but in such case, you
      * should consider if it would make sense to contribute that verb to this project instead, so
      * it would not need to be a custom verb anymore.
      */
    def addCustomVerb(
        verb: TwimlElement.Verb
    )(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, VerifiedFalse, L] =
      new Builder(verbs :+ verb)

    /** Same as [[addCustomVerb]] just for multiple verbs at once. */
    def addCustomVerbs(
        verbsToAdd: Seq[TwimlElement.Verb]
    )(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, VerifiedFalse, L] =
      new Builder(verbs ++ verbsToAdd)

    /** Add a [[com.dixa.twilio.model.twiml.verb.GatherVerb.Verified]] to the response.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather
      */
    def addGather(fun: GatherVerb.BuildFunction)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, L] =
      new Builder(verbs :+ GatherVerb.build(fun))

    /** Add a [[com.dixa.twilio.model.twiml.verb.GatherVerb.Unverified]] to the response.
      *
      * Calling this will prevent you from calling [[buildVerified]] and instead limit you to
      * [[buildUnverified]]. For that reason it's recommended to call [[addGather]] instead if
      * possible. However in some situations it might not be possible, such as if you create the
      * nested verbs of the gather from very dynamic input data, it might not be possible, or at
      * least a lot more easy to call this instead.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather
      */
    def addGatherUnverified(fun: GatherVerb.BuildFunctionUnverified)(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, TwimlConstraints.VerifiedFalse, L] =
      new Builder(verbs :+ GatherVerb.build(fun))

    /** Add the Hangup verb to the response.
      *
      * After this, no other verbs will be allowed on the response.
      *
      * The Hangup verb ends a call. If used as the first verb in a TwiML response it does not
      * prevent Twilio from answering the call and billing your account. The only way to not answer
      * a call and prevent billing is to use the Reject verb.
      *
      * A Hangup verb don't have any attributes and don't support nesting any verbs.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/hangup
      */
    def addHangup(
        fun: HangupVerb.BuildFunction
    )(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, LastAddedVerbProhibitMoreVerbsTrue] =
      new Builder(verbs :+ HangupVerb.build(fun))

    /** Add the Reject verb to the response.
      *
      * The <Reject> verb rejects an incoming call to your Twilio number without billing you. This
      * is very useful for blocking unwanted calls.
      *
      * If the first verb in a TwiML document is <Reject>, Twilio will not pick up the call. The
      * call ends with a status of busy or no-answer, depending on the verb’s reason attribute. Any
      * verbs after <Reject> are unreachable and ignored.
      *
      * Using <Reject> as the first verb in your response is the only way to prevent Twilio from
      * answering a call. Any other response will result in an answered call and your account will
      * be billed.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/reject
      */
    def addReject(
        fun: RejectVerb.BuildFunction
    )(
        implicit ev: L =:= LastAddedVerbProhibitMoreVerbsFalse
    ): Builder[BuildableTrue, V, LastAddedVerbProhibitMoreVerbsTrue] = new Builder(
      verbs :+ RejectVerb.build(fun)
    )

    /** Build a verified [[Response]]
      *
      * By verified we mean an instance of a [[Response]], that is guaranteed to produce valid
      * TwiML.
      *
      * To call this method you must have:
      *   1. Added at least one verb.
      *   1. Added no custom verb - [[Response.Builder.addCustomVerbs]].
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
      *   1. Added a custom vert via [[Response.Builder.addCustomVerbs]]
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
    * exactly as is, both when `xmlCompact` and `xmlPretty` is called.
    */
  def fromString(suppliedTwiml: String): UnverifiedFromString = UnverifiedFromStringImpl(
    suppliedTwiml
  )

}
