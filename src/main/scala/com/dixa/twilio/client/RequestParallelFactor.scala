package com.dixa.twilio.client

/** Represent a parallel factor for request to run at.
  *
  * Request that supporting processing in parallel, could use this class to represent the count of
  * parallelism.
  */
private[client] final case class RequestParallelFactor(asInt: Int)
