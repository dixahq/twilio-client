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
- Twilio's mutual-exclusion rule is enforced **at compile time, for every
  construction path**:
  - Exactly one of `Body` or `ContentSid` must be set.
  - `MediaUrl` cannot be combined with `ContentSid` (in either call order).
  - `ContentVariables` cannot be set without `ContentSid`.
- Minimal-diff modernization: only the fields involved in the new
  constraint become phantom-typed. The four pre-existing required fields
  (`accountSid`, `from`, `to`, `statusCallback`) keep their current
  `Option.get`-in-`build()` behavior, unchanged.

### Amendment (2026-07-14, during plan-writing): sealing `MessageSendRequest`

`MessageSendRequest` is currently a plain public case class with a public
constructor — `MessageSendTest.scala`'s `Fixture` constructs it directly
(e.g. `MessageSendRequest(accountSid = ..., body = MessageBody(...), ...)`)
for every one of its 8 test cases, **never** going through
`MessageSendRequest.build { ... }`. A phantom-typed `Builder` alone would
only guard construction through `.build()`; direct case-class construction
— the path this repo's own tests already use — would remain completely
unguarded, silently defeating the "enforced at compile time" goal.

Decision: `MessageSendRequest` becomes a `sealed trait` (with a `private`
impl case class), the same pattern the newly-merged `ContentCreateRequest`
already uses. This makes `.build { ... }` the only construction path, so
the compile-time guarantee is real everywhere, at the cost of being a
breaking API change for any external consumer constructing
`MessageSendRequest` directly by name (unknown blast radius — this is a
published library consumed by other services we cannot grep). This
repo's own `MessageSendTest.scala` fixture is migrated to `.build { ... }`
as part of this work (see Testing section).

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

`MessageSendRequestExecutor.MessageSendRequest` becomes a sealed trait
(matching `ContentCreateRequest`'s pattern) with a private impl case class,
so it can only be constructed via `MessageSendRequest.build { ... }`:

```scala
sealed trait MessageSendRequest {
  def accountSid: TwilioAccount.Sid
  def from: MessageSender
  def to: MessageRecipient
  def body: Option[MessageBody]                       // was: MessageBody (required)
  def statusCallback: MessageStatusCallback
  def mediaUrls: Seq[MediaResourceUrl]
  def contentSid: Option[ContentTemplate.Sid]          // new
  def contentVariables: Map[String, String]            // new
}

private final case class MessageSendRequestImpl(
    accountSid: TwilioAccount.Sid,
    from: MessageSender,
    to: MessageRecipient,
    body: Option[MessageBody],
    statusCallback: MessageStatusCallback,
    mediaUrls: Seq[MediaResourceUrl],
    contentSid: Option[ContentTemplate.Sid],
    contentVariables: Map[String, String]
) extends MessageSendRequest
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
`req.contentSid`. Field **order matters** here: the existing tests assert
substrings like `From=...&To=...&Body=...&StatusCallback=...` via
`WireMock.containing(...)`, so `Body`/`ContentSid`(+`ContentVariables`) must
stay in the same slot Body already occupies today, with `StatusCallback`
still last before `MediaUrl`:

```scala
val bodyOrContentFields: Seq[(String, String)] = req.contentSid match {
  case Some(contentSid) =>
    val contentVariablesField =
      if (req.contentVariables.nonEmpty)
        Seq("ContentVariables" -> write(req.contentVariables))
      else
        Seq.empty
    Seq("ContentSid" -> contentSid.toString) ++ contentVariablesField
  case None =>
    req.body.toSeq.map(b => "Body" -> b.toString)
}

val baseFields = Seq(
  "From" -> req.from.asString,
  "To"   -> req.to.asString
) ++ bodyOrContentFields ++ Seq(
  "StatusCallback" -> req.statusCallback.toString
)

val mediaFields = req.mediaUrls.map(url => "MediaUrl" -> url.toString)
val reqEntity    = FormData(baseFields ++ mediaFields: _*).toEntity
```

For the existing Body path this produces the exact same field order as
today (`From&To&Body&StatusCallback&MediaUrl*`); for the new template path
it produces `From&To&ContentSid[&ContentVariables]&StatusCallback`.

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
- The `Fixture` class's three direct `MessageSendRequest(...)` case-class
  constructions (`messageSendRequest`,
  `messageSendRequestWhatsappToPhoneNumber`,
  `messageSendRequestWhatsappToExternalUserId`) are migrated to
  `MessageSendRequest.build { b => b.withAccountSid(...)....withBody(...).build() }`,
  since the sealed trait no longer has a public constructor. All existing
  SMS/MMS/WhatsApp test assertions are otherwise unchanged.

No compile-fail ("shouldNot compile") tests exist elsewhere in this
codebase's test suite today, so none are introduced here — the type-level
guarantees are exercised implicitly by the valid-path tests above compiling
at all under `-Xfatal-warnings`.

## Risks / open questions carried into the implementation plan

- **Accepted breaking change**: sealing `MessageSendRequest` breaks any
  external consumer constructing it directly by name (case-class literal
  or copy-based construction) instead of via `.build { ... }`. This is a
  deliberate trade-off (see Amendment above) to make the compile-time
  mutual-exclusion guarantee real; it should be called out prominently in
  the PR description as a breaking change. This repo tags releases with
  semver (`v4.0.0` is the latest tag), so this should ship as `v5.0.0`, not
  a patch/minor bump — tagging/publishing itself is a separate, deliberate
  release step outside this implementation plan.
- Confirm the exact `Content-Type: application/x-www-form-urlencoded`
  URL-encoding of a JSON object value (e.g. `{` → `%7B`) matches what the
  existing `WireMock.containing(...)` assertion style expects, consistent
  with how other JSON-shaped form values are asserted in this test suite
  (if none exist yet, this will be the first — verify during
  implementation).
