package com.dixa.twilio.client

/** Represent a Api level exception.
  *
  * API level = exceptions that are shared and identical in all API calls. Each call should have its
  * own exception, where one of them should be an ApiException embedding this type.
  *
  * The different implementation of ApiException are all found in the companion object.
  */
sealed trait ApiException extends RuntimeException {}

object ApiException {

  /** Invalid credentials used for the request */
  final case class AuthenticationException() extends ApiException
}
