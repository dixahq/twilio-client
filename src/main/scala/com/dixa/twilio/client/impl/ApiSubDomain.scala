package com.dixa.twilio.client.impl

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable

/** Twilio use different subdomains for different APIs. This enum specifies them. */
private[client] sealed abstract class ApiSubDomain(
    override val toString: String,
    val pagingStyle: PagingStyle
) extends EnumEntry

private[client] object ApiSubDomain extends Enum[ApiSubDomain] {

  override val values: immutable.IndexedSeq[ApiSubDomain] = findValues

  case object Accounts  extends ApiSubDomain("accounts", PagingStyle.NoPaging)
  case object Api       extends ApiSubDomain("api", PagingStyle.PagingAttributesInRootJson)
  case object Messaging extends ApiSubDomain("messaging", PagingStyle.MetaObject)
  case object Preview   extends ApiSubDomain("preview", PagingStyle.MetaObject)
}
