package com.dixa.twilio.client.model

sealed trait RecordingStatus {
  def toTwilioString: String
}
case object Paused extends RecordingStatus {
  override def toTwilioString: String = "paused"
}
case object InProgress extends RecordingStatus {
  override def toTwilioString: String = "in-progress"
}
case object Stopped extends RecordingStatus {
  override def toTwilioString: String = "stopped"
}
