package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipIpAddressFetchRequestExecutor
import com.dixa.twilio.client.voice.SipIpAddressFetchRequestExecutor.{
  SipIpAddressFetchException,
  SipIpAddressFetchRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.SipIpAddress

import scala.concurrent.ExecutionContext

private[client] class SipIpAddressFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipIpAddressFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipIpAddressFetchRequest
  ): Either[SipIpAddressFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists/${req.ipAccessControlListSid}/IpAddresses/${req.sid}.json",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SipIpAddressFetchException.Api =
    SipIpAddressFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipIpAddressFetchException.Unspecified =
    SipIpAddressFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: SipIpAddressFetchRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[SipIpAddressFetchException, SipIpAddress] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[SipIpAddressJsonRep](entity).map(_.toModelUnsafe)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(
      req: SipIpAddressFetchRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => SipIpAddressFetchException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              SipIpAddressFetchException.SipIpAddressNotFound(
                req.accountSid,
                req.ipAccessControlListSid,
                req.sid
              )
            )
          case other =>
            Left(
              SipIpAddressFetchException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
