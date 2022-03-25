package com.dixa.twilio.client.twilioClient

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchRequest
import com.dixa.twilio.client.iam.{AccountFetchRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client._
import com.dixa.twilio.client.impl.messaging.MessageJsonRep
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, ListJsonRep}
import com.dixa.twilio.client.messaging.MessageResourceReadSource.MessageResourceReadException
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
              responseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] =
            sharedHttpParser(responseEntity)

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Option[HttpRequest] =
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString)
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
              responseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] = {
            responseEntity
              .parse[ListJsonRep[TestSuccessJsonRep]]() match {
              case Left(ex) =>
                List(
                  Left(
                    AbstractTestException.Undefined(
                      Some(s"Wrong entity given to implementation: ${ex.cause.getMessage}"),
                      None
                    )
                  )
                )
              case Right(listJsonRep) =>
                listJsonRep.successes
                  .map {
                    toModel
                  }
                  .map {
                    Right(_)
                  }
            }
          }

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Option[HttpRequest] =
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString)
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
            reponseEntity: HttpEntityString
        ): List[Either[AbstractTestException, TestSuccess]] =
          sharedHttpParser(reponseEntity)

        override protected def nextPageHttpRequestBuilder(
            connectionSettings: TwilioConnectionSettings,
            entityString: HttpEntityString
        ): Option[HttpRequest] = sharedNextPageHttpRequestBuilder(connectionSettings, entityString)
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
            reponseEntity: HttpEntityString
        ): List[Either[AbstractTestException, TestSuccess]] =
          List(Left(AbstractTestException.ConcreateTestException()))

        override protected def nextPageHttpRequestBuilder(
            connectionSettings: TwilioConnectionSettings,
            entityString: HttpEntityString
        ): Option[HttpRequest] = sharedNextPageHttpRequestBuilder(connectionSettings, entityString)
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

          override protected def http: HttpExt = Http()

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
              reponseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] =
            List(Left(AbstractTestException.Undefined(Some(toThrow.getMessage), Some(toThrow))))

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Option[HttpRequest] =
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString)
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

  final case class TestSuccessJsonRep(body: String)

  final case class ListJsonRep[A](successes: List[A])

  final case class TestResponseNextPageJsonRep(next_page_uri: Option[String], test: TestSuccess)

  def sharedNextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Option[HttpRequest] =
    entityString
      .parseUnsafe[TestResponseNextPageJsonRep]()
      .next_page_uri
      .map(s => {
        val hostname = connectionSettings.hostNameFor(ApiSubDomain.Api)
        val url =
          s"${connectionSettings.protocol}://$hostname:${connectionSettings.endpoint.port}$s"
        HttpRequest(HttpMethods.GET, url).addHeader(
          Authorization(
            BasicHttpCredentials(
              connectionSettings.accountSid.toString,
              connectionSettings.authToken.asString
            )
          )
        )
      })

  def buildPagingBody(nextPageUri: Option[String], bodyText: String) =
    s"""
       |{
       |  "next_page_uri":${nextPageUri.map { string => s""""$string"""" }.getOrElse("null")},
       |  "successes": [
       |    {
       |      "body":"$bodyText"
       |    },
       |    {
       |      "body":"${bodyText}second"
       |    }
       |  ]
       |}
       |""".stripMargin

  def toModel(jsonRep: TestSuccessJsonRep): TestSuccess =
    TestSuccess(jsonRep.body)

  def sharedHttpParser(
      responseEntity: HttpEntityString
  ): List[Either[AbstractTestException, TestSuccess]] = {
    responseEntity
      .parse[ListJsonRep[TestSuccessJsonRep]]() match {
      case Left(ex) =>
        List(
          Left(
            AbstractTestException.Undefined(Some(ex.cause.getMessage), Some(ex.cause))
          )
        )
      case Right(listJsonRep) =>
        listJsonRep.successes.map { toModel }.map { Right(_) }
    }
  }
}
