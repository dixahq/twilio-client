package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.{HttpMethod, StringUtil}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.annotation.nowarn

/** Representation of the Redirect Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait RedirectVerb extends TwimlElement.Verb {}

object RedirectVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private[RedirectVerb] (
      callbackUrl: Option[CallbackUrl],
      method: Option[HttpMethod]
  ) {

    def withCallbackUrl(callbackUrl: CallbackUrl): Builder[TwimlConstraints.BuildableTrue] =
      new Builder(Some(callbackUrl), method)

    def withMethod(method: HttpMethod): Builder[B] = new Builder(callbackUrl, Some(method))

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): RedirectVerb =
      RedirectVerbImpl(callbackUrl.get, method)
  }

  type BuilderStartState = Builder[TwimlConstraints.BuildableFalse]
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
