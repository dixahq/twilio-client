package com.dixa.twilio.model.messaging

final case class MessageBody(override val toString: String)

object MessageBody {

  private val gsm7Chars = Set('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
    'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '!', '#', ' ', '"', '%', '&', '\'', '(',
    ')', '*', ',', '.', '?', '+', '-', '/', ';', ':', '<', '=', '>', '¡', '¿', '_', '@', '$', '£',
    '¥', '¤', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', 'Ø', 'ø', 'Æ', 'æ', 'ß', 'É', 'Å', 'å', 'Ä', 'Ö', 'Ñ',
    'Ü', '§', 'ä', 'ö', 'ñ', 'ü', 'à', 'Δ', 'Φ', 'Ξ', 'Γ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Λ', '\n', '\r')

  def isGSM7(s: String): Boolean = s.forall(gsm7Chars.contains)
}
