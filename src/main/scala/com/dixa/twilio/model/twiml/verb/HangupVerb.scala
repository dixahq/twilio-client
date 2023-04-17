package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.twiml.TwimlElement

/** Representation of the Hangup Verb from TwiML.
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/hangup
  */
sealed trait HangupVerb extends TwimlElement.Verb {}

object HangupVerb {

  final class Builder private[HangupVerb] () {

    def build(): HangupVerb = HangupVerbImpl
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => HangupVerb

  private val singleTonBuilderStartState = new BuilderStartState()

  def build(fun: BuildFunction): HangupVerb = fun(singleTonBuilderStartState)

  private final case object HangupVerbImpl extends HangupVerb {

    override val xmlCompact: String = "<Hangup/>"

    override val xmlPretty: String = "<Hangup />"
  }

}
