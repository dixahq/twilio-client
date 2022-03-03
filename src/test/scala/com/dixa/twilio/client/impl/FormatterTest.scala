package com.dixa.twilio.client.impl

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.time.format.DateTimeParseException

final class FormatterTest extends AnyFlatSpec with Matchers {

  "Formatter" should "parse a string representing an instant in time to an Instant object if " +
    "the string has the right format" in {
      val instanceString   = "Thu, 30 Sep 2021 06:30:46 +0000"
      val expectedInstance = Instant.parse("2021-09-30T06:30:46Z")

      Instant.from(Formatter.dateTime.parse(instanceString)) equals expectedInstance
    }

  it should "throw exception when parsing a string not containing the right format" in {
    val instanceString   = "2021-09-30T06:30:46Z"
    val expectedInstance = Instant.parse("2021-09-30T06:30:46Z")

    assertThrows[DateTimeParseException] {
      Instant.from(Formatter.dateTime.parse(instanceString)) equals expectedInstance
    }
  }
}
