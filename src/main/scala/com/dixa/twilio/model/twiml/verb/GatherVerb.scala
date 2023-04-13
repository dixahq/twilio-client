package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod, StringUtil}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfDigit
import com.dixa.twilio.model.twiml.TwimlElement

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
sealed trait GatherVerb extends TwimlElement.Verb

object GatherVerb {

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
  }

  /** Enum entry, representing a Language code that the Gather verb support */
  sealed abstract class LanguageCode(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Language codes that the Gather verb support */
  // noinspection ScalaUnusedSymbol
  object LanguageCode extends EnumWithTwilioString[LanguageCode] {
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
    case object `da-DK` extends LanguageCode("da-DK")

    /** German (Germany) */
    case object `de-DE` extends LanguageCode("de-DE")

    /** English (Australia) */
    case object `en-AU` extends LanguageCode("en-AU")

    /** English (Canada) */
    case object `en-CA` extends LanguageCode("en-CA")

    /** English (Ghana) */
    case object `en-GH` extends LanguageCode("en-GH")

    /** English (United Kingdom) */
    case object `en-GB` extends LanguageCode("en-GB")

    /** English (India) */
    case object `en-IN` extends LanguageCode("en-IN")

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
    case object `en-US` extends LanguageCode("en-US")

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
    case object `es-ES` extends LanguageCode("es-ES")

    /** Spanish (United States) */
    case object `es-US` extends LanguageCode("es-US")

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
    case object `fr-CA` extends LanguageCode("fr-CA")

    /** French (France) */
    case object `fr-FR` extends LanguageCode("fr-FR")

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

    /** Hungarian (Hungary) */
    case object `hu-HU` extends LanguageCode("hu-HU")

    /** Malayalam (India) */
    case object `ml-IN` extends LanguageCode("ml-IN")

    /** Marathi (India) */
    case object `mr-IN` extends LanguageCode("mr-IN")

    /** Dutch (Netherlands) */
    case object `nl-NL` extends LanguageCode("nl-NL")

    /** Nepali (Nepal) */
    case object `ne-NP` extends LanguageCode("ne-NP")

    /** Norwegian Bokmål (Norway) */
    case object `nb-NO` extends LanguageCode("nb-NO")

    /** Polish (Poland) */
    case object `pl-PL` extends LanguageCode("pl-PL")

    /** Portuguese (Brazil) */
    case object `pt-BR` extends LanguageCode("pt-BR")

    /** Portuguese (Portugal) */
    case object `pt-PT` extends LanguageCode("pt-PT")

    /** Romanian (Romania) */
    case object `ro-RO` extends LanguageCode("ro-RO")

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
    case object `fi-FI` extends LanguageCode("fi-FI")

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
    case object `vi-VN` extends LanguageCode("vi-VN")

    /** Turkish (Turkey) */
    case object `tr-TR` extends LanguageCode("tr-TR")

    /** Urdu (Pakistan) */
    case object `ur-PK` extends LanguageCode("ur-PK")

    /** Urdu (India) */
    case object `ur-IN` extends LanguageCode("ur-IN")

    /** Greek (Greece) */
    case object `el-GR` extends LanguageCode("el-GR")

    /** Bulgarian (Bulgaria) */
    case object `bg-BG` extends LanguageCode("bg-BG")

    /** Russian (Russia) */
    case object `ru-RU` extends LanguageCode("ru-RU")

    /** Serbian (Serbia) */
    case object `sr-RS` extends LanguageCode("sr-RS")

    /** Ukrainian (Ukraine) */
    case object `uk-UA` extends LanguageCode("uk-UA")

    /** Hebrew (Israel) */
    case object `he-IL` extends LanguageCode("he-IL")

    /** Arabic (Israel) */
    case object `ar-IL` extends LanguageCode("ar-IL")

    /** Arabic (Jordan) */
    case object `ar-JO` extends LanguageCode("ar-JO")

    /** Arabic (United Arab Emirates) */
    case object `ar-AE` extends LanguageCode("ar-AE")

    /** Arabic (Bahrain) */
    case object `ar-BH` extends LanguageCode("ar-BH")

    /** Arabic (Algeria) */
    case object `ar-DZ` extends LanguageCode("ar-DZ")

    /** Arabic (Saudi Arabia) */
    case object `ar-SA` extends LanguageCode("ar-SA")

    /** Arabic (Iraq) */
    case object `ar-IQ` extends LanguageCode("ar-IQ")

    /** Arabic (Kuwait) */
    case object `ar-KW` extends LanguageCode("ar-KW")

    /** Arabic (Morocco) */
    case object `ar-MA` extends LanguageCode("ar-MA")

    /** Arabic (Tunisia) */
    case object `ar-TN` extends LanguageCode("ar-TN")

    /** Arabic (Oman) */
    case object `ar-OM` extends LanguageCode("ar-OM")

    /** Arabic (State of Palestine) */
    case object `ar-PS` extends LanguageCode("ar-PS")

    /** Arabic (Qatar) */
    case object `ar-QA` extends LanguageCode("ar-QA")

    /** Arabic (Lebanon) */
    case object `ar-LB` extends LanguageCode("ar-LB")

    /** Arabic (Egypt) */
    case object `ar-EG` extends LanguageCode("ar-EG")

    /** Persian (Iran) */
    case object `fa-IR` extends LanguageCode("fa-IR")

    /** Hindi (India) */
    case object `hi-IN` extends LanguageCode("hi-IN")

    /** Thai (Thailand) */
    case object `th-TH` extends LanguageCode("th-TH")

    /** Korean (South Korea) */
    case object `ko-KR` extends LanguageCode("ko-KR")

    /** "Chinese, Mandarin (Traditional, Taiwan)" */
    case object `cmn-Hant-TW` extends LanguageCode("cmn-Hant-TW")

    /** "Chinese, Cantonese (Traditional, Hong Kong)" */
    case object `yue-Hant-HK` extends LanguageCode("yue-Hant-HK")

    /** Japanese (Japan) */
    case object `ja-JP` extends LanguageCode("ja-JP")

    /** "Chinese, Mandarin (Simplified, Hong Kong)" */
    case object `cmn-Hans-HK` extends LanguageCode("cmn-Hans-HK")

    /** "Chinese, Mandarin (Simplified, China)" */
    case object `cmn-Hans-CN` extends LanguageCode("cmn-Hans-CN")
  }

  final class Builder[
      DtmfInput <: PhantomTypes.HasDtmfInput,
      SpeechInput <: PhantomTypes.HasSpeechInput,
      DtmfInputRequired <: PhantomTypes.DtmfInputRequired,
      SpeechInputRequired <: PhantomTypes.SpeechInputRequired,
      ActionHasBeenSet <: PhantomTypes.ActionHasBeenSet
  ] private[GatherVerb] (
      nestedVerbs: Vector[TwimlElement.Verb] = Vector.empty,
      action: Option[CallbackUrl] = None,
      // Double option, as the value itself is actually an option
      finishOnKey: Option[Option[DtmfDigit]] = None,
      hints: Vector[String] = Vector.empty,
      input: Option[String] = None,
      language: Option[LanguageCode] = None,
      method: Option[HttpMethod] = None
  ) {

    @nowarn(value = "cat=unused")
    private type BuilderWithSameTypes =
      Builder[DtmfInput, SpeechInput, DtmfInputRequired, SpeechInputRequired, ActionHasBeenSet]

    private def copy[
        DtmfInput2 <: PhantomTypes.HasDtmfInput,
        SpeechInput2 <: PhantomTypes.HasSpeechInput,
        DtmfInputRequired2 <: PhantomTypes.DtmfInputRequired,
        SpeechInputRequired2 <: PhantomTypes.SpeechInputRequired,
        ActionHasBeenSet2 <: PhantomTypes.ActionHasBeenSet
    ](
        nestedVerbs: Vector[TwimlElement.Verb] = this.nestedVerbs,
        action: Option[CallbackUrl] = this.action,
        finishOnKey: Option[Option[DtmfDigit]] = this.finishOnKey,
        hints: Vector[String] = this.hints,
        input: Option[String] = this.input,
        language: Option[LanguageCode] = this.language,
        method: Option[HttpMethod] = this.method
    ) = new Builder[
      DtmfInput2,
      SpeechInput2,
      DtmfInputRequired2,
      SpeechInputRequired2,
      ActionHasBeenSet2
    ](
      nestedVerbs,
      action,
      finishOnKey,
      hints,
      input,
      language,
      method
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
      PhantomTypes.ActionHasBeenSetTrue
    ] =
      copy(action = Some(callbackUrl))

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
      ActionHasBeenSet
    ] =
      copy(finishOnKey = Some(finishOnKey))

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
      ActionHasBeenSet
    ] =
      copy(hints = this.hints :+ hint)

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
      ActionHasBeenSet
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
      ActionHasBeenSet
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
      ActionHasBeenSet
    ] = copy(input = Some("dtmf speech"))

    /** Set the language attribute.
      *
      * Supported languages are mapped in an enum, so we can guaranty valid values compile time.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/gather#language
      */
    def withLanguage(
        language: LanguageCode
    ): BuilderWithSameTypes =
      copy(language = Some(language))

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

    def build(): GatherVerb =
      GatherVerbImpl(nestedVerbs, action, finishOnKey, hints, input, language, method)
  }

  // Dtmf is default input, so set HasDtmfInputTrue to begin with.
  type BuilderStartState = Builder[
    PhantomTypes.HasDtmfInputTrue,
    PhantomTypes.HasSpeechInputFalse,
    PhantomTypes.DtmfInputRequiredFalse,
    PhantomTypes.SpeechInputRequiredFalse,
    PhantomTypes.ActionHasBeenSetFalse
  ]
  type BuildFunction = BuilderStartState => GatherVerb

  def build(fun: BuildFunction): GatherVerb = fun(
    new BuilderStartState()
  )

  private final case class GatherVerbImpl(
      nestedVerbs: Seq[TwimlElement.Verb],
      action: Option[CallbackUrl],
      finishOnKey: Option[Option[DtmfDigit]],
      hints: Seq[String],
      input: Option[String],
      language: Option[LanguageCode],
      method: Option[HttpMethod]
  ) extends GatherVerb {

    private val actionAttribute = action.map(x => s""" action="${x.twilioString}"""").getOrElse("")
    private val finishOnKeyAttribute = finishOnKey
      .map(x => s""" finishOnKey="${x.map(_.twilioString).getOrElse("")}"""")
      .getOrElse("")
    private val hintsAttribute = if (hints.isEmpty) "" else s""" hints="${hints.mkString(", ")}""""
    private val inputAttribute = input.map(x => s""" input="$x"""").getOrElse("")
    private val languageAttribute =
      language.map(x => s""" language="${x.twilioString}"""").getOrElse("")
    private val methodAttribute = method.map(x => s""" method="${x.twilioString}"""").getOrElse("")
    private val gatherStart =
      s"""<Gather$actionAttribute$finishOnKeyAttribute$hintsAttribute$inputAttribute$languageAttribute$methodAttribute"""

    override def xmlCompact: String = {
      if (nestedVerbs.isEmpty) s"""$gatherStart/>"""
      else s"""$gatherStart>${nestedVerbs.map(_.xmlCompact).mkString}</Gather>"""
    }

    override def xmlPretty: String = if (nestedVerbs.isEmpty) s"""$gatherStart />"""
    else {
      val verbsAsXmlList = nestedVerbs.map(v => StringUtil.indentEveryLineWith2Spaces(v.xmlPretty))
      s"""$gatherStart>
         |${verbsAsXmlList.mkString(System.lineSeparator())}
         |</Gather>
         |""".stripMargin
    }
  }
}
