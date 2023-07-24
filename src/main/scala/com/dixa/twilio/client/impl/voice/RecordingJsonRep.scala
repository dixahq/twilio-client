package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.impl.voice.RecordingJsonRep.EncryptionDetailsJsonRep
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Recording.EncryptionDetails
import com.dixa.twilio.model.voice.{Call, Conference, Recording}

import java.time.{Duration, Instant}

/** Json representation of a Recording */
private[impl] case class RecordingJsonRep(
    account_sid: String,
    api_version: String,
    call_sid: String,
    conference_sid: String,
    date_created: String,
    date_updated: String,
    start_time: String,
    duration: String,
    sid: String,
    price: String,
    price_unit: String,
    status: String,
    channels: String,
    source: String,
    error_code: Option[Int],
    encryption_details: Option[EncryptionDetailsJsonRep] = None,
    media_url: String,
    track: Option[String]
) {

  def toModel: Recording = Recording(
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    callSid = Call.Sid.unsafe(call_sid),
    conferenceSid = Conference.Sid.unsafe(conference_sid),
    channels = Recording.Channels.unsafe(channels.toInt),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateUpdate = Instant.from(Formatter.dateTime.parse(date_updated)),
    startTime = Instant.from(Formatter.dateTime.parse(start_time)),
    price = Recording.Price(BigDecimal(price), Iso4127CountryCode.apply(price_unit)),
    duration = Duration.ofSeconds(duration.toLong),
    sid = Recording.Sid.unsafe(sid),
    source = Recording.Source.fromTwilioStringUnsafe(source),
    status = Recording.Status.fromTwilioStringUnsafe(status),
    errorCode = error_code.map(Recording.ErrorCode),
    encryptionDetails = encryption_details.map(ed =>
      EncryptionDetailsJsonRep(
        ed.encryption_type,
        ed.encryption_public_key_sid,
        ed.encryption_cek,
        ed.encryption_iv
      ).toModel
    ),
    mediaUrl = Recording.MediaUrl.apply(media_url),
    track = track.map(Recording.Track.fromTwilioStringUnsafe)
  )
}

private[voice] object RecordingJsonRep {

  private[voice] case class EncryptionDetailsJsonRep(
      encryption_type: Option[String] = None,
      encryption_public_key_sid: String,
      encryption_cek: String,
      encryption_iv: String
  ) {
    def toModel: EncryptionDetails = EncryptionDetails(
      encryptionType = encryption_type
        .map(EncryptionDetails.EncryptionType.fromTwilioStringUnsafe)
        .getOrElse(EncryptionDetails.EncryptionType.RsaAes),
      publicKeySid = EncryptionDetails.PublicKey.Sid.unsafe(encryption_public_key_sid),
      encryptedCek = EncryptionDetails.ContentEncryptionKey.apply(encryption_cek),
      iv = EncryptionDetails.InitialVector.apply(encryption_iv)
    )
  }

  object EncryptionDetailsJsonRep {
    implicit val upickleReader: Reader[EncryptionDetailsJsonRep] =
      macroR[EncryptionDetailsJsonRep]
  }

  implicit val upickleReader: Reader[CallJsonRep] =
    macroR[CallJsonRep]
}
