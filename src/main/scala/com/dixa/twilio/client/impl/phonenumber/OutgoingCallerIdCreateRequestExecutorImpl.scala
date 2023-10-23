package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdCreateRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

private[client] class OutgoingCallerIdCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends OutgoingCallerIdCreateRequestExecutor {

  import OutgoingCallerIdCreateRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateRequest
  ): Either[OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException, HttpRequest] = {
    val params =
      QueryParamBuilder.empty
        .withParam(phoneNumberKey, req.phoneNumber)
        .withOptionalParam(friendlyNameKey, req.friendlyName)
        .withOptionalParam(callDelayKey, req.callDelay)
        .withOptionalParam(extensionKey, req.extension)
        .withOptionalParam(statusCallbackKey, req.statusCallback)
        .withOptionalParam(statusCallbackMethodKey, req.statusCallbackMethod)
        .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/OutgoingCallerIds.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[
    OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException,
    OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse
  ] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[OutgoingCallerIdCreateResponseJsonRep](entity).flatMap(_.toModelSafe)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdCreateException.Api =
    OutgoingCallerIdCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): OutgoingCallerIdCreateException.Unspecified =
    OutgoingCallerIdCreateException.Unspecified(msg, cause)
}

private object OutgoingCallerIdCreateRequestExecutorImpl {
  private val phoneNumberKey          = "PhoneNumber"
  private val friendlyNameKey         = "FriendlyName"
  private val callDelayKey            = "CallDelay"
  private val extensionKey            = "Extension"
  private val statusCallbackKey       = "StatusCallback"
  private val statusCallbackMethodKey = "StatusCallbackMethod"

  private final case class OutgoingCallerIdCreateResponseJsonRep(
      account_sid: String,
      phone_number: String,
      friendly_name: Option[String],
      validation_code: String,
      call_sid: String,
  ) {

    private[phonenumber] def toModelSafe: Either[
      OutgoingCallerIdCreateException.ValidationCodeFormatException,
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse
    ] = {
      DtmfString
        .fromStringOnlyDtmfDigits(validation_code)
        .map(dtmfString =>
          OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse(
            accountSid = TwilioAccount.Sid.unsafe(account_sid),
            friendlyName =
              emptyStringToNone(friendly_name).map(OutgoingCallerId.FriendlyName.constructInstance),
            phoneNumber = PhoneNumberE164.unsafe(phone_number),
            validationCode = dtmfString,
            callSid = Call.Sid.unsafe(call_sid)
          )
        )
        .left
        .map(ex => OutgoingCallerIdCreateException.ValidationCodeFormatException(ex.getMessage))
    }

    private[phonenumber] def toModelUnsafe
        : OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse = {
      toModelSafe.fold(
        ex => throw ex,
        identity
      )
    }
  }

  private object OutgoingCallerIdCreateResponseJsonRep {

    implicit val outgoingCallerIdJsonRepReader: Reader[OutgoingCallerIdCreateResponseJsonRep] =
      macroR[OutgoingCallerIdCreateResponseJsonRep]
  }
}
