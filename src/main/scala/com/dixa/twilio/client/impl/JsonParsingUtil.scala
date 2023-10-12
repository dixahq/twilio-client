package com.dixa.twilio.client.impl

/** small utility methods, helpfully when parsing the Twilio json responses */
private[impl] object JsonParsingUtil {

  /** Convert Option[String] into a None, if it contains an empty String
    *
    * Some Twilio API are a bit confused, when it comes to representing unset values, where they mix
    * the use of null and empty strings in there output. This method can help in such cases, if you
    * have you json representation map such fields as option, so that a null value will be mapped
    * into None, and then call this method on it, so that an empty string would also be mapped into
    * a None.
    */
  def emptyStringToNone(x: Option[String]): Option[String] =
    x.flatMap(s => if (s.isEmpty) None else x)
}
