package com.dixa.twilio.client

import org.apache.pekko.NotUsed
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source}
import org.apache.pekko.stream.{FlowShape, SourceShape}
import com.dixa.twilio.client.impl.{
  DefaultApiErrorEntityJsonRep,
  HttpEntityString,
  MetaRootJsonResp,
  PagingStyle,
  TwilioResponseNextPageJsonRep,
  TwilioUri
}
import upickle.core.AbortException

import scala.concurrent.Future
import scala.util.Failure

/** Base trait for an executor that is able and ready to fire a specific request in different ways.
  *
  * This trait handles an API call that returns multiple elements wrapped in pagination, by
  * flattening them into a akka stream Source.
  *
  * The basic structure of the underlying flow of the source, takes a request and returns the
  * elements from the response, while requesting the next page in a continuous loop until all
  * elements has be fetched:
  *
  * {{{
  *                 ------->   execute request   -------->
  *               /                                        \
  *  request ----                                            ----> response
  *              \                                        /
  *                <------ find next page request <------
  * }}}
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
trait MultipleResponseRequestExecutor[Req, Err <: RuntimeException, Success]
    extends RequestExecutor[Req, Err] {

  /** Run the request, with typesafe error handling
    *
    * Returns a Source of elements that flattens paginated elements, and communicate errors of the
    * request as part of the return type, in form as an Either.
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
      req: Req,
  ): Source[Either[Err, Success], NotUsed] = {
    Source
      .fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        import GraphDSL.Implicits._

        val starter =
          Source.single[Either[Err, HttpRequest]](createHttpReq(connSettings, req))

        val httpReqMerge = builder.add(Merge[Either[Err, HttpRequest]](2))

        val httpFlow            = requestFlow
        val httpResponseHandler = responseHandlerFlow(connSettings)

        val httpEntityBroadcast =
          builder.add(Broadcast[Either[Err, (HttpResponse, HttpEntityString)]](2))

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

  /** Run the request, returning failed Source on errors.
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
      req: Req,
  ): Source[Success, NotUsed] =
    source(connSettings, req).map {
      case Left(value)  => throw value
      case Right(value) => value
    }

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
    * throwing an Exception, then MultipleResponseSource will make sure to map the exception into
    * the UndefinedException type of the request.
    *
    * When looking for errors, the [[buildResultForUnhandledResponse]] is an easy way to create a
    * wildcard for the cases not handled.
    *
    * @return
    *   Left in case of errors, right in case of success.
    * @param request
    *   The Req instance that that the HttpRequest was build upon
    * @param httpRequest
    *   The HttpRequest that the httpResponse is a response to
    * @param httpResponse
    *   The HttpResponse to parse (Entity has already been read to a Strict)
    * @param responseEntity
    *   The Strict version of the Http entity.
    */
  protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[Err, Success]]

  /** Detect uri for next page in the entity and build optional http request dependent on uri is
    * present or not
    */
  protected final def nextPageHttpRequestBuilder(
      connectionSettings: TwilioConnectionSettings,
      entityString: HttpEntityString
  ): Either[Err, Option[HttpRequest]] = {
    subDomain.pagingStyle match {
      case PagingStyle.NoPaging => Right(None)
      case PagingStyle.PagingAttributesInRootJson =>
        extractNextPageFromRootJson(entityString)
          .flatMap(fromOptionalNextPageUriToHttpRequest(_, connectionSettings))
      case PagingStyle.MetaObject =>
        extractNextPageFromMeta(entityString)
          .flatMap(fromOptionalNextPageUriToHttpRequest(_, connectionSettings))
    }
  }

  private def extractNextPageFromRootJson(entity: HttpEntityString): Either[Err, Option[String]] = {
    entity
      .parse[TwilioResponseNextPageJsonRep]()
      .map(_.next_page_uri)
      .left
      .map(t =>
        createUnspecifiedException(
          s"Failed to parse response for getting the next page element: ${t.getMessage}",
        )
      )
  }

  private def extractNextPageFromMeta(entity: HttpEntityString): Either[Err, Option[String]] = {
    entity
      .parse[MetaRootJsonResp]()
      .map(_.meta.next_page_url)
      .left
      .map(t =>
        createUnspecifiedException(
          s"Failed to parse response for getting the next page element: ${t.getMessage}"
        )
      )
  }

  private def fromOptionalNextPageUriToHttpRequest(
      nextPageOpt: Option[String],
      connectionSettings: TwilioConnectionSettings
  ): Either[Err, Option[HttpRequest]] = {
    nextPageOpt match {
      case Some(nextPage) =>
        TwilioUri
          .autoDetect(nextPage, method, subDomain)
          .flatMap(_.createHttpRequest(connectionSettings))
          .map(Some(_))
          .left
          .map(t =>
            createUnspecifiedException(
              s"Error creating HttpRequest for nextPage: $nextPage - ${t.getMessage}"
            )
          )
      case None => Right(None)
    }
  }

  /** Helper method for creating a response to cases where we have no support for handling a
    * Responese.
    */
  protected final def buildResultForUnhandledResponse(
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[Err, Success] = {
    val msg =
      s"No support for handling response to $request, due to status code ${httpResponse.status} " +
        s"after firing $httpRequest. Full entity of response is: $entity"
    Left(createUnspecifiedException(Some(msg), None))
  }

  /** As the overall flow is recursive, this flow is is implemented to left Left elements bypass the
    * http.superpool request execution flow to avoid an infinite loop, as the superpool can't handle
    * Either
    */
  private def requestFlow: Flow[Either[Err, HttpRequest], Either[Err, HttpResponse], NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val eitherBroadcast = builder.add(Broadcast[Either[Err, HttpRequest]](2))

      val eitherMerge = builder.add(Merge[Either[Err, HttpResponse]](2))

      val requestFilter = builder.add(
        Flow[Either[Err, HttpRequest]]
          .takeWhile(_.isRight)
          .map { either =>
            (either.toOption.get, NotUsed)
          }
      )

      val errFilter = builder.add(
        Flow[Either[Err, HttpRequest]]
          .takeWhile(_.isLeft)
          // This is needed to change the return type to match the downstream flow
          .map[Either[Err, HttpResponse]] {
            case Left(value) => Left(value)
            case Right(_)    => Right(HttpResponse())
          }
      )

      val requestExecutorFlow: FlowShape[(HttpRequest, NotUsed), Either[Err, HttpResponse]] =
        builder.add(
          http
            .superPool[NotUsed]()
            .map(
              _._1 match {
                case Failure(exception: AbortException) =>
                  // Don't expose upickle exception, as upickle is a implementation detail.
                  Left(createUnspecifiedException(Some(exception.getMessage), None))
                case Failure(exception: RuntimeException) =>
                  Left(createUnspecifiedException(Some(exception.getMessage), Some(exception)))
                // If the exception returned if more server then a RuntimeException, we want it thrown to clearly signal that something is very wrong
                case Failure(exception: Throwable) =>
                  throw exception
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
  ): Flow[Either[Err, HttpResponse], Either[Err, (HttpResponse, HttpEntityString)], NotUsed] =
    Flow[Either[Err, HttpResponse]]
      .mapAsync(connectionSettings.parallelFactor.asInt) {
        case Left(value) => Future.successful(Left(value))
        case Right(response) =>
          response.entity
            .toStrict(connectionSettings.timeouts.requestEntityTimeout)
            .map(entityStrict =>
              evaluateResponse(response, HttpEntityString(entityStrict.data.utf8String)).left
                .map(mapApiException)
            )
      }

  private def evaluateResponse(
      resp: HttpResponse,
      httpEntityString: HttpEntityString
  ): Either[ApiException, (HttpResponse, HttpEntityString)] = {
    resp.status match {
      case StatusCodes.Unauthorized => Left(ApiException.AuthenticationException())
      case StatusCodes.Conflict =>
        httpEntityString.parse[DefaultApiErrorEntityJsonRep]() match {
          case Right(DefaultApiErrorEntityJsonRep(20409L, _, _, _)) =>
            Left(ApiException.Conflict())
          case _ => Right((resp, httpEntityString))
        }
      case _ => Right((resp, httpEntityString))
    }
  }

  private def nextPageHttpRequestBuildFlow(
      connectionSettings: TwilioConnectionSettings
  ): Flow[Either[Err, (HttpResponse, HttpEntityString)], Either[Err, HttpRequest], NotUsed] =
    Flow[Either[Err, (HttpResponse, HttpEntityString)]]
      .map { responseEither =>
        for {
          responseEntity        <- responseEither
          nextPageRequestOption <- nextPageHttpRequestBuilder(connectionSettings, responseEntity._2)
          nextPageRequest <- nextPageRequestOption.toRight(
            createUnspecifiedException(Some("next page is not defined"), None)
          )
        } yield nextPageRequest
      }
      // Need to ignore Left, else the flow will loop eternally ejection the same Left again and again
      .takeWhile(_.isRight)

  private def parseHttpEntityFlow(
      connectionSettings: TwilioConnectionSettings,
      req: Req
  ): Flow[Either[Err, (HttpResponse, HttpEntityString)], Either[Err, Success], NotUsed] =
    Flow[Either[Err, (HttpResponse, HttpEntityString)]]
      .mapConcat {
        case Left(value) => Seq(Left(value))
        case Right(value) =>
          try {
            createHttpReq(connectionSettings, req) match {
              case Left(value) => Seq(Left(value))
              case Right(httpRequest) =>
                parseHttpResponse(
                  connectionSettings,
                  req,
                  httpRequest,
                  value._1,
                  value._2
                )
            }
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
