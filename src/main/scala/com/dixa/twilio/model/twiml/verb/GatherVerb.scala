package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.StringUtil
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

  final class Builder private[GatherVerb] (nestedVerbs: Vector[TwimlElement.Verb] = Vector.empty) {

    /** Add a nested Pause verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/pause
      */
    def addPause(fun: PauseVerb.BuildFunction): Builder = new Builder(
      nestedVerbs :+ PauseVerb.build(fun)
    )

    /** Add a nested Play verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/play
      */
    def addPlay(fun: PlayVerb.BuildFunction): Builder =
      new Builder(nestedVerbs :+ PlayVerb.build(fun))

    /** Add a nested Say verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/say
      */
    def addSay(fun: SayVerb.BuildFunction): Builder =
      new Builder(nestedVerbs :+ SayVerb.build(fun))

    def build(): GatherVerb = GatherVerbImpl(nestedVerbs)
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => GatherVerb

  def build(fun: BuildFunction): GatherVerb = fun(
    new BuilderStartState()
  )

  private final case class GatherVerbImpl(
      nestedVerbs: Seq[TwimlElement.Verb]
  ) extends GatherVerb {
    override def xmlCompact: String = {
//      val digitsAttribute = digits.map(d => s""" digits="${d.twilioString}"""").getOrElse("")
//      val loopAttribute = loopValue.map(l => s""" loop="$l"""").getOrElse("")
//      s"""<Play$digitsAttribute$loopAttribute>${StringUtil.xmlEscape(url)}</Play>"""
      if (nestedVerbs.isEmpty) """<Gather/>"""
      else s"""<Gather>${nestedVerbs.map(_.xmlCompact).mkString}</Gather>"""
    }

    override def xmlPretty: String = if (nestedVerbs.isEmpty) """<Gather />"""
    else {
      val verbsAsXmlList = nestedVerbs.map(v => StringUtil.indentEveryLineWith2Spaces(v.xmlPretty))
      s"""<Gather>
         |${verbsAsXmlList.mkString(System.lineSeparator())}
         |</Gather>
         |""".stripMargin
    }
  }
}
