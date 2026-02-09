package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.IpAccessControlListMappingCreateRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.IpAccessControlListMapping

import scala.concurrent.ExecutionContext

private[client] class IpAccessControlListMappingCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends IpAccessControlListMappingCreateRequestExecutor {

  import com.dixa.twilio.client.voice.IpAccessControlListMappingCreateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IpAccessControlListMappingCreateRequest
  ): Either[IpAccessControlListMappingCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("IpAccessControlListSid", req.ipAccessControlListSid)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/Domains/${req.domainSid}/Auth/Calls/IpAccessControlListMappings.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: IpAccessControlListMappingCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[IpAccessControlListMappingCreateException, IpAccessControlListMapping] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        // This is a bit of a special case, but because this is a sub resource, there is no valuable
        // informations in the returned json, so just ignore it, and fill in the model class
        // with the information that we used in the request.
        Right(
          IpAccessControlListMapping(
            request.accountSid,
            request.domainSid,
            request.ipAccessControlListSid
          )
        )
      case StatusCodes.BadRequest => buildResultForBadRequestResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def buildResultForBadRequestResponse(
      req: IpAccessControlListMappingCreateRequest,
      entity: HttpEntityString
  ): Either[IpAccessControlListMappingCreateException, IpAccessControlListMapping] = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => IpAccessControlListMappingCreateException.Unspecified(None, Some(e)))
      .flatMap { decoded =>
        decoded.code match {
          case 21231L =>
            Left(
              IpAccessControlListMappingCreateException.IpAccessControlListMappingAlreadyExists(
                req.domainSid,
                req.ipAccessControlListSid
              )
            )
          case other =>
            Left(
              IpAccessControlListMappingCreateException.Unspecified(
                Some(
                  s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                    s"$other represent. Full error entity from Twilio: $entity"
                ),
                None
              )
            )
        }
      }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): IpAccessControlListMappingCreateException.Api =
    IpAccessControlListMappingCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): IpAccessControlListMappingCreateException.Unspecified =
    IpAccessControlListMappingCreateException.Unspecified(msg, cause)
}
