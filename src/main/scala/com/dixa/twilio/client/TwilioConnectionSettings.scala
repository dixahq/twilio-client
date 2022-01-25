package com.dixa.twilio.client

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest}
import org.scalactic.TypeCheckedTripleEquals._

import java.net.URL
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/** Connection settings to use when communicating with Twilio
  *
  * @param url
  *   The URL to connect to.
  * @param accountSid
  *   The account sid to connect as.
  * @param authToken
  *   The auth token for the supplied account sid.
  * @param parallelFactor
  *   Some request support doing some of the work in parallel, this value will in such cases be used
  *   for that. Note that not all request support running in parellel, and in the onces that do,
  *   only some part of it may. So this is more of a guideline to the library, rather than a law.
  * @param timeouts
  *   Timeouts to use in the clients.
  */
final case class TwilioConnectionSettings(
    url: URL,
    accountSid: String,
    authToken: String,
    parallelFactor: TwilioConnectionSettings.ParallelFactor,
    timeouts: TwilioConnectionSettings.Timeouts
) {

  private[client] def createBaseRequest(
      method: HttpMethod = HttpMethods.GET,
      pathOrUri: String,
  ): HttpRequest = {
    val safeUri =
      if (pathOrUri.startsWith("http")) pathOrUri
      else {
        val pathWithSlashPrefix = if (pathOrUri.startsWith("/")) pathOrUri else s"/$pathOrUri"
        s"${url.getProtocol}://${url.getHost}:${url.getPort}$pathWithSlashPrefix"
      }
    HttpRequest(method, safeUri).addHeader(
      Authorization(
        BasicHttpCredentials(accountSid, authToken)
      )
    )
  }
}

object TwilioConnectionSettings {

  /** Represent a parallel factor for request to run at. */
  final case class ParallelFactor(asInt: Int)

  object ParallelFactor {

    /** Parallel factor corosponding to half of the cpu cores, or 1 if there only is one cpu core.
      *
      * A good default, if you system is also doing other things, than communicating with Twilio.
      */
    lazy val halfCpuCores: ParallelFactor = {
      val halfCpu = Runtime.getRuntime.availableProcessors() match {
        case numberOfCores if numberOfCores === 1 => 1
        case numberOfCores                        => numberOfCores / 2
      }
      ParallelFactor(halfCpu)
    }
  }

  /** Specify the different timeouts to be used by this client.
    *
    * @param requestEntityTimeout
    *   The max time to use on fetching an entity of a single request against Twilio.
    */
  final case class Timeouts(requestEntityTimeout: FiniteDuration)

  object Timeouts {

    lazy val default: Timeouts = Timeouts(
      requestEntityTimeout = 30.seconds
    )
  }

}
