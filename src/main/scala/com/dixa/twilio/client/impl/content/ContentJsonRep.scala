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
    case "twilio/text"        => ContentType.Text(json("body").str)
    case "twilio/quick-reply" =>
      val body    = json("body").str
      val actions = json.obj
        .get("actions")
        .map(
          _.arr
            .map { a =>
              ContentType.QuickReplyAction(a("title").str, a("id").str)
            }
            .toList
        )
        .getOrElse(List.empty)
      ContentType.QuickReply(body, actions)
    case "twilio/card" =>
      val title = json.obj.get("title") match {
        case Some(ujson.Null) | None => None
        case Some(v)                 => Some(v.str)
      }
      val subtitle = json.obj.get("subtitle") match {
        case Some(ujson.Null) | None => None
        case Some(v)                 => Some(v.str)
      }
      val body = json.obj.get("body") match {
        case Some(ujson.Null) | None => None
        case Some(v)                 => Some(v.str)
      }
      val media   = json.obj.get("media").map(_.arr.map(_.str).toList).getOrElse(List.empty)
      val actions =
        json.obj.get("actions").map(_.arr.map(parseCardAction).toList).getOrElse(List.empty)
      ContentType.Card(title, subtitle, body, media, actions)
    case _ =>
      ContentType.Unknown(typeKey, ujson.write(json))
  }

  private def parseCardAction(json: ujson.Value): ContentType.CardAction = {
    val title   = json("title").str
    val rawType = json.obj.get("type").map(_.str).getOrElse("")
    rawType match {
      case "URL"          => ContentType.CardAction.Url(title, json("url").str)
      case "PHONE_NUMBER" => ContentType.CardAction.PhoneNumber(title, json("phone").str)
      case "QUICK_REPLY"  => ContentType.CardAction.QuickReply(title, json("id").str)
      case other          => ContentType.CardAction.Unknown(title, other)
    }
  }

  def contentTypeToJson(contentType: ContentType): ujson.Value = contentType match {
    case ContentType.Text(body) =>
      ujson.Obj("body" -> body)
    case ContentType.QuickReply(body, actions) =>
      ujson.Obj(
        "body"    -> body,
        "actions" -> ujson.Arr.from(actions.map(a => ujson.Obj("title" -> a.title, "id" -> a.id)))
      )
    case ContentType.Card(title, subtitle, body, media, actions) =>
      ujson.Obj(
        "title"    -> title.fold(ujson.Null: ujson.Value)(ujson.Str(_)),
        "subtitle" -> subtitle.fold(ujson.Null: ujson.Value)(ujson.Str(_)),
        "body"     -> body.fold(ujson.Null: ujson.Value)(ujson.Str(_)),
        "media"    -> ujson.Arr.from(media.map(ujson.Str(_))),
        "actions"  -> ujson.Arr.from(actions.map(cardActionToJson))
      )
    case ContentType.Unknown(_, rawJson) =>
      ujson.read(rawJson)
  }

  private def cardActionToJson(action: ContentType.CardAction): ujson.Value = action match {
    case ContentType.CardAction.Url(title, url) =>
      ujson.Obj("type" -> "URL", "title" -> title, "url" -> url)
    case ContentType.CardAction.PhoneNumber(title, phone) =>
      ujson.Obj("type" -> "PHONE_NUMBER", "title" -> title, "phone" -> phone)
    case ContentType.CardAction.QuickReply(title, id) =>
      ujson.Obj("type" -> "QUICK_REPLY", "title" -> title, "id" -> id)
    case ContentType.CardAction.Unknown(title, rawType) =>
      ujson.Obj("type" -> rawType, "title" -> title)
  }

  def parseContentTemplateWithApproval(
      json: ujson.Value
  ): Either[String, ContentTemplateWithApproval] =
    parseContentTemplate(json).map { template =>
      val whatsappApproval = json.obj.get("approval_requests").flatMap {
        case ujson.Null => None
        case ar         =>
          val status = ContentApproval.ApprovalStatus.values
            .find(_.twilioString == ar("status").str)
            .getOrElse(ContentApproval.ApprovalStatus.Unsubmitted)
          val rejectionReason = ar.obj.get("rejection_reason").flatMap {
            case ujson.Null => None
            case r          => if (r.str.isEmpty) None else Some(r.str)
          }
          val allowCategoryChange = ar.obj.get("allow_category_change").exists {
            case ujson.Bool(b) => b
            case _             => false
          }
          Some(
            ContentApproval.WhatsappApproval(
              name = ar("name").str,
              category = ar("category").str,
              contentType = ar("content_type").str,
              status = status,
              rejectionReason = rejectionReason,
              allowCategoryChange = allowCategoryChange
            )
          )
      }
      ContentTemplateWithApproval(template, whatsappApproval)
    }

  def parseApproval(json: ujson.Value): Either[String, ContentApproval] =
    Try {
      val sid        = ContentTemplate.Sid.unsafe(json("sid").str)
      val accountSid = json.obj.get("account_sid").map(v => TwilioAccount.Sid.unsafe(v.str))
      val whatsapp   = json.obj.get("whatsapp").flatMap {
        case ujson.Null => None
        case w          =>
          val status = ContentApproval.ApprovalStatus.values
            .find(_.twilioString == w("status").str)
            .getOrElse(ContentApproval.ApprovalStatus.Received)
          val rejectionReason = w.obj.get("rejection_reason").flatMap {
            case ujson.Null => None
            case r          => if (r.str.isEmpty) None else Some(r.str)
          }
          val allowCategoryChange = w.obj.get("allow_category_change").exists {
            case ujson.Bool(b) => b
            case _             => false
          }
          Some(
            ContentApproval.WhatsappApproval(
              name = w("name").str,
              category = w("category").str,
              contentType = w("content_type").str,
              status = status,
              rejectionReason = rejectionReason,
              allowCategoryChange = allowCategoryChange
            )
          )
      }
      ContentApproval(sid, accountSid, whatsapp)
    } match {
      case Success(a)  => Right(a)
      case Failure(ex) => Left(ex.getMessage)
    }
}
