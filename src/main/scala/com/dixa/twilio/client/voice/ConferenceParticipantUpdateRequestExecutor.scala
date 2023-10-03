package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}

import scala.annotation.nowarn

trait ConferenceParticipantUpdateRequestExecutor
    extends SingleRequestExecutor[
      ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest,
      ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateException,
      Conference.Participant
    ] {

  import ConferenceParticipantUpdateRequestExecutor._

  override protected final type ApiExceptionWrapper = ConferenceParticipantUpdateException.Api

  override protected final type UnspecifiedException =
    ConferenceParticipantUpdateException.Unspecified
}

object ConferenceParticipantUpdateRequestExecutor {

  sealed trait ConferenceParticipantUpdateRequest {
    def accountSid: TwilioAccount.Sid
    def conferenceSid: Conference.Sid

    def participantId: Either[Call.Sid, Conference.Participant.Label]
    def muted: Option[Boolean]
    def hold: Option[Boolean]
    def holdUrl: Option[CallbackUrl]
    def holdMethod: Option[HttpMethod]
    def announceUrl: Option[CallbackUrl]
    def announceMethod: Option[HttpMethod]
    def waitUrl: Option[CallbackUrl]
    def waitMethod: Option[HttpMethod]
    def beepOnExit: Option[Boolean]
    def endConferenceOnExit: Option[Boolean]
    def callSidToCoach: Option[Call.Sid]
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      participantId: Either[Call.Sid, Conference.Participant.Label],
      muted: Option[Boolean],
      hold: Option[Boolean],
      holdUrl: Option[CallbackUrl],
      holdMethod: Option[HttpMethod],
      announceUrl: Option[CallbackUrl],
      announceMethod: Option[HttpMethod],
      waitUrl: Option[CallbackUrl],
      waitMethod: Option[HttpMethod],
      beepOnExit: Option[Boolean],
      endConferenceOnExit: Option[Boolean],
      callSidToCoach: Option[Call.Sid]
  ) extends ConferenceParticipantUpdateRequest

  object ConferenceParticipantUpdateRequest {

    /** Phantom types used to enforce request constraints compile time. */
    object PhantomTypes {
      sealed trait AccountSidSetBool
      sealed trait AccountSidSetTrue  extends AccountSidSetBool
      sealed trait AccountSidSetFalse extends AccountSidSetBool

      sealed trait ConferenceSidSetBool
      sealed trait ConferenceSidSetTrue  extends ConferenceSidSetBool
      sealed trait ConferenceSidSetFalse extends ConferenceSidSetBool

      sealed trait CallIdSetBool
      sealed trait CallIdSetTrue  extends CallIdSetBool
      sealed trait CallIdSetFalse extends CallIdSetBool

      sealed trait HoldSetBool
      sealed trait HoldSetTrue  extends HoldSetBool
      sealed trait HoldSetFalse extends HoldSetBool

      sealed trait HoldUrlSetBool
      sealed trait HoldUrlSetTrue  extends HoldUrlSetBool
      sealed trait HoldUrlSetFalse extends HoldUrlSetBool

      sealed trait AnnounceUrlSetBool
      sealed trait AnnounceUrlSetTrue  extends AnnounceUrlSetBool
      sealed trait AnnounceUrlSetFalse extends AnnounceUrlSetBool

      sealed trait WaitUrlSetBool
      sealed trait WaitUrlSetTrue  extends WaitUrlSetBool
      sealed trait WaitUrlSetFalse extends WaitUrlSetBool

    }

    import PhantomTypes._

