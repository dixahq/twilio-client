package com.dixa.twilio.model.messaging.repository

import com.dixa.twilio.model.phonenumber.CountryCallingCode

trait CountryAlphanumericCapabilityRepository {

  def fromCountryCallingCode(countryCallingCode: CountryCallingCode): Option[CountryCapability]
}

object CountryAlphanumericCapabilityRepository {

  def hardcodedImpl(): CountryAlphanumericCapabilityRepository = {
    new CountryAlphanumericCapabilityRepository {
      override def fromCountryCallingCode(
          countryCallingCode: CountryCallingCode
      ): Option[CountryCapability] = {
        HardcodedCountryAlphanumericCapabilities.countryCodeToCapabilityMap.get(
          countryCallingCode
        )
      }
    }
  }
}
