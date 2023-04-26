package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod, PositiveInteger}

import scala.annotation.nowarn
import scala.collection.immutable

/** Representation of the Gather Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/gather
  */
sealed trait GatherVerb extends TwimlElement.Verb {

  override protected def tagName: String = "Gather"

  override final protected val tagValue: Option[String] = None
}

object GatherVerb {

  /** Sub type of GatherVerb that indicates that it's an instance guaranteed to be valid.
    *
    * By valid is only meant that it will produce valid TwiML, not that the TwiML necessarily would
    * work. You can still make it fail, by providing unreachable URLs etc, but the TwiML itself will
    * be valid.
    */
  sealed trait Verified extends GatherVerb

  /** Sub type of GatherVerb that is not guaranteed to be valid */
  sealed trait Unverified extends GatherVerb

  /** GatherVerb specific phantom types, used to enforce build constraints compile time. */
  object PhantomTypes {

    sealed trait HasDtmfInput
    sealed trait HasDtmfInputTrue  extends HasDtmfInput
    sealed trait HasDtmfInputFalse extends HasDtmfInput

    sealed trait HasSpeechInput
    sealed trait HasSpeechInputTrue  extends HasSpeechInput
    sealed trait HasSpeechInputFalse extends HasSpeechInput

    sealed trait DtmfInputRequired
    sealed trait DtmfInputRequiredTrue  extends DtmfInputRequired
    sealed trait DtmfInputRequiredFalse extends DtmfInputRequired

    sealed trait SpeechInputRequired
    sealed trait SpeechInputRequiredTrue  extends SpeechInputRequired
    sealed trait SpeechInputRequiredFalse extends SpeechInputRequired

    sealed trait ActionHasBeenSet
    sealed trait ActionHasBeenSetTrue  extends ActionHasBeenSet
    sealed trait ActionHasBeenSetFalse extends ActionHasBeenSet

    sealed trait LanguageHasBeenSet
    sealed trait LanguageHasBeenSetTrue  extends LanguageHasBeenSet
    sealed trait LanguageHasBeenSetFalse extends LanguageHasBeenSet

    sealed trait ValidForVerified
    sealed trait ValidForVerifiedTrue  extends ValidForVerified
    sealed trait ValidForVerifiedFalse extends ValidForVerified
  }

