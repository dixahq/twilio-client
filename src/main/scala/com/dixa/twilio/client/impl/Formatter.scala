package com.dixa.twilio.client.impl

import java.time.format.DateTimeFormatter

private[impl] object Formatter {

  val dateTime: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z")
}
