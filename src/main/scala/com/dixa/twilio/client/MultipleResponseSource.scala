package com.dixa.twilio.client

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.{FlowShape, Materializer}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source}
import com.dixa.twilio.client.impl.HttpEntityString
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/** Base trait for an source builder that is able and ready to fire a specific request in different
  * ways, that returns multiple elements of a resource.
  *
  * Different users have different preferences when it comes to error handling. So an instance of
  * this, is ready to perform a specific request, but allows the user to decide how he prefers the
  * response.
  *
  * @tparam Req
  *   The Request type that is ready to be executed by this instance.
  * @tparam Err
  *   The Err type that the request might produce.
  * @tparam Success
  *   The type of a successfully response.
  */
trait MultipleResponseSource[Req, Err <: RuntimeException, Success] {

  /** Run the request, with typesafe error handling
    *
    * Always return a Successful future, and communicate errors of the request as part of the return
    * type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the error is communicated.
    *
    * Function can be overridden for the soul purpose of stubbing or mocking by scalatest, should
    * never be overridden in extended classes.
    */
  def unsafeSource(
      connSettings: TwilioConnectionSettings,
      req: Req
  ): Source[Success, NotUsed] = {
    val httpRequest = createHttpReq(connSettings, req)
    Source
      .future[HttpResponse] {
        http
          .singleRequest(httpRequest)
          .map { evaluateResponse }
          .map {
            case Left(value)  => throw value
            case Right(value) => value
          }
      }
      .via(buildUnsafePagingFlow(connSettings))
      .via { unsafeParseHttpEntityFlow(connSettings, req) }
  }

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the error is communicated.
    *
    * Function can be overridden for the soul purpose of stubbing or mocking by scalatest, should
    * never be overridden in extended classes.
    */
  def source(
      connSettings: TwilioConnectionSettings,
      req: Req
  ): Source[Either[Err, Success], NotUsed] = {
    val httpRequest = createHttpReq(connSettings, req)
    Source
      .future[Either[Err, HttpResponse]] {
        http
          .singleRequest(httpRequest)
          .map {
            evaluateResponse
          }
      }
      .via(buildPagingFlow(connSettings))
      .via { parseHttpEntityFlow(connSettings, req) }
  }

  def semisafeSource(
      connSettings: TwilioConnectionSettings,
      req: Req
  ): Future[Either[Err, Source[Success, NotUsed]]] = {
    val httpRequest = createHttpReq(connSettings, req)
    for {
      httpResponse <- http.singleRequest(httpRequest)
      evaluatedResponse = evaluateResponse(httpResponse)
      eitherResult = evaluatedResponse.map { response =>
        Source
          .single(response)
          .via(buildUnsafePagingFlow(connSettings))
          .via {
            unsafeParseHttpEntityFlow(connSettings, req)
          }
      }
    } yield eitherResult
  }

  protected def http: HttpExt

  protected implicit def materializer: Materializer

  /** Execution context to use for Async operations. No blocking operation will be run on this. */
  protected implicit def executionContext: ExecutionContext

  /** Type for the request specific wrapper for an ApiException.
    *
    * All implementations is expected to have there own Exception ADT, where one one of the possible
    * values should be a ApiException wrapper
    */
  protected type ApiExceptionWrapper <: Err

  /** Type for the request specific UnspecifiedException.
    *
    * All implementations is expected to have there own Exception ADT, where one one of the possible
    * values should be a UnspecifiedException for representing all the error cases, that does not
    * have it own type for representing it.
    */
  protected type UnspecifiedException <: Err

  /** TODO: msf - describe
    *
    * @return
    */
  protected def detectNextPage(entityString: HttpEntityString): Option[Uri]

  private val detectNextPageFlow: Flow[HttpEntityString, Uri, NotUsed] =
    Flow[HttpEntityString]
      .map { detectNextPage }
      .takeWhile(_.isDefined)
      .map(_.get)

  /** TODO: msf - describe
    *
    * @return
    */
  protected def nextPageHttpRequestBuilder(uri: Uri): HttpRequest

  private val nextPageHttpRequestBuildFlow: Flow[Uri, HttpRequest, NotUsed] =
    Flow[Uri].map { nextPageHttpRequestBuilder }

