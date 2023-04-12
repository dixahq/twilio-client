package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.callback.CallbackUrl
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
      action: Option[CallbackUrl] = None
  ) {

    private def copy(
        nestedVerbs: Vector[TwimlElement.Verb] = this.nestedVerbs,
        action: Option[CallbackUrl] = this.action
    ) = new Builder(nestedVerbs, action)

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

    def withAction(callbackUrl: CallbackUrl): Builder = copy(action = Some(callbackUrl))

    def build(): GatherVerb = GatherVerbImpl(nestedVerbs, action)
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => GatherVerb

  def build(fun: BuildFunction): GatherVerb = fun(
    new BuilderStartState()
  )

  private final case class GatherVerbImpl(
      nestedVerbs: Seq[TwimlElement.Verb],
      action: Option[CallbackUrl]
  ) extends GatherVerb {

    private val actionAttribute = action.map(x => s""" action="${x.twilioString}"""").getOrElse("")
    private val gatherStart     = s"""<Gather$actionAttribute"""

    override def xmlCompact: String = {
//      val loopAttribute = loopValue.map(l => s""" loop="$l"""").getOrElse("")
//      s"""<Play$digitsAttribute$loopAttribute>${StringUtil.xmlEscape(url)}</Play>"""
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
