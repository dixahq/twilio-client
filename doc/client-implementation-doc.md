# Twilio Client Implementation Guide

## Overview

The twilio-client library provides a type-safe Scala client for communicating with the Twilio API. It is organised into two main modules:

- **Models** (`com.dixa.twilio.model`) — Scala representations of Twilio entities (e.g. `MessageResource`, `Recording`, `Call`).
- **Client** (`com.dixa.twilio.client`) — Request execution logic against the Twilio HTTP API.

The client is built on top of Apache Pekko HTTP and uses Pekko Streams for
paginated (streaming) responses. Pekko is declared as a `Provided` dependency,
so consumers supply their own version.

---

## Client Structure

### TwilioClient

`TwilioClient` is the main entry point. It does not expose Twilio operations
directly but instead provides access to **sub-clients**, one per Twilio API
domain:

```scala
trait TwilioClient {
  def iam: TwilioClientIam
  def voice: TwilioClientVoice
  def messaging: TwilioClientMessaging
  def phoneNumber: TwilioClientPhoneNumber
  def general: TwilioClientGeneral
  def stunTurn: TwilioClientStunTurn
}
```

An instance is obtained via `TwilioClient.defaultImpl()` (requires an implicit `ClassicActorSystemProvider`).

### Sub-clients

Each sub-client (e.g. `TwilioClientMessaging`) is a trait that exposes `RequestExecutor` instances for the operations it supports:

```scala
trait TwilioClientMessaging {
  def messageSend: MessageSendRequestExecutor
  def servicesRead: ServicesReadRequestExecutor
  // ...
}
```

---

## RequestExecutor

`RequestExecutor[Req, Err]` is the base trait for all request implementations. It defines the contract every Twilio request must fulfil:

| Abstract member                  | Purpose                                                              |
|----------------------------------|----------------------------------------------------------------------|
| `subDomain: ApiSubDomain`        | Which Twilio subdomain to target (`Api`, `Messaging`, `Accounts`, `Preview`) |
| `method: HttpMethod`             | The HTTP method (`GET`, `POST`, `PUT`, `DELETE`)                     |
| `createHttpReq(connSettings, req)` | Build the `HttpRequest` from the request object                    |
| `mapApiException(apiException)`  | Wrap a shared `ApiException` in the request-specific error type      |
| `createUnspecifiedException(msg, cause)` | Create the request-specific catch-all error                  |

It also declares two abstract types that must be overridden in every concrete executor:

- `ApiExceptionWrapper` — a variant of the request-specific error ADT that wraps a shared `ApiException`.
- `UnspecifiedException` — a variant that represents any error not covered by a specific case.

There are two extensions of `RequestExecutor`, described below.

### SingleRequestExecutor

`SingleRequestExecutor[Req, Err, Success]` is for requests that return a single response (create, fetch, update, delete).

It provides two run methods:

- `run(connSettings, req): Future[Either[Err, Success]]` — type-safe error handling via `Either`.
- `unsafeRun(connSettings, req): Future[Success]` — throws the error as an exception on failure.

Implementors must additionally provide:

```scala
protected def parseHttpResponse(
    request: Req,
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
    entity: HttpEntityString
): Either[Err, Success]
```

Common API errors (401, 409) are already handled by the base trait before
`parseHttpResponse` is called. Implementations only need to handle
request-specific status codes and error codes.

### MultipleResponseRequestExecutor

`MultipleResponseRequestExecutor[Req, Err, Success]` is for requests that
return paginated lists. It automatically handles pagination using a Pekko
Streams `GraphDSL` loop that fetches subsequent pages until exhausted.

It provides two run methods:

- `source(connSettings, req): Source[Either[Err, Success], NotUsed]` — type-safe error handling.
- `unsafeSource(connSettings, req): Source[Success, NotUsed]` — throws on error.

Implementors must additionally provide:

```scala
protected def parseHttpResponse(
    connectionSettings: TwilioConnectionSettings,
    request: Req,
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
    responseEntity: HttpEntityString
): List[Either[Err, Success]]
```

