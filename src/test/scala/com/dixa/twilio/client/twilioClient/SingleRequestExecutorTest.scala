package com.dixa.twilio.client.twilioClient

import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchRequest
import com.dixa.twilio.client.iam.{AccountFetchRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.{
  ApiException,
  SingleRequestExecutor,
  TwilioClient,
  TwilioConnectionSettings,
  TwilioTestConstants
}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalamock.scalatest.proxy.AsyncMockFactory

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

final class SingleRequestExecutorTest extends TwilioClientTest with AsyncMockFactory {

  import SingleRequestExecutorTest._

  classOf[SingleRequestExecutor[_, _, _]].getSimpleName should {

    "Provide a run method that async executes the http request the implementation provides, and " +
      "use the implementations response parsing to get the end result to return" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "plain/txt")
                .withBody("ResponseFromTwilio")
            )
        )

        val impl = new SingleRequestExecutorTestBaseImplemented {

          override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

          override protected def method: HttpMethod = HttpMethods.GET
          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): Either[AbstractTestException, HttpRequest] = Right(
            HttpRequest(
              method,
              uri = s"http://localhost:${wireMockServer.port()}/test"
            )
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              entity: HttpEntityString
          ): Either[AbstractTestException, TestSuccess] = {
            if (entity.toString == "ResponseFromTwilio") Right(TestSuccess())
            else
              Left(
                AbstractTestException.Undefined(
                  Some(s"Wrong entity given to implementation: $entity"),
                  None
                )
              )
          }
        }

        impl.run(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest()).map {
          result =>
            assert(result === Right(TestSuccess()))
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
                .withHeader("Content-Type", "plain/txt")
                .withBody("ResponseFromTwilio")
            )
        )

        val impl = new SingleRequestExecutorTestBaseImplemented {

          override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

          override protected def method: HttpMethod = HttpMethods.GET

          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): Either[AbstractTestException, HttpRequest] = Right(
            HttpRequest(
              method = method,
              uri = s"http://localhost:${wireMockServer.port()}/test"
            )
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              entity: HttpEntityString
          ): Either[AbstractTestException, TestSuccess] = {
            Left(AbstractTestException.ConcreateTestException())
          }
        }

        impl.run(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest()).map {
          result =>
            assert(result === Left(AbstractTestException.ConcreateTestException()))
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
              .withBody("ResponseFromTwilio")
          )
      )

      val impl = new SingleRequestExecutorTestBaseImplemented {

        override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

        override protected def method: HttpMethod = HttpMethods.GET

        override protected def createHttpReq(
            connSettings: TwilioConnectionSettings,
            req: TestRequest
        ): Either[AbstractTestException, HttpRequest] = Right(
          HttpRequest(
            method = method,
            uri = s"http://localhost:${wireMockServer.port()}/test"
          )
        )

        override protected def parseHttpResponse(
            request: TestRequest,
            httpRequest: HttpRequest,
            httpResponse: HttpResponse,
            entity: HttpEntityString
        ): Either[AbstractTestException, TestSuccess] = {
          if (entity.toString == "ResponseFromTwilio") Right(TestSuccess())
          else
            Left(
              AbstractTestException.Undefined(
                Some(s"Wrong entity given to implementation: $entity"),
                None
              )
            )
        }
      }

      impl.unsafeRun(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest()).map {
        result =>
          assert(result === TestSuccess())
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
              .withBody("ResponseFromTwilio")
          )
      )

      val impl = new SingleRequestExecutorTestBaseImplemented {

        override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

        override protected def method: HttpMethod = HttpMethods.GET

        override protected def createHttpReq(
            connSettings: TwilioConnectionSettings,
            req: TestRequest
        ): Either[AbstractTestException, HttpRequest] = Right(
          HttpRequest(
            method,
            uri = s"http://localhost:${wireMockServer.port()}/test"
          )
        )

        override protected def parseHttpResponse(
            request: TestRequest,
            httpRequest: HttpRequest,
            httpResponse: HttpResponse,
            entity: HttpEntityString
        ): Either[AbstractTestException, TestSuccess] = {
          Left(AbstractTestException.ConcreateTestException())
        }
      }

      impl
        .unsafeRun(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
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
                .withBody("ResponseFromTwilio")
            )
        )

        val toThrow = new NullPointerException("Booom")

        val impl = new SingleRequestExecutorTestBaseImplemented {

          override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

          override protected def method: HttpMethod = HttpMethods.GET

          override protected def createHttpReq(
              connSettings: TwilioConnectionSettings,
              req: TestRequest
          ): Either[AbstractTestException, HttpRequest] = Right(
            HttpRequest(
              method = HttpMethods.GET,
              uri = s"http://localhost:${wireMockServer.port()}/test"
            )
          )

          override protected def parseHttpResponse(
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              entity: HttpEntityString
          ): Either[AbstractTestException, TestSuccess] = throw toThrow
        }

        impl.run(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest()).map {
          result =>
            assert(result.isLeft)
            result match {
              case Left(ue: AbstractTestException.Undefined) => assert(ue.getCause === toThrow)
              case _                                         => fail("Wrong cause in Exception")
            }
        }
      }

    "SingleRequestExecutor's run methods should be able to be overridden for testing and not throw " +
      "NoSuchMethodException" in {
        val ownerAccountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXA")
        val accountSid      = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXB")
        val accountToken    = AuthToken.Primary("TestAuthToken")
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

        val twilioClientIam: TwilioClientIam = stub[TwilioClientIam]("testTwilioClientIam")

        val client: TwilioClient = stub[TwilioClient]("testTwilioClient")
        (() => client.iam).when().returns(twilioClientIam)

        val accountFetchReqExecutor: AccountFetchRequestExecutor =
          stub[AccountFetchRequestExecutor]("testAccountFetchRequestExecutor")
        (() => twilioClientIam.accountFetch).when().returns(accountFetchReqExecutor)

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
  }

  private trait SingleRequestExecutorTestBaseImplemented
      extends SingleRequestExecutor[TestRequest, AbstractTestException, TestSuccess] {
    override protected def http: HttpExt = Http()

    override protected implicit def materializer: Materializer = Materializer.matFromSystem

    override protected implicit def executionContext: ExecutionContext =
      actorSystemProvider.classicSystem.dispatcher

    override protected type ApiExceptionWrapper = AbstractTestException.Api

    override protected type UnspecifiedException = AbstractTestException.Undefined

    override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
      AbstractTestException.Api(apiException)

    override protected def createUnspecifiedException(
        msg: Option[String],
        cause: Option[Throwable]
    ): UnspecifiedException = AbstractTestException.Undefined(msg, cause)
  }
}

private object SingleRequestExecutorTest {

  final case class TestRequest()

  sealed trait AbstractTestException extends RuntimeException
  object AbstractTestException {
    final case class ConcreateTestException() extends AbstractTestException
    final case class Api(cause: ApiException) extends AbstractTestException with ApiExceptionWrapper
    final case class Undefined(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(msg.orNull, cause.orNull)
        with AbstractTestException
  }

  final case class TestSuccess()

}
