package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.TwimlElement

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

  final class Builder private[GatherVerb] (
      nestedVerbs: Vector[TwimlElement.Verb] = Vector.empty,
      action: Option[CallbackUrl] = None,
      // Double option, as the value itself is actually an option
      finishOnKey: Option[Option[DtmfDigit]] = None
  ) {

    private def copy(
        nestedVerbs: Vector[TwimlElement.Verb] = this.nestedVerbs,
        action: Option[CallbackUrl] = this.action,
        finishOnKey: Option[Option[DtmfDigit]] = this.finishOnKey
    ) = new Builder(nestedVerbs, action, finishOnKey)

    /** Add a nested Pause verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/pause
      */
    def addPause(fun: PauseVerb.BuildFunction): Builder =
      copy(nestedVerbs = nestedVerbs :+ PauseVerb.build(fun))

    /** Add a nested Play verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/play
      */
    def addPlay(fun: PlayVerb.BuildFunction): Builder =
      copy(nestedVerbs = nestedVerbs :+ PlayVerb.build(fun))

    /** Add a nested Say verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/say
      */
    def addSay(fun: SayVerb.BuildFunction): Builder =
      copy(nestedVerbs = nestedVerbs :+ SayVerb.build(fun))

    /** Sets the action attribute
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#action
      */
    def withAction(callbackUrl: CallbackUrl): Builder = copy(action = Some(callbackUrl))

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
    def withFinishOnKey(finishOnKey: Option[DtmfDigit]): Builder =
      copy(finishOnKey = Some(finishOnKey))

    def build(): GatherVerb = GatherVerbImpl(nestedVerbs, action, finishOnKey)
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => GatherVerb

  def build(fun: BuildFunction): GatherVerb = fun(
    new BuilderStartState()
  )

  private final case class GatherVerbImpl(
      nestedVerbs: Seq[TwimlElement.Verb],
      action: Option[CallbackUrl],
      finishOnKey: Option[Option[DtmfDigit]]
  ) extends GatherVerb {

    private val actionAttribute = action.map(x => s""" action="${x.twilioString}"""").getOrElse("")
    private val finishOnKeyAttribute = finishOnKey
      .map(x => s""" finishOnKey="${x.map(_.twilioString).getOrElse("")}"""")
      .getOrElse("")
    private val gatherStart = s"""<Gather$actionAttribute$finishOnKeyAttribute"""

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
