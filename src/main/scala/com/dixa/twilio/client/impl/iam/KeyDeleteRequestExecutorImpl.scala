package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.KeyDeleteRequestExecutor
import com.dixa.twilio.client.iam.KeyDeleteRequestExecutor.{KeyDeleteException, KeyDeleteRequest}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit

import scala.concurrent.ExecutionContext

private[client] class KeyDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends KeyDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Iam

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: KeyDeleteRequest
  ): Either[KeyDeleteException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid.toString)
      .build

    createHttpRequestFor(s"/v1/Keys/${req.sid}$params", connSettings)
  }

  override protected def mapApiException(
      apiException: ApiException
  ): KeyDeleteException.Api = KeyDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): KeyDeleteException.Unspecified = KeyDeleteException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: KeyDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[KeyDeleteException, FUnit] =
    httpResponse.status match {
      case StatusCodes.NoContent => Right(FUnit)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
