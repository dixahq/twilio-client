package com.dixa.twilio.client.twilioClient

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.impl.HttpEntityString
import com.dixa.twilio.client.{
  ApiException,
  MultipleResponseRequestExecutor,
  TestActorSystem,
  TwilioConnectionSettings,
  TwilioTestConstants
}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, matching}
import io.circe.CursorOp.{DownArray, DownField}
import io.circe.DecodingFailure
import org.scalamock.scalatest.AsyncMockFactory
import io.circe.generic.auto._
import org.scalatest.wordspec.AsyncWordSpec

import scala.concurrent.ExecutionContext

final class MultipleResponseRequestExecutorTest
    extends AsyncWordSpec
    with WireMockTest
    with AsyncMockFactory
    with TestActorSystem {

  import MultipleResponseRequestExecutorTest._

  classOf[MultipleResponseRequestExecutor[_, _, _]].getSimpleName should {

    "Provide a source method that executes the initial http request the implementation provides, " +
      "and use the implementations response parsing to get the end result " +
      "while flattening the API's paging logic, to return result as a Source model objects" in {

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
                    "ResponseFromDidww"
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
                .withBody(buildPagingBody(None, "ResponseFromDidwwMore"))
            )
        )

        val impl = new MultipleResponseRequestExecutorTestBaseImplemented {

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
              connectionSettings: TwilioConnectionSettings,
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              responseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] = {
            responseEntity
              .parse[ListJsonRep[TestSuccessJsonRep]]() match {
              case Left(ex) =>
                List(
                  Left(
                    AbstractTestException.Undefined(
                      Some(s"Wrong entity given to implementation"),
                      None
                    )
                  )
                )
              case Right(listJsonRep) =>
                listJsonRep.successes
                  .map { item =>
                    Right(toModel(item))
                  }
            }
          }

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Either[UnspecifiedException, Option[HttpRequest]] = {
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString).left.map(ex =>
              createUnspecifiedException(Some(ex.getMessage), Some(ex))
            )
          }
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { result =>
            assert(
              result === Seq(
                Right(TestSuccess("ResponseFromDidww")),
                Right(TestSuccess("ResponseFromDidwwSecond")),
                Right(TestSuccess("ResponseFromDidwwMore")),
                Right(TestSuccess("ResponseFromDidwwMoreSecond")),
              )
            )
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
                .withBody(buildPagingBody(None, "ResponseFromDidww"))
            )
        )

        val toThrow = new NullPointerException("Booom")

        val impl = new MultipleResponseRequestExecutorTestBaseImplemented {

          override protected def http: HttpExt = Http()

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
              connectionSettings: TwilioConnectionSettings,
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              reponseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] = {
            throw toThrow
          }

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Either[UnspecifiedException, Option[HttpRequest]] =
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString).left
              .map(ex => createUnspecifiedException(Some(ex.getMessage), Some(ex)))
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { seq =>
            assert(seq.length === 1)
            seq.head match {
              case Left(value) =>
                value match {
                  case ue: AbstractTestException.Undefined => assert(ue.getCause === toThrow)
                  case _                                   => fail("Wrong cause in Exception")
                }
              case Right(value) => fail("should throw exception")
            }
          }
      }

    "Catch potential thrown exception when parsing unexpected json body, and " +
      "convert them into a Undefined Error" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/test"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "plain/txt")
                .withBody(buildFailingPagingBody(None))
            )
        )

        val impl = new MultipleResponseRequestExecutorTestBaseImplemented {

          override protected def http: HttpExt = Http()

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
              connectionSettings: TwilioConnectionSettings,
              request: TestRequest,
              httpRequest: HttpRequest,
              httpResponse: HttpResponse,
              reponseEntity: HttpEntityString
          ): List[Either[AbstractTestException, TestSuccess]] = {
            sharedHttpParser(reponseEntity)
          }

          override protected def nextPageHttpRequestBuilder(
              connectionSettings: TwilioConnectionSettings,
              entityString: HttpEntityString
          ): Either[UnspecifiedException, Option[HttpRequest]] =
            sharedNextPageHttpRequestBuilder(connectionSettings, entityString).left
              .map(ex => createUnspecifiedException(Some(ex.getMessage), Some(ex)))
        }

        impl
          .source(TwilioTestConstants.connSettings(wireMockServer.port()), TestRequest())
          .runWith(Sink.seq)
          .map { seq =>
            assert(seq.length === 1)
            seq.head match {
              case Left(value) =>
                value match {
                  case AbstractTestException.Undefined(_, Some(cause)) =>
                    val expectedCause = DecodingFailure(
                      "Missing required field",
                      List(DownField("body"), DownArray, DownField("successes"))
                    )
                    assert(
                      cause.getMessage == expectedCause.getMessage
                    )
                  case _ => fail("Wrong cause in Exception")

                }
              case Right(_) => fail("should throw exception")
            }
          }
      }
  }

  private trait MultipleResponseRequestExecutorTestBaseImplemented
      extends MultipleResponseRequestExecutor[TestRequest, AbstractTestException, TestSuccess] {
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

private object MultipleResponseRequestExecutorTest {

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

  final case class TestResponseNextPageJsonRep(
      next_page_uri: Option[String],
      successes: List[TestSuccess]
  )

  def sharedNextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[AbstractTestException, Option[HttpRequest]] = {
    entityString
      .parse[TestResponseNextPageJsonRep]()
      .map {
        _.next_page_uri.map(s => {
          HttpRequest(HttpMethods.GET, s).addHeader(
            RawHeader("Api-Key", connectionSettings.authToken.asString)
          )
        })
      }
      .left
      .map { ex => AbstractTestException.Undefined(Some(ex.cause.getMessage), Some(ex.cause)) }
  }

  def buildFailingPagingBody(nextPageUri: Option[String]): String =
    s"""
       |{
       |  "next_page_uri":${nextPageUri.map { string => s""""$string"""" }.getOrElse("null")},
       |  "successes": [
       |    {
       |      "failingObjectAttribute":"kaBOOOOOM"
       |    }
       |  ]
       |}
       |""".stripMargin

  def buildPagingBody(nextPageUri: Option[String], bodyText: String): String =
    s"""
       |{
       |  "next_page_uri":${nextPageUri.map { string => s""""$string"""" }.getOrElse("null")},
       |  "successes": [
       |    {
       |      "body":"$bodyText"
       |    },
       |    {
       |      "body":"${bodyText}Second"
       |    }
       |  ]
       |}
       |""".stripMargin

  val badRequestBody: String =
    """
      |{
      |    "errors": [
      |        {
      |            "title": "Bad Request",
      |            "detail": "Invalid request",
      |            "code": "400",
      |            "status": "400"
      |        }
      |    ]
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
