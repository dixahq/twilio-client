package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.ConferenceRecordingReadRequestExecutor
import com.dixa.twilio.client.voice.ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

class ConferenceRecordingReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ConferenceRecordingReadRequestExecutor {

  import ConferenceRecordingReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest
  ): Either[
    ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withParam(conferenceParamKey, req.conferenceSid)
      .withOptionalParam(callSidParamKey, req.callSid)
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalBooleanParam(includeSoftDeletedParamKey, req.includeSoftDeleted)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Conferences/${req.conferenceSid}/Recordings.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ConferenceRecordingReadException.Api =
    ConferenceRecordingReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceRecordingReadException.Unspecified =
    ConferenceRecordingReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    ConferenceRecordingReadRequestExecutor.ConferenceRecordingReadException,
    Recording
  ]] = {
    responseEntity.parse[RecordingListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            ConferenceRecordingReadException.Unspecified(
              Some(ex.cause.getMessage),
              Some(ex.cause)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.recordings.map { _.toModel }.map { Right(_) }
    }

  }

}

private object ConferenceRecordingReadRequestExecutorImpl {
  private val callSidParamKey            = "CallSid"
  private val conferenceParamKey         = "ConferenceSid"
  private val dateCreatedParamKey        = "DateCreated"
  private val includeSoftDeletedParamKey = "IncludeSoftDeleted"
}
