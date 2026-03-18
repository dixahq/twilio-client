// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
    conference_sid: Option[String] = None,
    date_created: String,
    date_updated: String,
    start_time: String,
    duration: Option[String] = None,
    sid: String,
    price: Option[String] = None,
    price_unit: Option[String] = None,
    status: String,
    channels: Int,
    source: String,
    error_code: Option[Int] = None,
    encryption_details: Option[EncryptionDetailsJsonRep] = None,
    media_url: Option[String] = None,
    track: Option[String] = None
) {

  def toModel: Recording = Recording(
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    callSid = Call.Sid.unsafe(call_sid),
    conferenceSid = conference_sid.map(Conference.Sid.unsafe),
    channels = Recording.Channels.unsafe(channels),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateUpdate = Instant.from(Formatter.dateTime.parse(date_updated)),
    startTime = Instant.from(Formatter.dateTime.parse(start_time)),
    price = price.flatMap(pr =>
      price_unit.map(pu => Recording.Price(BigDecimal(pr), Iso4127CountryCode.apply(pu)))
    ),
    duration = duration.map(d => Duration.ofSeconds(d.toLong)),
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
    mediaUrl = media_url.map(Recording.MediaUrl.apply),
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

  implicit val upickleReader: Reader[RecordingJsonRep] =
    macroR[RecordingJsonRep]
}
