package com.dixa.twilio.client

import scala.concurrent.{ExecutionContext, Future}

trait SingleRequestExecutor[Req, Err <: RuntimeException, Success] {

  protected implicit def executionContext: ExecutionContext

  /** Run the request, with typesafe error handling
    *
    * Always return a Successful future, and communicates errors of the request as part of the
    * return type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * that error is communicated.
    */
  def run(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]]

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * that error is communicated.
    */
  final def unsafeRun(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    run(connSettings, req).map(_.fold(e => throw e, res => res))
}