Note this returns a `List` since a single page may contain multiple elements.

Pagination style is determined by the `ApiSubDomain`:

| Subdomain   | Paging style                | How next page is found              |
|-------------|-----------------------------|-------------------------------------|
| `Api`       | `PagingAttributesInRootJson`| `next_page_uri` field at JSON root  |
| `Messaging` | `MetaObject`                | `next_page_url` in `meta` object    |
| `Preview`   | `MetaObject`                | `next_page_url` in `meta` object    |
| `Accounts`  | `NoPaging`                  | No pagination                       |

---

## Implementing a New RequestExecutor

### Package structure

- The **public trait** goes in the appropriate sub-client package (e.g. `com.dixa.twilio.client.voice`).
- The **implementation class** goes in the corresponding `impl` package (e.g. `com.dixa.twilio.client.impl.voice`), marked `private[impl]`.

### Step 1: Define the request type, exception ADT, and executor trait

Create a file in the sub-client package (e.g. `voice/CallCreateRequestExecutor.scala`):

```scala
package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}

trait CallCreateRequestExecutor
    extends SingleRequestExecutor[
      CallCreateRequestExecutor.CallCreateRequest,
      CallCreateRequestExecutor.CallCreateException,
      CallResource
    ] {
  override protected final type ApiExceptionWrapper = CallCreateException.Api
  override protected final type UnspecifiedException = CallCreateException.Unspecified
}

object CallCreateRequestExecutor {

  // Request type — always use the builder pattern (see "Builder Pattern" section below).
  // Some older requests use a plain case class without a builder for legacy reasons.
  sealed trait CallCreateRequest { ... }

  private final case class CallCreateRequestImpl(...) extends CallCreateRequest

  object CallCreateRequest {
    // Builder with phantom types — see "Builder Pattern" section below
  }

  // Exception ADT — must always contain Api and Unspecified variants
  sealed trait CallCreateException extends RuntimeException
  object CallCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallCreateException
        with ApiExceptionWrapper

    // Add request-specific error cases here, e.g.:
    // final case class NumberNotVerified() extends ... with CallCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(msg.getOrElse("Unspecified error"), cause.orNull)
        with CallCreateException
  }
}
```

**Exception ADT rules:**

1. Must be a `sealed trait` extending `RuntimeException`.
2. Must contain an `Api` case that wraps `ApiException` and mixes in `RequestExecutor.ApiExceptionWrapper`.
3. Must contain an `Unspecified` case with `msg: Option[String]` and `cause: Option[Throwable]`.
4. May contain additional request-specific error cases mapped from Twilio error codes.

### Step 2: Implement the executor

Create the implementation in the `impl` sub-package:

```scala
package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.voice.CallCreateRequestExecutor
import com.dixa.twilio.client.voice.CallCreateRequestExecutor._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import scala.concurrent.ExecutionContext

private[impl] final class CallCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends CallCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallCreateRequest
  ): Either[CallCreateException, HttpRequest] = {
    val reqEntity = FormData(
      "From" -> req.from.toString,
      "To"   -> req.to.toString,
      "Url"  -> req.url.toString
    ).toEntity

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Calls.json",
      connSettings
    ).map(_.withEntity(reqEntity))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    CallCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = CallCreateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: CallCreateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallCreateException, CallResource] = httpResponse.status match {
    case StatusCodes.Created    => // parse success response
    case StatusCodes.BadRequest => // parse error codes and map to specific exceptions
    case _                      => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }
}
```

### Step 3: Wire it into the sub-client

Add a method to the sub-client trait and instantiate it in the impl.

---

## Type-Safe Builder Pattern

All requests should use the **phantom type builder pattern** to enforce as
many of Twilio's API rules as possible at compile time. This includes not
only required parameters, but also constraints between parameters — such as
mutual exclusion (only one of several fields may be set), conditional
dependencies (a parameter is only valid if another parameter has been set),
and any other rules documented by Twilio for the endpoint. The goal is to
make invalid requests unrepresentable. Some older requests use a plain case
class without a builder for legacy reasons. But going forward, even simple request
without any constraint should use the builder pattern for consistency.

