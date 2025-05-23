package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.CallRecordingReadRequestExecutor
import com.dixa.twilio.client.voice.CallRecordingReadRequestExecutor.CallRecordingReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

class CallRecordingReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallRecordingReadRequestExecutor {

  import CallRecordingReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallRecordingReadRequestExecutor.CallRecordingReadRequest
  ): Either[
    CallRecordingReadRequestExecutor.CallRecordingReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withParam(callSidParamKey, req.callSid)
      .withOptionalParam(conferenceParamKey, req.conferenceSid)
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalBooleanParam(includeSoftDeletedParamKey, req.includeSoftDeleted)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.callSid}/Recordings.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): CallRecordingReadException.Api =
    CallRecordingReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallRecordingReadException.Unspecified =
    CallRecordingReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: CallRecordingReadRequestExecutor.CallRecordingReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    CallRecordingReadRequestExecutor.CallRecordingReadException,
    Recording
  ]] = {
    responseEntity.parse[RecordingListJsonRep]() match {
      case Left(parsingException: HttpEntityString.JsonParsingException) =>
        List(
          Left(
            CallRecordingReadException.ResponseParsingFailed(
              parsingException.entity.toString,
              parsingException.getMessage,
              Some(parsingException)
            )
          )
        )
      case Left(ex) =>
        List(
          Left(
            CallRecordingReadException.Unspecified(
              Some(ex.getMessage),
              Some(ex)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.recordings.map { _.toModel }.map { Right(_) }
    }

  }

}

private object CallRecordingReadRequestExecutorImpl {
  private val callSidParamKey            = "CallSid"
  private val conferenceParamKey         = "ConferenceSid"
  private val dateCreatedParamKey        = "DateCreated"
  private val includeSoftDeletedParamKey = "IncludeSoftDeleted"
}
