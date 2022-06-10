package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor.{
  ReadAllAccountsException,
  ReadAllAccountsRequest
}
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  Formatter,
  HttpEntityString,
  PagingStyle,
  TwilioUri
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import io.circe.generic.auto._

import java.time.Instant
import scala.concurrent.ExecutionContext

class ReadAllAccountsRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ReadAllAccountsRequestExecutor {

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ReadAllAccountsRequestExecutor.ReadAllAccountsRequest
  ): Either[ReadAllAccountsException, HttpRequest] = {
    val statusParam = req.status.map(s => s"&Status=${s.twilioString}").getOrElse("")
    Right(
      TwilioPath(
        ApiSubDomain.Api,
        HttpMethods.GET,
        s"/2010-04-01/Accounts.json?PageSize=1000$statusParam"
      ).createHttpRequest(connSettings)
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ReadAllAccountsException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = ReadAllAccountsException.Unspecified(msg, cause)

  private final case class TwilioAccountsOuterJsonRep(accounts: Vector[TwilioAccountJsonRep])

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ReadAllAccountsRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ReadAllAccountsException, TwilioAccount]] = {
    responseEntity.parse[TwilioAccountsOuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ReadAllAccountsException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: TwilioAccountsOuterJsonRep) =>
        decoded.accounts.map { jsonRep =>
          Right(
            TwilioAccount(
              TwilioAccount.Name(jsonRep.friendly_name),
              TwilioAccount.Sid(jsonRep.sid),
              TwilioAccount.Status.fromTwilioStringUnsafe(jsonRep.status),
              TwilioAccount.Sid(jsonRep.owner_account_sid),
              TwilioAccount.AuthToken(jsonRep.auth_token),
              TwilioAccount.Type.fromTwilioStringUnsafe(jsonRep.`type`),
              Instant.from(Formatter.dateTime.parse(jsonRep.date_created)),
              Instant.from(Formatter.dateTime.parse(jsonRep.date_updated))
            )
          )
        }.toList
    }
  }

  private final case class TwilioResponseNextPageJsonRep(next_page_uri: Option[String])

  override protected def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[UnspecifiedException, Option[HttpRequest]] = {
    entityString
      .parse[TwilioResponseNextPageJsonRep]()
      .left
      .map { ex =>
        createUnspecifiedException(Some(ex.getMessage), Some(ex.cause))
      }
      .map { response =>
        response.next_page_uri.map { nextPage =>
          TwilioUri
            .autoDetect(nextPage, HttpMethods.GET, ApiSubDomain.Api)
            .createHttpRequest(connectionSettings)
        }
      }
  }
}
