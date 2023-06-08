package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.twiml.TwimlElement

import scala.collection.immutable

/** Representation of the Hangup Verb from TwiML.
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/hangup
  */
sealed trait HangupVerb extends TwimlElement.Verb {
  override final protected val tagName: String = "Hangup"

  override final protected val tagAttributes: immutable.Seq[(String, String)] = Nil

  override final protected val tagSubElements: immutable.Seq[TwimlElement] = Nil

  override final protected val tagValue: Option[String] = None
}

object HangupVerb {

  final class Builder private () {

    def build(): HangupVerb = HangupVerbImpl
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => HangupVerb

  def build(fun: BuildFunction): HangupVerb = fun(Builder.empty)

  private final case object HangupVerbImpl extends HangupVerb

}
