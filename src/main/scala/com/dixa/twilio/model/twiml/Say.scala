package com.dixa.twilio.model.twiml

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait Say extends TwimlElement.Verb {}

object Say {

  final class Builder[B <: PhantomTypes.Buildable] private[Say] (text: String) {

    def withText(text: String): Builder[PhantomTypes.BuildableTrue] =
      new Builder[PhantomTypes.BuildableTrue](text = text)

    def build()(
        implicit ev: B =:= PhantomTypes.BuildableTrue
    ): Say = SayImpl(text)
  }
  type BuilderStartState = Builder[PhantomTypes.BuildableFalse]
  type BuildFunction     = BuilderStartState => Say

  def build(fun: BuildFunction): Say = fun(
    new BuilderStartState("")
  )

  private final case class SayImpl(text: String) extends Say {
    override val xmlCompact: String = s"""<Say>$text</Say>"""

    override def xmlPretty: String = xmlCompact
  }
}
