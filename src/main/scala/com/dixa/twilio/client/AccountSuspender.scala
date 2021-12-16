package com.dixa.twilio.client

import com.dixa.thrift.generated.TelephonyAccount
import scala.collection.JavaConverters._

trait AccountSuspender {
  def suspend(accountSid: String): Unit
}

class AccountSuspenderImpl(mainAccount: TelephonyAccount, factory: TwilioRestClientFactory)
    extends AccountSuspender {
  private val client = factory.createClient(mainAccount)

  def suspend(accountSid: String): Unit = {
    val params = Map(
      "Status" -> "Suspended"
    )

    client.getAccount(accountSid).update(params.asJava)
  }
}
