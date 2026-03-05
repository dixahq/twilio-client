package com.dixa.twilio.client.impl.iam

import com.dixa.twilio.client.iam.ApiKeyReadRequestExecutor
import com.dixa.twilio.client.iam.ApiKeyReadRequestExecutor.{KeyReadException, KeyReadRequest}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.ApiKey
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

private[client] class ApiKeyReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ApiKeyReadRequestExecutor {

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

  private def TimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

  // There are always flags but no policies on read operation. Eg something like:
  //  {
  //    "date_created": "Wed, 04 Mar 2026 10:23:47 +0000",
  //    "date_updated": "Wed, 04 Mar 2026 10:23:47 +0000",
  //    "flags": [ "restricted",  "rest_api", "signing" ],
  //    "friendly_name": "Tmp restricted test key",
  //    "sid": "SK289dbe6204dba5182886ac31d93ffeef"
  //  }
  private case class KeyJsonRep(
      sid: String,
      friendly_name: String,
      date_created: String,
      date_updated: String,
      // Should be there, but keep the option just in case twilio decide to return null instead of an empty array if key has no flags. I don't think that is a valid key, but better safe than sorry :D
      flags: Option[Set[String]] = None,
  ) {
    def toModel: ApiKey with ApiKey.HasFlags = {
      val flagsNoneOptional = flags.getOrElse(Set.empty)
      val flagsModel        =
        flagsNoneOptional.map(ApiKey.Flag.fromTwilioString).collect { case Some(flag) => flag }
      ApiKey(
        sid = ApiKey.Sid(sid),
        friendlyName = ApiKey.FriendlyName(friendly_name),
        dateCreated = Instant.from(TimeFormatter.parse(date_created)),
        dateUpdated = Instant.from(TimeFormatter.parse(date_updated))
      ).withFlags(flagsModel)
    }
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
  ): List[Either[KeyReadException, ApiKey with ApiKey.HasFlags]] =
    responseEntity.parse[KeyListJsonRep]() match {
      case Left(ex) =>
        List(Left(KeyReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(decoded) =>
        decoded.keys.map(jsonRep => Right(jsonRep.toModel))
    }
}
