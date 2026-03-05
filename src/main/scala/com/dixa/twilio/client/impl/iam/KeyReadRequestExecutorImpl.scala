package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.KeyReadRequestExecutor
import com.dixa.twilio.client.iam.KeyReadRequestExecutor.{KeyReadException, KeyReadRequest}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.ApiKey

import scala.concurrent.ExecutionContext

private[client] class KeyReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends KeyReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Iam

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: KeyReadRequest
  ): Either[KeyReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid.toString)
      .build

    createHttpRequestFor(s"/v1/Keys$params", connSettings)
  }

  override protected def mapApiException(
      apiException: ApiException
  ): KeyReadException.Api = KeyReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): KeyReadException.Unspecified = KeyReadException.Unspecified(msg, cause)

  private case class KeyJsonRep(sid: String, friendly_name: String) {
    def toModel: ApiKey = ApiKey(
      sid = ApiKey.Sid(sid),
      friendlyName = ApiKey.FriendlyName(friendly_name)
    )
  }

  private case class KeyListJsonRep(keys: List[KeyJsonRep])

  private implicit val keyJsonRepReader: Reader[KeyJsonRep]         = macroR[KeyJsonRep]
  private implicit val keyListJsonRepReader: Reader[KeyListJsonRep] = macroR[KeyListJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: KeyReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[KeyReadException, ApiKey]] =
    responseEntity.parse[KeyListJsonRep]() match {
      case Left(ex) =>
        List(Left(KeyReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(decoded) =>
        decoded.keys.map(jsonRep => Right(jsonRep.toModel))
    }
}