### Builder construction pattern

The `Builder` constructor is always private, and builders are only
exposed through a `builder` method that takes a function:

```scala
def builder(
    fun: BuilderStartState => RecordingReadRequest
): RecordingReadRequest =
  fun(new Builder(None, None))
```

This design serves three purposes:

1. **Type safety:** prevents users from constructing a `Builder` in an
   arbitrary type state. They always start from `BuilderStartState`
   with all attributes unset.
2. **Ergonomics:** the caller never needs to construct the builder
   themselves. They simply write `builder(_.withX(...).build())` and
   use their IDE's autocompletion on the builder parameter to discover
   all available methods.

### Builder strategies

There are two strategies for implementing the builder pattern, each
suited to different levels of constraint complexity.

### Strategy 1: Single Intersection Type Parameter

Uses a single phantom type parameter that accumulates traits via `with` as
required fields are set. The `build()` method requires a single `=:=`
evidence that all required traits have been mixed in.

**When to use:** The request has straightforward required/optional fields
with no cross-field constraints (e.g. "field A requires field B" or
"exactly one of X, Y, Z must be set").

**How it works:**

1. Define a base `sealed trait RequestAttribute` and one sub-trait per required field.
2. Define `RequestRequiredAttributes` as the intersection of all required traits.
3. The `Builder` has a single type parameter `Attributes <: RequestAttribute`.
4. Required `with*` methods return `Builder[Attributes with ThatAttribute]`, narrowing the type.
5. Optional `with*` methods return `Builder[Attributes]` (type unchanged).
6. `build()` requires `Attributes =:= RequestRequiredAttributes`.

**Example** (from `RecordingReadRequestExecutor`):

```scala
object RecordingReadRequest {

  sealed trait RequestAttribute
  sealed trait RequestAccountSidAttribute extends RequestAttribute

  type RequestRequiredAttributes = RequestAttribute with RequestAccountSidAttribute
  type BuilderStartState = Builder[RequestAttribute]

  final class Builder[Attributes <: RequestAttribute] private[RecordingReadRequest](
      accountSid: Option[TwilioAccount.Sid],
      callSid: Option[Call.Sid]
  ) {
    // Required — narrows the phantom type
    def withAccountSid(
        accountSid: TwilioAccount.Sid
    ): Builder[Attributes with RequestAccountSidAttribute] =
      new Builder(Some(accountSid), callSid)

    // Optional — type stays the same
    def withCallSid(callSid: Call.Sid): Builder[Attributes] =
      new Builder(accountSid, Some(callSid))

    // Only compiles when all required attributes are present
    def build()(implicit ev: Attributes =:= RequestRequiredAttributes): RecordingReadRequest =
      RecordingReadRequestImpl(accountSid.get, callSid)
  }

  def builder(fun: BuilderStartState => RecordingReadRequest): RecordingReadRequest =
    fun(new Builder(None, None))
}
```

Usage:

```scala
RecordingReadRequest.builder { b =>
  b.withAccountSid(accountSid)       // required
   .withCallSid(callSid)             // optional
   .build()
}
```

Trying to call `.build()` without `.withAccountSid(...)` will result in a compile error.

### Strategy 2: Multiple Individual Type Parameters

Uses a separate phantom type parameter per constraint, each with
`True`/`False` variants. The `build()` method requires multiple `=:=`
evidence implicits — one per required constraint.

**When to use:** The request has complex cross-field constraints such as:

- **Mutual exclusion:** only one of several fields may be set (e.g. exactly one of `url`, `twiml`, or `applicationSid`).
- **Conditional requirements:** setting field A requires field B to also
  be set (e.g. `method` requires `url`, recording attributes require
  `record`).
- **Mutual suppression:** setting one field makes other fields invalid
  (e.g. `applicationSid` makes `url`, `method`, `fallbackUrl`, etc.
  irrelevant).

These constraints cannot be expressed with a single intersection type.

**How it works:**

