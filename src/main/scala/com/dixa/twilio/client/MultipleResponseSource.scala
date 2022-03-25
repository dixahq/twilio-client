package com.dixa.twilio.client

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source}
import akka.stream.{FlowShape, Materializer, SourceShape}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

trait MultipleResponseSource[Req, Err <: RuntimeException, Success] {

  def source(
      connSettings: TwilioConnectionSettings,
      req: Req,
  ): Source[Either[Err, Success], NotUsed] = {
    Source
      .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val starter =
          Source.single[Either[Err, HttpRequest]](Right(createHttpReq(connSettings, req)))

        val httpReqMerge = builder.add(Merge[Either[Err, HttpRequest]](2))

        val httpFlow            = requestFlow
        val httpResponseHandler = responseHandlerFlow(connSettings)

        val httpEntityBroadcast = builder.add(Broadcast[Either[Err, HttpEntityString]](2))

        val nextPageHttpRequestBuild = nextPageHttpRequestBuildFlow(connSettings)


      // format: off
      starter ~> httpReqMerge.in(0)
      httpReqMerge ~> httpFlow ~> httpResponseHandler ~> httpEntityBroadcast
      httpReqMerge.in(1) <~ nextPageHttpRequestBuild <~ httpEntityBroadcast.out(0)
      // format: on
        SourceShape(httpEntityBroadcast.out(1))
      })
      .via { parseHttpEntityFlow(connSettings, req) }
  }

  def unsafeSource(
      connSettings: TwilioConnectionSettings,
      req: Req,
  ): Source[Success, NotUsed] =
    source(connSettings, req).map {
      case Left(value)  => throw value
      case Right(value) => value
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

  /** Build the http request.
    *
    * Implementations should provide this for building the HttpRequest for the request represented
    * by the concrete implementation.
    */
  protected def createHttpReq(connSettings: TwilioConnectionSettings, req: Req): HttpRequest

  /** TODO: msf: figure out if this is needed here Convert an ApiException into the request specific
    * Exception.
    */
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
      responseEntity: HttpEntityString
  ): List[Either[Err, Success]]

  /** Detect uri for next page in the entity and build optional http request dependent on uri is
    * present or not
    */
  protected def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Option[HttpRequest]

  private val requestFlow: Flow[Either[Err, HttpRequest], Either[Err, HttpResponse], NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val eitherBroadcast = builder.add(Broadcast[Either[Err, HttpRequest]](2))

      val eitherMerge = builder.add(Merge[Either[Err, HttpResponse]](2))

      val requestFilter = builder.add(
        Flow[Either[Err, HttpRequest]]
          .takeWhile(_.isRight)
          .map { either =>
            (either.right.get, NotUsed)
          }
      )

      val errFilter = builder.add(
        Flow[Either[Err, HttpRequest]]
          .takeWhile(_.isLeft)
          .map[Either[Err, HttpResponse]] { either =>
            Left(either.left.get)
          }
      )

      val requestExecutorFlow: FlowShape[(HttpRequest, NotUsed), Either[Err, HttpResponse]] =
        builder.add(
          http
            .superPool[NotUsed]()
            .map(
              _._1 match {
                case Failure(exception: RuntimeException) =>
                  Left(createUnspecifiedException(Some(exception.getMessage), Some(exception)))
                case Failure(throwable: Throwable) =>
                  Left(createUnspecifiedException(Some(throwable.getMessage), None))
                case util.Success(value) => Right(value)
              }
            )
        )

      eitherBroadcast.out(0) ~> requestFilter ~> requestExecutorFlow ~> eitherMerge.in(0)

      eitherBroadcast.out(1) ~> errFilter ~> eitherMerge.in(1)

      FlowShape(eitherBroadcast.in, eitherMerge.out)
    })
  }

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

  private def nextPageHttpRequestBuildFlow(
      connectionSettings: TwilioConnectionSettings
  ): Flow[Either[Err, HttpEntityString], Either[Err, HttpRequest], NotUsed] =
    Flow[Either[Err, HttpEntityString]]
      .map {
        case Left(value) => Left(value)
        case Right(value) =>
          try {
            Right(nextPageHttpRequestBuilder(connectionSettings, value))
          } catch {
            case e: Exception =>
              Left(createUnspecifiedException(Some(e.getMessage), Some(e)))
          }
      }
      .takeWhile(_.isRight)
      .takeWhile(_.right.get.isDefined)
      .map { either =>
        Right(either.right.get.get)
      }

  private def parseHttpEntityFlow(
      connectionSettings: TwilioConnectionSettings,
      req: Req
  ): Flow[Either[Err, HttpEntityString], Either[Err, Success], NotUsed] =
    Flow[Either[Err, HttpEntityString]]
      .mapConcat {
        case Left(value) => Seq(Left(value))
        case Right(value) =>
          try {
            parseHttpResponse(
              req,
              createHttpReq(connectionSettings, req),
              value
            )
          } catch {
            case e: Exception =>
              List(
                Left(
                  createUnspecifiedException(
                    Some(s"Uncaught Exception thrown when parsing httpResponse for request: $req"),
                    Some(e)
                  )
                )
              )
          }
      }
}
