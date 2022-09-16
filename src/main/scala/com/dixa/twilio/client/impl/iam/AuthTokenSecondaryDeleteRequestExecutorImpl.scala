package com.dixa.twilio.client.impl.iam

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.iam.AuthTokenSecondaryDeleteRequestExecutor
import com.dixa.twilio.client.iam.AuthTokenSecondaryDeleteRequestExecutor.{
  AuthTokenSecondaryDeleteException,
  AuthTokenSecondaryDeleteRequest
}
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[iam] final class AuthTokenSecondaryDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AuthTokenSecondaryDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Accounts

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AuthTokenSecondaryDeleteRequest
  ): Either[AuthTokenSecondaryDeleteException, HttpRequest] =
    createHttpRequestFor(s"/v1/AuthTokens/Secondary", connSettings)

  override protected def mapApiException(
      apiException: ApiException
  ): AuthTokenSecondaryDeleteException.Api =
    AuthTokenSecondaryDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AuthTokenSecondaryDeleteException.UnspecifiedError =
    AuthTokenSecondaryDeleteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AuthTokenSecondaryDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AuthTokenSecondaryDeleteException, Done] =
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Done)
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForNotFoundResponse(entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => createUnspecifiedException("Error parsing entity for 404 response", e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            // Twilio returns this if you do not have the API enabled, and as there is no
            // variables in the path, it should be safe to assume that it's the ony thing
            // this code can mean for this API call.
            Left(AuthTokenSecondaryDeleteException.SecondaryAuthTokenNotFoundOnAccountException())
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
