# Template Message Send Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let `messaging.messageSend` send Twilio Content Template messages (`ContentSid` + optional `ContentVariables`) as an alternative to `Body`/`MediaUrl`, with Twilio's mutual-exclusion rule enforced at compile time for every construction path.

**Architecture:** Add `contentSid`/`contentVariables` fields and branch the wire-format construction in `MessageSendRequestExecutorImpl` (Task 1). Then seal `MessageSendRequest` into a `sealed trait` + private impl case class and add a Strategy 2 phantom-type `Builder` (three new dimensions: `HasBodySet`/`HasContentSidSet`/`HasMediaUrlsSet`, plus an evidence trait for "exactly one of Body/ContentSid"), forcing all construction through `.build { ... }` (Task 2).

**Tech Stack:** Scala 2.13.18, Apache Pekko HTTP (`Provided`), upickle for JSON, ScalaTest + WireMock for tests.

**Spec:** `docs/superpowers/specs/2026-07-14-template-message-send-design.md`

## Global Constraints

- Scala 2.13.18, Java 17 (`-release 17`); the compiler runs with `-Xfatal-warnings` — any warning fails the build.
- `Test / compile` depends on `Test / scalafmtCheckAll` — run `sbt scalafmtSbt scalafmtAll` before compiling/testing whenever you've hand-written new code, or `Test / compile` fails on formatting, not on your logic.
- `ContentSid` must reuse `com.dixa.twilio.model.content.ContentTemplate.Sid` (prefix `"HX"`) — do not introduce a new SID type.
- `contentVariables` must be `Map[String, String]`, matching `ContentTemplate.variables`'s shape.
- Form-field order in `createHttpReq` must exactly preserve today's order for the Body path (`From&To&Body&StatusCallback&MediaUrl*`) — existing WireMock assertions match on this literal substring order.
- This is an accepted breaking change (sealing `MessageSendRequest` removes its public constructor and `.copy`). It ships as `v5.0.0` per this repo's semver tags (`v4.0.0` is latest) — tagging/publishing is a separate step outside this plan.

---

### Task 1: Add ContentSid/ContentVariables data model and wire format

**Files:**
- Modify: `src/main/scala/com/dixa/twilio/client/messaging/MessageSendRequestExecutor.scala`
- Modify: `src/main/scala/com/dixa/twilio/client/impl/messaging/MessageSendRequestExecutorImpl.scala`
- Test: `src/test/scala/com/dixa/twilio/client/twilioClient/messaging/MessageSendTest.scala`

**Interfaces:**
- Produces: `MessageSendRequest.body: Option[MessageBody]` (was `MessageBody`, non-`Option`), `MessageSendRequest.contentSid: Option[com.dixa.twilio.model.content.ContentTemplate.Sid]` (new, default `None`), `MessageSendRequest.contentVariables: Map[String, String]` (new, default `Map.empty`). `MessageSendRequest` stays a plain case class in this task — Task 2 seals it.
- Consumes: `com.dixa.twilio.model.content.ContentTemplate.Sid` (already on `master`, prefix `"HX"`, built via `.safe`/`.unsafe`), `upickle.default.write` (already used elsewhere in `client.impl.messaging`, e.g. `ChannelsSendersVerificationRequestExecutorImpl`).

- [ ] **Step 1: Write the failing tests**

Open `src/test/scala/com/dixa/twilio/client/twilioClient/messaging/MessageSendTest.scala`.

Add this import alongside the existing ones (after the `com.dixa.twilio.client.twilioClient.TwilioClientTest` import, line 24):

```scala
import com.dixa.twilio.model.content.ContentTemplate
```

In the `Fixture` class (after the `testStatusCallback` val, line 574), add:

```scala
val contentSid    = ContentTemplate.Sid.unsafe("HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
val encContentSid = encode(contentSid.toString)
```

(Place the `val contentSid` line before the `private def encode(s: String)` definition is used — since `encode` is defined a few lines below `testStatusCallback` in the current file, add `contentSid` right after `testStatusCallback`, and add `encContentSid` right after the existing `val encStatusCallback = encode(testStatusCallback)` line so `encode` is already in scope.)

Add a new top-level describe block, right after the closing `}` of the `"asked to send a whatsapp message"` block (after line 520, before the outer `}` that closes the `when` block at line 521):

