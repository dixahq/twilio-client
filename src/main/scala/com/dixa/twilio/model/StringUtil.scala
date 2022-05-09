package com.dixa.twilio.model

private[model] object StringUtil {

  private val twoSpaecs = "  "

  private[model] def indentEveryLineWith2Spaces(input: String): String =
    input.lines.map(twoSpaecs + _).mkString(System.lineSeparator())

  /** Escape all the XML special chars from this string.
    *
    * Source of list:
    * https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references#Predefined%5Fentities%5Fin%5FXML
    */
  private[model] def xmlEscape(input: String): String = input
    .replace("&", "&amp;") // Important that this goes first, as they other adds & chars.
    .replace("\"", "&quot;")
    .replace("'", "&apos;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

}
