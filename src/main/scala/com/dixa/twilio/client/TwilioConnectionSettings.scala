package com.dixa.twilio.client

import com.dixa.twilio.client
import com.dixa.twilio.client.implDetails.ApiSubDomain
import com.dixa.twilio.client.model.iam.TwilioAccount
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/** Connection settings to use when communicating with Twilio
  *
  * @param baseHostName
  *   The base host name to connect to without the sup domain part. The URL to connect to. For
  *   production this would be `twilio.com` and then request would end up being made agains the
  *   respective sub domains like `api.twilio.com` and `messagin.twilio.com` depending on the
  *   request. The Exception is localhost or 127.0.0.1, if that is set, then no subdomain will be
  *   added, no matter what the request is.
  * @param port
  *   TCP port to use for connecting to Twilio
  * @param protocol
  *   Protocol to use for connecting to Twilio
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
    baseHostName: String,
    port: Int,
    protocol: client.TwilioConnectionSettings.Protocol,
    accountSid: TwilioAccount.Sid,
    authToken: TwilioAccount.AuthToken,
    parallelFactor: TwilioConnectionSettings.ParallelFactor,
    timeouts: TwilioConnectionSettings.Timeouts
) {

  /** Return the hostname for a specific API sub domain.
    *
    * Twilio is using different sub domains, for different parts of there API, so the
    * TwilioConnectionSetting is only setting what base hostname to connect agains, and it then up
    * to each individual request, to specify what sub domain they should use, and use this method
    * for constructing the full hostname.
    *
    * If the base hostname is localhost or 127.0.0.1, then this method will just return that without
    * changing it. This is to make it possible to stub request in unit test with a tool like
    * Wiremock, where the request should just go to localhost without a subdomain appended.
    */
  private[client] def hostNameFor(subDomain: ApiSubDomain): String = {
    if (baseHostName === "localhost" || baseHostName === "127.0.0.1") baseHostName
    else s"$subDomain.$baseHostName"
  }
}

object TwilioConnectionSettings {

  sealed abstract class Protocol(override val toString: String) extends EnumEntry
  object Protocol extends Enum[Protocol] {
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
