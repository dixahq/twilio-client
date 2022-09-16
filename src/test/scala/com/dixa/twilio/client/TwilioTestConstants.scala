package com.dixa.twilio.client

import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}

import java.time.Instant

object TwilioTestConstants {

  val createdTime: Instant = Instant.ofEpochSecond(1661836497) // 2022-08-30T05:14:57Z
  val updatedTime: Instant = Instant.ofEpochSecond(1662834697) // 2022-09-10T18:31:37Z

  val accountSid: TwilioAccount.Sid    = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  val authToken: AuthToken.UnknownType = AuthToken.UnknownType("testPassword")

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    TwilioEndpoint(baseHostName = "localhost", port = port),
    protocol = TwilioConnectionSettings.Protocol.Http,
    accountSid = accountSid,
    authToken = authToken,
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
