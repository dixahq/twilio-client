package com.dixa.twilio.client.impl.phonenumber

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.Uri.Query
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.{
  OutgoingCallerIdReadException,
  OutgoingCallerIdReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.OutgoingCallerId
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.concurrent.ExecutionContext

private[impl] class OutgoingCallerIdReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends OutgoingCallerIdReadRequestExecutor {

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: OutgoingCallerIdReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[
    Either[OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException, OutgoingCallerId]
  ] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(OutgoingCallerIdReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.outgoing_caller_ids.map { jsonReq =>
          Right(jsonReq.toModel)
        }
    }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest
  ): Either[OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException, HttpRequest] = {
    val query = {
      val friendlyNameQuery: Option[(String, String)] = req.filter.friendlyName.map {
        friendlyName =>
          "FriendlyName" -> friendlyName.toString
      }
      val phonenumberQuery: Option[(String, String)] = req.filter.phoneNumber.map { phoneNumber =>
        "PhoneNumber" -> phoneNumber.asString
      }
      Query(
        Map("PageSize" -> req.filter.pageSize.toString) ++
          List(
            friendlyNameQuery,
            phonenumberQuery
          ).flatten.toMap
      )
    }

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/OutgoingCallerIds.json?${query.toString}",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdReadException.Api = OutgoingCallerIdReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): OutgoingCallerIdReadException.Unspecified =
    OutgoingCallerIdReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(outgoing_caller_ids: List[OutgoingCallerIdJsonRep])

  private implicit val outerJsonRepReader: Reader[OuterJsonRep] =
    macroR[OuterJsonRep]

}
