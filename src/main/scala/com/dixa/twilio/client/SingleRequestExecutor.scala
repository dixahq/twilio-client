package com.dixa.twilio.client

import scala.concurrent.{ExecutionContext, Future}

/** Base trait for an executor that is able and ready to fire a specific request in different ways.
  *
  * Different users have different preferences when it comes to error handling. So an instance of
  * this, is ready to perform a specific request, but allows the user to decide how he prefers the
  * response.
  *
  * @tparam Req
  *   The Request type that is ready to be executed by this instance.
  * @tparam Err
  *   The Err type that the request might produce.
  * @tparam Success
  *   The type of a successfully response.
  */
trait SingleRequestExecutor[Req, Err <: RuntimeException, Success] {

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
  def run(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]]

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  final def unsafeRun(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    run(connSettings, req).map(_.fold(e => throw e, res => res))
}
