package com.dixa.twilio.client.twilioClient

import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import com.dixa.twilio.client.{
  ApiException,
  SingleRequestExecutor,
  TwilioConnectionSettings,
  TwilioTestConstants
}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.ExecutionContext

final class SingleRequestExecutorTest extends TwilioClientTest {

  import SingleRequestExecutorTest._

  classOf[SingleRequestExecutor[_, _, _]].getSimpleName should {

    "Provide a safe method that async executes the http request the implementation provides, and " +
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
              httpResponse: HttpResponse,
              entity: HttpEntity.Strict
          ): Either[AbstractTestException, TestSuccess] = {
            val entityAsString = entity.data.utf8String
            if (entityAsString == "ResponseFromTwilio") Right(TestSuccess())
            else
              Left(
                AbstractTestException.Undefined(
                  Some(s"Wrong entity given to implementation: $entityAsString"),
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
  }

  private trait SingleRequestExecutorTestBaseImplemented
      extends SingleRequestExecutor[TestRequest, AbstractTestException, TestSuccess] {
    override protected def http: HttpExt = Http()

    override protected implicit def materializer: Materializer = Materializer.matFromSystem

    override protected implicit def executionContext: ExecutionContext = actorSystem.dispatcher

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

private object SingleRequestExecutorTest {

  final case class TestRequest()

  sealed trait AbstractTestException extends RuntimeException
  object AbstractTestException {
    final case class ConcreateTestException() extends AbstractTestException
    final case class Api(cause: ApiException) extends AbstractTestException
    final case class Undefined(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(msg.orNull, cause.orNull)
        with AbstractTestException
  }

  final case class TestSuccess()

}
