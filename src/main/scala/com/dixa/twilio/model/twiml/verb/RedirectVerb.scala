package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.{HttpMethod, StringUtil}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.twiml.{PhantomTypes, Response, TwimlElement}

/** Representation of the Redirect Verb from TwiML
  *
  * Creating a [[Response]] via the [[Response.build]] method, is the preferred way to use this
  * trait.
  */
sealed trait RedirectVerb extends TwimlElement.Verb {}

object RedirectVerb {

  final class Builder[B <: PhantomTypes.Buildable] private[RedirectVerb] (
      callbackUrl: Option[CallbackUrl],
      method: Option[HttpMethod]
  ) {

    def withCallbackUrl(callbackUrl: CallbackUrl): Builder[PhantomTypes.BuildableTrue] =
      new Builder(Some(callbackUrl), method)

    def withMethod(method: HttpMethod): Builder[B] = new Builder(callbackUrl, Some(method))

    def build()(
        implicit ev: B =:= PhantomTypes.BuildableTrue
    ): RedirectVerb =
      RedirectVerbImpl(callbackUrl.get, method)
  }

  type BuilderStartState = Builder[PhantomTypes.BuildableFalse]
  type BuildFunction     = BuilderStartState => RedirectVerb

  def build(fun: BuildFunction): RedirectVerb = fun(
    new BuilderStartState(None, None)
  )

  private final case class RedirectVerbImpl(callbackUrl: CallbackUrl, method: Option[HttpMethod])
      extends RedirectVerb {

    override val xmlCompact: String = {
      val methodAtt = method.map(m => s""" method="$m"""").getOrElse("")
      s"""<Redirect$methodAtt>${StringUtil.xmlEscape(callbackUrl.toString)}</Redirect>"""
    }

    override def xmlPretty: String = xmlCompact
  }

}
