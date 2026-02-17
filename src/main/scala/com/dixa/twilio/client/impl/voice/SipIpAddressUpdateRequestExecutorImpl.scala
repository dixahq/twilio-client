package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipIpAddressUpdateRequestExecutor
import com.dixa.twilio.client.voice.SipIpAddressUpdateRequestExecutor.{
  SipIpAddressUpdateException,
  SipIpAddressUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.SipIpAddress

import scala.concurrent.ExecutionContext

private[client] class SipIpAddressUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipIpAddressUpdateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipIpAddressUpdateRequest
  ): Either[SipIpAddressUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalParam("IpAddress", req.ipAddress)
      .withOptionalParam("CidrPrefixLength", req.cidrPrefixLength)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists/${req.ipAccessControlListSid}/IpAddresses/${req.sid}.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SipIpAddressUpdateException.Api =
    SipIpAddressUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipIpAddressUpdateException.Unspecified =
    SipIpAddressUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: SipIpAddressUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[SipIpAddressUpdateException, SipIpAddress] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[SipIpAddressJsonRep](entity).map(_.toModelUnsafe)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(
      req: SipIpAddressUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => SipIpAddressUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              SipIpAddressUpdateException.SipIpAddressNotFound(
                req.accountSid,
                req.ipAccessControlListSid,
                req.sid
              )
            )
          case other =>
            Left(
              SipIpAddressUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
