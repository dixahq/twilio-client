package com.dixa.twilio.client.twilioClient

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchRequest
import com.dixa.twilio.client.iam.{AccountFetchRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client._
import com.dixa.twilio.client.impl.HttpEntityString
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, matching}
import org.scalamock.scalatest.AsyncMockFactory
import io.circe.generic.auto._

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

final class MultipleResponseSourceTest extends TwilioClientTest with AsyncMockFactory {

  import MultipleResponseSourceTest._

  classOf[MultipleResponseSource[_, _, _]].getSimpleName should {

    "Provide a source method that executes the http initial request the implementation provides, and " +
      "use the implementations response parsing to get the end result to return" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "plain/txt")
                .withBody(buildPagingBody(None, "ResponseFromTwilio"))
            )
        )

        val impl = new MultipleResponseSourceTestBaseImplemented {

          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): HttpRequest = HttpRequest(
            method = HttpMethods.GET,
            uri = s"http://localhost:${wireMockServer.port()}/test"
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              reponseEntity: HttpEntity.Strict
          ): Either[AbstractTestException, TestSuccess] = {
            val entityAsString = reponseEntity.data.utf8String
            try {
              Right(
                HttpEntityString(entityAsString).parseUnsafe[TestResponseNextPageJsonRep]().test
              )
            } catch {
              case _: Exception =>
                Left(
                  AbstractTestException.Undefined(
                    Some(s"Wrong entity given to implementation: $entityAsString"),
                    None
                  )
                )
            }
          }

          override protected def detectNextPage(entityString: HttpEntityString): Option[Uri] =
            detectNextPageImpl(entityString)

          override protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest =
            nextPageHttpRequestBuilderImpl(uri)
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { result =>
            assert(result === Seq(Right(TestSuccess("ResponseFromTwilio"))))
          }
      }

    "Provide a run method that async executes the http request the implementation provides, and " +
      "use the implementations response parsing to get the end result to return, also in cases " +
      "where it returns an error" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  buildPagingBody(
                    nextPageUri = Option(s"http://localhost:${wireMockServer.port()}/test?Page=2"),
                    "ResponseFromTwilio"
                  )
                )
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .withQueryParam("Page", matching("2"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildPagingBody(None, "ResponseFromTwilioMore"))
            )
        )

        val impl = new MultipleResponseSourceTestBaseImplemented {

          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): HttpRequest = HttpRequest(
            method = HttpMethods.GET,
            uri = s"http://localhost:${wireMockServer.port()}/test"
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              reponseEntity: HttpEntity.Strict
          ): Either[AbstractTestException, TestSuccess] = {
            val entityAsString = reponseEntity.data.utf8String
            try {
              Right(
                HttpEntityString(entityAsString).parseUnsafe[TestResponseNextPageJsonRep]().test
              )
            } catch {
              case _: Exception =>
                Left(
                  AbstractTestException.Undefined(
                    Some(s"Wrong entity given to implementation: $entityAsString"),
                    None
                  )
                )
            }
          }

          override protected def detectNextPage(entityString: HttpEntityString): Option[Uri] =
            detectNextPageImpl(entityString)

          override protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest =
            nextPageHttpRequestBuilderImpl(uri)
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { result =>
            assert(
              result === Seq(
                Right(TestSuccess("ResponseFromTwilio")),
                Right(TestSuccess("ResponseFromTwilioMore"))
              )
            )
          }
      }

    "Provide a unsafeRun that does the same as the run method that returns result not wrapped in an either" in {

      wireMockServer.stubFor(
        WireMock
          .get(WireMock.urlPathEqualTo("/test"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "plain/txt")
              .withBody(buildPagingBody(None, "ResponseFromTwilio"))
          )
      )

      val impl = new MultipleResponseSourceTestBaseImplemented {

        override protected def createHttpReq(
            connSettings: TwilioConnectionSettings,
            req: TestRequest
        ): HttpRequest = HttpRequest(
          method = HttpMethods.GET,
          uri = s"http://localhost:${wireMockServer.port()}/test"
        )

        override protected def parseHttpResponse(
            request: TestRequest,
            httpRequest: HttpRequest,
            reponseEntity: HttpEntity.Strict
        ): Either[AbstractTestException, TestSuccess] =
          sharedHttpParser(reponseEntity)

        override protected def detectNextPage(entityString: HttpEntityString): Option[Uri] =
          detectNextPageImpl(entityString)

        override protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest =
          nextPageHttpRequestBuilderImpl(uri)
      }

      impl
        .unsafeSource(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
        .runWith(Sink.seq)
        .map { result =>
          assert(result === Seq(TestSuccess("ResponseFromTwilio")))
        }
    }

    "Provide a unsafeRun that does the same as the run method but returns failures as a failed Future" in {

      wireMockServer.stubFor(
        WireMock
          .get(WireMock.urlPathEqualTo("/test"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "plain/txt")
              .withBody(buildPagingBody(None, "ResponseFromTwilio"))
          )
      )

      val impl = new MultipleResponseSourceTestBaseImplemented {

        override protected def createHttpReq(
            connSettings: TwilioConnectionSettings,
            req: TestRequest
        ): HttpRequest = HttpRequest(
          method = HttpMethods.GET,
          uri = s"http://localhost:${wireMockServer.port()}/test"
        )

        override protected def parseHttpResponse(
            request: TestRequest,
            httpRequest: HttpRequest,
            reponseEntity: HttpEntity.Strict
        ): Either[AbstractTestException, TestSuccess] = {
          Left(AbstractTestException.ConcreateTestException())
        }

        override protected def detectNextPage(entityString: HttpEntityString): Option[Uri] =
          detectNextPageImpl(entityString)

        override protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest =
          nextPageHttpRequestBuilderImpl(uri)
      }

      impl
        .unsafeSource(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
        .runWith(Sink.seq)
        .map(_ => fail("Should have gotten an exception by know"))
        .recover { case AbstractTestException.ConcreateTestException() =>
          succeed
        }
    }

    "Catch potential thrown exception by the implementations parseHttpResponse method, and " +
      "convert them into a Undefined Error" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "plain/txt")
                .withBody(buildPagingBody(None, "ResponseFromTwilio"))
            )
        )

        val toThrow = new NullPointerException("Booom")

        val impl = new MultipleResponseSourceTestBaseImplemented {

          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): HttpRequest = HttpRequest(
            method = HttpMethods.GET,
            uri = s"http://localhost:${wireMockServer.port()}/test"
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              reponseEntity: HttpEntity.Strict
          ): Either[AbstractTestException, TestSuccess] = throw toThrow

          override protected def detectNextPage(entityString: HttpEntityString): Option[Uri] =
            detectNextPageImpl(entityString)

          override protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest =
            nextPageHttpRequestBuilderImpl(uri)
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { seq =>
            assert(seq.length === 1)
            seq.filter(_.isLeft).head.left.get match {
              case ue: AbstractTestException.Undefined => assert(ue.getCause === toThrow)
              case _                                   => fail("Wrong cause in Exception")
            }
          }
      }
  }

  "SingleRequestExecutor's run methods should be able to be overridden for testing and not throw " +
    "NoSuchMethodException" in {
      val ownerAccountSid = TwilioAccount.Sid("TestOwnerAccountSid")
      val accountSid      = TwilioAccount.Sid("TestAccountSid")
      val accountToken    = TwilioAccount.AuthToken("TestAuthToken")
      val timeStamp       = Instant.parse("2021-09-30T06:30:46Z")
      val account = TwilioAccount(
        name = TwilioAccount.Name("TestAccount"),
        sid = accountSid,
        status = TwilioAccount.Status.Active,
        ownerAccountSid = ownerAccountSid,
        authToken = accountToken,
        accountType = TwilioAccount.Type.Full,
        timeCreated = timeStamp,
        timeUpdated = timeStamp
      )

      val twilioEndpoint = TwilioEndpoint(
        "noneExistingHost.dixa.com",
        443
      )

      val connSettings = TwilioConnectionSettings(
        twilioEndpoint,
        TwilioConnectionSettings.Protocol.Https,
        accountSid,
        accountToken,
        TwilioConnectionSettings.ParallelFactor.halfCpuCores,
        TwilioConnectionSettings.Timeouts.default
      )

      val twilioClientIam = stub[TwilioClientIam]

      val client = stub[TwilioClient]
      (client.iam _).when().returns(twilioClientIam)

      val accountFetchReqExecutor = stub[AccountFetchRequestExecutor]
      (twilioClientIam.accountFetch _).when().returns(accountFetchReqExecutor)

      val fetchReq = AccountFetchRequest(accountSid = accountSid)

      try {
        (accountFetchReqExecutor.unsafeRun _)
          .when(connSettings, fetchReq)
          .returns(Future.successful(account))

        (accountFetchReqExecutor.run _)
          .when(connSettings, fetchReq)
          .returns(Future.successful(Right(account)))
        succeed
      } catch {
        case _: NoSuchMethodException => fail()
      }
    }

  private trait MultipleResponseSourceTestBaseImplemented
      extends MultipleResponseSource[TestRequest, AbstractTestException, TestSuccess] {
    override protected def http: HttpExt = Http()

    override protected implicit def materializer: Materializer = Materializer.matFromSystem

    override protected implicit def executionContext: ExecutionContext =
      actorSystemProvider.classicSystem.dispatcher

    override protected type ApiExceptionWrapper = AbstractTestException

    override protected type UnspecifiedException = AbstractTestException.Undefined

    override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
      AbstractTestException.Api(apiException)

    override protected def createUnspecifiedException(
        msg: Option[String],
        cause: Option[Exception]
    ): UnspecifiedException = AbstractTestException.Undefined(msg, cause)
  }
}