    final class Builder[
        AccountSidSet <: AccountSidSetBool,
        ConferenceSidSet <: ConferenceSidSetBool,
        CallIdSet <: CallIdSetBool,
        HoldSet <: HoldSetBool,
        HoldUrlSet <: HoldUrlSetBool,
        AnnounceUrlSet <: AnnounceUrlSetBool,
        WaitUrlSet <: WaitUrlSetBool
    ] private[ConferenceParticipantUpdateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        conferenceSid: Option[Conference.Sid],
        callSid: Option[Call.Sid],
        label: Option[Conference.Participant.Label],
        muted: Option[Boolean],
        hold: Option[Boolean],
        holdUrl: Option[CallbackUrl],
        holdMethod: Option[HttpMethod],
        announceUrl: Option[CallbackUrl],
        announceMethod: Option[HttpMethod],
        waitUrl: Option[CallbackUrl],
        waitMethod: Option[HttpMethod],
        beepOnExit: Option[Boolean],
        endConferenceOnExit: Option[Boolean],
        callSidToCoach: Option[Call.Sid]
    ) {

      private def copy[
          AccountSidSetNew <: AccountSidSetBool,
          ConferenceSidSetNew <: ConferenceSidSetBool,
          CallIdSetNew <: CallIdSetBool,
          HoldSetNew <: HoldSetBool,
          HoldUrlSetNew <: HoldUrlSetBool,
          AnnounceUrlSetNew <: AnnounceUrlSetBool,
          WaitUrlSetNew <: WaitUrlSetBool
      ](
          accountSid: Option[TwilioAccount.Sid] = this.accountSid,
          conferenceSid: Option[Conference.Sid] = this.conferenceSid,
          callSid: Option[Call.Sid] = this.callSid,
          label: Option[Conference.Participant.Label] = this.label,
          muted: Option[Boolean] = this.muted,
          hold: Option[Boolean] = this.hold,
          holdUrl: Option[CallbackUrl] = this.holdUrl,
          holdMethod: Option[HttpMethod] = this.holdMethod,
          announceUrl: Option[CallbackUrl] = this.announceUrl,
          announceMethod: Option[HttpMethod] = this.announceMethod,
          waitUrl: Option[CallbackUrl] = this.waitUrl,
          waitMethod: Option[HttpMethod] = this.waitMethod,
          beepOnExit: Option[Boolean] = this.beepOnExit,
          endConferenceOnExit: Option[Boolean] = this.endConferenceOnExit,
          callSidToCoach: Option[Call.Sid] = this.callSidToCoach
      ) = new Builder[
        AccountSidSetNew,
        ConferenceSidSetNew,
        CallIdSetNew,
        HoldSetNew,
        HoldUrlSetNew,
        AnnounceUrlSetNew,
        WaitUrlSetNew
      ](
        accountSid,
        conferenceSid,
        callSid,
        label,
        muted,
        hold,
        holdUrl,
        holdMethod,
        announceUrl,
        announceMethod,
        waitUrl,
        waitMethod,
        beepOnExit,
        endConferenceOnExit,
        callSidToCoach
      )

      private type BuilderWithSameTypes =
        Builder[
          AccountSidSet,
          ConferenceSidSet,
          CallIdSet,
          HoldSet,
          HoldUrlSet,
          AnnounceUrlSet,
          WaitUrlSet
        ]

      /** The SID of the Account that created the Participant resources to update. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidSetTrue,
        ConferenceSidSet,
        CallIdSet,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(accountSid = Some(accountSid))

      /** The SID of the conference with the participant to update. */
      def withConferenceSid(
          conferenceSid: Conference.Sid
      ): Builder[
        AccountSidSet,
        ConferenceSidSetTrue,
        CallIdSet,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(conferenceSid = Some(conferenceSid))

      /** Call sid of the participant to update.
        *
        * Is mutual exclusive with [[withLabel]] but using one of them is required.
        */
      def withCallSid(
          callSid: Call.Sid
      ): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSetTrue,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(callSid = Some(callSid))

      /** Label of the participant to update.
        *
        * Is mutual exclusive with [[withCallSid]] but using one of them is required.
        */
      def withLabel(
          label: Conference.Participant.Label
      ): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSetTrue,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(label = Some(label))

      /** Whether the participant should be muted. Can be true or false. true will mute the
        * participant, and false will un-mute them. Anything value other than true or false is
        * interpreted as false.
        */
      def withMuted(muted: Boolean): BuilderWithSameTypes = copy(muted = Some(muted))

      /** puts the participant on hold. */
      def withHoldTrue(): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSet,
        HoldSetTrue,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(hold = Some(true))

      /** Let participant rejoin the conference.
        *
        * You cannot call this, if you have already called [[withHoldUrl]]
        */
      def withHoldFalse()(
          implicit ev: HoldUrlSet =:= HoldUrlSetFalse
      ): BuilderWithSameTypes = copy(hold = Some(false))

      /** The URL we call using the hold_method for music that plays when the participant is on
        * hold. The URL may return an MP3 file, a WAV file, or a TwiML document that contains
        * <Play>, <Say>, <Pause>, or <Redirect> verbs.
        *
        * You cannot set this, if you have not called [[withHoldTrue]] first.
        */
      def withHoldUrl(holdUrl: CallbackUrl)(
          implicit ev: HoldSet =:= HoldSetTrue
      ): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSet,
        HoldSet,
        HoldUrlSetTrue,
        AnnounceUrlSet,
        WaitUrlSet
      ] =
        copy(holdUrl = Some(holdUrl))

      /** The HTTP method we should use to call hold_url. Can be: GET or POST and the default is
        * GET.
        */
      def withHoldMethod(holdMethod: HttpMethod)(
          implicit ev: HoldUrlSet =:= HoldUrlSetTrue
      ): BuilderWithSameTypes = copy(holdMethod = Some(holdMethod))

      /** The URL we call using the announce_method for an announcement to the participant. The URL
        * may return an MP3 file, a WAV file, or a TwiML document that contains <Play>, <Say>,
        * <Pause>, or <Redirect> verbs.
        */
      def withAnnounceUrl(announceUrl: CallbackUrl): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSet,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSetTrue,
        WaitUrlSet
      ] = copy(announceUrl = Some(announceUrl))

