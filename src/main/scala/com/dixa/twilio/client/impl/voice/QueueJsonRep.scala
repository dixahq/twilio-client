package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Queue

import java.time.{Duration, Instant}

/** Json representation of a Call */
private[impl] case class QueueJsonRep(
    account_sid: String,
    average_wait_time: Int,
    current_size: Int,
    date_created: String,
    date_updated: String,
    friendly_name: String,
    max_size: Int,
    sid: String
) {

  def toModel: Queue = Queue(
    Queue.Sid.unsafe(sid),
    Queue.FriendlyName(friendly_name),
    TwilioAccount.Sid.unsafe(account_sid),
    Queue.CurrentSize(current_size),
    Queue.MaxSize(max_size),
    Duration.ofSeconds(average_wait_time),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}