  /** Build the http request.
    *
    * Implementations should provide this for building the HttpRequest for the request represented
    * by the concrete implementation.
    */
  protected def createHttpReq(connSettings: TwilioConnectionSettings, req: Req): HttpRequest

  /** Convert an ApiException into the request specific Exception. */
  protected def mapApiException(apiException: ApiException): ApiExceptionWrapper

  /** Create the request specific Unspecified exception. */
  protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException

  /** Parse the response of the http request.
    *
    * Some parsing will already have happened before this method is called, as the response will
    * already have been checked for the common errors that all Twilio APIs can return, and if one
    * was found, this method would not be called at all. Instead the [[mapApiException]] method will
    * be used to map it into an Err.
    *
    * The implementation of this method, should still check for errors, but it would only have to
    * check for errors specific to the request.
    *
    * The entity will already be read fully into memory when this is called. This is why it is
    * parsed in separately as a HttpEntity.Strict. This allows for easy use by implementations,
    * without needing to worry about double reading the entity, or forgetting to either read it or
    * discard it in the first place.
    *
    * It should try to return all possible errors as a Left, but in case it slips, and ends up
    * throwing an Exception, then SingleRequestExecutor will make sure to map the exception into the
    * UndefinedException type of the request.
    *
    * When looking for errors, the [[buildResultForUnhandledResponse]] is an easy way to create a
    * willcard for the cases not handled.
    *
    * @return
    *   Left in case of errors, right in case of success.
    * @param request
    *   The Req instance that that the HttpRequest was build upon
    * @param httpRequest
    *   The HttpRequest that the httpResponse is a response to
    * @param responseEntity
    *   The Strict version of the Http entity.
    */
  protected def parseHttpResponse(
      request: Req,
      httpRequest: HttpRequest,
      responseEntity: HttpEntity.Strict
  ): Either[Err, Success]

  /** Helper method for creating a response to cases where we have no support for handling a
    * Responese.
    */
  protected def buildResultForUnhandledResponse(
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entityString: String
  ): Either[Err, Success] = {
    val msg =
      s"No support for handling response to $request, due to getting status code ${httpResponse.status} " +
        s"after firing $httpRequest. Full entity of response is: $entityString"
    Left(createUnspecifiedException(Some(msg), None))
  }

  private def parseHttpEntityFlow(
      connectionSettings: TwilioConnectionSettings,
      req: Req
  ): Flow[Either[Err, HttpEntityString], Either[Err, Success], NotUsed] =
    Flow[Either[Err, HttpEntityString]]
      .map {
        case Left(value) => Left(value)
        case Right(value) =>
          try {
            parseHttpResponse(
              req,
              createHttpReq(connectionSettings, req),
              value.toString
            )
          } catch {
            case e: Exception =>
              Left(
                createUnspecifiedException(
                  Some(s"Uncaught Exception thrown when parsing httpResponse for request: $req"),
                  Some(e)
                )
              )
          }
      }

  private def unsafeParseHttpEntityFlow(
      connectionSettings: TwilioConnectionSettings,
      req: Req
  ): Flow[HttpEntityString, Success, NotUsed] =
    Flow[HttpEntityString]
      .map { entity =>
        parseHttpResponse(
          req,
          createHttpReq(connectionSettings, req),
          entity.toString
        ) match {
          case Left(value)  => throw value
          case Right(value) => value
        }
      }

  /** TODO: msf - describe and implement handling of api errors
    *
    * @return
    */
  private def unsafeResponseHandlerFlow(
      connectionSettings: TwilioConnectionSettings
  ): Flow[Try[HttpResponse], HttpEntityString, NotUsed] =
    Flow[Try[HttpResponse]]
      .mapAsync(1) { tryResp =>
        val resp = tryResp.get
        evaluateResponse(resp) match {
          case Left(value) => throw value
          case Right(value) =>
            value.entity
              .toStrict(connectionSettings.timeouts.requestEntityTimeout)
              .map(entityStrict => HttpEntityString(entityStrict.data.utf8String))
        }
      }

