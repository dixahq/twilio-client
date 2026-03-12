package com.dixa.twilio.client.impl.iam

import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.AccessTokenCreateRequestExecutor
import com.dixa.twilio.client.iam.AccessTokenCreateRequestExecutor.{
  AccessTokenCreateException,
  AccessTokenCreateRequest
}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.Region
import com.dixa.twilio.model.iam.AccessToken

import scala.concurrent.{ExecutionContext, Future}

private[iam] final class AccessTokenCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccessTokenCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  /** AccessToken is generated locally and does not involve an HTTP request. We override run to
    * perform the generation directly.
    *
    * Tokens have a maximum age of 24 hours. Exceeding it results in error 20157: Expiration Time
    * Exceeds Maximum Time Allowed on Voice SDK.
    */
  override def run(
      connSettings: TwilioConnectionSettings,
      req: AccessTokenCreateRequest
  ): Future[Either[AccessTokenCreateException, AccessToken]] = Future {
    val ttlSeconds = req.ttl.toSeconds
    if (ttlSeconds <= 0 || ttlSeconds > 86400) {
      Left(AccessTokenCreateException.InvalidTtl(ttlSeconds))
    } else {
      try {
        val now     = System.currentTimeMillis() / 1000
        val encoder = Base64.getUrlEncoder.withoutPadding()

        def base64(s: String): String =
          encoder.encodeToString(s.getBytes("UTF-8"))

        val apiKeySid    = connSettings.apiKeySid.toString
        val apiKeySecret = connSettings.apiKeySecret.value
        val accountSid   = connSettings.accountSid.toString

        val regionValue = req.region.getOrElse(Region.Us1).twilioString

        val grantsFields = req.grants
          .map(g => s""""${g.grantKey}":${g.toJson}""")
          .mkString(",")

        val grantsJson = s""""identity":"${req.identity}",$grantsFields"""

        val rawPayload =
          s"""{""" +
            s""""jti":"$apiKeySid-$now",""" +
            s""""iss":"$apiKeySid",""" +
            s""""sub":"$accountSid",""" +
            s""""iat":$now,""" +
            s""""exp":${now + ttlSeconds},""" +
            s""""region":"$regionValue",""" +
            s""""grants":{$grantsJson}""" +
            s"""}"""

        val header  = base64("""{"typ":"JWT","alg":"HS256","cty":"twilio-fpa;v=1"}""")
        val payload = base64(rawPayload)

        val signingInput = s"$header.$payload"

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(new SecretKeySpec(apiKeySecret.getBytes("UTF-8"), "HmacSHA256"))
        val signature = encoder.encodeToString(mac.doFinal(signingInput.getBytes("UTF-8")))

        Right(AccessToken(s"$signingInput.$signature"))
      } catch {
        case e: Exception =>
          Left(createUnspecifiedException(Some("Error generating AccessToken"), Some(e)))
      }
    }
  }

  // These are not used since run is overridden, but must be implemented to satisfy the trait
  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccessTokenCreateRequest
  ): Either[AccessTokenCreateException, HttpRequest] =
    Left(
      createUnspecifiedException(Some("AccessToken generation does not use HTTP requests"), None)
    )

  override protected def parseHttpResponse(
      request: AccessTokenCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AccessTokenCreateException, AccessToken] =
    Left(
      createUnspecifiedException(Some("AccessToken generation does not use HTTP requests"), None)
    )

  override protected def mapApiException(
      apiException: ApiException
  ): AccessTokenCreateException.Api =
    AccessTokenCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AccessTokenCreateException.Unspecified =
    AccessTokenCreateException.Unspecified(msg, cause)

}
