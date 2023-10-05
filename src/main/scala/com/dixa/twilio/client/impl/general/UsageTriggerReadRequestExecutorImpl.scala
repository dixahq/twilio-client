package com.dixa.twilio.client.impl.general

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.UsageTriggerReadRequestExecutor
import com.dixa.twilio.client.general.UsageTriggerReadRequestExecutor.UsageTriggerReadException
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.usage.UsageTrigger

import scala.concurrent.ExecutionContext

class UsageTriggerReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends UsageTriggerReadRequestExecutor {

  import UsageTriggerReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: UsageTriggerReadRequestExecutor.UsageTriggerReadRequest
  ): Either[
    UsageTriggerReadRequestExecutor.UsageTriggerReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam(triggerByKey, req.triggerBy)
      .withOptionalParam(usageCategoryKey, req.usageCategory)
      .withOptionalParam(recurringKey, req.recurring)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Usage/Triggers.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): UsageTriggerReadException.Api =
    UsageTriggerReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UsageTriggerReadException.Unspecified =
    UsageTriggerReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: UsageTriggerReadRequestExecutor.UsageTriggerReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    UsageTriggerReadRequestExecutor.UsageTriggerReadException,
    UsageTrigger
  ]] = {
    responseEntity.parse[UsageTriggerListJsonRep]() match {
      case Left(ex) =>
        println(ex)
        List(
          Left(
            UsageTriggerReadException.Unspecified(
              Some(ex.cause.getMessage),
              Some(ex.cause)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.usage_triggers.map { _.toModel }.map { Right(_) }
    }

  }

}

private object UsageTriggerReadRequestExecutorImpl {
  private val triggerByKey     = "TriggerBy"
  private val recurringKey     = "Recurring"
  private val usageCategoryKey = "UsageCategory"
}
