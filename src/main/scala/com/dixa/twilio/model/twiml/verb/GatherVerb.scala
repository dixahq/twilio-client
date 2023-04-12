package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.TwimlElement

import scala.annotation.nowarn

/** Representation of the Gather Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/gather
  */
sealed trait GatherVerb extends TwimlElement.Verb

object GatherVerb {

  /** GatherVerb specific phantom types, used to enforce build constraints compile time. */
  object PhantomTypes {

    sealed trait HasDtmfInput
    sealed trait HasDtmfInputTrue  extends HasDtmfInput
    sealed trait HasDtmfInputFalse extends HasDtmfInput

    sealed trait HasSpeechInput
    sealed trait HasSpeechInputTrue  extends HasSpeechInput
    sealed trait HasSpeechInputFalse extends HasSpeechInput

    sealed trait DtmfInputRequired
    sealed trait DtmfInputRequiredTrue  extends DtmfInputRequired
    sealed trait DtmfInputRequiredFalse extends DtmfInputRequired

    sealed trait SpeechInputRequired
    sealed trait SpeechInputRequiredTrue  extends SpeechInputRequired
    sealed trait SpeechInputRequiredFalse extends SpeechInputRequired
  }

  final class Builder[
      DtmfInput <: PhantomTypes.HasDtmfInput,
      SpeechInput <: PhantomTypes.HasSpeechInput,
      DtmfInputRequired <: PhantomTypes.DtmfInputRequired,
      SpeechInputRequired <: PhantomTypes.SpeechInputRequired
  ] private[GatherVerb] (
      nestedVerbs: Vector[TwimlElement.Verb] = Vector.empty,
      action: Option[CallbackUrl] = None,
      // Double option, as the value itself is actually an option
      finishOnKey: Option[Option[DtmfDigit]] = None,
      hints: Vector[String] = Vector.empty,
      input: Option[String] = None
  ) {

    private def copy[
        DtmfInput2 <: PhantomTypes.HasDtmfInput,
        SpeechInput2 <: PhantomTypes.HasSpeechInput,
        DtmfInputRequired2 <: PhantomTypes.DtmfInputRequired,
        SpeechInputRequired2 <: PhantomTypes.SpeechInputRequired
    ](
        nestedVerbs: Vector[TwimlElement.Verb] = this.nestedVerbs,
        action: Option[CallbackUrl] = this.action,
        finishOnKey: Option[Option[DtmfDigit]] = this.finishOnKey,
        hints: Vector[String] = this.hints,
        input: Option[String] = this.input
    ) = new Builder[DtmfInput2, SpeechInput2, DtmfInputRequired2, SpeechInputRequired2](
      nestedVerbs,
      action,
      finishOnKey,
      hints,
      input
    )

    /** Add a nested Pause verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/pause
      */
    def addPause(
        fun: PauseVerb.BuildFunction
    ): Builder[DtmfInput, SpeechInput, DtmfInputRequired, SpeechInputRequired] =
      copy(nestedVerbs = nestedVerbs :+ PauseVerb.build(fun))

    /** Add a nested Play verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/play
      */
    def addPlay(
        fun: PlayVerb.BuildFunction
    ): Builder[DtmfInput, SpeechInput, DtmfInputRequired, SpeechInputRequired] =
      copy(nestedVerbs = nestedVerbs :+ PlayVerb.build(fun))

    /** Add a nested Say verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/say
      */
    def addSay(
        fun: SayVerb.BuildFunction
    ): Builder[DtmfInput, SpeechInput, DtmfInputRequired, SpeechInputRequired] =
      copy(nestedVerbs = nestedVerbs :+ SayVerb.build(fun))

    /** Sets the action attribute
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#action
      */
    def withAction(
        callbackUrl: CallbackUrl
    ): Builder[DtmfInput, SpeechInput, DtmfInputRequired, SpeechInputRequired] =
      copy(action = Some(callbackUrl))

    /** Sets the finishOnKey attribute.
      *
      * The default is '#' so not setting it at all would be the same as setting it to
      * `Some(DtmfDiget.#)`. Setting it to None correspond to setting it to empty String, and that
      * corresponds to no key ending the gather, and that will result in the gather only being ended
      * by timeout.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#finishonkey
      */
    @nowarn(value = "cat=unused-params")
    def withFinishOnKey(finishOnKey: Option[DtmfDigit])(
        implicit ev: DtmfInput =:= PhantomTypes.HasDtmfInputTrue
    ): Builder[DtmfInput, SpeechInput, PhantomTypes.DtmfInputRequiredTrue, SpeechInputRequired] =
      copy(finishOnKey = Some(finishOnKey))

    /** Add a hint to the hint attribute.
      *
      * Can be called multiple time, however twilio does have some constraints on how many hints you
      * can add, and how many chars a hint max can consist off. The compiler cannot help you with
      * that, so you would need to look that up in Twilio documentation (link below), and make sure
      * you obey to these rules.
      *
      * Hints are only used for speech, and as such is only allowed to be added, if the input
      * attribute includes speech.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#hints
      */
    @nowarn(value = "cat=unused-params")
    def addHint(hint: String)(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[DtmfInput, SpeechInput, DtmfInputRequired, PhantomTypes.SpeechInputRequiredTrue] =
      copy(hints = this.hints :+ hint)

    /** Sets the input attribute value to: dtmf
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    @nowarn(value = "cat=unused-params")
    def withInputDtmf()(
        implicit ev: SpeechInputRequired =:= PhantomTypes.SpeechInputRequiredFalse
    ): Builder[
      PhantomTypes.HasDtmfInputTrue,
      PhantomTypes.HasSpeechInputFalse,
      DtmfInputRequired,
      SpeechInputRequired
    ] = copy(input = Some("dtmf"))

    /** Sets the input attribute value to: speech
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    @nowarn(value = "cat=unused-params")
    def withInputSpeech()(
        implicit ev: DtmfInputRequired =:= PhantomTypes.DtmfInputRequiredFalse
    ): Builder[
      PhantomTypes.HasDtmfInputFalse,
      PhantomTypes.HasSpeechInputTrue,
      DtmfInputRequired,
      SpeechInputRequired
    ] =
      copy(input = Some("speech"))

    /** Sets the input attribute value to: dtmf speech
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    def withInputDtmfSpeech(): Builder[
      PhantomTypes.HasDtmfInputTrue,
      PhantomTypes.HasSpeechInputTrue,
      DtmfInputRequired,
      SpeechInputRequired
    ] = copy(input = Some("dtmf speech"))

    def build(): GatherVerb = GatherVerbImpl(nestedVerbs, action, finishOnKey, hints, input)
  }

  // Dtmf is default input, so set HasDtmfInputTrue to begin with.
  type BuilderStartState = Builder[
    PhantomTypes.HasDtmfInputTrue,
    PhantomTypes.HasSpeechInputFalse,
    PhantomTypes.DtmfInputRequiredFalse,
    PhantomTypes.SpeechInputRequiredFalse
  ]
  type BuildFunction = BuilderStartState => GatherVerb

  def build(fun: BuildFunction): GatherVerb = fun(
    new BuilderStartState()
  )

  private final case class GatherVerbImpl(
      nestedVerbs: Seq[TwimlElement.Verb],
      action: Option[CallbackUrl],
      finishOnKey: Option[Option[DtmfDigit]],
      hints: Seq[String],
      input: Option[String]
  ) extends GatherVerb {

    private val actionAttribute = action.map(x => s""" action="${x.twilioString}"""").getOrElse("")
    private val finishOnKeyAttribute = finishOnKey
      .map(x => s""" finishOnKey="${x.map(_.twilioString).getOrElse("")}"""")
      .getOrElse("")
    private val hintsAttribute = if (hints.isEmpty) "" else s""" hints="${hints.mkString(", ")}""""
    private val inputAttribute = input.map(x => s""" input="$x"""").getOrElse("")
    private val gatherStart =
      s"""<Gather$actionAttribute$finishOnKeyAttribute$hintsAttribute$inputAttribute"""

    override def xmlCompact: String = {
      if (nestedVerbs.isEmpty) s"""$gatherStart/>"""
      else s"""$gatherStart>${nestedVerbs.map(_.xmlCompact).mkString}</Gather>"""
    }

    override def xmlPretty: String = if (nestedVerbs.isEmpty) s"""$gatherStart />"""
    else {
      val verbsAsXmlList = nestedVerbs.map(v => StringUtil.indentEveryLineWith2Spaces(v.xmlPretty))
      s"""$gatherStart>
         |${verbsAsXmlList.mkString(System.lineSeparator())}
         |</Gather>
         |""".stripMargin
    }
  }
}
