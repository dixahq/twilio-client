package com.dixa.twilio.client.implDetails

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

private[client] sealed abstract class ApiSubDomain(override val toString: String) extends EnumEntry

private[client] object ApiSubDomain extends Enum[ApiSubDomain] {

  override val values: immutable.IndexedSeq[ApiSubDomain] = findValues

  case object Api       extends ApiSubDomain("api")
  case object Messaging extends ApiSubDomain("messaging")
}
