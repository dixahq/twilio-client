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

import com.dixa.twilio.model.content.{
  ContentApproval,
  ContentTemplate,
  ContentTemplateWithApproval,
  ContentType
}
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant
import scala.util.{Failure, Success, Try}

private[content] object ContentJsonRep {

  def parseContentTemplate(json: ujson.Value): Either[String, ContentTemplate] =
    Try {
      val sid          = ContentTemplate.Sid.unsafe(json("sid").str)
      val accountSid   = TwilioAccount.Sid.unsafe(json("account_sid").str)
      val friendlyName = json("friendly_name").str
      val language     = json("language").str
      val variables    = json.obj.get("variables") match {
        case Some(ujson.Null) | None => Map.empty[String, String]
        case Some(v)                 => v.obj.map { case (k, vv) => k -> vv.str }.toMap
      }
      val types       = json("types").obj.map { case (k, v) => k -> parseContentType(k, v) }.toMap
      val dateCreated = Instant.parse(json("date_created").str)
      val dateUpdated = Instant.parse(json("date_updated").str)
      ContentTemplate(
        sid,
        accountSid,
        friendlyName,
        language,
        variables,
        types,
        dateCreated,
        dateUpdated
      )
    } match {
      case Success(t)  => Right(t)
      case Failure(ex) => Left(ex.getMessage)
    }

  private def parseContentType(typeKey: String, json: ujson.Value): ContentType = typeKey match {
    case "twilio/text" =>
      ContentType.Text(json("body").str)

    case "twilio/media" =>
      val body  = optStr(json, "body")
      val media = json.obj.get("media").map(_.arr.map(_.str).toList).getOrElse(List.empty)
      ContentType.Media(body, media)

    case _ =>
      ContentType.Unknown(typeKey, ujson.write(json))
  }

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

  def parseContentTemplateWithApproval(
      json: ujson.Value
  ): Either[String, ContentTemplateWithApproval] =
    parseContentTemplate(json).map { template =>
      val whatsappApproval = json.obj.get("approval_requests").flatMap {
        case ujson.Null => None
        case ar         => Some(parseWhatsappApproval(ar))
      }
      ContentTemplateWithApproval(template, whatsappApproval)
    }

  def parseApproval(json: ujson.Value): Either[String, ContentApproval] =
    Try {
      val sid        = ContentTemplate.Sid.unsafe(json("sid").str)
      val accountSid = json.obj.get("account_sid").map(v => TwilioAccount.Sid.unsafe(v.str))
      val whatsapp   = json.obj.get("whatsapp").flatMap {
        case ujson.Null => None
        case w          => Some(parseWhatsappApproval(w, ContentApproval.ApprovalStatus.Received))
      }
      ContentApproval(sid, accountSid, whatsapp)
    } match {
      case Success(a)  => Right(a)
      case Failure(ex) => Left(ex.getMessage)
    }

  private[content] def parseWhatsappApproval(
      json: ujson.Value,
      defaultStatus: ContentApproval.ApprovalStatus = ContentApproval.ApprovalStatus.Unsubmitted
  ): ContentApproval.WhatsappApproval = {
    val status = ContentApproval.ApprovalStatus.values
      .find(_.twilioString == json("status").str)
      .getOrElse(defaultStatus)
    val rejectionReason = json.obj.get("rejection_reason").flatMap {
      case ujson.Null => None
      case r          => if (r.str.isEmpty) None else Some(r.str)
    }
    val allowCategoryChange = json.obj.get("allow_category_change").exists {
      case ujson.Bool(b) => b
      case _             => false
    }
    ContentApproval.WhatsappApproval(
      name = optNonEmptyStr(json, "name"),
      category = optNonEmptyStr(json, "category"),
      contentType = optNonEmptyStr(json, "content_type"),
      status = status,
      rejectionReason = rejectionReason,
      allowCategoryChange = allowCategoryChange
    )
  }

  private def optStr(json: ujson.Value, key: String): Option[String] =
    json.obj.get(key).flatMap {
      case ujson.Null => None
      case v          => Some(v.str)
    }

  private def optNonEmptyStr(json: ujson.Value, key: String): Option[String] =
    json.obj.get(key).flatMap {
      case ujson.Null         => None
      case v if v.str.isEmpty => None
      case v                  => Some(v.str)
    }
}
