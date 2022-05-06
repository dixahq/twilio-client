package com.dixa.twilio.model

private[model] object StringUtil {

  private val twoSpaecs = "  "

  private[model] def indentEveryLineWith2Spaces(input: String): String =
    input.lines.map(twoSpaecs + _).mkString(System.lineSeparator())
}
