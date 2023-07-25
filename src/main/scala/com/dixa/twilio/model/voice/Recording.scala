package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.{Prefix, SidCompanionObject}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.{
  EnumWithTwilioString,
  Iso4127CountryCode,
  SidAbstract,
  TwilioStringValue
}

import java.time.{Duration, Instant}
import scala.annotation.nowarn
import scala.collection.immutable

final case class Recording(
    accountSid: TwilioAccount.Sid,
    callSid: Call.Sid,
    conferenceSid: Conference.Sid,
    dateCreated: Instant,
    dateUpdate: Instant,
    startTime: Instant,
    duration: Duration,
    sid: Recording.Sid,
    price: Recording.Price,
    status: Recording.Status,
    channels: Recording.Channels,
    source: Recording.Source,
    errorCode: Option[Recording.ErrorCode],
    encryptionDetails: Option[Recording.EncryptionDetails],
    mediaUrl: Recording.MediaUrl,
    track: Option[Recording.Track]
)

object Recording {

  /** Represent a Twilio Recording SID
    *
    * Input must apply to the format that Twilio specify as a Call SID: "It is a 34 character string
    * that starts with RE"
    */
  final case class Sid private[Recording] (override val toString: String) extends SidAbstract

  object Sid extends SidCompanionObject(List(Prefix("RE")), new Sid(_))

  sealed abstract class Status(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Processing extends Status("processing")
    case object Completed  extends Status("completed")
    case object Absent     extends Status("absent")
    case object Deleted    extends Status("deleted")
  }

  final case class Price(amount: BigDecimal, unit: Iso4127CountryCode) extends TwilioStringValue {
    override def twilioString: String = s"$amount $unit"
  }

  sealed abstract class Source(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Source extends EnumWithTwilioString[Source] {
    override val values: immutable.IndexedSeq[Source] = findValues
    case object DialVerb                    extends Source("DialVerb")
    case object Conference                  extends Source("Conference")
    case object OutboundAPI                 extends Source("OutboundAPI")
    case object Trunking                    extends Source("Trunking")
    case object RecordVerb                  extends Source("RecordVerb")
    case object StartCallRecordingAPI       extends Source("StartCallRecordingAPI")
    case object StartConferenceRecordingAPI extends Source("StartConferenceRecordingAPI")
  }

  final case class ErrorCode(code: Int) extends TwilioStringValue {
    override def twilioString: String = code.toString
  }

  // None encrypted recordings can be fetched by calling this media url post pended with the media file type
  final case class MediaUrl(url: String) extends TwilioStringValue {
    override def twilioString: String = url

    def asFileTypeMp3: String = s"${url}.mp3"

    def asFileTypeWav: String = s"${url}.wav"

    def asSingularMp3Channel: String = s"${url}.mp3?RequestedChannels=1"

    def asSingularWavChannel: String = s"${url}.wav?RequestedChannels=1"

    def asDualMp3Channel: String = s"${url}.mp3?RequestedChannels=2"

    def asDualWavChannel: String = s"${url}.wav?RequestedChannels=2"

    def asTranscriptions: String = s"${url}/Transcriptions"
  }

  sealed abstract class Track(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Track extends EnumWithTwilioString[Track] {
    override val values: immutable.IndexedSeq[Track] = findValues
    case object Inbound  extends Track("inbound")
    case object OutBound extends Track("outbound")
    case object Both     extends Track("both")
  }

  final case class EncryptionDetails(
      encryptionType: EncryptionDetails.EncryptionType,
      publicKeySid: EncryptionDetails.PublicKey.Sid,
      encryptedCek: EncryptionDetails.ContentEncryptionKey,
      iv: EncryptionDetails.InitialVector
  )

  object EncryptionDetails {

    final case class InitialVector(vector: String) extends TwilioStringValue {
      override def twilioString: String = "******"
    }

    final case class ContentEncryptionKey(key: String) extends TwilioStringValue {
      override def twilioString: String = "******"
    }

    object PublicKey {

      /** Represent a encrypted Twilio Recording public key SID
        *
        * Input must apply to the format that Twilio specify as a Public Key SID: "It is a 34
        * character string that starts with CR"
        */
      final case class Sid private[PublicKey] (override val toString: String) extends SidAbstract

      object Sid extends SidCompanionObject(List(Prefix("CR")), new Sid(_))
    }

    sealed abstract class EncryptionType(
        override val twilioString: String,
    ) extends EnumWithTwilioString.EnumEntry

    object EncryptionType extends EnumWithTwilioString[EncryptionType] {
      override val values: immutable.IndexedSeq[EncryptionType] = findValues
      case object RsaAes extends EncryptionType("rsa-aes")
    }

  }

  final case class Channels private (int: Int) extends TwilioStringValue {
    override def twilioString: String = int.toString
  }

  object Channels {
    sealed trait Err extends RuntimeException

    object Err {
      case class ValueBelowAllowed(int: Int)
          extends RuntimeException("There must be at least one channel for a recording")
          with Err

      case class ValueAboveAllowed(int: Int)
          extends RuntimeException(
            s"There can at most be two channel for a recording, one pr call leg, not $int"
          )
          with Err
    }

    // override apply method as private, to ensure clients cannot create invalid instance.
    @nowarn(value = "cat=unused")
    private def apply(int: Int): Channels = {
      new Channels(int)
    }

    def safe(int: Int): Either[Err, Channels] = {
      if (int < 1) Left(Err.ValueBelowAllowed(int))
      else if (int > 2) Left(Err.ValueAboveAllowed(int))
      else Right(new Channels(int))
    }

    def unsafe(int: Int): Channels = safe(int).toTry.get
  }
}
