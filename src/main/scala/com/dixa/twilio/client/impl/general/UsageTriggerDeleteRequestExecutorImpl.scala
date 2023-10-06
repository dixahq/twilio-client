package com.dixa.twilio.client.impl.general

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.UsageTriggerDeleteRequestExecutor
import com.dixa.twilio.client.general.UsageTriggerDeleteRequestExecutor.{
  UsageTriggerDeleteException,
  UsageTriggerDeleteRequest
}
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[general] final class UsageTriggerDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends UsageTriggerDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Accounts

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: UsageTriggerDeleteRequest
  ): Either[UsageTriggerDeleteException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid.twilioString}/Usage/Triggers/${req.usageTriggerSid.twilioString}.json",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): UsageTriggerDeleteException.Api =
    UsageTriggerDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UsageTriggerDeleteException.UnspecifiedError =
    UsageTriggerDeleteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: UsageTriggerDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[UsageTriggerDeleteException, Done] =
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Done)
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForNotFoundResponse(
      request: UsageTriggerDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => createUnspecifiedException("Error parsing entity for 404 response", e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            // Twilio returns this if you do not have the API enabled, and as there is no
            // variables in the path, it should be safe to assume that it's the ony thing
            // this code can mean for this API call.
            Left(
              UsageTriggerDeleteException
                .UsageTriggerNotFoundOnAccountException(request.accountSid, request.usageTriggerSid)
            )
          case other =>
            Left(
              createUnspecifiedException(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