  /** Enum entry, representing a Language code that the Gather verb support */
  sealed abstract class LanguageCode(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Language codes that the Gather verb support */
  // noinspection ScalaUnusedSymbol
  object LanguageCode extends EnumWithTwilioString[LanguageCode] {

    sealed trait SupportsEnhancedModel
    sealed trait SupportsExperimentalModel
    sealed trait SupportsPhoneCallModel

    val values: immutable.IndexedSeq[LanguageCode] = findValues

    // I generated all of these, by downloading the csv from here:
    // https://www.twilio.com/docs/voice/twiml/gather#languagetags
    // and then copied them into a String val in a sandbox project (without the headings),
    // and then generated them with the following code:
    // val split = a.lines.toList.asScala.map { line =>
    //    val lastCommaIndex = line.lastIndexOf(",")
    //    val (name, codeWithComma) =line.splitAt(lastCommaIndex)
    //    (name, codeWithComma.tail)
    //  }.map { x =>
    //      s"""/** ${x._1} */
    //        |    case object `${x._2}` extends LanguageCodes("${x._2}")
    //        |""".stripMargin
    //    }
    //  println(split.mkString(System.lineSeparator()))
    //
    // However the CSV did not include information about support enhanced or experimental models,
    // soo I manually made them mix in those trait, based on the information from the twilio site,
    // already linked to.

    /** Afrikaans (South Africa) */
    case object `af-ZA` extends LanguageCode("af-ZA")

    /** Amharic (Ethiopia) */
    case object `am-ET` extends LanguageCode("am-ET")

    /** Armenian (Armenia) */
    case object `hy-AM` extends LanguageCode("hy-AM")

    /** Azerbaijani (Azerbaijani) */
    case object `az-AZ` extends LanguageCode("az-AZ")

    /** Indonesian (Indonesia) */
    case object `id-ID` extends LanguageCode("id-ID")

    /** Malay (Malaysia) */
    case object `ms-MY` extends LanguageCode("ms-MY")

    /** Bengali (Bangladesh) */
    case object `bn-BD` extends LanguageCode("bn-BD")

    /** Bengali (India) */
    case object `bn-IN` extends LanguageCode("bn-IN")

    /** Catalan (Spain) */
    case object `ca-ES` extends LanguageCode("ca-ES")

    /** Czech (Czech Republic) */
    case object `cs-CZ` extends LanguageCode("cs-CZ")

    /** Danish (Denmark) */
    case object `da-DK` extends LanguageCode("da-DK") with SupportsExperimentalModel

    /** German (Germany) */
    case object `de-DE` extends LanguageCode("de-DE") with SupportsExperimentalModel

    /** English (Australia) */
    case object `en-AU`
        extends LanguageCode("en-AU")
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** English (Canada) */
    case object `en-CA` extends LanguageCode("en-CA")

    /** English (Ghana) */
    case object `en-GH` extends LanguageCode("en-GH")

    /** English (United Kingdom) */
    case object `en-GB`
        extends LanguageCode("en-GB")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** English (India) */
    case object `en-IN` extends LanguageCode("en-IN") with SupportsExperimentalModel

    /** English (Ireland) */
    case object `en-IE` extends LanguageCode("en-IE")

    /** English (Kenya) */
    case object `en-KE` extends LanguageCode("en-KE")

    /** English (New Zealand) */
    case object `en-NZ` extends LanguageCode("en-NZ")

    /** English (Nigeria) */
    case object `en-NG` extends LanguageCode("en-NG")

    /** English (Philippines) */
    case object `en-PH` extends LanguageCode("en-PH")

    /** English (South Africa) */
    case object `en-ZA` extends LanguageCode("en-ZA")

    /** English (Tanzania) */
    case object `en-TZ` extends LanguageCode("en-TZ")

    /** English (United States) */
    case object `en-US`
        extends LanguageCode("en-US")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** Spanish (Argentina) */
    case object `es-AR` extends LanguageCode("es-AR")

    /** Spanish (Bolivia) */
    case object `es-BO` extends LanguageCode("es-BO")

    /** Spanish (Chile) */
    case object `es-CL` extends LanguageCode("es-CL")

    /** Spanish (Colombia) */
    case object `es-CO` extends LanguageCode("es-CO")

    /** Spanish (Costa Rica) */
    case object `es-CR` extends LanguageCode("es-CR")

    /** Spanish (Ecuador) */
    case object `es-EC` extends LanguageCode("es-EC")

    /** Spanish (El Salvador) */
    case object `es-SV` extends LanguageCode("es-SV")

    /** Spanish (Spain) */
    case object `es-ES`
        extends LanguageCode("es-ES")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** Spanish (United States) */
    case object `es-US`
        extends LanguageCode("es-US")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** Spanish (Guatemala) */
    case object `es-GT` extends LanguageCode("es-GT")

    /** Spanish (Honduras) */
    case object `es-HN` extends LanguageCode("es-HN")

    /** Spanish (Mexico) */
    case object `es-MX` extends LanguageCode("es-MX")

    /** Spanish (Nicaragua) */
    case object `es-NI` extends LanguageCode("es-NI")

    /** Spanish (Panama) */
    case object `es-PA` extends LanguageCode("es-PA")

    /** Spanish (Paraguay) */
    case object `es-PY` extends LanguageCode("es-PY")

    /** Spanish (Peru) */
    case object `es-PE` extends LanguageCode("es-PE")

    /** Spanish (Puerto Rico) */
    case object `es-PR` extends LanguageCode("es-PR")

    /** Spanish (Dominican Republic) */
    case object `es-DO` extends LanguageCode("es-DO")

    /** Spanish (Uruguay) */
    case object `es-UY` extends LanguageCode("es-UY")

    /** Spanish (Venezuela) */
    case object `es-VE` extends LanguageCode("es-VE")

    /** Basque (Spain) */
    case object `eu-ES` extends LanguageCode("eu-ES")

    /** Filipino (Philippines) */
    case object `fil-PH` extends LanguageCode("fil-PH")

    /** French (Canada) */
    case object `fr-CA`
        extends LanguageCode("fr-CA")
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** French (France) */
    case object `fr-FR`
        extends LanguageCode("fr-FR")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** French (Switzerland) */
    case object `fr-CH` extends LanguageCode("fr-CH") with SupportsEnhancedModel

    /** Galician (Spain) */
    case object `gl-ES` extends LanguageCode("gl-ES")

    /** Georgian (Georgia) */
    case object `ka-GE` extends LanguageCode("ka-GE")

    /** Gujarati (India) */
    case object `gu-IN` extends LanguageCode("gu-IN")

    /** Croatian (Croatia) */
    case object `hr-HR` extends LanguageCode("hr-HR")

    /** Zulu (South Africa) */
    case object `zu-ZA` extends LanguageCode("zu-ZA")

    /** Icelandic (Iceland) */
    case object `is-IS` extends LanguageCode("is-IS")

    /** Italian (Italy) */
    case object `it-IT` extends LanguageCode("it-IT")

    /** Javanese (Indonesia) */
    case object `jv-ID` extends LanguageCode("jv-ID")

    /** Kannada (India) */
    case object `kn-IN` extends LanguageCode("kn-IN")

    /** Khmer (Cambodian) */
    case object `km-KH` extends LanguageCode("km-KH")

    /** Lao (Laos) */
    case object `lo-LA` extends LanguageCode("lo-LA")

    /** Latvian (Latvia) */
    case object `lv-LV` extends LanguageCode("lv-LV")

    /** Lithuanian (Lithuania) */
    case object `lt-LT` extends LanguageCode("lt-LT")

    /** Macedonian (North Macedonia) */
    case object `mk-MK` extends LanguageCode("mk-MK") with SupportsExperimentalModel

    /** Hungarian (Hungary) */
    case object `hu-HU` extends LanguageCode("hu-HU")

    /** Malayalam (India) */
    case object `ml-IN` extends LanguageCode("ml-IN")

    /** Marathi (India) */
    case object `mr-IN` extends LanguageCode("mr-IN")

    /** Dutch (Netherlands) */
    case object `nl-NL` extends LanguageCode("nl-NL") with SupportsExperimentalModel

    /** Nepali (Nepal) */
    case object `ne-NP` extends LanguageCode("ne-NP")

    /** Norwegian Bokmål (Norway) */
    case object `nb-NO` extends LanguageCode("nb-NO") with SupportsExperimentalModel

    /** Polish (Poland) */
    case object `pl-PL` extends LanguageCode("pl-PL") with SupportsExperimentalModel

    /** Portuguese (Brazil) */
    case object `pt-BR`
        extends LanguageCode("pt-BR")
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** Portuguese (Portugal) */
    case object `pt-PT` extends LanguageCode("pt-PT") with SupportsExperimentalModel

    /** Punjabi (Gurmukhi India) */
    case object `pa-guru-IN` extends LanguageCode("pa-guru-IN")

    /** Romanian (Romania) */
    case object `ro-RO` extends LanguageCode("ro-RO") with SupportsExperimentalModel

    /** Sinhala (Sri Lanka) */
    case object `si-LK` extends LanguageCode("si-LK")

    /** Slovak (Slovakia) */
    case object `sk-SK` extends LanguageCode("sk-SK")

    /** Slovenian (Slovenia) */
    case object `sl-SI` extends LanguageCode("sl-SI")

    /** Sundanese (Indonesia) */
    case object `su-ID` extends LanguageCode("su-ID")

    /** Swahili (Tanzania) */
    case object `sw-TZ` extends LanguageCode("sw-TZ")

    /** Swahili (Kenya) */
    case object `sw-KE` extends LanguageCode("sw-KE")

    /** Finnish (Finland) */
    case object `fi-FI` extends LanguageCode("fi-FI") with SupportsExperimentalModel

    /** Swedish (Sweden) */
    case object `sv-SE` extends LanguageCode("sv-SE")

    /** Tamil (India) */
    case object `ta-IN` extends LanguageCode("ta-IN")

    /** Tamil (Singapore) */
    case object `ta-SG` extends LanguageCode("ta-SG")

    /** Tamil (Sri Lanka) */
    case object `ta-LK` extends LanguageCode("ta-LK")

    /** Tamil (Malaysia) */
    case object `ta-MY` extends LanguageCode("ta-MY")

    /** Telugu (India) */
    case object `te-IN` extends LanguageCode("te-IN")

    /** Vietnamese (Vietnam) */
    case object `vi-VN` extends LanguageCode("vi-VN") with SupportsExperimentalModel

    /** Turkish (Turkey) */
    case object `tr-TR` extends LanguageCode("tr-TR") with SupportsExperimentalModel

    /** Urdu (Pakistan) */
    case object `ur-PK` extends LanguageCode("ur-PK")

    /** Urdu (India) */
    case object `ur-IN` extends LanguageCode("ur-IN")

    /** Greek (Greece) */
    case object `el-GR` extends LanguageCode("el-GR")

    /** Bulgarian (Bulgaria) */
    case object `bg-BG` extends LanguageCode("bg-BG")

    /** Russian (Russia) */
    case object `ru-RU`
        extends LanguageCode("ru-RU")
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** Serbian (Serbia) */
    case object `sr-RS` extends LanguageCode("sr-RS")

    /** Ukrainian (Ukraine) */
    case object `uk-UA` extends LanguageCode("uk-UA") with SupportsExperimentalModel

    /** Hebrew (Israel) */
    case object `he-IL` extends LanguageCode("he-IL")

    /** Arabic (Israel) */
    case object `ar-IL` extends LanguageCode("ar-IL") with SupportsExperimentalModel

    /** Arabic (Jordan) */
    case object `ar-JO` extends LanguageCode("ar-JO") with SupportsExperimentalModel

    /** Arabic (United Arab Emirates) */
    case object `ar-AE` extends LanguageCode("ar-AE") with SupportsExperimentalModel

    /** Arabic (Yemen) */
    case object `ar-YE` extends LanguageCode("ar-YE") with SupportsExperimentalModel

    /** Arabic (Bahrain) */
    case object `ar-BH` extends LanguageCode("ar-BH") with SupportsExperimentalModel

    /** Arabic (Algeria) */
    case object `ar-DZ` extends LanguageCode("ar-DZ") with SupportsExperimentalModel

    /** Arabic (Saudi Arabia) */
    case object `ar-SA` extends LanguageCode("ar-SA") with SupportsExperimentalModel

    /** Arabic (Iraq) */
    case object `ar-IQ` extends LanguageCode("ar-IQ") with SupportsExperimentalModel

    /** Arabic (Kuwait) */
    case object `ar-KW` extends LanguageCode("ar-KW") with SupportsExperimentalModel

    /** Arabic (Morocco) */
    case object `ar-MA` extends LanguageCode("ar-MA") with SupportsExperimentalModel

    /** Arabic (Tunisia) */
    case object `ar-TN` extends LanguageCode("ar-TN") with SupportsExperimentalModel

    /** Arabic (Oman) */
    case object `ar-OM` extends LanguageCode("ar-OM") with SupportsExperimentalModel

    /** Arabic (State of Palestine) */
    case object `ar-PS` extends LanguageCode("ar-PS") with SupportsExperimentalModel

    /** Arabic (Qatar) */
    case object `ar-QA` extends LanguageCode("ar-QA") with SupportsExperimentalModel

    /** Arabic (Lebanon) */
    case object `ar-LB` extends LanguageCode("ar-LB") with SupportsExperimentalModel

    /** Arabic (Mauritania) */
    case object `ar-MR` extends LanguageCode("ar-MR") with SupportsExperimentalModel

    /** Arabic (Egypt) */
    case object `ar-EG` extends LanguageCode("ar-EG") with SupportsExperimentalModel

    /** Persian (Iran) */
    case object `fa-IR` extends LanguageCode("fa-IR")

    /** Hindi (India) */
    case object `hi-IN` extends LanguageCode("hi-IN") with SupportsExperimentalModel

    /** Thai (Thailand) */
    case object `th-TH` extends LanguageCode("th-TH") with SupportsExperimentalModel

    /** Korean (South Korea) */
    case object `ko-KR` extends LanguageCode("ko-KR") with SupportsExperimentalModel

    /** "Chinese, Mandarin (Traditional, Taiwan)" */
    case object `cmn-Hant-TW` extends LanguageCode("cmn-Hant-TW")

    /** "Chinese, Cantonese (Traditional, Hong Kong)" */
    case object `yue-Hant-HK` extends LanguageCode("yue-Hant-HK")

    /** Japanese (Japan) */
    case object `ja-JP`
        extends LanguageCode("ja-JP")
        with SupportsEnhancedModel
        with SupportsExperimentalModel
        with SupportsPhoneCallModel

    /** "Chinese, Mandarin (Simplified, Hong Kong)" */
    case object `cmn-Hans-HK` extends LanguageCode("cmn-Hans-HK")

    /** "Chinese, Mandarin (Simplified, China)" */
    case object `cmn-Hans-CN` extends LanguageCode("cmn-Hans-CN")
  }

  private sealed abstract class SpeechModelType(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  private[GatherVerb] object SpeechModelType extends EnumWithTwilioString[SpeechModelType] {
    override def values: immutable.IndexedSeq[SpeechModelType] = findValues

    case object Default                   extends SpeechModelType("default")
    case object NumbersAndCommands        extends SpeechModelType("numbers_and_commands")
    case object PhoneCall                 extends SpeechModelType("phone_call")
    case object ExperimentalConversations extends SpeechModelType("experimental_conversations")
    case object ExperimentalUtterances    extends SpeechModelType("experimental_utterances")
  }

  final class Builder[
      DtmfInput <: PhantomTypes.HasDtmfInput,
      SpeechInput <: PhantomTypes.HasSpeechInput,
      DtmfInputRequired <: PhantomTypes.DtmfInputRequired,
      SpeechInputRequired <: PhantomTypes.SpeechInputRequired,
      ActionHasBeenSet <: PhantomTypes.ActionHasBeenSet,
      LanguageHasBeenSet <: PhantomTypes.LanguageHasBeenSet,
      ValidForVerified <: PhantomTypes.ValidForVerified
  ] private[GatherVerb] (
      nestedVerbs: Vector[TwimlElement.Verb] = Vector.empty,
      action: Option[CallbackUrl] = None,
      // Double option, as the value itself is actually an option
      finishOnKey: Option[Option[DtmfDigit]] = None,
      hints: Vector[String] = Vector.empty,
      input: Option[String] = None,
      language: Option[LanguageCode] = None,
      method: Option[HttpMethod] = None,
      numDigits: Option[Int] = None,
      partialResultCallback: Option[CallbackUrl] = None,
      profanityFilter: Option[Boolean] = None,
      speechTimeout: Option[PositiveInteger] = None,
      timeout: Option[PositiveInteger] = None,
      speechModelType: Option[SpeechModelType] = None,
      enhanced: Option[Boolean] = None,
      actionOnEmptyResult: Option[Boolean] = None
  ) {

    @nowarn(value = "cat=unused")
    private type BuilderWithSameTypes =
      Builder[
        DtmfInput,
        SpeechInput,
        DtmfInputRequired,
        SpeechInputRequired,
        ActionHasBeenSet,
        LanguageHasBeenSet,
        ValidForVerified
      ]

    private def copy[
        DtmfInput2 <: PhantomTypes.HasDtmfInput,
        SpeechInput2 <: PhantomTypes.HasSpeechInput,
        DtmfInputRequired2 <: PhantomTypes.DtmfInputRequired,
        SpeechInputRequired2 <: PhantomTypes.SpeechInputRequired,
        ActionHasBeenSet2 <: PhantomTypes.ActionHasBeenSet,
        LanguageHasBeenSet2 <: PhantomTypes.LanguageHasBeenSet,
        Verified2 <: PhantomTypes.ValidForVerified
    ](
        nestedVerbs: Vector[TwimlElement.Verb] = this.nestedVerbs,
        action: Option[CallbackUrl] = this.action,
        finishOnKey: Option[Option[DtmfDigit]] = this.finishOnKey,
        hints: Vector[String] = this.hints,
        input: Option[String] = this.input,
        language: Option[LanguageCode] = this.language,
        method: Option[HttpMethod] = this.method,
        numDigits: Option[Int] = this.numDigits,
        partialResultCallback: Option[CallbackUrl] = this.partialResultCallback,
        profanityFilter: Option[Boolean] = this.profanityFilter,
        speechTimeout: Option[PositiveInteger] = this.speechTimeout,
        timeout: Option[PositiveInteger] = this.timeout,
        speechModelType: Option[SpeechModelType] = this.speechModelType,
        enhanced: Option[Boolean] = this.enhanced,
        actionOnEmptyResult: Option[Boolean] = this.actionOnEmptyResult
    ) = new Builder[
      DtmfInput2,
      SpeechInput2,
      DtmfInputRequired2,
      SpeechInputRequired2,
      ActionHasBeenSet2,
      LanguageHasBeenSet2,
      Verified2
    ](
      nestedVerbs,
      action,
      finishOnKey,
      hints,
      input,
      language,
      method,
      numDigits,
      partialResultCallback,
      profanityFilter,
      speechTimeout,
      timeout,
      speechModelType,
      enhanced,
      actionOnEmptyResult
    )

    /** Add a nested Pause verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/pause
      */
    def addPause(
        fun: PauseVerb.BuildFunction
    ): BuilderWithSameTypes =
      copy(nestedVerbs = nestedVerbs :+ PauseVerb.build(fun))

    /** Add a nested Play verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/play
      */
    def addPlay(
        fun: PlayVerb.BuildFunction
    ): BuilderWithSameTypes =
      copy(nestedVerbs = nestedVerbs :+ PlayVerb.build(fun))

    /** Add a nested Say verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/say
      */
    def addSay(
        fun: SayVerb.BuildFunction
    ): BuilderWithSameTypes =
      copy(nestedVerbs = nestedVerbs :+ SayVerb.build(fun))

    /** Sets the action attribute
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#action
      */
    def withAction(
        callbackUrl: CallbackUrl
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      SpeechInputRequired,
      PhantomTypes.ActionHasBeenSetTrue,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(action = Some(callbackUrl))

    /** Sets the finishOnKey attribute.
      *
      * The default is '#' so not setting it at all would be the same as setting it to
      * `Some(DtmfDiget.#)`. Setting it to None correspond to setting it to empty String, and that
      * corresponds to no key ending the gather, and that will result in the gather only being ended
      * by timeout.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#finishonkey
      */
    @nowarn(value = "cat=unused-params")
    def withFinishOnKey(finishOnKey: Option[DtmfDigit])(
        implicit ev: DtmfInput =:= PhantomTypes.HasDtmfInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      PhantomTypes.DtmfInputRequiredTrue,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(finishOnKey = Some(finishOnKey))

    /** Add a hint to the hint attribute.
      *
      * Can be called multiple time, however twilio does have some constraints on how many hints you
      * can add, and how many chars a hint max can consist off. The compiler cannot help you with
      * that, so you would need to look that up in Twilio documentation (link below), and make sure
      * you obey to these rules.
      *
      * Hints are only used for speech, and as such is only allowed to be added, if the input
      * attribute includes speech.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#hints
      */
    @nowarn(value = "cat=unused-params")
    def addHint(hint: String)(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(hints = this.hints :+ hint)

    /** Sets the input attribute value to: dtmf
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    @nowarn(value = "cat=unused-params")
    def withInputDtmf()(
        implicit ev: SpeechInputRequired =:= PhantomTypes.SpeechInputRequiredFalse
    ): Builder[
      PhantomTypes.HasDtmfInputTrue,
      PhantomTypes.HasSpeechInputFalse,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(input = Some("dtmf"))

    /** Sets the input attribute value to: speech
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    @nowarn(value = "cat=unused-params")
    def withInputSpeech()(
        implicit ev: DtmfInputRequired =:= PhantomTypes.DtmfInputRequiredFalse
    ): Builder[
      PhantomTypes.HasDtmfInputFalse,
      PhantomTypes.HasSpeechInputTrue,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(input = Some("speech"))

    /** Sets the input attribute value to: dtmf speech
      *
      * Some attributes only make sense to set, if input has a specific value, that is why there is
      * a set method for each possible value of input, so that we can change the Phantom types of
      * the builder accordingly, and enforce these constraints compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#input
      */
    def withInputDtmfSpeech(): Builder[
      PhantomTypes.HasDtmfInputTrue,
      PhantomTypes.HasSpeechInputTrue,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(input = Some("dtmf speech"))

    /** Set the language attribute.
      *
      * Supported languages are mapped in an enum, so we can guaranty valid values compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#language
      */
    @nowarn(value = "cat=unused-params")
    def withLanguage(language: LanguageCode)(
        implicit ev: LanguageHasBeenSet =:= PhantomTypes.LanguageHasBeenSetFalse
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      PhantomTypes.LanguageHasBeenSetTrue,
      ValidForVerified
    ] = copy(language = Some(language))

    /** Sets the method attribute.
      *
      * Can only be set if you have set an Action URL, as it only has an effect on callbacks to that
      * URL.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#method
      */
    @nowarn(value = "cat=unused-params")
    def withMethod(method: HttpMethod)(
        implicit ev: ActionHasBeenSet =:= PhantomTypes.ActionHasBeenSetTrue
    ): BuilderWithSameTypes = copy(method = Some(method))

    /** Set the numDigits attribute
      *
      * Can only be set if input includes DTMF
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#numdigits
      */
    @nowarn(value = "cat=unused-params")
    def withNumDigits(numDigits: Int)(
        implicit ev: DtmfInput =:= PhantomTypes.HasDtmfInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      PhantomTypes.DtmfInputRequiredTrue,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(numDigits = Some(numDigits))

    /** Sets the partialResultCallback attribute
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#partialresultcallback
      */
    def withPartialResultCallback(callbackUrl: CallbackUrl): BuilderWithSameTypes =
      copy(partialResultCallback = Some(callbackUrl))

    /** Set the profanityFilter attribute.
      *
      * Not allowed to be set, if input don't include speech, as this only has effect on speech.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#profanityfilter
      */
    @nowarn(value = "cat=unused-params")
    def withProfanityFilter(bool: Boolean)(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(profanityFilter = Some(bool))

    /** Set the speechTimeout attribute.
      *
      * You cannot set this attribute, if the input attribute do not include speech.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechtimeout
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechTimeout(positiveInteger: PositiveInteger)(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(speechTimeout = Some(positiveInteger))

    /** Set the timeout attribute.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#timeout
      */
    def withTimeout(positiveInteger: PositiveInteger): BuilderWithSameTypes =
      copy(timeout = Some(positiveInteger))

    /** Set the speechModel attribute to default.
      *
      * This will require speech to be part of the input attribute.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelDefault()(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(speechModelType = Some(SpeechModelType.Default))

    /** Set the speechModel attribute to numbers_and_commands.
      *
      * This will require speech to be part of the input attribute.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelNumbersAndCommands()(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      ValidForVerified
    ] = copy(speechModelType = Some(SpeechModelType.NumbersAndCommands))

    /** Set the speechModel attribute to phone_call.
      *
      * This will require speech to be part of the input attribute.
      *
      * This model only support a very limited numbers of languages, and for this reason you need to
      * provide the language, and is not allowed to call the withLanguage() method if you call this
      * one, so that we can enforce this constraint compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelPhoneCall(language: LanguageCode with LanguageCode.SupportsPhoneCallModel)(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue,
        ev2: LanguageHasBeenSet =:= PhantomTypes.LanguageHasBeenSetFalse
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      PhantomTypes.LanguageHasBeenSetTrue,
      ValidForVerified
    ] = copy(speechModelType = Some(SpeechModelType.PhoneCall), language = Some(language))

    /** Set the speechModel attribute to phone_call + the enhanced attribute to true
      *
      * This will require speech to be part of the input attribute.
      *
      * This model only support a very limited numbers of languages, and for this reason you need to
      * provide the language, and is not allowed to call the withLanguage() method if you call this
      * one, so that we can enforce this constraint compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#enhanced
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelPhoneCallPlusEnhanced(
        language: LanguageCode
          with LanguageCode.SupportsPhoneCallModel
          with LanguageCode.SupportsEnhancedModel
    )(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue,
        ev2: LanguageHasBeenSet =:= PhantomTypes.LanguageHasBeenSetFalse
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      PhantomTypes.LanguageHasBeenSetTrue,
      ValidForVerified
    ] = copy(
      speechModelType = Some(SpeechModelType.PhoneCall),
      language = Some(language),
      enhanced = Some(true)
    )

    /** Set the speechModel attribute to experimental_conversations.
      *
      * This will require speech to be part of the input attribute.
      *
      * This model only support a very limited numbers of languages, and for this reason you need to
      * provide the language, and is not allowed to call the withLanguage() method if you call this
      * one, so that we can enforce this constraint compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelExperimentalConversation(
        language: LanguageCode with LanguageCode.SupportsExperimentalModel
    )(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue,
        ev2: LanguageHasBeenSet =:= PhantomTypes.LanguageHasBeenSetFalse
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      PhantomTypes.LanguageHasBeenSetTrue,
      ValidForVerified
    ] = copy(
      speechModelType = Some(SpeechModelType.ExperimentalConversations),
      language = Some(language)
    )

    /** Set the speechModel attribute to experimental_utterances.
      *
      * This will require speech to be part of the input attribute.
      *
      * This model only support a very limited numbers of languages, and for this reason you need to
      * provide the language, and is not allowed to call the withLanguage() method if you call this
      * one, so that we can enforce this constraint compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#speechmodel
      */
    @nowarn(value = "cat=unused-params")
    def withSpeechModelExperimentalUtterances(
        language: LanguageCode with LanguageCode.SupportsExperimentalModel
    )(
        implicit ev: SpeechInput =:= PhantomTypes.HasSpeechInputTrue,
        ev2: LanguageHasBeenSet =:= PhantomTypes.LanguageHasBeenSetFalse
    ): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      PhantomTypes.SpeechInputRequiredTrue,
      ActionHasBeenSet,
      PhantomTypes.LanguageHasBeenSetTrue,
      ValidForVerified
    ] = copy(
      speechModelType = Some(SpeechModelType.ExperimentalUtterances),
      language = Some(language)
    )

    /** Set the actionOnEmptyResult attribute.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#actiononemptyresult
      */
    def withActionOnEmptyResult(bool: Boolean): BuilderWithSameTypes =
      copy(actionOnEmptyResult = Some(bool))

    def addCustomVerb(verb: TwimlElement.Verb): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      PhantomTypes.ValidForVerifiedFalse
    ] = copy(nestedVerbs = nestedVerbs :+ verb)

    def addCustomVerbs(verbs: Seq[TwimlElement.Verb]): Builder[
      DtmfInput,
      SpeechInput,
      DtmfInputRequired,
      SpeechInputRequired,
      ActionHasBeenSet,
      LanguageHasBeenSet,
      PhantomTypes.ValidForVerifiedFalse
    ] = copy(nestedVerbs = nestedVerbs ++ verbs)

    /** Build a [[GatherVerb.Verified]] instance from this builder.
      *
      * You can only call this if you have not called [[addCustomVerb]] or [[addCustomVerbs]]. In
      * such case you must call [[buildUnverified]] instead.
      */
    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: ValidForVerified =:= PhantomTypes.ValidForVerifiedTrue
    ): GatherVerb.Verified =
      new GatherVerbImpl(
        nestedVerbs,
        action,
        finishOnKey,
        hints,
        input,
        language,
        method,
        numDigits,
        partialResultCallback,
        profanityFilter,
        speechTimeout,
        timeout,
        speechModelType,
        enhanced,
        actionOnEmptyResult
      ).toVerified

    /** Build a [[GatherVerb.Unverified]] instance from this builder.
      *
      * You can only call this, if you have called either [[addCustomVerb]] or [[addCustomVerbs]].
      * If not you should call [[build]] instead.
      */
    @nowarn(value = "cat=unused-params")
    def buildUnverified()(
        implicit ev: ValidForVerified =:= PhantomTypes.ValidForVerifiedFalse
    ): GatherVerb.Unverified =
      new GatherVerbImpl(
        nestedVerbs,
        action,
        finishOnKey,
        hints,
        input,
        language,
        method,
        numDigits,
        partialResultCallback,
        profanityFilter,
        speechTimeout,
        timeout,
        speechModelType,
        enhanced,
        actionOnEmptyResult
      ).toUnverified
  }

  // Dtmf is default input, so set HasDtmfInputTrue to begin with.
  type BuilderStartState = Builder[
    PhantomTypes.HasDtmfInputTrue,
    PhantomTypes.HasSpeechInputFalse,
    PhantomTypes.DtmfInputRequiredFalse,
    PhantomTypes.SpeechInputRequiredFalse,
    PhantomTypes.ActionHasBeenSetFalse,
    PhantomTypes.LanguageHasBeenSetFalse,
    PhantomTypes.ValidForVerifiedTrue
  ]
  type BuildFunction           = BuilderStartState => GatherVerb.Verified
  type BuildFunctionUnverified = BuilderStartState => GatherVerb.Unverified

  def build(fun: BuildFunction): GatherVerb.Verified = fun(new BuilderStartState())

  def build(fun: BuildFunctionUnverified): GatherVerb.Unverified = fun(new BuilderStartState)

  private class GatherVerbImpl(
      nestedVerbs: immutable.Seq[TwimlElement.Verb],
      action: Option[CallbackUrl],
      finishOnKey: Option[Option[DtmfDigit]],
      hints: Seq[String],
      input: Option[String],
      language: Option[LanguageCode],
      method: Option[HttpMethod],
      numDigits: Option[Int],
      partialResultCallback: Option[CallbackUrl],
      profanityFilter: Option[Boolean],
      speechTimeout: Option[PositiveInteger],
      timeout: Option[PositiveInteger],
      speechModelType: Option[SpeechModelType],
      enhanced: Option[Boolean],
      actionOnEmptyResult: Option[Boolean]
  ) {

    private val attributes = new TagAttributeBuilder()
      .add("action", action)
      .addString("finishOnKey", finishOnKey.map(_.map(_.twilioString).getOrElse("")))
      .addString("hints", if (hints.isEmpty) None else Some(hints.mkString(", ")))
      .addString("input", input)
      .add("language", language)
      .add("method", method)
      .addInt("numDigits", numDigits)
      .add("partialResultCallback", partialResultCallback)
      .addBoolean("profanityFilter", profanityFilter)
      .add("speechTimeout", speechTimeout)
      .add("timeout", timeout)
      .add("speechModel", speechModelType)
      .addBoolean("enhanced", enhanced)
      .addBoolean("actionOnEmptyResult", actionOnEmptyResult)
      .build

    def toVerified: Verified     = VerifiedImpl(attributes, nestedVerbs)
    def toUnverified: Unverified = UnverifiedImpl(attributes, nestedVerbs)
  }

  private final case class VerifiedImpl(
      override protected val tagAttributes: immutable.Seq[(String, String)],
      override protected val tagSubElements: immutable.Seq[TwimlElement],
  ) extends Verified
  private final case class UnverifiedImpl(
      override protected val tagAttributes: immutable.Seq[(String, String)],
      override protected val tagSubElements: immutable.Seq[TwimlElement],
  ) extends Unverified
}