```scala
    "asked to send a template message" should {
      "successfully send a template message with a content sid only" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "$from",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "$to",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val reqTemplateEntity =
          s"From=$encFrom&To=$encTo&ContentSid=$encContentSid&StatusCallback=$encStatusCallback"

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqTemplateEntity))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestTemplate = MessageSendRequest(
          accountSid = accountSid,
          from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
          to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
          body = None,
          statusCallback = MessageStatusCallback(new URL(testStatusCallback)),
          contentSid = Some(contentSid)
        )

        val resultFut: Future[Either[MessageSendException, MessageResource]] =
          instance.run(connSettings, requestTemplate)
        resultFut.map(result => assert(result === expected))
      }

      "successfully send a template message with content variables" in {
        val f = new Fixture
        import f._

        val messageSendTwilioSuccessResponse =
          s"""{
             |  "account_sid": "$accountSid",
             |  "api_version": "2010-04-01",
             |  "body": "$messageBody",
             |  "date_created": null,
             |  "date_sent": null,
             |  "date_updated": null,
             |  "direction": "outbound-api",
             |  "error_code": null,
             |  "error_message": null,
             |  "from": "$from",
             |  "messaging_service_sid": null,
             |  "num_media": "0",
             |  "num_segments": "1",
             |  "price": null,
             |  "price_unit": null,
             |  "sid": "SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
             |  "status": "sent",
             |  "subresource_uris": {
             |    "media": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Media.json"
             |  },
             |  "to": "$to",
             |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Messages/SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
             |}""".stripMargin

        val encContentVariables = encode("""{"1":"Jose"}""")
        val reqTemplateEntity =
          s"From=$encFrom&To=$encTo&ContentSid=$encContentSid&ContentVariables=$encContentVariables&StatusCallback=$encStatusCallback"

        wireMockServer.stubFor(
          WireMock
            .post(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$accountSid/Messages.json"))
            .withRequestBody(WireMock.containing(reqTemplateEntity))
            .withBasicAuth(accountSid.toString, authToken.asString)
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(messageSendTwilioSuccessResponse)
            )
        )

        val expected = Right(
          MessageResource(
            accountSid = accountSid,
            body = MessageBody(messageBody),
            dateCreated = None,
            dateSent = None,
            dateUpdated = None,
            direction = MessageDirection.withName("OutboundApi"),
            from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
            messagingServiceSid = None,
            numMedia = 0,
            numSegments = MessageNumSegments(1),
            price = None,
            sid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            status = MessageStatus.withName("Sent"),
            to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
            error = None
          )
        )

        val requestTemplate = MessageSendRequest(
          accountSid = accountSid,
          from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
          to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
          body = None,
          statusCallback = MessageStatusCallback(new URL(testStatusCallback)),
          contentSid = Some(contentSid),
          contentVariables = Map("1" -> "Jose")
        )

        val resultFut: Future[Either[MessageSendException, MessageResource]] =
          instance.run(connSettings, requestTemplate)
        resultFut.map(result => assert(result === expected))
      }
    }
```

- [ ] **Step 2: Run the tests to verify they fail to compile**

Run: `sbt "testOnly com.dixa.twilio.client.twilioClient.messaging.MessageSendTest"`

Expected: **compile failure** — `MessageSendRequest` has no parameter named `contentSid`/`contentVariables`, and `body = None` doesn't type-check against `body: MessageBody`. This is the "red" for this task: the new API surface doesn't exist yet.

- [ ] **Step 3: Implement the data model and wire format**

In `src/main/scala/com/dixa/twilio/client/messaging/MessageSendRequestExecutor.scala`, add the import (after the existing `com.dixa.twilio.client.messaging.MessageSendRequestExecutor.MessageSendException` import):

```scala
import com.dixa.twilio.model.content.ContentTemplate
```

Change the case class and `Builder.build()` (lines 45–100) to:

