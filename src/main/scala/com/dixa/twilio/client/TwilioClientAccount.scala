package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.TwilioAccount

trait TwilioClientAccount {

  def fetchAllAccounts(connSettings: TwilioConnectionSettings): Source[TwilioAccount, NotUsed]
}