      /** The HTTP method we should use to call announce_url. Can be: GET or POST and defaults to
        * POST.
        *
        * Can only be called if you have first called [[withAnnounceUrl]]
        */
      def withAnnounceMethod(announceMethod: HttpMethod)(
          ev: AnnounceUrlSet =:= AnnounceUrlSetTrue
      ): BuilderWithSameTypes =
        copy(announceMethod = Some(announceMethod))

      /** The URL we call using the wait_method for the music to play while participants are waiting
        * for the conference to start. The URL may return an MP3 file, a WAV file, or a TwiML
        * document that contains <Play>, <Say>, <Pause>, or <Redirect> verbs. The default value is
        * the URL of our standard hold music.
        *
        * @see
        *   https://www.twilio.com/labs/twimlets/holdmusic
        */
      def withWaitUrl(waitUrl: CallbackUrl): Builder[
        AccountSidSet,
        ConferenceSidSet,
        CallIdSet,
        HoldSet,
        HoldUrlSet,
        AnnounceUrlSet,
        WaitUrlSetTrue
      ] = copy(waitUrl = Some(waitUrl))

      /** The HTTP method we should use to call wait_url. Can be GET or POST and the default is
        * POST. When using a static audio file, this should be GET so that we can cache the file.
        */
      def withWaitMethod(waitMethod: HttpMethod)(
          implicit ev: WaitUrlSet =:= WaitUrlSetTrue
      ): BuilderWithSameTypes = copy(waitMethod = Some(waitMethod))

      /** Whether to play a notification beep to the conference when the participant exits. Can be:
        * true or false.
        */
      def withBeepOnExit(beepOnExit: Boolean): BuilderWithSameTypes =
        copy(beepOnExit = Some(beepOnExit))

      /** Whether to end the conference when the participant leaves. Can be: true or false and
        * defaults to false.
        */
      def endConferenceOnExit(endConferenceOnExit: Boolean): BuilderWithSameTypes =
        copy(endConferenceOnExit = Some(endConferenceOnExit))

      /** The SID of the participant who is being coached. The participant being coached is the only
        * participant who can hear the participant who is coaching.
        *
        * Note that this library does not require you to set the coaching attribute, as it is
        * completly controlled via this attribute instead.
        */
      def callSidToCoach(callSidToCoach: Call.Sid): BuilderWithSameTypes =
        copy(callSidToCoach = Some(callSidToCoach))

      private[this] def throwNeverReachHereError: Nothing = throw new AssertionError(
        s"Should never reach here due to build in compile time constraints. This is clearly a bug in ${getClass.getName}"
      )

      def build()(
          implicit ev1: AccountSidSet =:= AccountSidSetTrue,
          ev2: ConferenceSidSet =:= ConferenceSidSetTrue,
          ev3: CallIdSet =:= CallIdSetTrue
      ): ConferenceParticipantUpdateRequest = {
        val participantId = callSid
          .map(Left(_))
          .getOrElse(
            label
              .map(Right(_))
              .getOrElse(throwNeverReachHereError)
          )
        RequestImpl(
          accountSid.getOrElse(throwNeverReachHereError),
          conferenceSid.getOrElse(throwNeverReachHereError),
          participantId,
          muted,
          hold,
          holdUrl,
          holdMethod,
          announceUrl,
          announceMethod,
          waitUrl,
          waitMethod,
          beepOnExit,
          endConferenceOnExit,
          callSidToCoach
        )
      }
    }

    type BuilderStartState = Builder[
      AccountSidSetFalse,
      ConferenceSidSetFalse,
      CallIdSetFalse,
      HoldSetFalse,
      HoldUrlSetFalse,
      AnnounceUrlSetFalse,
      WaitUrlSetFalse
    ]

    def build(
        fun: BuilderStartState => ConferenceParticipantUpdateRequest
    ): ConferenceParticipantUpdateRequest =
      fun(
        new BuilderStartState(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
      )
  }

  sealed trait ConferenceParticipantUpdateException extends RuntimeException

  object ConferenceParticipantUpdateException {

    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ConferenceParticipantUpdateException
        with ApiExceptionWrapper

    final case class ParticipantNotFound(
        accountSid: TwilioAccount.Sid,
        conferenceSid: Conference.Sid,
        participantId: Either[Call.Sid, Conference.Participant.Label]
    ) extends RuntimeException(
          s"Conference participant with ${participantId
              .fold(cs => s"callSid=$cs", label => s"label=$label")} in conference $conferenceSid on account $accountSid was not found."
        )
        with ConferenceParticipantUpdateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to update Conference participant"
          ),
          cause.orNull
        )
        with ConferenceParticipantUpdateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