```scala
  final case class MessageSendRequest(
      accountSid: TwilioAccount.Sid,
      from: MessageSender,
      to: MessageRecipient,
      body: Option[MessageBody],
      statusCallback: MessageStatusCallback,
      mediaUrls: Seq[MediaResourceUrl] = Seq.empty,
      contentSid: Option[ContentTemplate.Sid] = None,
      contentVariables: Map[String, String] = Map.empty
  )
  object MessageSendRequest {
    type BuilderStartState = Builder

    final class Builder private[messaging] (
        accountSid: Option[TwilioAccount.Sid],
        from: Option[MessageSender],
        to: Option[MessageRecipient],
        body: Option[MessageBody],
        statusCallback: Option[MessageStatusCallback],
        mediaUrls: Seq[MediaResourceUrl]
    ) {
      def withAccountSid(accountSid: TwilioAccount.Sid): Builder =
        new Builder(Some(accountSid), from, to, body, statusCallback, mediaUrls)
      def withFrom(from: MessageSender): Builder =
        new Builder(accountSid, Some(from), to, body, statusCallback, mediaUrls)
      def withTo(to: MessageRecipient): Builder =
        new Builder(accountSid, from, Some(to), body, statusCallback, mediaUrls)
      def withBody(body: MessageBody): Builder =
        new Builder(accountSid, from, to, Some(body), statusCallback, mediaUrls)
      def withStatusCallback(statusCallback: MessageStatusCallback): Builder =
        new Builder(accountSid, from, to, body, Some(statusCallback), mediaUrls)
      def withMediaUrls(mediaUrls: Seq[MediaResourceUrl]): Builder =
        new Builder(accountSid, from, to, body, statusCallback, mediaUrls)
      def build(): MessageSendRequest =
        MessageSendRequest(
          accountSid.get,
          from.get,
          to.get,
          body,
          statusCallback.get,
          mediaUrls
        )
    }

    object Builder {
      val empty: BuilderStartState = new BuilderStartState(
        None,
        None,
        None,
        None,
        None,
        Seq.empty
      )
    }

    def build(fun: BuilderStartState => MessageSendRequest): MessageSendRequest =
      fun(Builder.empty)
  }
```