1. Define a sealed trait pair per constraint: `SomeConstraintSet`,
   `SomeConstraintSetTrue extends SomeConstraintSet`,
   `SomeConstraintSetFalse extends SomeConstraintSet`.
2. The `Builder` has one type parameter per constraint, all starting at `False`.
3. A `with*` method flips the relevant type parameter(s) to `True` in its return type.
4. A `with*` method can also require evidence that another parameter is
   in a specific state (e.g. `withMethod` requires
   `UrlAndMethod =:= HasUrlForMethodSetTrue`), enforcing that a
   prerequisite field was set first.
5. A `with*` method can require evidence that a mutually exclusive parameter is `False`, preventing two conflicting fields from being set.
6. `build()` requires `=:=` evidence for each required constraint.

**Example** (simplified from `CallCreateRequestExecutor`):

```scala
object CallCreateRequest {

  // Simple required fields
  sealed trait AccountSidAttributeSet
  sealed trait AccountSidAttributeSetTrue extends AccountSidAttributeSet
  sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

  sealed trait ToCallerIdAttributeSet
  sealed trait ToCallerIdAttributeSetTrue extends ToCallerIdAttributeSet
  sealed trait ToCallerIdAttributeSetFalse extends ToCallerIdAttributeSet

  sealed trait FromCallerIdAttributeSet
  sealed trait FromCallerIdAttributeSetTrue extends FromCallerIdAttributeSet
  sealed trait FromCallerIdAttributeSetFalse extends FromCallerIdAttributeSet

  // "One of" constraint — at least one of url/twiml/applicationSid must be set
  sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSet
  sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue
      extends OneOfUrlOrTwimlOrApplicationSidAttributeSet
  sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSetFalse
      extends OneOfUrlOrTwimlOrApplicationSidAttributeSet

  // Mutual exclusion — only one of url/twiml/applicationSid may be set
  sealed trait HasUrlOrTwimlOrApplicationSidSet
  sealed trait HasUrlOrTwimlOrApplicationSidTrue extends HasUrlOrTwimlOrApplicationSidSet
  sealed trait HasUrlOrTwimlOrApplicationSidFalse extends HasUrlOrTwimlOrApplicationSidSet

  // Conditional requirement — method requires url to be set first
  sealed trait HasUrlForMethodSet
  sealed trait HasUrlForMethodSetTrue extends HasUrlForMethodSet
  sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

  type BuilderStartState = Builder[
    AccountSidAttributeSetFalse,
    ToCallerIdAttributeSetFalse,
    FromCallerIdAttributeSetFalse,
    OneOfUrlOrTwimlOrApplicationSidAttributeSetFalse,
    HasUrlForMethodSetFalse,
    HasUrlOrTwimlOrApplicationSidFalse,
    // ... more parameters
  ]

  final class Builder[
      AccountSidSet <: AccountSidAttributeSet,
      ToCallerIdSet <: ToCallerIdAttributeSet,
      FromCallerIdSet <: FromCallerIdAttributeSet,
      OneOfUrlOrTwimlOrApplicationSidSet <: OneOfUrlOrTwimlOrApplicationSidAttributeSet,
      UrlAndMethod <: HasUrlForMethodSet,
      UrlOrTwimlOrApplicationSid <: HasUrlOrTwimlOrApplicationSidSet,
      // ... more parameters
  ] private[CallCreateRequest] (...) {

    // Simple required field
    def withAccountSid(accountSid: TwilioAccount.Sid): Builder[
      AccountSidAttributeSetTrue,  // flipped to True
      ToCallerIdSet, FromCallerIdSet, OneOfUrlOrTwimlOrApplicationSidSet,
      UrlAndMethod, UrlOrTwimlOrApplicationSid, ...
    ] = ...

    // Mutually exclusive field — requires that no other option was set yet
    def withUrl(url: CallbackUrl.VoiceUrl)(
        implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse
    ): Builder[
      AccountSidSet, ToCallerIdSet, FromCallerIdSet,
      OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue,  // satisfies the "one of" requirement
      HasUrlForMethodSetTrue,                            // unlocks withMethod
      HasUrlOrTwimlOrApplicationSidTrue,                 // prevents withTwiml/withApplicationSid
      ...
    ] = ...

    // Conditional field — requires url to have been set
    def withMethod(method: HttpMethod)(
        implicit ev: UrlAndMethod =:= HasUrlForMethodSetTrue
    ): Builder[...] = ...

    // build requires all mandatory constraints to be True
    def build()(
        implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
        ev2: ToCallerIdSet =:= ToCallerIdAttributeSetTrue,
        ev3: FromCallerIdSet =:= FromCallerIdAttributeSetTrue,
        ev4: OneOfUrlOrTwimlOrApplicationSidSet =:= OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue
    ): CallCreateRequest = ...
  }
}
```