private object MultipleResponseSourceTest {

  final case class TestRequest()

  sealed trait AbstractTestException extends RuntimeException
  object AbstractTestException {
    final case class ConcreateTestException() extends AbstractTestException
    final case class Api(cause: ApiException) extends AbstractTestException
    final case class Undefined(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(msg.orNull, cause.orNull)
        with AbstractTestException
  }

  final case class TestSuccess(body: String = "")

  final case class TestResponseNextPageJsonRep(next_page_uri: Option[String], test: TestSuccess)

  def detectNextPageImpl(entityString: HttpEntityString): Option[Uri] =
    entityString.parseUnsafe[TestResponseNextPageJsonRep]().next_page_uri.map(s => Uri(s))

  def nextPageHttpRequestBuilderImpl(uri: Uri): HttpRequest =
    HttpRequest(uri = uri)

  def buildPagingBody(nextPageUri: Option[String], bodyText: String) =
    s"""
       |{
       |  "next_page_uri":${nextPageUri.map { string => s""""$string"""" }.getOrElse("null")},
       |  "test":{
       |    "body":"$bodyText"
       |  }
       |}
       |""".stripMargin

  def sharedHttpParser(responseEntity: HttpEntity.Strict) = {
    val entityAsString = responseEntity.data.utf8String
    try {
      Right(
        HttpEntityString(entityAsString).parseUnsafe[TestResponseNextPageJsonRep]().test
      )
    } catch {
      case _: Exception =>
        Left(
          AbstractTestException.Undefined(
            Some(s"Wrong entity given to implementation: $entityAsString"),
            None
          )
        )
    }
  }
}
