package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.TwimlConstraints._
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.noun.ConferenceNoun

import scala.collection.immutable

/** Represent the Dial verb in TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
trait DialVerb extends TwimlElement.Verb {
  override final protected def tagName: String = "Dial"

  override final protected def tagAttributes: immutable.Seq[(String, String)] = Nil
}

object DialVerb {

  /** Trait that should be mixed in by Noun traits that are supported in the Dial verb. */
  trait DialNoun extends TwimlElement.Noun

  final class Builder[
      B <: Buildable,
      S <: HasSingleAllowedValueAlready
  ] private (value: ValueToUse) {

    def withPhoneNumber(pn: PhoneNumberE164)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue] =
      new Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue](ValuePhoneNumber(pn))

    def withConference(fun: ConferenceNoun.BuildFunction)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue] = {
      val conference = ConferenceNoun.build(fun)
      new Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue](ValueNoun(conference))
    }

    def build()(
        implicit evB: B =:= BuildableTrue
    ): DialVerb = DialVerbImpl(value)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState(NotSetValue)
  }

  type BuilderStartState = Builder[BuildableFalse, HasSingleAllowedValueAlreadyFalse]
  type BuildFunction     = BuilderStartState => DialVerb

  def build(fun: BuildFunction): DialVerb = fun(Builder.empty)

  private sealed abstract class ValueToUse
  private object NotSetValue                                     extends ValueToUse
  private final case class ValuePhoneNumber(pn: PhoneNumberE164) extends ValueToUse
  private final case class ValueNoun(noun: DialNoun)             extends ValueToUse

  private final case class DialVerbImpl(value: ValueToUse) extends DialVerb {

    override protected def tagSubElements: immutable.Seq[TwimlElement] = value match {
      case ValueNoun(noun) => List(noun)
      case _               => Nil
    }

    override protected def tagValue: Option[String] = value match {
      case ValuePhoneNumber(pn) => Some(pn.asString)
      case _                    => None
    }
  }

}
