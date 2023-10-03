package com.dixa.twilio.client

import com.dixa.twilio.client.TwilioConnectionSettings.TwilioEndpoint
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.dixa.twilio.model.voice.Trunk

import java.time.Instant

object TwilioTestConstants {

  val createdTime: Instant = Instant.ofEpochSecond(1661836497) // 2022-08-30T05:14:57Z
  val updatedTime: Instant = Instant.ofEpochSecond(1662834697) // 2022-09-10T18:31:37Z

  val accountSid: TwilioAccount.Sid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  val authToken: AuthToken.UnknownType = AuthToken.UnknownType("testPassword")

  val trunkUsername1AsString         = "TwilioTestConstants.trunkUsername1"
  val trunkUsername1: Trunk.Username = Trunk.Username(trunkUsername1AsString)
  val trunkPassword1AsString         = "TwilioTestConstants.trunkPassword1"
  val trunkPassword1: Trunk.Password = Trunk.Password(trunkPassword1AsString)

  def connSettings(port: Int): TwilioConnectionSettings = TwilioConnectionSettings(
    TwilioEndpoint(baseHostName = "localhost", port = port),
    protocol = TwilioConnectionSettings.Protocol.Http,
    accountSid = accountSid,
    authToken = authToken,
    parallelFactor = TwilioConnectionSettings.ParallelFactor.halfCpuCores,
    timeouts = TwilioConnectionSettings.Timeouts.default
  )
}