(The only change from today's code is `body: Option[MessageBody]` in the case class and dropping `.get` on `body` inside `build()` — everything else in the `Builder` is untouched in this task; Task 2 rewrites it.)

In `src/test/scala/.../MessageSendTest.scala`, fix the 3 pre-existing direct constructions in `Fixture` (lines 600–622) — wrap `body` in `Some(...)`:

```scala
    val messageSendRequest = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.E164(PhoneNumberE164.unsafe(from)),
      to = MessageRecipient.E164(PhoneNumberE164.unsafe(to)),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )

    val messageSendRequestWhatsappToPhoneNumber = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.Whatsapp(fromWhatsapp),
      to = MessageRecipient.WhatsappNumber(toWhatsapp),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )

    val messageSendRequestWhatsappToExternalUserId = MessageSendRequest(
      accountSid = accountSid,
      from = MessageSender.Whatsapp(fromWhatsapp),
      to = MessageRecipient.WhatsappId(toExternalUserIdWhatsapp),
      body = Some(MessageBody(messageBody)),
      statusCallback = MessageStatusCallback(new URL(testStatusCallback))
    )
```

In `src/main/scala/com/dixa/twilio/client/impl/messaging/MessageSendRequestExecutorImpl.scala`, add the import (after the `java.time.Instant` import):

```scala
import upickle.default.write
```

Replace `createHttpReq` (lines 54–69) with:

```scala
  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: MessageSendRequest
  ): Either[MessageSendException, HttpRequest] = {
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
    val reqEntity   = FormData(baseFields ++ mediaFields: _*).toEntity

    createHttpRequestFor(s"/2010-04-01/Accounts/${req.accountSid}/Messages.json", connSettings)
      .map(_.withEntity(reqEntity))
  }
```

- [ ] **Step 4: Format and run the tests again to verify they pass**

Run: `sbt scalafmtSbt scalafmtAll`
Run: `sbt "testOnly com.dixa.twilio.client.twilioClient.messaging.MessageSendTest"`

Expected: **all tests PASS** — the 2 new template tests, plus all pre-existing SMS/MMS/WhatsApp tests (unaffected, since the Body-path field order is unchanged).

- [ ] **Step 5: Commit**

```bash
git add src/main/scala/com/dixa/twilio/client/messaging/MessageSendRequestExecutor.scala \
        src/main/scala/com/dixa/twilio/client/impl/messaging/MessageSendRequestExecutorImpl.scala \
        src/test/scala/com/dixa/twilio/client/twilioClient/messaging/MessageSendTest.scala
git commit -m "feat(IO-4437): add ContentSid/ContentVariables to MessageSendRequest

Extends messaging.messageSend to support Twilio Content Template sends
(ContentSid + optional ContentVariables) as an alternative to Body/MediaUrl,
on the same Messages endpoint. No compile-time mutual-exclusion enforcement
yet — that lands in the next commit."
```

---

### Task 2: Seal `MessageSendRequest` and enforce mutual exclusion at compile time

**Files:**
- Modify: `src/main/scala/com/dixa/twilio/client/messaging/MessageSendRequestExecutor.scala`
- Test: `src/test/scala/com/dixa/twilio/client/twilioClient/messaging/MessageSendTest.scala`

**Interfaces:**
- Consumes: `MessageSendRequest.body`/`contentSid`/`contentVariables`/`mediaUrls` from Task 1 (unchanged names/types — only the construction mechanism changes).
- Produces: `MessageSendRequest` becomes a `sealed trait` (no public constructor, no `.copy`); `MessageSendRequest.build { b => ... }` is the only construction path, with `b.withBody(...)`, `b.withContentSid(...)`, `b.withMediaUrls(...)`, `b.withContentVariables(...)` mutually constrained at compile time as described in the spec.

- [ ] **Step 1: Seal the request type and add the phantom-type builder**

Replace the entire `MessageSendRequestExecutor.scala` object body (everything inside `object MessageSendRequestExecutor { ... }`, i.e. from `final case class MessageSendRequest(` through the `object MessageSendRequest { ... }` closing brace — keep `MessageSendException` and everything below it unchanged) with:

```scala
  sealed trait MessageSendRequest {
    def accountSid: TwilioAccount.Sid
    def from: MessageSender
    def to: MessageRecipient
    def body: Option[MessageBody]
    def statusCallback: MessageStatusCallback
    def mediaUrls: Seq[MediaResourceUrl]
    def contentSid: Option[ContentTemplate.Sid]
    def contentVariables: Map[String, String]
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

  object MessageSendRequest {

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

      // "Advanced Strategy 2" evidence trait (see doc/client-implementation-doc.md):
      // only the two valid end states get an implicit instance, so build() rejects
      // "neither set" for free. "Both set" can never arise — see the with* guards below.
      sealed trait ExactlyOneOfBodyOrContentSid[B <: HasBodySet, C <: HasContentSidSet]
      object ExactlyOneOfBodyOrContentSid {
        implicit val bodySet
            : ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] =
          new ExactlyOneOfBodyOrContentSid[HasBodySetTrue, HasContentSidSetFalse] {}
        implicit val contentSidSet
            : ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] =
          new ExactlyOneOfBodyOrContentSid[HasBodySetFalse, HasContentSidSetTrue] {}
      }
    }

    import PhantomTypes._

    type BuilderStartState = Builder[HasBodySetFalse, HasContentSidSetFalse, HasMediaUrlsSetFalse]

    final class Builder[
        BodySet <: HasBodySet,
        ContentSidSet <: HasContentSidSet,
        MediaUrlsSet <: HasMediaUrlsSet
    ] private[messaging] (
        accountSid: Option[TwilioAccount.Sid],
        from: Option[MessageSender],
        to: Option[MessageRecipient],
        body: Option[MessageBody],
        statusCallback: Option[MessageStatusCallback],
        mediaUrls: Seq[MediaResourceUrl],
        contentSid: Option[ContentTemplate.Sid],
        contentVariables: Map[String, String]
    ) {
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          Some(accountSid),
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withFrom(from: MessageSender): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          Some(from),
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withTo(to: MessageRecipient): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          Some(to),
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withStatusCallback(
          statusCallback: MessageStatusCallback
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          Some(statusCallback),
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withBody(
          body: MessageBody
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetFalse
      ): Builder[HasBodySetTrue, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          Some(body),
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withMediaUrls(
          mediaUrls: Seq[MediaResourceUrl]
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetFalse
      ): Builder[BodySet, ContentSidSet, HasMediaUrlsSetTrue] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def withContentSid(
          contentSid: ContentTemplate.Sid
      )(
          implicit
          evBody: BodySet =:= HasBodySetFalse,
          evMedia: MediaUrlsSet =:= HasMediaUrlsSetFalse
      ): Builder[BodySet, HasContentSidSetTrue, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          Some(contentSid),
          contentVariables
        )

      def withContentVariables(
          contentVariables: Map[String, String]
      )(
          implicit ev: ContentSidSet =:= HasContentSidSetTrue
      ): Builder[BodySet, ContentSidSet, MediaUrlsSet] =
        new Builder(
          accountSid,
          from,
          to,
          body,
          statusCallback,
          mediaUrls,
          contentSid,
          contentVariables
        )

      def build()(
          implicit evValid: ExactlyOneOfBodyOrContentSid[BodySet, ContentSidSet]
      ): MessageSendRequest =
        MessageSendRequestImpl(
          accountSid.get,
          from.get,
          to.get,
          body,
          statusCallback.get,
          mediaUrls,
          contentSid,
          contentVariables
        )
    }

    object Builder {
      val empty: BuilderStartState =
        new Builder(None, None, None, None, None, Seq.empty, None, Map.empty)
    }

    def build(fun: BuilderStartState => MessageSendRequest): MessageSendRequest =
      fun(Builder.empty)
  }
```

Also update the trait's type parameter reference (near the top of the file) from `MessageSendRequestExecutor.MessageSendRequest.Builder` to `MessageSendRequestExecutor.MessageSendRequest.BuilderStartState`:

```scala
trait MessageSendRequestExecutor
    extends SingleRequestExecutor[
      MessageSendRequestExecutor.MessageSendRequest,
      MessageSendRequestExecutor.MessageSendException,
      MessageResource,
      MessageSendRequestExecutor.MessageSendRequest.BuilderStartState
    ] {

  override protected final type ApiExceptionWrapper = MessageSendException.Api

  override protected final type UnspecifiedException = MessageSendException.Unspecified

  override protected final def createBuilderStartState()
      : MessageSendRequestExecutor.MessageSendRequest.BuilderStartState =
    MessageSendRequestExecutor.MessageSendRequest.Builder.empty
}
```

- [ ] **Step 2: Run the tests to verify they fail to compile**

Run: `sbt "testOnly com.dixa.twilio.client.twilioClient.messaging.MessageSendTest"`

Expected: **compile failure** at every direct-construction/`.copy` call site in `MessageSendTest.scala` — `MessageSendRequest` is now abstract (sealed trait, no accessible constructor) and has no `.copy` method. This is the "red" that proves the seal works: 7 sites break —
`messageSendRequest`, `messageSendRequestWhatsappToPhoneNumber`, `messageSendRequestWhatsappToExternalUserId` (Fixture, direct construction), the two `.copy(mediaUrls = ...)` calls in the MMS tests, and the two `requestTemplate` vals added in Task 1 (direct construction).

- [ ] **Step 3: Migrate all call sites to `.build { ... }`**

In `Fixture` (replacing the 3 vals from Task 1's Step 3 edit):

```scala
    val messageSendRequest = MessageSendRequest.build { b =>
      b.withAccountSid(accountSid)
        .withFrom(MessageSender.E164(PhoneNumberE164.unsafe(from)))
        .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe(to)))
        .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
        .withBody(MessageBody(messageBody))
        .build()
    }

    val messageSendRequestWhatsappToPhoneNumber = MessageSendRequest.build { b =>
      b.withAccountSid(accountSid)
        .withFrom(MessageSender.Whatsapp(fromWhatsapp))
        .withTo(MessageRecipient.WhatsappNumber(toWhatsapp))
        .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
        .withBody(MessageBody(messageBody))
        .build()
    }

    val messageSendRequestWhatsappToExternalUserId = MessageSendRequest.build { b =>
      b.withAccountSid(accountSid)
        .withFrom(MessageSender.Whatsapp(fromWhatsapp))
        .withTo(MessageRecipient.WhatsappId(toExternalUserIdWhatsapp))
        .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
        .withBody(MessageBody(messageBody))
        .build()
    }
```

Replace the single-media-url `.copy` call (in `"successfully send an mms with a single media url"`, originally lines 105–106):

```scala
        val requestWithMedia = MessageSendRequest.build { b =>
          b.withAccountSid(accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe(from)))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe(to)))
            .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
            .withBody(MessageBody(messageBody))
            .withMediaUrls(Seq(MediaResourceUrl(mediaUrl1.toString)))
            .build()
        }
```

Replace the multi-media-url `.copy` call (in `"successfully send an mms with multiple media urls"`, originally lines 183–188):

```scala
        val requestWithMedia = MessageSendRequest.build { b =>
          b.withAccountSid(accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe(from)))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe(to)))
            .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
            .withBody(MessageBody(messageBody))
            .withMediaUrls(
              Seq(MediaResourceUrl(mediaUrl1.toString), MediaResourceUrl(mediaUrl2.toString))
            )
            .build()
        }
```

Replace the two `requestTemplate` vals added in Task 1. For `"successfully send a template message with a content sid only"`:

```scala
        val requestTemplate = MessageSendRequest.build { b =>
          b.withAccountSid(accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe(from)))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe(to)))
            .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
            .withContentSid(contentSid)
            .build()
        }
```

For `"successfully send a template message with content variables"`:

```scala
        val requestTemplate = MessageSendRequest.build { b =>
          b.withAccountSid(accountSid)
            .withFrom(MessageSender.E164(PhoneNumberE164.unsafe(from)))
            .withTo(MessageRecipient.E164(PhoneNumberE164.unsafe(to)))
            .withStatusCallback(MessageStatusCallback(new URL(testStatusCallback)))
            .withContentSid(contentSid)
            .withContentVariables(Map("1" -> "Jose"))
            .build()
        }
```

- [ ] **Step 4: Format and run the full test file to verify everything passes**

Run: `sbt scalafmtSbt scalafmtAll`
Run: `sbt "testOnly com.dixa.twilio.client.twilioClient.messaging.MessageSendTest"`

Expected: **all tests PASS** (the 7 pre-existing tests plus the 2 template tests, all now built via `.build { ... }`).

- [ ] **Step 5: Run the full project test suite**

Run: `sbt "+test"`

Expected: **all tests PASS** across the whole project — confirms no other file in the repo constructed `MessageSendRequest` directly (already verified by grep during planning: the only call sites were in `MessageSendTest.scala`), and that sealing didn't break an unrelated consumer within this repo.

- [ ] **Step 6: Commit**

```bash
git add src/main/scala/com/dixa/twilio/client/messaging/MessageSendRequestExecutor.scala \
        src/test/scala/com/dixa/twilio/client/twilioClient/messaging/MessageSendTest.scala
git commit -m "feat(IO-4437)!: enforce Body/ContentSid mutual exclusion at compile time

Seal MessageSendRequest (sealed trait + private impl case class, matching
ContentCreateRequest's pattern) so MessageSendRequest.build { ... } is the
only construction path. Add a Strategy 2 phantom-type builder so that:
exactly one of Body/ContentSid must be set, MediaUrl cannot combine with
ContentSid in either call order, and ContentVariables requires ContentSid.

BREAKING CHANGE: MessageSendRequest no longer has a public constructor or
.copy — external consumers constructing it directly must switch to
MessageSendRequest.build { ... }. Ships as v5.0.0."
```

---

## Self-Review Notes

**Spec coverage:**
- ContentSid/ContentVariables data model → Task 1, Step 3.
- Compile-time mutual exclusion (Body/ContentSid, MediaUrl/ContentSid, ContentVariables-requires-ContentSid) → Task 2, Step 1.
- Sealing `MessageSendRequest` (spec Amendment) → Task 2, Step 1.
- HTTP layer field-order preservation → Task 1, Step 3.
- Testing (new template tests + fixture/`.copy` migration) → Task 1 Step 1, Task 2 Step 3.
- Breaking-change/versioning note → Global Constraints, Task 2 commit message.

**Type consistency:** `contentSid: Option[ContentTemplate.Sid]`, `contentVariables: Map[String, String]` are the same names/types from Task 1's case class fields through Task 2's sealed trait accessors, the impl class, and both test tasks — verified consistent throughout.

**Placeholder scan:** no TBD/TODO; every step shows complete code, not a description of code.
