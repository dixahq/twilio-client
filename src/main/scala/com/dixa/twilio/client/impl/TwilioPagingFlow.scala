package com.dixa.twilio.client.impl

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source}
import akka.stream.{Materializer, SourceShape}
import com.dixa.twilio.client.TwilioConnectionSettings
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.util.Try

@deprecated("Implement extension of MultipleResponseSource instead", "0.11.0")
private[impl] object TwilioPagingFlow {

  /** Creates a flow, running paging GET request agains Twilio as a stream.
    *
    * Fetching resources in Twilios API is using paging, so it will only return a fixed amount of
    * elements, and a path for fetching the next batch. This method lets you create a flow, that
    * does this for you, and just returns the body of each request. You only need to supply the
    * initial path to call, and then connect the returned source to a flow/sink that can parse the
    * bodies into something usefull.
    *
    * Note that the reason for this being a Source and not a flow, is that the internal logic will
    * only work for 1 initial HttpRequest, so by requiring the initial path as a parameter for
    * building the source, we can enforce this.
    *
    * @param initPath
    *   The path to use for the initial request.
    * @param connSettings
    *   Twilio connection settings.
    * @param materializer
    *   Well it's using streams :)
    * @return
    *   Source[HttpEntityString] representing the entity/body of each http request made.
    */
  private[client] def createPagingSrc(
      connSettings: TwilioConnectionSettings,
      initPath: TwilioUri
  )(
      implicit materializer: Materializer,
      http: HttpExt
  ): Source[HttpEntityString, NotUsed] = {
    Source.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val starter = Source.single(initPath.createHttpRequest(connSettings))

      val httpReqMerge  = builder.add(Merge[HttpRequest](2))
      val httpReqTupler = Flow[HttpRequest].map((_, NotUsed))

      val httpFlow = builder.add(http.superPool[NotUsed]())
      val httpResponseHandler = builder.add(
        Flow[(Try[HttpResponse], NotUsed)]
          .map { tuple2 =>
            val tryResp = tuple2._1
            // Just fail the stream on non 200 request, as we would always expect 200 here
            val resp = tryResp.get
            if (resp.status !== StatusCodes.OK)
              throw new RuntimeException(s"Non 200 response code from twilio: ${resp.status}")
            resp
          }
          .mapAsync(1) { resp =>
            resp.entity.toStrict(connSettings.timeouts.requestEntityTimeout)
          }
          .map(entityStrict => HttpEntityString(entityStrict.data.utf8String))
      )
      val httpEntityBroadcast = builder.add(Broadcast[HttpEntityString](2))

      val detectNextPageFlow = builder
        .add(
          Flow[HttpEntityString]
            .map(extractNextUrlPathFromHttpEntity(_, initPath.subDomain))
            .takeWhile(_.isDefined)
            .map(_.get)
        )

      val nextPageHttpRequestBuild =
        builder.add(Flow[TwilioUri].map(_.createHttpRequest(connSettings)))

      //    val httpRespBroadCast = builder.add(Broadcast[])
      // format: off
      starter ~> httpReqMerge.in(0)
      httpReqMerge ~>     httpReqTupler  ~> httpFlow
      httpFlow ~> httpResponseHandler ~> httpEntityBroadcast
      httpReqMerge.in(1) <~ nextPageHttpRequestBuild  <~ detectNextPageFlow <~ httpEntityBroadcast.out(0)
      // format: on
      SourceShape(httpEntityBroadcast.out(1))
    })
  }

  private final case class TwilioResponseNextPageJsonRep(next_page_uri: Option[String])

//  Full meta json object likes like this, but for now we only need the nex_page_url:
//  "meta": {
//    "page": 1,
//    "page_size": 2,
//    "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
//    "previous_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0&PageToken=PTMGd8410e59416697cb4455c87eba98a6d0",
//    "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=1&PageToken=PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd",
//    "next_page_url": null,
//    "key": "services"
//  }
  private final case class MetaJsonRep(next_page_url: Option[String])
  private final case class MetaRootJsonResp(meta: MetaJsonRep)

  private def extractNextUrlPathFromHttpEntity(
      in: HttpEntityString,
      apiSubDomain: ApiSubDomain
  ): Option[TwilioUri] = {
    val optionalUri = apiSubDomain.pagingStyle match {
      case PagingStyle.NoPaging => None
      case PagingStyle.PagingAttributesInRootJson =>
        in.parse[TwilioResponseNextPageJsonRep]().toTry.get.next_page_uri
      case PagingStyle.MetaObject =>
        in.parse[MetaRootJsonResp]().toTry.get.meta.next_page_url
    }
    optionalUri.map(s => TwilioUri.autoDetect(s, HttpMethods.GET, apiSubDomain))
  }

}
