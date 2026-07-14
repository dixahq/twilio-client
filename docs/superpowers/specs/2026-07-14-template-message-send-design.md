# Design: Template message support in `MessageSendRequestExecutor`

- **Ticket**: [IO-4437](https://dixa-product.atlassian.net/browse/IO-4437) — "[Conversation] Implement sending template messages in twilio-client"
- **Date**: 2026-07-14
- **Author**: José Pintado (with Claude)

## Context

Twilio's Content Template Builder lets a message be sent by referencing a
pre-approved content template (`ContentSid`) with placeholder values
(`ContentVariables`), instead of a literal `Body`. This is the same
`POST /2010-04-01/Accounts/{AccountSid}/Messages.json` endpoint the client
already uses for `messaging.messageSend` — Twilio just accepts a different,
mutually-exclusive combination of form parameters on it:

- `ContentSid` (string, identifies the template)
- `ContentVariables` (optional JSON string, up to 100 key/value pairs,
  substituted into the template's placeholders)
- `Body` and `MediaUrl` **must not** be present when `ContentSid` is used.

Reference: <https://www.twilio.com/docs/content/send-templates-created-with-the-content-template-builder>

The Content API for *managing* templates (create/fetch/delete/approve) has
just landed on `master` (`5c19d66`, "feat: add Content API"). It defines
`ContentTemplate.Sid` (`com.dixa.twilio.model.content.ContentTemplate.Sid`),
a `SidAbstract` subtype validated against the `"HX"` prefix via
`ContentTemplate.Sid.safe`/`.unsafe`. This is the natural type to reuse for
the new `ContentSid` parameter — no new SID type needed.

The existing `messageSend` builder
(`client/messaging/MessageSendRequestExecutor.scala`) is "legacy style": a
plain `Option`-based `Builder` where `build()` calls `.get` on each field,
with no compile-time required-field checking. The ticket description
("extending send message functionality... with content SID and variables")
points at extending this existing executor rather than adding a parallel
one.

`doc/client-implementation-doc.md` documents a "Strategy 2" phantom-type
builder pattern (exemplified by `CallCreateRequestExecutor`) specifically
for cross-field constraints like mutual exclusion and conditional
requirements — exactly what `Body`/`MediaUrl` vs `ContentSid` needs. The
newly-merged `ContentCreateRequestExecutor` already uses the simpler
Strategy 1 phantom-type builder, showing the codebase is actively adopting
this pattern.

## Goals

- `messaging.messageSend` can send a template message: `ContentSid` +
  optional `ContentVariables`, as an alternative to `Body`/`MediaUrl`.
- Twilio's mutual-exclusion rule is enforced **at compile time**:
  - Exactly one of `Body` or `ContentSid` must be set.
  - `MediaUrl` cannot be combined with `ContentSid` (in either call order).
  - `ContentVariables` cannot be set without `ContentSid`.
- Minimal-diff modernization: only the fields involved in the new
  constraint become phantom-typed. The four pre-existing required fields
  (`accountSid`, `from`, `to`, `statusCallback`) keep their current
  `Option.get`-in-`build()` behavior, unchanged.

## Non-goals

- Modernizing `accountSid`/`from`/`to`/`statusCallback` to phantom types.
  Out of scope for this ticket; not requested and adds risk for no benefit
  here.
- Any change to the Content API template-management executors
  (`ContentCreateRequestExecutor` etc.) — those are unrelated, already
  shipped.
- Modeling specific Twilio error codes for template-send failures (e.g.
  invalid/unapproved `ContentSid`). No specific codes are documented for
  this beyond generic 400s, so they fall through to the existing
  `MessageSendException.Api` wrapper, same as any other unmapped 4xx.

## Data model changes

`MessageSendRequestExecutor.MessageSendRequest`:

```scala
final case class MessageSendRequest(
    accountSid: TwilioAccount.Sid,
    from: MessageSender,
    to: MessageRecipient,
    body: Option[MessageBody],                    // was: MessageBody (required)
    statusCallback: MessageStatusCallback,
    mediaUrls: Seq[MediaResourceUrl] = Seq.empty,
    contentSid: Option[ContentTemplate.Sid] = None,        // new
    contentVariables: Map[String, String] = Map.empty      // new
)
```

`contentVariables` uses `Map[String, String]`, matching
`ContentTemplate.variables`'s shape for consistency with the Content API
models.

## Builder: phantom-type mutual exclusion

Three new phantom dimensions are added to the existing `Builder`, alongside
the untouched `Option`-based `accountSid`/`from`/`to`/`statusCallback`:

```scala
object PhantomTypes {
  sealed trait HasBodySet
  sealed trait HasBodySetTrue  extends HasBodySet
  sealed trait HasBodySetFalse extends HasBodySet

  sealed trait HasContentSidSet
  sealed trait HasContentSidSetTrue  extends HasContentSidSet
  sealed trait HasContentSidSetFalse extends HasContentSidSet

  sealed trait HasMediaUrlsSet
  sealed trait HasMediaUrlsSetTrue  extends HasMediaUrlsSet
  sealed trait HasMediaUrlsSetFalse extends HasMediaUrlsSet

  // "Advanced Strategy 2" evidence trait (doc's ValidPolicyCombinations
  // idiom) — only the two valid end states get an implicit instance, so
  // "neither set" is rejected by build() for free (and "both set" can
  // never arise — see with* guards below).
  sealed trait ExactlyOneOfBodyOrContentSid[B <: HasBodySet, C <: HasContentSidSet]
  object ExactlyOneOfBodyOrContentSid {
    implicit val bodySet: ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] =
      new ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] {}
    implicit val contentSidSet: ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] =
      new ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] {}
  }
}
```

`Builder` gains three type parameters (`BodySet`, `ContentSidSet`,
`MediaUrlsSet`), all starting at `*SetFalse`:

| Method                   | Requires (implicit evidence)                                          | Flips                    |
|--------------------------|-------------------------------------------------------------------------|---------------------------|
| `withBody`               | `ContentSidSet =:= HasContentSidSetFalse`                              | `BodySet → True`          |
| `withContentSid`         | `BodySet =:= HasBodySetFalse`, `MediaUrlsSet =:= HasMediaUrlsSetFalse`  | `ContentSidSet → True`    |
| `withMediaUrls`          | `ContentSidSet =:= HasContentSidSetFalse`                              | `MediaUrlsSet → True`     |
| `withContentVariables`   | `ContentSidSet =:= HasContentSidSetTrue`                               | *(none — fully optional)* |
| `build()`                | *(new)* `ExactlyOneOfBodyOrContentSid[BodySet, ContentSidSet]`         | —                          |

This gives, at compile time:

- `withContentSid(...).withBody(...)` — **compile error** (Body after
  ContentSid).
- `withBody(...).withContentSid(...)` — **compile error** (ContentSid after
  Body).
- `withMediaUrls(...).withContentSid(...)` and
  `withContentSid(...).withMediaUrls(...)` — **compile error** either order.
- `withContentVariables(...)` before `withContentSid(...)` — **compile
  error**.
- `build()` with neither `withBody` nor `withContentSid` called — **compile
  error** (no matching `ExactlyOneOfBodyOrContentSid` instance for
  `(False, False)`).
- `withBody(...).withMediaUrls(...)` — still allowed (MMS, unchanged).

## HTTP layer

`MessageSendRequestExecutorImpl.createHttpReq` branches on
`req.contentSid`:

```scala
val templateFields: Seq[(String, String)] = req.contentSid match {
  case Some(contentSid) =>
    Seq("ContentSid" -> contentSid.toString) ++
      (if (req.contentVariables.nonEmpty)
         Seq("ContentVariables" -> write(req.contentVariables))
       else Seq.empty)
  case None =>
    req.body.toSeq.map(b => "Body" -> b.toString) ++
      req.mediaUrls.map(url => "MediaUrl" -> url.toString)
}

val baseFields = Seq(
  "From"           -> req.from.asString,
  "To"             -> req.to.asString,
  "StatusCallback" -> req.statusCallback.toString
)

val reqEntity = FormData(baseFields ++ templateFields: _*).toEntity
```

`write(req.contentVariables)` uses `upickle.default.write` (already used
elsewhere in `client.impl.messaging`, e.g.
`ChannelsSendersVerificationRequestExecutorImpl`), which encodes a
`Map[String, String]` directly as a JSON object string (e.g.
`{"1":"Jose"}`) with no intermediate case class needed.

Endpoint, method, response parsing (`parseHttpResponse`), and
`MessageJsonRep` are all unchanged — Twilio's Message resource response
doesn't echo `ContentSid`/`ContentVariables` back.

## Testing

New `"asked to send a template message"` describe-block in
`MessageSendTest.scala`, mirroring the existing `Fixture` + WireMock
pattern used for the SMS/MMS/WhatsApp groups:

- Send with `ContentSid` only → asserts `ContentSid=HX...` present,
  `Body`/`MediaUrl`/`ContentVariables` absent from the request body.
- Send with `ContentSid` + `ContentVariables` → asserts both
  `ContentSid=HX...` and the URL-encoded JSON `ContentVariables=...`
  substring.
- Existing SMS/MMS/WhatsApp tests continue passing unchanged (`body` call
  sites in tests already call `.withBody(...)`, which remains valid).

No compile-fail ("shouldNot compile") tests exist elsewhere in this
codebase's test suite today, so none are introduced here — the type-level
guarantees are exercised implicitly by the valid-path tests above compiling
at all under `-Xfatal-warnings`.

## Risks / open questions carried into the implementation plan

- Confirm no external consumer of this library pattern-matches on
  `MessageSendRequest.body: MessageBody` expecting it non-`Option` (unlikely
  given construction is builder-only, but worth a final grep before
  merging).
- Confirm the exact `Content-Type: application/x-www-form-urlencoded`
  URL-encoding of a JSON object value (e.g. `{` → `%7B`) matches what the
  existing `WireMock.containing(...)` assertion style expects, consistent
  with how other JSON-shaped form values are asserted in this test suite
  (if none exist yet, this will be the first — verify during
  implementation).