This ensures at compile time that:
- All required fields (`accountSid`, `to`, `from`) are set.
- Exactly one of `url`, `twiml`, or `applicationSid` is set.
- `method` can only be set if `url` was set first.

### Choosing a strategy

| Concern | Strategy 1 (single type param) | Strategy 2 (multiple type params) |
|---------|-------------------------------|-----------------------------------|
| Simple required/optional fields | Yes | Yes (but overkill) |
| Mutual exclusion ("one of X, Y, Z") | No | Yes |
| Conditional requirements ("A requires B") | No | Yes |
| Readability | Clean, minimal boilerplate | More verbose, but constraints are explicit |

**Default to Strategy 1** unless you need cross-field constraints. If the
Twilio endpoint has mutually exclusive parameters or conditional
dependencies between fields, use Strategy 2.

---

## Streaming vs Non-Streaming Requests

| Aspect              | Non-streaming (`SingleRequestExecutor`)          | Streaming (`MultipleResponseRequestExecutor`)               |
|---------------------|--------------------------------------------------|-------------------------------------------------------------|
| Use case            | Create, Fetch, Update, Delete                    | List/Read operations that return paginated results          |
| Return type (safe)  | `Future[Either[Err, Success]]`                   | `Source[Either[Err, Success], NotUsed]`                     |
| Return type (unsafe)| `Future[Success]`                                | `Source[Success, NotUsed]`                                  |
| Pagination          | N/A                                              | Automatic — handled by `GraphDSL` loop                      |
| `parseHttpResponse` | Returns `Either[Err, Success]`                   | Returns `List[Either[Err, Success]]`                        |

Choose `MultipleResponseRequestExecutor` when the Twilio endpoint returns a
list of resources wrapped in pagination metadata. Choose
`SingleRequestExecutor` for everything else.

---

## Naming Conventions

### RequestExecutor naming

The pattern is: `[Resource][TwilioOperation]RequestExecutor`

Where `TwilioOperation` is the name Twilio uses for the endpoint in their documentation. The most common operations are:

| Twilio operation | HTTP method | Description             |
|------------------|-------------|-------------------------|
| Create           | POST        | Create a new resource   |
| Fetch            | GET         | Get a single resource   |
| Read             | GET         | List resources (paginated) |
| Update           | POST        | Update an existing resource |
| Delete           | DELETE      | Delete a resource       |

Some endpoints use domain-specific operation names (e.g. `Send` for messages). Follow what Twilio calls it.

**Examples:**

| Twilio endpoint                     | Executor name                       |
|-------------------------------------|-------------------------------------|
| Message / Send (Create a Message)   | `MessageSendRequestExecutor`        |
| Recording / Read                    | `RecordingReadRequestExecutor`      |
| Account / Create                    | `AccountCreateRequestExecutor`      |
| PhoneNumber / Delete                | `PhoneNumberDeleteRequestExecutor`  |
| Services / Read                     | `ServicesReadRequestExecutor`       |

### Associated types

- **Request class:** `[Resource][Operation]Request` — e.g. `RecordingReadRequest`
- **Exception ADT:** `[Resource][Operation]Exception` — e.g. `RecordingReadException`

### File naming

- Trait file: `[Resource][Operation]RequestExecutor.scala` in the sub-client package.
- Impl file: `[Resource][Operation]RequestExecutorImpl.scala` in the `impl` sub-package.
