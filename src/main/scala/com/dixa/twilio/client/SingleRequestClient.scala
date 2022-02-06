package com.dixa.twilio.client

import scala.concurrent.{ExecutionContext, Future}

trait SingleRequestClient[Req, Err <: RuntimeException, Success] {

  protected implicit def executionContext: ExecutionContext

  /** Run the request, with typesafe error handling
    *
    * Always return a Successfull future, and communicates erros of the request as part of the
    * return type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  def safe(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]]

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  final def unsafe(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    safe(connSettings, req).map(_.fold(e => throw e, res => res))
}
