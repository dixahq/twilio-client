package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipIpAddressDeleteRequestExecutor
import com.dixa.twilio.client.voice.SipIpAddressDeleteRequestExecutor.{
  SipIpAddressDeleteException,
  SipIpAddressDeleteRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit

import scala.concurrent.ExecutionContext

private[client] class SipIpAddressDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipIpAddressDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipIpAddressDeleteRequest
  ): Either[SipIpAddressDeleteException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/IpAccessControlLists/${req.ipAccessControlListSid}/IpAddresses/${req.sid}.json",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SipIpAddressDeleteException.Api =
    SipIpAddressDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipIpAddressDeleteException.Unspecified =
    SipIpAddressDeleteException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: SipIpAddressDeleteRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[SipIpAddressDeleteException, FUnit] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(FUnit)
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(req, entity)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: SipIpAddressDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => SipIpAddressDeleteException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              SipIpAddressDeleteException.SipIpAddressNotFound(
                req.accountSid,
                req.ipAccessControlListSid,
                req.sid
              )
            )
          case other =>
            Left(
              SipIpAddressDeleteException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