  /** TODO: msf - describe and implement handling of api errors
    *
    * @return
    */
  private def responseHandlerFlow(
      connectionSettings: TwilioConnectionSettings
  ): Flow[Either[Err, HttpResponse], Either[Err, HttpEntityString], NotUsed] =
    Flow[Either[Err, HttpResponse]]
      .mapAsync(1) {
        case Left(value) => Future.successful(Left(value))
        case Right(value) =>
          evaluateResponse(value) match {
            case Left(value) => Future.successful(Left(value))
            case Right(value) =>
              value.entity
                .toStrict(connectionSettings.timeouts.requestEntityTimeout)
                .map(entityStrict => Right(HttpEntityString(entityStrict.data.utf8String)))
          }
      }

  private def evaluateResponse(resp: HttpResponse): Either[Err, HttpResponse] = {
    if (resp.status !== StatusCodes.OK) {
      resp.status match {
        case StatusCodes.Unauthorized       => Left(ApiException.AuthenticationException())
        case StatusCodes.BadRequest         => Left(ApiException.BadRequestException())
        case StatusCodes.TooManyRequests    => Left(ApiException.TooManyRequestsException())
        case StatusCodes.ServiceUnavailable => Left(ApiException.ServiceUnavailable())
        case _ =>
          val msg = s"No support for handling response due to status code ${resp.status}"
          Left(createUnspecifiedException(Some(msg), None))
      }
    }
    Right(resp)
  }

  private def buildPagingFlow(
      connSettings: TwilioConnectionSettings
  ): Flow[Either[Err, HttpResponse], Either[Err, HttpEntityString], NotUsed] = {
    Flow
      .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val httpResponseMerge = builder.add(Merge[Either[Err, HttpResponse]](2))

        val httpResponseTupler = Flow[Either[Err, HttpResponse]].map((_, NotUsed))

        val requestExecutorFlowBuilder = builder.add(requestExecutorFlow)

        val httpResponseHandler = builder.add(
          responseHandlerFlow(connSettings)
        )
        val httpEntityBroadcast = builder.add(Broadcast[Either[Err, HttpEntityString]](2))

        val parseOnlyRightFlowBuilder = builder.add(
          Flow[Either[Err, HttpEntityString]].filter(_.isRight).map(_.right.get)
        )

        val wrapInEitherFlowBuilder = builder.add(
          Flow[HttpResponse].map(Right(_))
        )

        // format: off
        httpResponseMerge ~> httpResponseHandler ~> httpEntityBroadcast
        httpEntityBroadcast.out(0) ~> parseOnlyRightFlowBuilder ~>
        requestExecutorFlowBuilder ~> wrapInEitherFlowBuilder ~> httpResponseMerge.in(1)

        // format: on
        FlowShape(httpResponseMerge.in(0), httpEntityBroadcast.out(1))
      })
  }

  private def buildUnsafePagingFlow(
      connSettings: TwilioConnectionSettings
  ): Flow[HttpResponse, HttpEntityString, NotUsed] = {
    Flow
      .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val httpResponseMerge = builder.add(Merge[HttpResponse](2))

        val wrapInTryFlowBuilder = builder.add(
          Flow[HttpResponse].map(Try(_))
        )

        val requestExecutorFlowBuilder = builder.add(requestExecutorFlow)

        val httpResponseHandler = builder.add(
          unsafeResponseHandlerFlow(connSettings)
        )
        val httpEntityBroadcast = builder.add(Broadcast[HttpEntityString](2))
        
        // format: off
        httpResponseMerge ~> wrapInTryFlowBuilder ~> httpResponseHandler ~> httpEntityBroadcast
        httpEntityBroadcast.out(0) ~> requestExecutorFlowBuilder ~> httpResponseMerge.in(1)

        // format: on
        FlowShape(httpResponseMerge.in(0), httpEntityBroadcast.out(1))
      })
  }

  private val requestExecutorFlow: Flow[HttpEntityString, HttpResponse, NotUsed] =
    Flow[HttpEntityString]
      .via(detectNextPageFlow)
      .via(nextPageHttpRequestBuildFlow)
      .map((_, NotUsed))
      .via(http.superPool[NotUsed]())
      .map(_._1.get)
}
