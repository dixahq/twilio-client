package com.dixa.twilio.model.twiml

import com.dixa.twilio.model.StringUtil
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.PhantomTypes.{
  Buildable,
  BuildableFalse,
  BuildableTrue,
  HasSingleAllowedValueAlready,
  HasSingleAllowedValueAlreadyFalse,
  HasSingleAllowedValueAlreadyTrue
}

/** Represent the Dial verb in TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
trait Dial extends TwimlElement.Verb

object Dial {

  /** Trait that should be mixed in by Noun traits that are supported in the Dial verb. */
  trait DialNoun extends TwimlElement.Noun

  final class Builder[
      B <: Buildable,
      S <: HasSingleAllowedValueAlready
  ](value: ValueToUse) {

    def withPhoneNumber(pn: PhoneNumberE164)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue] =
      new Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue](ValuePhoneNumber(pn))

    def withConference(fun: Conference.BuildFunction)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue] = {
      val conference = Conference.build(fun)
      new Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue](ValueNoun(conference))
    }

    def build()(
        implicit evB: B =:= BuildableTrue
    ): Dial = DialImpl(value)
  }

  type BuilderStartState = Builder[BuildableFalse, HasSingleAllowedValueAlreadyFalse]
  type BuildFunction     = BuilderStartState => Dial

  def build(fun: BuildFunction): Dial = fun(new BuilderStartState(NotSetValue))

  private sealed abstract class ValueToUse
  private object NotSetValue                                     extends ValueToUse
  private final case class ValuePhoneNumber(pn: PhoneNumberE164) extends ValueToUse
  private final case class ValueNoun(noun: DialNoun)             extends ValueToUse

  private final case class DialImpl(value: ValueToUse) extends Dial {
    override def xmlCompact: String = {
      val valueAsString = value match {
        case ValuePhoneNumber(pn) => pn.asString
        case ValueNoun(noun)      => noun.xmlCompact
        case NotSetValue => throw new AssertionError("This should never happen. Report a bug.")
      }
      s"""<Dial>$valueAsString</Dial>"""
    }

    override def xmlPretty: String = {
      val valueAsString = value match {
        case ValuePhoneNumber(pn) => pn.asString
        case ValueNoun(noun)      => noun.xmlPretty
        case NotSetValue => throw new AssertionError("This should never happen. Report a bug.")
      }
      s"""<Dial>
         |${StringUtil.indentEveryLineWith2Spaces(valueAsString)}
         |</Dial>""".stripMargin
    }
  }

}
