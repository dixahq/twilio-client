package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.annotation.nowarn
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

  /** Enum entry, representing a Language code that the Say verb support */
  sealed abstract class LanguageCode(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Language codes that the Say verb support */
  // noinspection ScalaUnusedSymbol
  object LanguageCode extends EnumWithTwilioString[LanguageCode] {

    sealed trait SupportsManVoice
    sealed trait SupportsWomanVoice
    sealed trait SupportsAliceVoice
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
    sealed trait SupportsPollyAyandaNeural
    sealed trait SupportsPollyGeraint
    sealed trait SupportsPollySuviNeural
    sealed trait SupportsPollyCéline
    sealed trait SupportsPollyLéa
    sealed trait SupportsPollyMathieu
    sealed trait SupportsPollyLeaNeural
    sealed trait SupportsPollyChantal
    sealed trait SupportsPollyGabrielleNeural
    sealed trait SupportsPollyLiamNeural
    sealed trait SupportsPollyHans
    sealed trait SupportsPollyMarlene
    sealed trait SupportsPollyVicki
    sealed trait SupportsPollyVickiNeural
    sealed trait SupportsPollyDanielNeural
    sealed trait SupportsPollyHannahNeural
    sealed trait SupportsPollyDóra
    sealed trait SupportsPollyKarl
    sealed trait SupportsPollyBianca
    sealed trait SupportsPollyCarla
    sealed trait SupportsPollyGiorgio
    sealed trait SupportsPollyBiancaNeural
    sealed trait SupportsPollyMizuki
    sealed trait SupportsPollyTakumi
    sealed trait SupportsPollyTakumiNeural
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
    sealed trait SupportsPollyVitória
    sealed trait SupportsPollyCamilaNeural
    sealed trait SupportsPollyVitoriaNeural
    sealed trait SupportsPollyCristiano
    sealed trait SupportsPollyInês
    sealed trait SupportsPollyInesNeural
    sealed trait SupportsPollyCarmen
    sealed trait SupportsPollyMaxim
    sealed trait SupportsPollyTatyana
    sealed trait SupportsPollyConchita
    sealed trait SupportsPollyEnrique
    sealed trait SupportsPollyLucia
    sealed trait SupportsPollyLuciaNeural
    sealed trait SupportsPollyMia
    sealed trait SupportsPollyMiaNeural
    sealed trait SupportsPollyLupe
    sealed trait SupportsPollyMiguel
    sealed trait SupportsPollyPenélope
    sealed trait SupportsPollyLupeNeural
    sealed trait SupportsPollyPedroNeural
    sealed trait SupportsPollyAstrid
    sealed trait SupportsPollyElinNeural
    sealed trait SupportsPollyFiliz
    sealed trait SupportsPollyGwyneth

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
    case object `da-DK`
        extends LanguageCode("da-DK")
        with SupportsAliceVoice
        with SupportsPollyMads
        with SupportsPollyNaja

    /** Dutch */
    case object `nl-NL`
        extends LanguageCode("nl-NL")
        with SupportsAliceVoice
        with SupportsPollyLotte
        with SupportsPollyRuben
        with SupportsPollyLauraNeural

    /** "English, Australia" */
    case object `en-AU`
        extends LanguageCode("en-AU")
        with SupportsAliceVoice
        with SupportsPollyNicole
        with SupportsPollyRussell
        with SupportsPollyOliviaNeural

    /** "English, Canada" */
    case object `en-CA` extends LanguageCode("en-CA") with SupportsAliceVoice

    /** "English, UK" */
    case object `en-GB`
        extends LanguageCode("en-GB")
        with SupportsAliceVoice
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
        with SupportsAliceVoice
        with SupportsPollyAditi
        with SupportsPollyRaveena
        with SupportsPollyKajalNeural

    /** "English, New Zealand" */
    case object `en-NZ` extends LanguageCode("en-NZ") with SupportsPollyAriaNeural

    /** "English, United States" */
    case object `en-US`
        extends LanguageCode("en-US")
        with SupportsAliceVoice
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

    /** "English, South African" */
    case object `en-ZA` extends LanguageCode("en-ZA") with SupportsPollyAyandaNeural

    /** "English, Welsh" */
    case object `en-BG-WLS` extends LanguageCode("en-BG-WLS") with SupportsPollyGeraint

    /** "Finnish, Finland" */
    case object `fi-FI`
        extends LanguageCode("fi-FI")
        with SupportsAliceVoice
        with SupportsPollySuviNeural

    /** "French, France" */
    case object `fr-FR`
        extends LanguageCode("fr-FR")
        with SupportsAliceVoice
        with SupportsPollyCéline
        with SupportsPollyLéa
        with SupportsPollyMathieu
        with SupportsPollyLeaNeural

    /** "French, Canada" */
    case object `fr-CA`
        extends LanguageCode("fr-CA")
        with SupportsAliceVoice
        with SupportsPollyChantal
        with SupportsPollyGabrielleNeural
        with SupportsPollyLiamNeural

    /** "German, Germany" */
    case object `de-DE`
        extends LanguageCode("de-DE")
        with SupportsAliceVoice
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
    case object `is-IS` extends LanguageCode("is-IS") with SupportsPollyDóra with SupportsPollyKarl

    /** "Italian, Italy" */
    case object `it-IT`
        extends LanguageCode("it-IT")
        with SupportsAliceVoice
        with SupportsPollyBianca
        with SupportsPollyCarla
        with SupportsPollyGiorgio
        with SupportsPollyBiancaNeural

    /** "Catalan, Spain" */
    case object `ca-ES`
        extends LanguageCode("ca-ES")
        with SupportsAliceVoice
        with SupportsPollyArletNeural

    /** "Spanish, Spain" */
    case object `es-ES`
        extends LanguageCode("es-ES")
        with SupportsAliceVoice
        with SupportsPollyConchita
        with SupportsPollyEnrique
        with SupportsPollyLucia
        with SupportsPollyLuciaNeural

    /** "Spanish, Mexico" */
    case object `es-MX`
        extends LanguageCode("es-MX")
        with SupportsAliceVoice
        with SupportsPollyMia
        with SupportsPollyMiaNeural

    /** "Spanish, USA" */
    case object `es-US`
        extends LanguageCode("es-US")
        with SupportsPollyLupe
        with SupportsPollyMiguel
        with SupportsPollyPenélope
        with SupportsPollyLupeNeural
        with SupportsPollyPedroNeural

    /** "Japanese, Japan" */
    case object `ja-JP`
        extends LanguageCode("ja-JP")
        with SupportsAliceVoice
        with SupportsPollyMizuki
        with SupportsPollyTakumi
        with SupportsPollyTakumiNeural

    /** "Korean, Korea" */
    case object `ko-KR`
        extends LanguageCode("ko-KR")
        with SupportsAliceVoice
        with SupportsPollySeoyeon
        with SupportsPollySeoyeonNeural

    /** "Norwegian, Norway" */
    case object `nb-NO` extends LanguageCode("nb-NO") with SupportsAliceVoice

    /** "Polish -Poland" */
    case object `pl-PL`
        extends LanguageCode("pl-PL")
        with SupportsAliceVoice
        with SupportsPollyJacek
        with SupportsPollyJan
        with SupportsPollyEwa
        with SupportsPollyMaja
        with SupportsPollyOlaNeural

    /** "Portuguese, Brazil" */
    case object `pt-BR`
        extends LanguageCode("pt-BR")
        with SupportsAliceVoice
        with SupportsPollyCamila
        with SupportsPollyRicardo
        with SupportsPollyVitória
        with SupportsPollyCamilaNeural
        with SupportsPollyVitoriaNeural

    /** "Portuguese, Portugal" */
    case object `pt-PT`
        extends LanguageCode("pt-PT")
        with SupportsAliceVoice
        with SupportsPollyCristiano
        with SupportsPollyInês
        with SupportsPollyInesNeural

    /** "Romanian, Romania" */
    case object `ro-RO` extends LanguageCode("ro-RO") with SupportsPollyCarmen

    /** "Russian, Russia" */
    case object `ru-RU`
        extends LanguageCode("ru-RU")
        with SupportsAliceVoice
        with SupportsPollyMaxim
        with SupportsPollyTatyana

    /** "Swedish, Sweden" */
    case object `sv-SE`
        extends LanguageCode("sv-SE")
        with SupportsAliceVoice
        with SupportsPollyAstrid
        with SupportsPollyElinNeural

    /** "Turkish, Turkey" */
    case object `tr-TR` extends LanguageCode("tr-TR") with SupportsPollyFiliz

    /** "Turkish, Turkey" */
    case object `cy-GB` extends LanguageCode("cy-GB") with SupportsPollyGwyneth

    /** "Chinese (Mandarin)" */
    case object `zh-CN` extends LanguageCode("zh-CN") with SupportsAliceVoice

    /** "Chinese (Cantonese)" */
    case object `zh-HK` extends LanguageCode("zh-HK") with SupportsAliceVoice

    /** "Chinese (Taiwanese Mandarin)" */
    case object `zh-TW` extends LanguageCode("zh-TW") with SupportsAliceVoice

    /** "English with an American accent" */
    case object `en` extends LanguageCode("en") with SupportsManVoice with SupportsWomanVoice

    /** "Spanish" */
    case object `es` extends LanguageCode("es") with SupportsManVoice with SupportsWomanVoice

    /** "French" */
    case object `fr` extends LanguageCode("fr") with SupportsManVoice with SupportsWomanVoice

    /** "Italian" */
    case object `it` extends LanguageCode("it") with SupportsManVoice with SupportsWomanVoice

    /** "German" */
    case object `de` extends LanguageCode("de") with SupportsManVoice with SupportsWomanVoice
  }

  sealed abstract class Gender extends enumeratum.EnumEntry

  object Gender extends enumeratum.Enum[Gender] {
    val values: immutable.IndexedSeq[Gender] = findValues

    case object Male      extends Gender
    case object Female    extends Gender
    case object MaleChild extends Gender
  }

  /** Enum entry, representing a Language code that the Say verb support */
  sealed abstract class Voice(override val twilioString: String, gender: Gender)
      extends EnumWithTwilioString.EnumEntry

  /** Enum representing all the Language codes that the Say verb support */
  // noinspection ScalaUnusedSymbol
  object Voice extends EnumWithTwilioString[Voice] {

    val values: immutable.IndexedSeq[Voice] = findValues

    sealed trait SupportsPolly
    sealed trait SupportsArb
    sealed trait SupportsArAE
    sealed trait SupportsCaES
    sealed trait SupportsYueCN
    sealed trait SupportsCmnCN
    sealed trait SupportsDaDK
    sealed trait SupportsNlNL
    sealed trait SupportsEnAU
    sealed trait SupportsEnGB
    sealed trait SupportsEnIN
    sealed trait SupportsEnNZ
    sealed trait SupportsEnUS
    sealed trait SupportsEnZA
    sealed trait SupportsEnGBWLS
    sealed trait SupportsFiFI
    sealed trait SupportsFrFR
    sealed trait SupportsFrCA
    sealed trait SupportsDeDE
    sealed trait SupportsDeAT
    sealed trait SupportsHiIN
    sealed trait SupportsIsIS
    sealed trait SupportsItIT
    sealed trait SupportsJaJP
    sealed trait SupportsKoKR
    sealed trait SupportsNbNO
    sealed trait SupportsPlPL
    sealed trait SupportsPtBR
    sealed trait SupportsPtPT
    sealed trait SupportsRoRO
    sealed trait SupportsRuRU
    sealed trait SupportsEsES
    sealed trait SupportsEsMX
    sealed trait SupportsEsUS
    sealed trait SupportsSvSE
    sealed trait SupportsTrTR
    sealed trait SupportsCyGB

    case object `man`   extends Voice("man", Gender.Male)
    case object `woman` extends Voice("woman", Gender.Female)
    case object `alice` extends Voice("alice", Gender.Female)
    case object `Polly.Zeina`
        extends Voice("Polly.Zeina", Gender.Female)
        with SupportsPolly
        with SupportsArb
    case object `Polly.Hala-Neural`
        extends Voice("Polly.Hala-Neural", Gender.Female)
        with SupportsPolly
        with SupportsArAE
    case object `Polly.Arlet-Neural`
        extends Voice("Polly.Arlet-Neural", Gender.Female)
        with SupportsPolly
        with SupportsCaES
    case object `Polly.Hiujin-Neural`
        extends Voice("Polly.Hiujin-Neural", Gender.Female)
        with SupportsPolly
        with SupportsYueCN
    case object `Polly.Zhiyu`
        extends Voice("Polly.Zhiyu", Gender.Female)
        with SupportsPolly
        with SupportsCmnCN
    case object `Polly.Zhiyu-Neural`
        extends Voice("Polly.Zhiyu-Neural", Gender.Female)
        with SupportsPolly
        with SupportsCmnCN
    case object `Polly.Mads`
        extends Voice("Polly.Mads", Gender.Male)
        with SupportsPolly
        with SupportsDaDK
    case object `Polly.Naja`
        extends Voice("Polly.Naja", Gender.Female)
        with SupportsPolly
        with SupportsDaDK
    case object `Polly.Lotte`
        extends Voice("Polly.Lotte", Gender.Female)
        with SupportsPolly
        with SupportsNlNL
    case object `Polly.Ruben`
        extends Voice("Polly.Ruben", Gender.Male)
        with SupportsPolly
        with SupportsNlNL
    case object `Polly.Laura-Neural`
        extends Voice("Polly.Laura-Neural", Gender.Female)
        with SupportsPolly
        with SupportsNlNL
    case object `Polly.Nicole`
        extends Voice("Polly.Nicole", Gender.Female)
        with SupportsPolly
        with SupportsEnAU
    case object `Polly.Russell`
        extends Voice("Polly.Russell", Gender.Male)
        with SupportsPolly
        with SupportsEnAU
    case object `Polly.Olivia-Neural`
        extends Voice("Polly.Olivia-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnAU
    case object `Polly.Amy`
        extends Voice("Polly.Amy", Gender.Female)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Brian`
        extends Voice("Polly.Brian", Gender.Male)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Emma`
        extends Voice("Polly.Emma", Gender.Female)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Amy-Neural`
        extends Voice("Polly.Amy-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Emma-Neural`
        extends Voice("Polly.Emma-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Brian-Neural`
        extends Voice("Polly.Brian-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Arthur-Neural`
        extends Voice("Polly.Arthur-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEnGB
    case object `Polly.Raveena`
        extends Voice("Polly.Raveena", Gender.Female)
        with SupportsPolly
        with SupportsEnIN
    case object `Polly.Aria-Neural`
        extends Voice("Polly.Aria-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnNZ
    case object `Polly.Ivy`
        extends Voice("Polly.Ivy", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Joanna`
        extends Voice("Polly.Joanna", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Joey`
        extends Voice("Polly.Joey", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Justin`
        extends Voice("Polly.Justin", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Kendra`
        extends Voice("Polly.Kendra", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Kimberly`
        extends Voice("Polly.Kimberly", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Matthew`
        extends Voice("Polly.Matthew", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Salli`
        extends Voice("Polly.Salli", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Ivy-Neural`
        extends Voice("Polly.Ivy-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Joanna-Neural`
        extends Voice("Polly.Joanna-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Kendra-Neural`
        extends Voice("Polly.Kendra-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Kevin-Neural`
        extends Voice("Polly.Kevin-Neural", Gender.MaleChild)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Kimberly-Neural`
        extends Voice("Polly.Kimberly-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Salli-Neural`
        extends Voice("Polly.Salli-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Joey-Neural`
        extends Voice("Polly.Joey-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Justin-Neural`
        extends Voice("Polly.Justin-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Matthew-Neural`
        extends Voice("Polly.Matthew-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEnUS
    case object `Polly.Ayanda-Neural`
        extends Voice("Polly.Ayanda-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEnZA
    case object `Polly.Geraint`
        extends Voice("Polly.Geraint", Gender.Male)
        with SupportsPolly
        with SupportsEnGBWLS
    case object `Polly.Suvi-Neural`
        extends Voice("Polly.Suvi - Neural", Gender.Female)
        with SupportsPolly
        with SupportsFiFI
    case object `Polly.Céline`
        extends Voice("Polly.Céline", Gender.Female)
        with SupportsPolly
        with SupportsFrFR
    case object `Polly.Léa`
        extends Voice("Polly.Léa", Gender.Female)
        with SupportsPolly
        with SupportsFrFR
    case object `Polly.Mathieu`
        extends Voice("Polly.Mathieu", Gender.Male)
        with SupportsPolly
        with SupportsFrFR
    case object `Polly.Lea-Neural`
        extends Voice("Polly.Lea-Neural", Gender.Female)
        with SupportsPolly
        with SupportsFrFR
    case object `Polly.Chantal`
        extends Voice("Polly.Chantal", Gender.Female)
        with SupportsPolly
        with SupportsFrCA
    case object `Polly.Gabrielle-Neural`
        extends Voice("Polly.Gabrielle-Neural", Gender.Female)
        with SupportsPolly
        with SupportsFrCA
    case object `Polly.Liam-Neural`
        extends Voice("Polly.Liam-Neural", Gender.Male)
        with SupportsPolly
        with SupportsFrCA
    case object `Polly.Hans`
        extends Voice("Polly.Hans", Gender.Male)
        with SupportsPolly
        with SupportsDeDE
    case object `Polly.Marlene`
        extends Voice("Polly.Marlene", Gender.Female)
        with SupportsPolly
        with SupportsDeDE
    case object `Polly.Vicki`
        extends Voice("Polly.Vicki", Gender.Female)
        with SupportsPolly
        with SupportsDeDE
    case object `Polly.Vicki-Neural`
        extends Voice("Polly.Vicki-Neural", Gender.Female)
        with SupportsPolly
        with SupportsDeDE
    case object `Polly.Daniel-Neural`
        extends Voice("Polly.Daniel-Neural", Gender.Male)
        with SupportsPolly
        with SupportsDeDE
    case object `Polly.Hannah-Neural`
        extends Voice("Polly.Hannah-Neural", Gender.Female)
        with SupportsPolly
        with SupportsDeAT
    case object `Polly.Aditi`
        extends Voice("Polly.Aditi", Gender.Female)
        with SupportsPolly
        with SupportsHiIN
        with SupportsEnIN
    case object `Polly.Kajal-Neural`
        extends Voice("Polly.Kajal-Neural", Gender.Female)
        with SupportsPolly
        with SupportsHiIN
        with SupportsEnIN
    case object `Polly.Dora`
        extends Voice("Polly.Dóra", Gender.Female)
        with SupportsPolly
        with SupportsIsIS
    case object `Polly.Karl`
        extends Voice("Polly.Karl", Gender.Male)
        with SupportsPolly
        with SupportsIsIS
    case object `Polly.Bianca`
        extends Voice("Polly.Bianca", Gender.Female)
        with SupportsPolly
        with SupportsItIT
    case object `Polly.Carla`
        extends Voice("Polly.Carla", Gender.Female)
        with SupportsPolly
        with SupportsItIT
    case object `Polly.Giorgio`
        extends Voice("Polly.Giorgio", Gender.Male)
        with SupportsPolly
        with SupportsItIT
    case object `Polly.Bianca-Neural`
        extends Voice("Polly.Bianca-Neural", Gender.Female)
        with SupportsPolly
        with SupportsItIT
    case object `Polly.Mizuki`
        extends Voice("Polly.Mizuki", Gender.Female)
        with SupportsPolly
        with SupportsJaJP
    case object `Polly.Takumi`
        extends Voice("Polly.Takumi", Gender.Male)
        with SupportsPolly
        with SupportsJaJP
    case object `Polly.Takumi-Neural`
        extends Voice("Polly.Takumi-Neural", Gender.Male)
        with SupportsPolly
        with SupportsJaJP
    case object `Polly.Seoyeon`
        extends Voice("Polly.Seoyeon", Gender.Female)
        with SupportsPolly
        with SupportsKoKR
    case object `Polly.Seoyeon-Neural`
        extends Voice("Polly.Seoyeon-Neural", Gender.Female)
        with SupportsPolly
        with SupportsKoKR
    case object `Polly.Liv`
        extends Voice("Polly.Liv", Gender.Female)
        with SupportsPolly
        with SupportsNbNO
    case object `Polly.Ida-Neural`
        extends Voice("Polly.Ida-Neural", Gender.Female)
        with SupportsPolly
        with SupportsNbNO
    case object `Polly.Jacek`
        extends Voice("Polly.Jacek", Gender.Male)
        with SupportsPolly
        with SupportsPlPL
    case object `Polly.Jan`
        extends Voice("Polly.Jan", Gender.Male)
        with SupportsPolly
        with SupportsPlPL
    case object `Polly.Ewa`
        extends Voice("Polly.Ewa", Gender.Female)
        with SupportsPolly
        with SupportsPlPL
    case object `Polly.Maja`
        extends Voice("Polly.Maja", Gender.Female)
        with SupportsPolly
        with SupportsPlPL
    case object `Polly.Ola-Neural`
        extends Voice("Polly.Ola-Neural", Gender.Female)
        with SupportsPolly
        with SupportsPlPL
    case object `Polly.Camila`
        extends Voice("Polly.Camila", Gender.Female)
        with SupportsPolly
        with SupportsPtBR
    case object `Polly.Ricardo`
        extends Voice("Polly.Ricardo", Gender.Male)
        with SupportsPolly
        with SupportsPtBR
    case object `Polly.Vitória`
        extends Voice("Polly.Vitória", Gender.Female)
        with SupportsPolly
        with SupportsPtBR
    case object `Polly.Camila-Neural`
        extends Voice("Polly.Camila-Neural", Gender.Female)
        with SupportsPolly
        with SupportsPtBR
    case object `Polly.Vitoria-Neural`
        extends Voice("Polly.Vitoria-Neural", Gender.Female)
        with SupportsPolly
        with SupportsPtBR
    case object `Polly.Cristiano`
        extends Voice("Polly.Cristiano", Gender.Male)
        with SupportsPolly
        with SupportsPtPT
    case object `Polly.Inês`
        extends Voice("Polly.Inês", Gender.Female)
        with SupportsPolly
        with SupportsPtPT
    case object `Polly.Ines-Neural`
        extends Voice("Polly.Ines-Neural", Gender.Female)
        with SupportsPolly
        with SupportsPtPT
    case object `Polly.Carmen`
        extends Voice("Polly.Carmen", Gender.Female)
        with SupportsPolly
        with SupportsRoRO
    case object `Polly.Maxim`
        extends Voice("Polly.Maxim", Gender.Male)
        with SupportsPolly
        with SupportsRuRU
    case object `Polly.Tatyana`
        extends Voice("Polly.Tatyana", Gender.Female)
        with SupportsPolly
        with SupportsRuRU
    case object `Polly.Conchita`
        extends Voice("Polly.Conchita", Gender.Female)
        with SupportsPolly
        with SupportsEsES
    case object `Polly.Enrique`
        extends Voice("Polly.Enrique", Gender.Male)
        with SupportsPolly
        with SupportsEsES
    case object `Polly.Lucia`
        extends Voice("Polly.Lucia", Gender.Female)
        with SupportsPolly
        with SupportsEsES
    case object `Polly.Lucia-Neural`
        extends Voice("Polly.Lucia-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEsES
    case object `Polly.Mia`
        extends Voice("Polly.Mia", Gender.Female)
        with SupportsPolly
        with SupportsEsMX
    case object `Polly.Mia-Neural`
        extends Voice("Polly.Mia-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEsMX
    case object `Polly.Lupe`
        extends Voice("Polly.Lupe", Gender.Female)
        with SupportsPolly
        with SupportsEsUS
    case object `Polly.Miguel`
        extends Voice("Polly.Miguel", Gender.Male)
        with SupportsPolly
        with SupportsEsUS
    case object `Polly.Penelope`
        extends Voice("Polly.Penélope", Gender.Female)
        with SupportsPolly
        with SupportsEsUS
    case object `Polly.Lupe-Neural`
        extends Voice("Polly.Lupe-Neural", Gender.Female)
        with SupportsPolly
        with SupportsEsUS
    case object `Polly.Pedro-Neural`
        extends Voice("Polly.Pedro-Neural", Gender.Male)
        with SupportsPolly
        with SupportsEsUS
    case object `Polly.Astrid`
        extends Voice("Polly.Astrid", Gender.Female)
        with SupportsPolly
        with SupportsSvSE
    case object `Polly.Elin-Neural`
        extends Voice("Polly.Elin-Neural", Gender.Female)
        with SupportsPolly
        with SupportsSvSE
    case object `Polly.Filiz`
        extends Voice("Polly.Filiz", Gender.Female)
        with SupportsPolly
        with SupportsTrTR
    case object `Polly.Gwyneth`
        extends Voice("Polly.Gwyneth", Gender.Female)
        with SupportsPolly
        with SupportsCyGB
  }

  /** The loop attribute specifies how many times you'd like the text repeated. The default is once.
    * Specifying 0 will cause the <Say> verb to loop until either the call is hung up or 1,000
    * iterations are performed.
    */
  case class Loop(value: Int = 1) {
    require(value >= 0, "integer >= 0")
  }

  sealed trait RequiredMatchingLanguageAdded
  sealed trait RequiredMatchingLanguageAddedTrue  extends RequiredMatchingLanguageAdded
  sealed trait RequiredMatchingLanguageAddedFalse extends RequiredMatchingLanguageAdded

  sealed trait RequiredMatchingVoiceAdded
  sealed trait RequiredMatchingVoiceAddedTrue  extends RequiredMatchingVoiceAdded
  sealed trait RequiredMatchingVoiceAddedFalse extends RequiredMatchingVoiceAdded

  final class Builder[
      B <: Buildable,
      L <: RequiredMatchingLanguageAdded,
      V <: RequiredMatchingVoiceAdded
  ] private[SayVerb] (
      text: String,
      language: Option[LanguageCode],
      voice: Option[Voice],
      loop: Option[Int]
  ) {

    def withText(text: String): Builder[BuildableTrue, L, V] =
      new Builder[BuildableTrue, L, V](text = text, language, voice, loop)

    def withLanguage(language: LanguageCode): Builder[B, L, V] =
      new Builder[B, L, V](text, language = Some(language), voice, loop)

    def withVoice(voice: Voice): Builder[B, L, V] =
      new Builder[B, L, V](text, language, voice = Some(voice), loop)

    def withLoop(loop: Int): Builder[B, L, V] =
      new Builder[B, L, V](text, language, voice, loop = Some(loop))

    @nowarn(value = "cat=unused-params")
    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): SayVerb = SayVerbImpl(text, language, voice, loop)
  }
  type BuilderStartState =
    Builder[BuildableFalse, RequiredMatchingLanguageAddedFalse, RequiredMatchingVoiceAddedFalse]
  type BuildFunction = BuilderStartState => SayVerb

  def build(fun: BuildFunction): SayVerb = fun(
    new BuilderStartState("", None, None, None)
  )

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
