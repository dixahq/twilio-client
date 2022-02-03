package com.dixa.twilio.client.model.iam

sealed trait ValidationRequestStatus
object ValidationStatus {
  case object Valid   extends ValidationRequestStatus
  case object Invalid extends ValidationRequestStatus
}
