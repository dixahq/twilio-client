package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.collection.immutable

/** Representation of the Say Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait SayVerb extends TwimlElement.Verb {
  override final protected val tagName: String = "Say"

  override final protected val tagSubElements: immutable.Seq[TwimlElement] = Nil
}

object SayVerb {

  /** Enum entry, representing a Language code that the Say verb supports */
  sealed abstract class LanguageCode(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Language codes that the Say verb supports */
  // noinspection ScalaUnusedSymbol
  object LanguageCode extends EnumWithTwilioString[LanguageCode] {

    val values: immutable.IndexedSeq[LanguageCode] = findValues

    /** Arabic */
    case object `arb` extends LanguageCode("arb") with SupportsPollyZeina

    /** Arabic (Gulf) */
    case object `ar-AE` extends LanguageCode("ar-AE") with SupportsPollyHalaNeural

    /** Chinese (Cantonese) */
    case object `yue-CN` extends LanguageCode("yue-CN") with SupportsPollyHiujinNeural

    /** Chinese (Mandarin) */
    case object `cmn-CN`
        extends LanguageCode("cmn-CN")
        with SupportsPollyZhiyu
        with SupportsPollyZhiyuNeural

    /** "Danish, Denmark" */
    case object `da-DK` extends LanguageCode("da-DK") with SupportsPollyMads with SupportsPollyNaja

    /** Dutch */
    case object `nl-NL`
        extends LanguageCode("nl-NL")
        with SupportsPollyLotte
        with SupportsPollyRuben
        with SupportsPollyLauraNeural

    /** "English, Australia" */
    case object `en-AU`
        extends LanguageCode("en-AU")
        with SupportsPollyNicole
        with SupportsPollyRussell
        with SupportsPollyOliviaNeural

    // TODO not sure if this is an option in Twilio anymore
    /** "English, Canada" */
    case object `en-CA` extends LanguageCode("en-CA") with SupportsWomanVoice

    /** "English, UK" */
    case object `en-GB`
        extends LanguageCode("en-GB")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyAmy
        with SupportsPollyBrian
        with SupportsPollyEmma
        with SupportsPollyAmyNeural
        with SupportsPollyEmmaNeural
        with SupportsPollyBrianNeural
        with SupportsPollyArthurNeural

    /** "English, India" */
    case object `en-IN`
        extends LanguageCode("en-IN")
        with SupportsPollyAditi
        with SupportsPollyRaveena
        with SupportsPollyKajalNeural

    /** "English, New Zealand" */
    case object `en-NZ` extends LanguageCode("en-NZ") with SupportsPollyAriaNeural

    /** "English, United States" */
    case object `en-US`
        extends LanguageCode("en-US")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyIvy
        with SupportsPollyJoanna
        with SupportsPollyJoey
        with SupportsPollyJustin
        with SupportsPollyKendra
        with SupportsPollyKimberly
        with SupportsPollyMatthew
        with SupportsPollySalli
        with SupportsPollyIvyNeural
        with SupportsPollyJoannaNeural
        with SupportsPollyKendraNeural
        with SupportsPollyKimberlyNeural
        with SupportsPollySalliNeural
        with SupportsPollyJoeyNeural
        with SupportsPollyJustinNeural
        with SupportsPollyMatthewNeural
        with SupportsPollyRuthNeural
        with SupportsPollyStephenNeural

    /** "English, South African" */
    case object `en-ZA` extends LanguageCode("en-ZA") with SupportsPollyAyandaNeural

    /** "English, Welsh" */
    case object `en-GB-WLS` extends LanguageCode("en-GB-WLS") with SupportsPollyGeraint

    /** "Welsh" */
    case object `cy-GB` extends LanguageCode("cy-GB") with SupportsPollyGwyneth

    /** "Finnish, Finland" */
    case object `fi-FI` extends LanguageCode("fi-FI") with SupportsPollySuviNeural

    /** "French, France" */
    case object `fr-FR`
        extends LanguageCode("fr-FR")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyCeline
        with SupportsPollyLea
        with SupportsPollyMathieu
        with SupportsPollyLeaNeural
        with SupportsPollyRemiNeural

    /** "French, Canada" */
    case object `fr-CA`
        extends LanguageCode("fr-CA")
        with SupportsPollyChantal
        with SupportsPollyGabrielleNeural
        with SupportsPollyLiamNeural

    /** "German, Germany" */
    case object `de-DE`
        extends LanguageCode("de-DE")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyHans
        with SupportsPollyMarlene
        with SupportsPollyVicki
        with SupportsPollyVickiNeural
        with SupportsPollyDanielNeural

    /** "German, Austrian" */
    case object `de-AT` extends LanguageCode("de-AT") with SupportsPollyHannahNeural

    /** "Hindi, India" */
    case object `hi-IN`
        extends LanguageCode("hi-IN")
        with SupportsPollyAditi
        with SupportsPollyKajalNeural

    /** "Icelandic, Iceland" */
    case object `is-IS` extends LanguageCode("is-IS") with SupportsPollyDora with SupportsPollyKarl

    /** "Italian, Italy" */
    case object `it-IT`
        extends LanguageCode("it-IT")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyBianca
        with SupportsPollyCarla
        with SupportsPollyGiorgio
        with SupportsPollyBiancaNeural
        with SupportsPollyAdrianoNeural

    /** "Catalan, Spain" */
    case object `ca-ES` extends LanguageCode("ca-ES") with SupportsPollyArletNeural

    /** "Spanish, Spain" */
    case object `es-ES`
        extends LanguageCode("es-ES")
        with SupportsWomanVoice
        with SupportsManVoice
        with SupportsPollyConchita
        with SupportsPollyEnrique
        with SupportsPollyLucia
        with SupportsPollyLuciaNeural
        with SupportsPollySergioNeural

    /** "Spanish, Mexico" */
    case object `es-MX`
        extends LanguageCode("es-MX")
        with SupportsWomanVoice
        with SupportsPollyMia
        with SupportsPollyMiaNeural
        with SupportsPollyAndresNeural

    /** "Spanish, USA" */
    case object `es-US`
        extends LanguageCode("es-US")
        with SupportsPollyLupe
        with SupportsPollyMiguel
        with SupportsPollyPenelope
        with SupportsPollyLupeNeural
        with SupportsPollyPedroNeural

    /** "Japanese, Japan" */
    case object `ja-JP`
        extends LanguageCode("ja-JP")
        with SupportsPollyMizuki
        with SupportsPollyTakumi
        with SupportsPollyTakumiNeural
        with SupportsPollyKazuhaNeural
        with SupportsPollyTomokoNeural

    /** "Korean, Korea" */
    case object `ko-KR`
        extends LanguageCode("ko-KR")
        with SupportsPollySeoyeon
        with SupportsPollySeoyeonNeural

    /** "Norwegian, Norway" */
    case object `nb-NO`
        extends LanguageCode("nb-NO")
        with SupportsPollyLiv
        with SupportsPollyIdaNeural

    /** "Polish -Poland" */
    case object `pl-PL`
        extends LanguageCode("pl-PL")
        with SupportsPollyJacek
        with SupportsPollyJan
        with SupportsPollyEwa
        with SupportsPollyMaja
        with SupportsPollyOlaNeural

    /** "Portuguese, Brazil" */
    case object `pt-BR`
        extends LanguageCode("pt-BR")
        with SupportsPollyCamila
        with SupportsPollyRicardo
        with SupportsPollyVitoria
        with SupportsPollyCamilaNeural
        with SupportsPollyVitoriaNeural
        with SupportsPollyThiagoNeural

    /** "Portuguese, Portugal" */
    case object `pt-PT`
        extends LanguageCode("pt-PT")
        with SupportsPollyCristiano
        with SupportsPollyInes
        with SupportsPollyInesNeural

    /** "Romanian, Romania" */
    case object `ro-RO` extends LanguageCode("ro-RO") with SupportsPollyCarmen

    /** "Russian, Russia" */
    case object `ru-RU`
        extends LanguageCode("ru-RU")
        with SupportsPollyMaxim
        with SupportsPollyTatyana

    /** "Swedish, Sweden" */
    case object `sv-SE`
        extends LanguageCode("sv-SE")
        with SupportsPollyAstrid
        with SupportsPollyElinNeural

    /** "Turkish, Turkey" */
    case object `tr-TR` extends LanguageCode("tr-TR") with SupportsPollyFiliz

    /** "Chinese (Mandarin)" */
    case object `zh-CN` extends LanguageCode("zh-CN") with SupportsPollyZhiyu

    /** "Chinese (Cantonese)" */
    case object `zh-HK` extends LanguageCode("zh-HK") with SupportsPollyHiujinNeural

    /** "Chinese (Taiwanese Mandarin) - COMING SOON" */
//    case object `zh-TW`
//        extends LanguageCode("zh-TW")
//        with

    sealed trait SupportsManVoice
    sealed trait SupportsWomanVoice
    sealed trait SupportsPollyZeina
    sealed trait SupportsPollyHalaNeural
    sealed trait SupportsPollyArletNeural
    sealed trait SupportsPollyHiujinNeural
    sealed trait SupportsPollyZhiyu
    sealed trait SupportsPollyZhiyuNeural
    sealed trait SupportsPollyMads
    sealed trait SupportsPollyNaja
    sealed trait SupportsPollyLotte
    sealed trait SupportsPollyRuben
    sealed trait SupportsPollyLauraNeural
    sealed trait SupportsPollyNicole
    sealed trait SupportsPollyRussell
    sealed trait SupportsPollyOliviaNeural
    sealed trait SupportsPollyAmy
    sealed trait SupportsPollyBrian
    sealed trait SupportsPollyEmma
    sealed trait SupportsPollyAmyNeural
    sealed trait SupportsPollyEmmaNeural
    sealed trait SupportsPollyBrianNeural
    sealed trait SupportsPollyArthurNeural
    sealed trait SupportsPollyAditi
    sealed trait SupportsPollyRaveena
    sealed trait SupportsPollyKajalNeural
    sealed trait SupportsPollyAriaNeural
    sealed trait SupportsPollyIvy
    sealed trait SupportsPollyJoanna
    sealed trait SupportsPollyJoey
    sealed trait SupportsPollyJustin
    sealed trait SupportsPollyKendra
    sealed trait SupportsPollyKimberly
    sealed trait SupportsPollyMatthew
    sealed trait SupportsPollySalli
    sealed trait SupportsPollyIvyNeural
    sealed trait SupportsPollyJoannaNeural
    sealed trait SupportsPollyKendraNeural
    sealed trait SupportsPollyKevinNeural
    sealed trait SupportsPollyKimberlyNeural
    sealed trait SupportsPollySalliNeural
    sealed trait SupportsPollyJoeyNeural
    sealed trait SupportsPollyJustinNeural
    sealed trait SupportsPollyMatthewNeural
    sealed trait SupportsPollyRuthNeural
    sealed trait SupportsPollyStephenNeural
    sealed trait SupportsPollyAyandaNeural
    sealed trait SupportsPollyGeraint
    sealed trait SupportsPollySuviNeural
    sealed trait SupportsPollyCeline
    sealed trait SupportsPollyLea
    sealed trait SupportsPollyMathieu
    sealed trait SupportsPollyLeaNeural
    sealed trait SupportsPollyRemiNeural
    sealed trait SupportsPollyChantal
    sealed trait SupportsPollyGabrielleNeural
    sealed trait SupportsPollyLiamNeural
    sealed trait SupportsPollyHans
    sealed trait SupportsPollyMarlene
    sealed trait SupportsPollyVicki
    sealed trait SupportsPollyVickiNeural
    sealed trait SupportsPollyDanielNeural
    sealed trait SupportsPollyHannahNeural
    sealed trait SupportsPollyDora
    sealed trait SupportsPollyKarl
    sealed trait SupportsPollyBianca
    sealed trait SupportsPollyCarla
    sealed trait SupportsPollyGiorgio
    sealed trait SupportsPollyBiancaNeural
    sealed trait SupportsPollyAdrianoNeural
    sealed trait SupportsPollyMizuki
    sealed trait SupportsPollyTakumi
    sealed trait SupportsPollyTakumiNeural
    sealed trait SupportsPollyKazuhaNeural
    sealed trait SupportsPollyTomokoNeural
    sealed trait SupportsPollySeoyeon
    sealed trait SupportsPollySeoyeonNeural
    sealed trait SupportsPollyLiv
    sealed trait SupportsPollyIdaNeural
    sealed trait SupportsPollyJacek
    sealed trait SupportsPollyJan
    sealed trait SupportsPollyEwa
    sealed trait SupportsPollyMaja
    sealed trait SupportsPollyOlaNeural
    sealed trait SupportsPollyCamila
    sealed trait SupportsPollyRicardo
    sealed trait SupportsPollyVitoria
    sealed trait SupportsPollyCamilaNeural
    sealed trait SupportsPollyVitoriaNeural
    sealed trait SupportsPollyThiagoNeural
    sealed trait SupportsPollyCristiano
    sealed trait SupportsPollyInes
    sealed trait SupportsPollyInesNeural
    sealed trait SupportsPollyCarmen
    sealed trait SupportsPollyMaxim
    sealed trait SupportsPollyTatyana
    sealed trait SupportsPollyConchita
    sealed trait SupportsPollyEnrique
    sealed trait SupportsPollyLucia
    sealed trait SupportsPollyLuciaNeural
    sealed trait SupportsPollySergioNeural
    sealed trait SupportsPollyMia
    sealed trait SupportsPollyMiaNeural
    sealed trait SupportsPollyAndresNeural
    sealed trait SupportsPollyLupe
    sealed trait SupportsPollyMiguel
    sealed trait SupportsPollyPenelope
    sealed trait SupportsPollyLupeNeural
    sealed trait SupportsPollyPedroNeural
    sealed trait SupportsPollyAstrid
    sealed trait SupportsPollyElinNeural
    sealed trait SupportsPollyFiliz
    sealed trait SupportsPollyGwyneth

  }

  sealed abstract class Gender extends enumeratum.EnumEntry

  object Gender extends enumeratum.Enum[Gender] {
    val values: immutable.IndexedSeq[Gender] = findValues

    case object Male      extends Gender
    case object Female    extends Gender
    case object MaleChild extends Gender
  }

  sealed abstract class Quality extends enumeratum.EnumEntry

  object Quality extends enumeratum.Enum[Quality] {
    val values: immutable.IndexedSeq[Quality] = findValues

    case object Basic extends Quality

    case object Standard extends Quality

    case object Premium extends Quality
  }

  /** Enum entry that represents a Voice that the Say verb supports */
  sealed abstract class Voice(
      override val twilioString: String,
      val gender: Gender,
      val languageCode: LanguageCode,
      val quality: Quality
  ) extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Voices that the Say verb supports and their capabilities */
  // noinspection ScalaUnusedSymbol
  object Voice extends EnumWithTwilioString[Voice] {

    val values: immutable.IndexedSeq[Voice] = findValues

    sealed trait SupportsPolly

    case object `man-EnGB` extends Voice("man", Gender.Male, LanguageCode.`en-GB`, Quality.Basic)
    case object `man-EnUS` extends Voice("man", Gender.Male, LanguageCode.`en-US`, Quality.Basic)
    case object `man-FrFR` extends Voice("man", Gender.Male, LanguageCode.`fr-FR`, Quality.Basic)
    case object `man-DeDE` extends Voice("man", Gender.Male, LanguageCode.`de-DE`, Quality.Basic)
    case object `man-ItIT` extends Voice("man", Gender.Male, LanguageCode.`it-IT`, Quality.Basic)
    case object `man-EsES` extends Voice("man", Gender.Male, LanguageCode.`es-ES`, Quality.Basic)

    case object `woman-EnGB`
        extends Voice("woman", Gender.Female, LanguageCode.`en-GB`, Quality.Basic)
    case object `woman-EnUS`
        extends Voice("woman", Gender.Female, LanguageCode.`en-US`, Quality.Basic)
    case object `woman-FrFR`
        extends Voice("woman", Gender.Female, LanguageCode.`fr-FR`, Quality.Basic)
    case object `woman-DeDE`
        extends Voice("woman", Gender.Female, LanguageCode.`de-DE`, Quality.Basic)
    case object `woman-ItIT`
        extends Voice("woman", Gender.Female, LanguageCode.`it-IT`, Quality.Basic)
    case object `woman-EsES`
        extends Voice("woman", Gender.Female, LanguageCode.`es-ES`, Quality.Basic)
    case object `woman-EsMX`
        extends Voice("woman", Gender.Female, LanguageCode.`es-MX`, Quality.Basic)
// TODO not sure if twilio is using en-CA
    case object `woman-EnCA`
        extends Voice("woman", Gender.Female, LanguageCode.`en-CA`, Quality.Basic)

    case object `Polly.Zeina`
        extends Voice("Polly.Zeina", Gender.Female, LanguageCode.`arb`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Hala-Neural`
        extends Voice("Polly.Hala-Neural", Gender.Female, LanguageCode.`ar-AE`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Arlet-Neural`
        extends Voice("Polly.Arlet-Neural", Gender.Female, LanguageCode.`ca-ES`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Hiujin-Neural`
        extends Voice("Polly.Hiujin-Neural", Gender.Female, LanguageCode.`yue-CN`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Zhiyu`
        extends Voice("Polly.Zhiyu", Gender.Female, LanguageCode.`cmn-CN`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Zhiyu-Neural`
        extends Voice("Polly.Zhiyu-Neural", Gender.Female, LanguageCode.`cmn-CN`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Mads`
        extends Voice("Polly.Mads", Gender.Male, LanguageCode.`da-DK`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Naja`
        extends Voice("Polly.Naja", Gender.Female, LanguageCode.`da-DK`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lotte`
        extends Voice("Polly.Lotte", Gender.Female, LanguageCode.`nl-NL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ruben`
        extends Voice("Polly.Ruben", Gender.Male, LanguageCode.`nl-NL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Laura-Neural`
        extends Voice("Polly.Laura-Neural", Gender.Female, LanguageCode.`nl-NL`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Nicole`
        extends Voice("Polly.Nicole", Gender.Female, LanguageCode.`en-AU`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Russell`
        extends Voice("Polly.Russell", Gender.Male, LanguageCode.`en-AU`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Olivia-Neural`
        extends Voice("Polly.Olivia-Neural", Gender.Female, LanguageCode.`en-AU`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Amy`
        extends Voice("Polly.Amy", Gender.Female, LanguageCode.`en-GB`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Brian`
        extends Voice("Polly.Brian", Gender.Male, LanguageCode.`en-GB`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Emma`
        extends Voice("Polly.Emma", Gender.Female, LanguageCode.`en-GB`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Amy-Neural`
        extends Voice("Polly.Amy-Neural", Gender.Female, LanguageCode.`en-GB`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Emma-Neural`
        extends Voice("Polly.Emma-Neural", Gender.Female, LanguageCode.`en-GB`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Brian-Neural`
        extends Voice("Polly.Brian-Neural", Gender.Male, LanguageCode.`en-GB`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Arthur-Neural`
        extends Voice("Polly.Arthur-Neural", Gender.Male, LanguageCode.`en-GB`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Raveena`
        extends Voice("Polly.Raveena", Gender.Female, LanguageCode.`en-IN`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Aria-Neural`
        extends Voice("Polly.Aria-Neural", Gender.Female, LanguageCode.`en-NZ`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Ivy`
        extends Voice("Polly.Ivy", Gender.Female, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Joanna`
        extends Voice("Polly.Joanna", Gender.Female, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Joey`
        extends Voice("Polly.Joey", Gender.Male, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Justin`
        extends Voice("Polly.Justin", Gender.Male, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Kendra`
        extends Voice("Polly.Kendra", Gender.Female, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Kimberly`
        extends Voice("Polly.Kimberly", Gender.Female, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Matthew`
        extends Voice("Polly.Matthew", Gender.Male, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Salli`
        extends Voice("Polly.Salli", Gender.Female, LanguageCode.`en-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ivy-Neural`
        extends Voice("Polly.Ivy-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Joanna-Neural`
        extends Voice("Polly.Joanna-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Kendra-Neural`
        extends Voice("Polly.Kendra-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Kevin-Neural`
        extends Voice("Polly.Kevin-Neural", Gender.MaleChild, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Kimberly-Neural`
        extends Voice("Polly.Kimberly-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Salli-Neural`
        extends Voice("Polly.Salli-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Joey-Neural`
        extends Voice("Polly.Joey-Neural", Gender.Male, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Justin-Neural`
        extends Voice("Polly.Justin-Neural", Gender.Male, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Matthew-Neural`
        extends Voice("Polly.Matthew-Neural", Gender.Male, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Ruth-Neural`
        extends Voice("Polly.Ruth-Neural", Gender.Female, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Stephen-Neural`
        extends Voice("Polly.Stephen-Neural", Gender.Male, LanguageCode.`en-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Ayanda-Neural`
        extends Voice("Polly.Ayanda-Neural", Gender.Female, LanguageCode.`en-ZA`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Geraint`
        extends Voice("Polly.Geraint", Gender.Male, LanguageCode.`en-GB-WLS`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Suvi-Neural`
        extends Voice("Polly.Suvi-Neural", Gender.Female, LanguageCode.`fi-FI`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Celine`
        extends Voice("Polly.Céline", Gender.Female, LanguageCode.`fr-FR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lea`
        extends Voice("Polly.Léa", Gender.Female, LanguageCode.`fr-FR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Mathieu`
        extends Voice("Polly.Mathieu", Gender.Male, LanguageCode.`fr-FR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lea-Neural`
        extends Voice("Polly.Lea-Neural", Gender.Female, LanguageCode.`fr-FR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Remi-Neural`
        extends Voice("Polly.Remi-Neural", Gender.Male, LanguageCode.`fr-FR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Chantal`
        extends Voice("Polly.Chantal", Gender.Female, LanguageCode.`fr-CA`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Gabrielle-Neural`
        extends Voice(
          "Polly.Gabrielle-Neural",
          Gender.Female,
          LanguageCode.`fr-CA`,
          Quality.Premium
        )
        with SupportsPolly
    case object `Polly.Liam-Neural`
        extends Voice("Polly.Liam-Neural", Gender.Male, LanguageCode.`fr-CA`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Hans`
        extends Voice("Polly.Hans", Gender.Male, LanguageCode.`de-DE`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Marlene`
        extends Voice("Polly.Marlene", Gender.Female, LanguageCode.`de-DE`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Vicki`
        extends Voice("Polly.Vicki", Gender.Female, LanguageCode.`de-DE`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Vicki-Neural`
        extends Voice("Polly.Vicki-Neural", Gender.Female, LanguageCode.`de-DE`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Daniel-Neural`
        extends Voice("Polly.Daniel-Neural", Gender.Male, LanguageCode.`de-DE`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Hannah-Neural`
        extends Voice("Polly.Hannah-Neural", Gender.Female, LanguageCode.`de-AT`, Quality.Premium)
        with SupportsPolly
    // TODO bilingual - supports also EnIN
    case object `Polly.Aditi`
        extends Voice("Polly.Aditi", Gender.Female, LanguageCode.`hi-IN`, Quality.Standard)
        with SupportsPolly
    // TODO bilingual - supports also EnIN
    case object `Polly.Kajal-Neural`
        extends Voice("Polly.Kajal-Neural", Gender.Female, LanguageCode.`hi-IN`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Dora`
        extends Voice("Polly.Dóra", Gender.Female, LanguageCode.`is-IS`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Karl`
        extends Voice("Polly.Karl", Gender.Male, LanguageCode.`is-IS`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Bianca`
        extends Voice("Polly.Bianca", Gender.Female, LanguageCode.`it-IT`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Carla`
        extends Voice("Polly.Carla", Gender.Female, LanguageCode.`it-IT`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Giorgio`
        extends Voice("Polly.Giorgio", Gender.Male, LanguageCode.`it-IT`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Bianca-Neural`
        extends Voice("Polly.Bianca-Neural", Gender.Female, LanguageCode.`it-IT`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Adriano-Neural`
        extends Voice("Polly.Adriano-Neural", Gender.Male, LanguageCode.`it-IT`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Mizuki`
        extends Voice("Polly.Mizuki", Gender.Female, LanguageCode.`ja-JP`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Takumi`
        extends Voice("Polly.Takumi", Gender.Male, LanguageCode.`ja-JP`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Takumi-Neural`
        extends Voice("Polly.Takumi-Neural", Gender.Male, LanguageCode.`ja-JP`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Kazuha-Neural`
        extends Voice("Polly.Kazuha-Neural", Gender.Female, LanguageCode.`ja-JP`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Tomoko-Neural`
        extends Voice("Polly.Tomoko-Neural", Gender.Female, LanguageCode.`ja-JP`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Seoyeon`
        extends Voice("Polly.Seoyeon", Gender.Female, LanguageCode.`ko-KR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Seoyeon-Neural`
        extends Voice("Polly.Seoyeon-Neural", Gender.Female, LanguageCode.`ko-KR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Liv`
        extends Voice("Polly.Liv", Gender.Female, LanguageCode.`nb-NO`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ida-Neural`
        extends Voice("Polly.Ida-Neural", Gender.Female, LanguageCode.`nb-NO`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Jacek`
        extends Voice("Polly.Jacek", Gender.Male, LanguageCode.`pl-PL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Jan`
        extends Voice("Polly.Jan", Gender.Male, LanguageCode.`pl-PL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ewa`
        extends Voice("Polly.Ewa", Gender.Female, LanguageCode.`pl-PL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Maja`
        extends Voice("Polly.Maja", Gender.Female, LanguageCode.`pl-PL`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ola-Neural`
        extends Voice("Polly.Ola-Neural", Gender.Female, LanguageCode.`pl-PL`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Camila`
        extends Voice("Polly.Camila", Gender.Female, LanguageCode.`pt-BR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ricardo`
        extends Voice("Polly.Ricardo", Gender.Male, LanguageCode.`pt-BR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Vitoria`
        extends Voice("Polly.Vitória", Gender.Female, LanguageCode.`pt-BR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Camila-Neural`
        extends Voice("Polly.Camila-Neural", Gender.Female, LanguageCode.`pt-BR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Vitoria-Neural`
        extends Voice("Polly.Vitoria-Neural", Gender.Female, LanguageCode.`pt-BR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Thiago-Neural`
        extends Voice("Polly.Thiago-Neural", Gender.Male, LanguageCode.`pt-BR`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Cristiano`
        extends Voice("Polly.Cristiano", Gender.Male, LanguageCode.`pt-PT`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ines`
        extends Voice("Polly.Inês", Gender.Female, LanguageCode.`pt-PT`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Ines-Neural`
        extends Voice("Polly.Ines-Neural", Gender.Female, LanguageCode.`pt-PT`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Carmen`
        extends Voice("Polly.Carmen", Gender.Female, LanguageCode.`ro-RO`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Maxim`
        extends Voice("Polly.Maxim", Gender.Male, LanguageCode.`ru-RU`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Tatyana`
        extends Voice("Polly.Tatyana", Gender.Female, LanguageCode.`ru-RU`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Conchita`
        extends Voice("Polly.Conchita", Gender.Female, LanguageCode.`es-ES`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Enrique`
        extends Voice("Polly.Enrique", Gender.Male, LanguageCode.`es-ES`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lucia`
        extends Voice("Polly.Lucia", Gender.Female, LanguageCode.`es-ES`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lucia-Neural`
        extends Voice("Polly.Lucia-Neural", Gender.Female, LanguageCode.`es-ES`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Sergio-Neural`
        extends Voice("Polly.Sergio-Neural", Gender.Male, LanguageCode.`es-ES`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Mia`
        extends Voice("Polly.Mia", Gender.Female, LanguageCode.`es-MX`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Mia-Neural`
        extends Voice("Polly.Mia-Neural", Gender.Female, LanguageCode.`es-MX`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Andres-Neural`
        extends Voice("Polly.Andres-Neural", Gender.Male, LanguageCode.`es-MX`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Lupe`
        extends Voice("Polly.Lupe", Gender.Female, LanguageCode.`es-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Miguel`
        extends Voice("Polly.Miguel", Gender.Male, LanguageCode.`es-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Penelope`
        extends Voice("Polly.Penélope", Gender.Female, LanguageCode.`es-US`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Lupe-Neural`
        extends Voice("Polly.Lupe-Neural", Gender.Female, LanguageCode.`es-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Pedro-Neural`
        extends Voice("Polly.Pedro-Neural", Gender.Male, LanguageCode.`es-US`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Astrid`
        extends Voice("Polly.Astrid", Gender.Female, LanguageCode.`sv-SE`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Elin-Neural`
        extends Voice("Polly.Elin-Neural", Gender.Female, LanguageCode.`sv-SE`, Quality.Premium)
        with SupportsPolly
    case object `Polly.Filiz`
        extends Voice("Polly.Filiz", Gender.Female, LanguageCode.`tr-TR`, Quality.Standard)
        with SupportsPolly
    case object `Polly.Gwyneth`
        extends Voice("Polly.Gwyneth", Gender.Female, LanguageCode.`cy-GB`, Quality.Standard)
        with SupportsPolly
  }

  /** The loop attribute specifies how many times you'd like the text repeated. The default is once.
    * Specifying 0 will cause the <Say> verb to loop until either the call is hung up or 1,000
    * iterations are performed.
    */
  case class Loop(value: Int = 1) {
    require(value >= 0, "integer >= 0")
  }

  final class Builder[
      B <: Buildable
  ] private (
      text: String,
      language: Option[LanguageCode],
      voice: Option[Voice],
      loop: Option[Int]
  ) {

    // TODO is this enough to make it buildable?
    def withText(text: String): Builder[BuildableTrue] =
      new Builder[BuildableTrue](text = text, language, voice, loop)

    def withVoice(voice: Voice): Builder[B] = {
      new Builder[B](text, language = Some(voice.languageCode), voice = Some(voice), loop)
    }

    def withBestQualityVoiceFemale(language: LanguageCode): Builder[B] = {
      val voicesWithLanguageAndGender =
        Voice.values.filter(v => v.languageCode == language && v.gender == Gender.Female)
      val voiceOpt = voicesWithLanguageAndGender
        .find(_.quality == Quality.Premium)
        // Standard Polly voices are available for every language in the current mapping
        .orElse(voicesWithLanguageAndGender.find(_.quality == Quality.Standard))
      new Builder[B](text, language = Some(language), voice = voiceOpt, loop)
    }

    def withLoop(loop: Int): Builder[B] =
      new Builder[B](text, language, voice, loop = Some(loop))

    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): SayVerb = SayVerbImpl(text, language, voice, loop)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState("", None, None, None)
  }

  type BuilderStartState = Builder[BuildableFalse]

  type BuildFunction = BuilderStartState => SayVerb

  def build(fun: BuildFunction): SayVerb = fun(Builder.empty)

  private final case class SayVerbImpl(
      text: String,
      language: Option[LanguageCode],
      voice: Option[Voice],
      loop: Option[Int]
  ) extends SayVerb {
    override protected def tagValue: Option[String] = Some(text)

    override protected val tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .add("language", language)
        .add("voice", voice)
        .addInt("loop", loop)
        .build

  }
}
