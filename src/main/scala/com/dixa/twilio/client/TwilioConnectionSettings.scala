package com.dixa.twilio.client

import com.dixa.twilio.client
import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/** Connection settings to use when communicating with Twilio
  *
  * @param endpoint
  *   The base host name and port to connect to without the sup domain part. For production this
  *   would be `twilio.com:443` and then request would end up being made against the respective sub
  *   domains like `api.twilio.com` and `messaging.twilio.com` depending on the request. The
  *   Exception is localhost or 127.0.0.1, if that is set, then no subdomain will be added, no
  *   matter what the request is. Should be "twilio.com" for the production Twilio API.
  * @param protocol
  *   Protocol to use for connecting to Twilio. Should be Https for production Twilio API.
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
    endpoint: TwilioEndpoint,
    protocol: client.TwilioConnectionSettings.Protocol,
    accountSid: TwilioAccount.Sid,
    authToken: AuthToken,
    parallelFactor: TwilioConnectionSettings.ParallelFactor,
    timeouts: TwilioConnectionSettings.Timeouts
) {

  /** Return the hostname for a specific API sub domain.
    *
    * Twilio is using different sub domains, for different parts of there API, so the
    * TwilioConnectionSetting is only setting what base hostname to connect against, and it then up
    * to each individual request, to specify what sub domain they should use, and use this method
    * for constructing the full hostname.
    *
    * If the base hostname is localhost or 127.0.0.1, then this method will just return that without
    * changing it. This is to make it possible to stub request in unit test with a tool like
    * Wiremock, where the request should just go to localhost without a subdomain appended.
    */
  private[client] def hostNameFor(subDomain: ApiSubDomain): String = {
    if (endpoint.baseHostName == "localhost" || endpoint.baseHostName == "127.0.0.1")
      endpoint.baseHostName
    else s"$subDomain.${endpoint.baseHostName}"
  }

}

object TwilioConnectionSettings {

  sealed abstract class Protocol(override val toString: String) extends EnumEntry
  object Protocol                                               extends Enum[Protocol] {
    override val values: immutable.IndexedSeq[Protocol] = findValues
    case object Http  extends Protocol("http")
    case object Https extends Protocol("https")
  }

  /** Represent a parallel factor for request to run at. */
  final case class ParallelFactor(asInt: Int)

  object ParallelFactor {

    /** Parallel factor corosponding to half of the cpu cores, or 1 if there only is one cpu core.
      *
      * A good default, if you system is also doing other things, than communicating with Twilio.
      */
    lazy val halfCpuCores: ParallelFactor = {
      val halfCpu = Runtime.getRuntime.availableProcessors() match {
        case numberOfCores if numberOfCores == 1 => 1
        case numberOfCores                       => numberOfCores / 2
      }
      ParallelFactor(halfCpu)
    }
  }

  final case class TwilioEndpoint private (baseHostName: String, port: Int)

  object TwilioEndpoint {

    /** The default endpoint for Twilio - twilio.com:443 */
    val default: TwilioEndpoint = new TwilioEndpoint("twilio.com", 443)

    def apply(baseHostName: String, port: Int): TwilioEndpoint = {
      // return already existing default instance if settings match the default settings.
      // Minor optimization that will save to identical object from existing, in the cases
      // where clients read settings from config, or for some other reason end up constructing
      // this object manually with default settings. And default settings is what would
      // end up being used in almost any other case than tests.
      if (baseHostName == default.baseHostName && port == default.port) default
      else new TwilioEndpoint(baseHostName, port)
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
