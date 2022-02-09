package com.dixa.twilio.client.model.phonenumber

import com.neovisionaries.i18n.CountryCode

final case class PhoneNumberGeography(
    isoCountry: CountryCode,
    lata: Option[String] = None,
    rateCenter: Option[String] = None,
    latitude: Option[String] = None,
    longitude: Option[String] = None,
    region: Option[String] = None,
    locality: Option[String] = None,
    postalCode: Option[String] = None,
)
