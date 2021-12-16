package com.dixa.twilio.client.model

import com.google.i18n.phonenumbers.PhoneNumberUtil

sealed trait PhoneNumber {

  // It would seem logical to make these public, but often they are not needed to be,
  // as users of a phonenumber class would normally need the phonenumber formatted
  // as a String in a specific format, and hence use one of the inFormatX methods instead.
  // Hence these fields is better not exposed before we know that they would be needed,
  // as keeping them protected, makes it possible for us, to change them if needed (like
  // if we find that a String would be a better fit for nationalNumber than an Long.
  protected def countryCode: CountryCode
  protected def nationalNumber: Long

  def inFormatE164: String = s"+$countryCode$nationalNumber"

  override final def equals(other: Any): Boolean = other match {
    case that: PhoneNumber =>
      countryCode == that.countryCode &&
        nationalNumber == that.nationalNumber
    case _ => false
  }

  override final def hashCode(): Int = {
    val state = Seq(countryCode, nationalNumber)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override final def toString = s"PhoneNumber($countryCode, $nationalNumber)"
}

object PhoneNumber {

  private val pnUtil = PhoneNumberUtil.getInstance()

  def fromE123OrE164(asString: String): PhoneNumber = {
    try {
      val pnUtilInstance = pnUtil.parse(asString, "")
      new FromLibPhonenumberImpl(pnUtilInstance)
    } catch {
      case e: Exception =>
        throw ParseException(
          s"Could not parse $asString as a PhoneNumber instance.",
          Some(e)
        )
    }
  }

  final case class ParseException(msg: String, cause: Option[Throwable] = None)
      extends IllegalArgumentException(msg, cause.orNull)

  private final class FromLibPhonenumberImpl(
      pn: com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
  ) extends PhoneNumber {
    override val countryCode: CountryCode = CountryCode(pn.getCountryCode)

    override val nationalNumber: Long = pn.getNationalNumber
  }
}
