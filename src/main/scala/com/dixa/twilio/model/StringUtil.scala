// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model

private[model] object StringUtil {

  private val twoSpaces = "  "

  private[model] def indentEveryLineWith2Spaces(input: String): String =
    input.linesIterator.map(twoSpaces + _).mkString(System.lineSeparator())

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
