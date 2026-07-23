// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.content

import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.content.{
  ContentApproval,
  ContentTemplate,
  ContentTemplateWithApproval,
  ContentType
}
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant
import scala.util.Try

private[content] object ContentJsonRep {

  // ── WhatsappApproval — fixed fields, macroR ───────────────────────────────

  private final case class WhatsappApprovalJsonRep(
      name: Option[String] = None,
      category: Option[String] = None,
      content_type: Option[String] = None,
      status: String,
      rejection_reason: Option[String] = None,
      allow_category_change: Boolean = false
  ) {
    def toModel(defaultStatus: ContentApproval.ApprovalStatus): ContentApproval.WhatsappApproval =
      ContentApproval.WhatsappApproval(
        name = emptyStringToNone(name),
        category = emptyStringToNone(category),
        contentType = emptyStringToNone(content_type),
        status = ContentApproval.ApprovalStatus.values
          .find(_.twilioString == status)
          .getOrElse(defaultStatus),
        rejectionReason = emptyStringToNone(rejection_reason),
        allowCategoryChange = allow_category_change
      )
  }

  private object WhatsappApprovalJsonRep {
    implicit val reader: Reader[WhatsappApprovalJsonRep] = macroR[WhatsappApprovalJsonRep]
  }

  // ── ContentTemplate — fixed fields macroR; types map stays manual ─────────

  private final case class ContentTemplateJsonRep(
      sid: String,
      account_sid: String,
      friendly_name: String,
      language: String,
      variables: Option[Map[String, String]] = None,
      types: Map[String, ujson.Value],
      date_created: String,
      date_updated: String
  ) {
    def toModel: ContentTemplate = ContentTemplate(
      sid = ContentTemplate.Sid.unsafe(sid),
      accountSid = TwilioAccount.Sid.unsafe(account_sid),
      friendlyName = friendly_name,
      language = language,
      variables = variables.getOrElse(Map.empty),
      types = types.map { case (k, v) => parseContentType(k, v) }.toList,
      dateCreated = Instant.parse(date_created),
      dateUpdated = Instant.parse(date_updated)
    )
  }

  private object ContentTemplateJsonRep {
    implicit val reader: Reader[ContentTemplateJsonRep] = macroR[ContentTemplateJsonRep]
  }

  // ── ContentTemplateWithApproval ───────────────────────────────────────────

  private final case class ContentTemplateWithApprovalJsonRep(
      sid: String,
      account_sid: String,
      friendly_name: String,
      language: String,
      variables: Option[Map[String, String]] = None,
      types: Map[String, ujson.Value],
      date_created: String,
      date_updated: String,
      approval_requests: Option[WhatsappApprovalJsonRep] = None
  ) {
    def toModel: ContentTemplateWithApproval = ContentTemplateWithApproval(
      template = ContentTemplate(
        sid = ContentTemplate.Sid.unsafe(sid),
        accountSid = TwilioAccount.Sid.unsafe(account_sid),
        friendlyName = friendly_name,
        language = language,
        variables = variables.getOrElse(Map.empty),
        types = types.map { case (k, v) => parseContentType(k, v) }.toList,
        dateCreated = Instant.parse(date_created),
        dateUpdated = Instant.parse(date_updated)
      ),
      approval = approval_requests.map(_.toModel(ContentApproval.ApprovalStatus.Unsubmitted))
    )
  }

  private object ContentTemplateWithApprovalJsonRep {
    implicit val reader: Reader[ContentTemplateWithApprovalJsonRep] =
      macroR[ContentTemplateWithApprovalJsonRep]
  }

  // ── ContentApproval ───────────────────────────────────────────────────────

  private final case class ContentApprovalJsonRep(
      sid: String,
      account_sid: Option[String] = None,
      whatsapp: Option[WhatsappApprovalJsonRep] = None
  ) {
    def toModel: ContentApproval = ContentApproval(
      sid = ContentTemplate.Sid.unsafe(sid),
      accountSid = account_sid.map(TwilioAccount.Sid.unsafe),
      whatsapp = whatsapp.map(_.toModel(ContentApproval.ApprovalStatus.Received))
    )
  }

  private object ContentApprovalJsonRep {
    implicit val reader: Reader[ContentApprovalJsonRep] = macroR[ContentApprovalJsonRep]
  }

  // ── Public parse API ──────────────────────────────────────────────────────

  def parseContentTemplate(json: ujson.Value): Either[String, ContentTemplate] =
    Try(TwilioClientPickler.read[ContentTemplateJsonRep](json.toString).toModel).toEither.left
      .map(_.getMessage)

  def parseContentTemplateWithApproval(
      json: ujson.Value
  ): Either[String, ContentTemplateWithApproval] =
    Try(
      TwilioClientPickler.read[ContentTemplateWithApprovalJsonRep](json.toString).toModel
    ).toEither.left.map(_.getMessage)

  def parseApproval(json: ujson.Value): Either[String, ContentApproval] =
    Try(TwilioClientPickler.read[ContentApprovalJsonRep](json.toString).toModel).toEither.left
      .map(_.getMessage)

  private[content] def parseWhatsappApproval(
      json: ujson.Value,
      defaultStatus: ContentApproval.ApprovalStatus = ContentApproval.ApprovalStatus.Unsubmitted
  ): ContentApproval.WhatsappApproval =
    TwilioClientPickler.read[WhatsappApprovalJsonRep](json.toString).toModel(defaultStatus)

  // ── Content type: keyed/polymorphic, stays manual ─────────────────────────

  def contentTypeToJson(contentType: ContentType): ujson.Value = contentType match {
    case ContentType.Text(body) =>
      ujson.Obj("body" -> body)

    case ContentType.Media(body, media) =>
      ujson.Obj(
        "body"  -> body.fold(ujson.Null: ujson.Value)(ujson.Str(_)),
        "media" -> ujson.Arr.from(media.map(ujson.Str(_)))
      )

    case ContentType.Unknown(_, rawJson) =>
      ujson.read(rawJson)
  }

  private def parseContentType(typeKey: String, json: ujson.Value): ContentType = typeKey match {
    case "twilio/text" =>
      ContentType.Text(json("body").str)

    case "twilio/media" =>
      val body  = json.obj.get("body").flatMap { case ujson.Null => None; case v => Some(v.str) }
      val media = json.obj
        .get("media")
        .flatMap {
          case ujson.Null => None
          case v          => Some(v.arr.map(_.str).toList)
        }
        .getOrElse(List.empty)
      ContentType.Media(body, media)

    case _ =>
      ContentType.Unknown(typeKey, ujson.write(json))
  }
}
