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

  /** There was an error with the request. The body of the response will have more info. */
  final case class BadRequestException() extends ApiException

  /** API usage limit. If you reach API usage limits, a 429 will be returned. Please wait until you
    * pass the limit and attempt the call again
    */
  final case class TooManyRequestsException() extends ApiException

  /** The service is unavailable */
  final case class ServiceUnavailable() extends ApiException

  /** The request could not be completed due to a conflict with the current state of the target
    * resource. For more info: https://www.twilio.com/docs/errors/20409
    */
  final case class Conflict() extends ApiException
}
