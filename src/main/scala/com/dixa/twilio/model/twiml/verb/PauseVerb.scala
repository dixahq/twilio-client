package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.PositiveInteger
import com.dixa.twilio.model.twiml.TwimlElement

import java.time.Duration

/** Representation of the Pause Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/pause
  */
sealed trait PauseVerb extends TwimlElement.Verb

object PauseVerb {
  final class Builder private[PauseVerb] (length: Option[Long] = None) {

    def withLengthInSeconds(length: PositiveInteger): Builder =
      new Builder(Some(length.int.longValue()))

    def withLength(length: Duration): Builder =
      new Builder(Some(length.getSeconds))

    def build(): PauseVerb = PauseVerbImpl(length)
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => PauseVerb

  def build(fun: BuildFunction): PauseVerb = fun(
    new BuilderStartState()
  )

  private final case class PauseVerbImpl(lengthInSeconds: Option[Long]) extends PauseVerb {

    private val lengthAttribute = lengthInSeconds.map(i => s""" length="$i"""").getOrElse("")

    override def xmlCompact: String =
      s"""<Pause$lengthAttribute/>"""

    override def xmlPretty: String =
      s"""<Pause$lengthAttribute />""".stripMargin
  }
}
