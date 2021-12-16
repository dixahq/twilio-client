package com.dixa.twilio.client

import cats.effect.IO
import com.dixa.thrift.generated.{ICEServer, TelephonyAccount}
import com.twilio.sdk.resource.instance.Token
import org.apache.http.NameValuePair

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

trait ICEServerUtil {
  def createTurnServerCredentials(telephonyAccount: TelephonyAccount): IO[Set[ICEServer]]
}

/** Utility for creating TURN / ICE server authentication tokens, via the Twilio API
  */
class TwilioICEServerUtil(clientFactory: TwilioRestClientFactory)(
    implicit ec: ExecutionContext
) extends ICEServerUtil {

  def createTurnServerCredentials(telephonyAccount: TelephonyAccount): IO[Set[ICEServer]] = {
    IO.blocking {
      val client = clientFactory.createClient(telephonyAccount)
      try {
        val tokenFactory = client.getAccount().getTokenFactory
        // Using default max TTL of 24 hours by not specifying TTL in params
        val params: List[NameValuePair] = Nil
        val token                       = tokenFactory.create(params.asJava)

        // Workaround for: java.lang.IllegalAccessError: tried to access class com.twilio.sdk.resource.instance.Token$IceServer from class com.dixa.telephony.twilio.TwilioICEServerUtil$$anonfun$createTurnServerCredentials$1$$anonfun$1
        TwilioICEServerUtil
          .getServers(token)
          .filter(server => isTcp443TurnServer(server.url))

      } finally client.getHttpClient.getConnectionManager.shutdown()
    }
  }

  private def isTcp443TurnServer(url: String): Boolean = {
    // turn:global.twilio.com:443?transport=tcp
    url.startsWith("turn:") && url.contains(":443") && url.contains("transport=tcp")
  }

}

object TwilioICEServerUtil {

  /** Get the servers from twilio token and Unsafely throws an exception if one is not parsed
    * correctly.
    *
    * @throws IllegalArgumentException()
    */
  private[twilio] def getServers(token: Token): Set[ICEServer] = {
    token.getObject("ice_servers") match {
      case untypedMapList: java.util.List[_] =>
        val scalaMaps = untypedMapList.asScala.toList.map { any =>
          any.asInstanceOf[java.util.Map[String, String]].asScala.toMap
        }
        scalaMaps.map { map =>
          ICEServer(
            url = map
              .get("url")
              .flatMap(Option.apply)
              .getOrElse(throw new IllegalArgumentException(s"Missing url in $map")),
            username = map.get("username").flatMap(Option.apply),
            password = map.get("credential").flatMap(Option.apply)
          )
        }.toSet

      case other =>
        throw new IllegalStateException(s"Expected a list of maps, not $other")
    }
  }
}
