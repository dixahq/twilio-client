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

`SingleRequestExecutor[Req, Err, Success, BuilderStartState]` is for requests that return a single response (create, fetch, update, delete).

It provides four run methods:

- `run(connSettings, req): Future[Either[Err, Success]]` — type-safe error handling via `Either`.
- `run(connSettings, builderFun: BuilderStartState => Req): Future[Either[Err, Success]]` — build and run inline.
- `unsafeRun(connSettings, req): Future[Success]` — throws the error as an exception on failure.
- `unsafeRun(connSettings, builderFun: BuilderStartState => Req): Future[Success]` — build and run inline (unsafe).

Implementors must additionally provide:

```scala
protected def createBuilderStartState(): BuilderStartState

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

`MultipleResponseRequestExecutor[Req, Err, Success, BuilderStartState]` is for requests that return paginated lists. It automatically handles pagination using a Pekko Streams `GraphDSL` loop that fetches subsequent pages until exhausted.

It provides four run methods:

- `source(connSettings, req): Source[Either[Err, Success], NotUsed]` — type-safe error handling.
- `source(connSettings, builderFun: BuilderStartState => Req): Source[Either[Err, Success], NotUsed]` — build and run inline.
- `unsafeSource(connSettings, req): Source[Success, NotUsed]` — throws on error.
- `unsafeSource(connSettings, builderFun: BuilderStartState => Req): Source[Success, NotUsed]` — build and run inline (unsafe).

Implementors must additionally provide:

```scala
protected def createBuilderStartState(): BuilderStartState

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
- The **implementation class** goes in the corresponding `impl` package
  (e.g. `com.dixa.twilio.client.impl.voice`). `private[impl]` is the
  least strict visibility allowed, but it is recommended to make it as
  strict as possible (e.g. `private[voice]` or `private[client]`).

### Step 1: Define the request type, exception ADT, and executor trait

Create a file in the sub-client package
(e.g. `voice/SipIpAddressDeleteRequestExecutor.scala`). The file must
follow this structure:

```
XRequestExecutor (trait)
└── companion object
    ├── XRequest (sealed trait)
    ├── XRequestImpl (private case class)
    ├── XRequest companion object
    │   ├── PhantomTypes (object)
    │   │   └── sealed traits for builder constraints
    │   ├── type RequestRequiredAttributes = ...
    │   ├── type BuilderStartState = ...
    │   ├── Builder (final class)
    │   │   └── object Builder (companion)
    │   │       └── val/def empty = ...
    │   └── def build(fun: ...) = ...
    ├── XException (sealed trait)
    └── XException companion object
        ├── Api (wraps ApiException)
        ├── request-specific error cases
        └── Unspecified (catch-all)
```

Full example:

```scala
package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

/** Delete an IpAddress resource.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#delete-a-sip-ipaddress-resource
  */
trait SipIpAddressDeleteRequestExecutor
    extends SingleRequestExecutor[
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest,
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteException,
      FUnit,
      SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest.BuilderStartState
    ] {

  import SipIpAddressDeleteRequestExecutor._

  override final protected type ApiExceptionWrapper =
    SipIpAddressDeleteException.Api

  override final protected type UnspecifiedException =
    SipIpAddressDeleteException.Unspecified

  override protected def createBuilderStartState(): SipIpAddressDeleteRequest.BuilderStartState =
    SipIpAddressDeleteRequest.Builder.empty
}

object SipIpAddressDeleteRequestExecutor {

  sealed trait SipIpAddressDeleteRequest {
    def accountSid: TwilioAccount.Sid
    def ipAccessControlListSid: IpAccessControlList.Sid
    def sid: SipIpAddress.Sid
  }

  private final case class SipIpAddressDeleteRequestImpl(
      accountSid: TwilioAccount.Sid,
      ipAccessControlListSid: IpAccessControlList.Sid,
      sid: SipIpAddress.Sid
  ) extends SipIpAddressDeleteRequest

  object SipIpAddressDeleteRequest {

    // Phantom types are always nested in a PhantomTypes object
    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute
          extends RequestAttribute
      sealed trait RequestIpAccessControlListSidAttribute
          extends RequestAttribute
      sealed trait RequestSidAttribute
          extends RequestAttribute
    }

    // Type aliases live in the XRequest companion object
    type RequestRequiredAttributes = PhantomTypes.RequestAttribute
      with PhantomTypes.RequestAccountSidAttribute
      with PhantomTypes.RequestIpAccessControlListSidAttribute
      with PhantomTypes.RequestSidAttribute

    type BuilderStartState =
      Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[SipIpAddressDeleteRequest] (
        accountSid: Option[TwilioAccount.Sid],
        ipAccessControlListSid: Option[IpAccessControlList.Sid],
        sid: Option[SipIpAddress.Sid]
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        Attributes with PhantomTypes.RequestAccountSidAttribute
      ] =
        new Builder(Some(accountSid), ipAccessControlListSid, sid)

      def withIpAccessControlListSid(
          ipAccessControlListSid: IpAccessControlList.Sid
      ): Builder[
        Attributes
          with PhantomTypes.RequestIpAccessControlListSidAttribute
      ] =
        new Builder(
          accountSid,
          Some(ipAccessControlListSid),
          sid
        )

      def withSid(
          sid: SipIpAddress.Sid
      ): Builder[
        Attributes with PhantomTypes.RequestSidAttribute
      ] =
        new Builder(accountSid, ipAccessControlListSid, Some(sid))

      def build()(
          implicit
          ev: Attributes =:= RequestRequiredAttributes
      ): SipIpAddressDeleteRequest =
        SipIpAddressDeleteRequestImpl(
          accountSid.get,
          ipAccessControlListSid.get,
          sid.get
        )
    }

    def build(
        fun: BuilderStartState => SipIpAddressDeleteRequest
    ): SipIpAddressDeleteRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState =
        new BuilderStartState(None, None, None)
    }
  }

  // Exception ADT
  sealed trait SipIpAddressDeleteException extends RuntimeException
  object SipIpAddressDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipIpAddressDeleteException
        with ApiExceptionWrapper

    // Request-specific error case
    final case class SipIpAddressNotFound(
        accountSid: TwilioAccount.Sid,
        ipAccessControlListSid: IpAccessControlList.Sid,
        sid: SipIpAddress.Sid
    ) extends RuntimeException(
          s"SipIpAddress with sid $sid was not found in " +
            s"IpAccessControlList $ipAccessControlListSid " +
            s"of account: $accountSid"
        )
        with SipIpAddressDeleteException

    final case class Unspecified(
        msg: Option[String],
        cause: Option[Throwable]
    ) extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to " +
              "delete SIP IP address"
          ),
          cause.orNull
        )
        with SipIpAddressDeleteException
    object Unspecified {
      def apply(msg: String) =
        new Unspecified(Some(msg), None)
      def apply(cause: Throwable) =
        new Unspecified(
          Option(cause.getMessage),
          Some(cause)
        )
    }
  }
}
```

**Key structural rules:**

- Phantom types always go in a `PhantomTypes` object nested inside
  the `XRequest` companion object.
- `RequestRequiredAttributes` and `BuilderStartState` type aliases
  go in the `XRequest` companion object (not inside `PhantomTypes`).
- The `Builder` class and the `build` factory method also go in the
  `XRequest` companion object.
- The `Builder` constructor is `private[XRequest]`.

**Exception ADT rules:**

1. Must be a `sealed trait` extending `RuntimeException`.
2. Must contain an `Api` case that wraps `ApiException` and mixes in
   `RequestExecutor.ApiExceptionWrapper`.
3. Must contain an `Unspecified` case with `msg: Option[String]` and
   `cause: Option[Throwable]`.
4. May contain additional request-specific error cases mapped from
   Twilio error codes.

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

private[client] final class CallCreateRequestExecutorImpl()(
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

All requests should use the **phantom type builder pattern** to enforce as many of Twilio's API rules as possible at compile time. This includes not only required parameters, but also constraints between parameters — such as mutual exclusion (only one of several fields may be set), conditional dependencies (a parameter is only valid if another parameter has been set), and any other rules documented by Twilio for the endpoint. The goal is to make invalid requests unrepresentable. Even simple requests without any constraints should use the builder pattern for consistency and to support the inline run methods. Legacy case classes should be kept for compatibility but should also have a builder added.

### Builder construction pattern

The `Builder` constructor is always private, and builders are only exposed through a `Builder.empty` factory and a `build` method that takes a function:

```scala
def build(
    fun: BuilderStartState => RecordingReadRequest
): RecordingReadRequest =
  fun(Builder.empty)
```

This design serves three purposes:

1. **Type safety:** prevents users from constructing a `Builder` in an arbitrary type state. They always start from `BuilderStartState` (via `Builder.empty`) with all attributes unset.
2. **Ergonomics:** the caller never needs to construct the builder themselves. They simply write `build(_.withX(...).build())` and use their IDE's autocompletion on the builder parameter to discover all available methods.
3. **Consistency:** the `createBuilderStartState` method in the executor can simply call `XRequest.Builder.empty`.
4. **More ergonomics** The consistency allow for the RequestExecutor base trait, to provide run methods that inline the builder, so you can just do `twilioClient.product.endpoint.unsafeRun(connSettings, _withX().build)`.

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

1. Define phantom traits inside a `PhantomTypes` object: a base
   `sealed trait RequestAttribute` and one sub-trait per required
   field.
2. Define `RequestRequiredAttributes` as the intersection of all
   required phantom traits (in the `XRequest` companion, not inside
   `PhantomTypes`).
3. The `Builder` has a single type parameter
   `Attributes <: PhantomTypes.RequestAttribute`.
4. Required `with*` methods return
   `Builder[Attributes with PhantomTypes.ThatAttribute]`, narrowing
   the type.
5. Optional `with*` methods return `Builder[Attributes]` (type
   unchanged).
6. `build()` requires `Attributes =:= RequestRequiredAttributes`.

**Example** (based on `RecordingReadRequestExecutor`):

```scala
trait RecordingReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      RecordingReadRequestExecutor.RecordingReadRequest,
      RecordingReadRequestExecutor.RecordingReadException,
      Recording
    ] {

  import RecordingReadRequestExecutor._

  override final protected type ApiExceptionWrapper =
    RecordingReadException.Api

  override final protected type UnspecifiedException =
    RecordingReadException.Unspecified
}

object RecordingReadRequestExecutor {

  sealed trait RecordingReadRequest {
    def accountSid: TwilioAccount.Sid
    def callSid: Option[Call.Sid]
  }

  private final case class RecordingReadRequestImpl(
      accountSid: TwilioAccount.Sid,
      callSid: Option[Call.Sid]
  ) extends RecordingReadRequest

  object RecordingReadRequest {

    object PhantomTypes {
      sealed trait RequestAttribute
      sealed trait RequestAccountSidAttribute
          extends RequestAttribute
    }

    type RequestRequiredAttributes =
      PhantomTypes.RequestAttribute
        with PhantomTypes.RequestAccountSidAttribute

    type BuilderStartState =
      Builder[PhantomTypes.RequestAttribute]

    final class Builder[
        Attributes <: PhantomTypes.RequestAttribute
    ] private[RecordingReadRequest](
        accountSid: Option[TwilioAccount.Sid],
        callSid: Option[Call.Sid]
    ) {
      // Required — narrows the phantom type
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        Attributes
          with PhantomTypes.RequestAccountSidAttribute
      ] =
        new Builder(Some(accountSid), callSid)

      // Optional — type stays the same
      def withCallSid(
          callSid: Call.Sid
      ): Builder[Attributes] =
        new Builder(accountSid, Some(callSid))

      // Only compiles when all required attributes are present
      def build()(
          implicit
          ev: Attributes =:= RequestRequiredAttributes
      ): RecordingReadRequest =
        RecordingReadRequestImpl(accountSid.get, callSid)
    }

    def build(
        fun: BuilderStartState => RecordingReadRequest
    ): RecordingReadRequest =
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState = new Builder(None, None)
    }
  }

  sealed trait RecordingReadException extends RuntimeException
  object RecordingReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with RecordingReadException
        with ApiExceptionWrapper

    final case class Unspecified(
        msg: Option[String],
        cause: Option[Throwable]
    ) extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to " +
              "read recordings"
          ),
          cause.orNull
        )
        with RecordingReadException
  }
}
```

Usage:

```scala
RecordingReadRequest.build { b =>
  b.withAccountSid(accountSid)       // required
   .withCallSid(callSid)             // optional
   .build()
}
```

Trying to call `.build()` without `.withAccountSid(...)` will
result in a compile error.

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

1. Define all phantom trait pairs inside a `PhantomTypes` object
   in the `XRequest` companion: `SomeConstraintSet`,
   `SomeConstraintSetTrue extends SomeConstraintSet`,
   `SomeConstraintSetFalse extends SomeConstraintSet`.
2. The `Builder` has one type parameter per constraint, all
   starting at `PhantomTypes.*False`.
3. A `with*` method flips the relevant type parameter(s) to
   `PhantomTypes.*True` in its return type.
4. A `with*` method can also require evidence that another
   parameter is in a specific state (e.g. `withMethod` requires
   `UrlAndMethod =:= PhantomTypes.HasUrlForMethodSetTrue`),
   enforcing that a prerequisite field was set first.
5. A `with*` method can require evidence that a mutually exclusive
   parameter is `*False`, preventing two conflicting fields from
   being set.
6. `build()` requires `=:=` evidence for each required constraint.

**Example** (simplified from `CallCreateRequestExecutor`):

```scala
object CallCreateRequest {

  object PhantomTypes {
    // Simple required fields
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue
        extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse
        extends AccountSidAttributeSet

    sealed trait ToCallerIdAttributeSet
    sealed trait ToCallerIdAttributeSetTrue
        extends ToCallerIdAttributeSet
    sealed trait ToCallerIdAttributeSetFalse
        extends ToCallerIdAttributeSet

    sealed trait FromCallerIdAttributeSet
    sealed trait FromCallerIdAttributeSetTrue
        extends FromCallerIdAttributeSet
    sealed trait FromCallerIdAttributeSetFalse
        extends FromCallerIdAttributeSet

    // "One of" — at least one of url/twiml/applicationSid
    sealed trait OneOfUrlOrTwimlOrAppSidAttributeSet
    sealed trait OneOfUrlOrTwimlOrAppSidAttributeSetTrue
        extends OneOfUrlOrTwimlOrAppSidAttributeSet
    sealed trait OneOfUrlOrTwimlOrAppSidAttributeSetFalse
        extends OneOfUrlOrTwimlOrAppSidAttributeSet

    // Mutual exclusion — only one may be set
    sealed trait HasUrlOrTwimlOrAppSidSet
    sealed trait HasUrlOrTwimlOrAppSidTrue
        extends HasUrlOrTwimlOrAppSidSet
    sealed trait HasUrlOrTwimlOrAppSidFalse
        extends HasUrlOrTwimlOrAppSidSet

    // Conditional — method requires url
    sealed trait HasUrlForMethodSet
    sealed trait HasUrlForMethodSetTrue
        extends HasUrlForMethodSet
    sealed trait HasUrlForMethodSetFalse
        extends HasUrlForMethodSet
  }

  import PhantomTypes._

  type BuilderStartState = Builder[
    AccountSidAttributeSetFalse,
    ToCallerIdAttributeSetFalse,
    FromCallerIdAttributeSetFalse,
    OneOfUrlOrTwimlOrAppSidAttributeSetFalse,
    HasUrlForMethodSetFalse,
    HasUrlOrTwimlOrAppSidFalse,
    // ... more parameters
  ]

  final class Builder[
      AccountSidSet <: AccountSidAttributeSet,
      ToCallerIdSet <: ToCallerIdAttributeSet,
      FromCallerIdSet <: FromCallerIdAttributeSet,
      OneOfUrlOrTwimlOrAppSidSet
          <: OneOfUrlOrTwimlOrAppSidAttributeSet,
      UrlAndMethod <: HasUrlForMethodSet,
      UrlOrTwimlOrAppSid <: HasUrlOrTwimlOrAppSidSet,
      // ... more parameters
  ] private[CallCreateRequest] (...) {

    // Simple required field
    def withAccountSid(
        accountSid: TwilioAccount.Sid
    ): Builder[
      AccountSidAttributeSetTrue, // flipped to True
      ToCallerIdSet,
      FromCallerIdSet,
      OneOfUrlOrTwimlOrAppSidSet,
      UrlAndMethod,
      UrlOrTwimlOrAppSid,
      ...
    ] = ...

    // Mutually exclusive field — requires no other option set
    def withUrl(url: CallbackUrl.VoiceUrl)(
        implicit ev: UrlOrTwimlOrAppSid
          =:= HasUrlOrTwimlOrAppSidFalse
    ): Builder[
      AccountSidSet,
      ToCallerIdSet,
      FromCallerIdSet,
      OneOfUrlOrTwimlOrAppSidAttributeSetTrue,
      HasUrlForMethodSetTrue,
      HasUrlOrTwimlOrAppSidTrue,
      ...
    ] = ...

    // Conditional field — requires url to have been set
    def withMethod(method: HttpMethod)(
        implicit ev: UrlAndMethod
          =:= HasUrlForMethodSetTrue
    ): Builder[...] = ...

    // build requires all mandatory constraints to be True
    def build()(
        implicit
        ev: AccountSidSet
          =:= AccountSidAttributeSetTrue,
        ev2: ToCallerIdSet
          =:= ToCallerIdAttributeSetTrue,
        ev3: FromCallerIdSet
          =:= FromCallerIdAttributeSetTrue,
        ev4: OneOfUrlOrTwimlOrAppSidSet
          =:= OneOfUrlOrTwimlOrAppSidAttributeSetTrue
    ): CallCreateRequest = ...
  }
}
```

This ensures at compile time that:
- All required fields (`accountSid`, `to`, `from`) are set.
- Exactly one of `url`, `twiml`, or `applicationSid` is set.
- `method` can only be set if `url` was set first.

#### Advanced Strategy 2 Trick: Evidence Trait (Type Class)

When you need to enforce complex truth tables between multiple parameters that cannot be easily expressed with simple `=:=` constraints in a single `build()` method, or when you would otherwise need to "overload" the `build()` method (which is not possible in Scala 2 due to erasure), you can use an auxiliary "evidence" trait (or type class) to define valid combinations of phantom types.

For example:
- Standard key $\rightarrow$ Policy must NOT be set.
- Restricted key $\rightarrow$ Policy MUST be set.

**How it works:**

1. Define the phantom type parameters as in Strategy 2.
2. Define an auxiliary trait (e.g., `ValidConfiguration`) inside the `PhantomTypes` object that takes the dependent phantom types as parameters.
3. Provide implicit instances for every **valid** combination of those types in the trait's companion object.
4. The `build()` method then requires an implicit instance of this trait.

**Example** (based on `KeyCreateRequestExecutor`):

```scala
object KeyCreateRequest {
  object PhantomTypes {
    sealed trait KeyTypeSet
    sealed trait KeyTypeSetTrue extends KeyTypeSet
    sealed trait KeyTypeSetFalse extends KeyTypeSet

    sealed trait PolicySet
    sealed trait PolicySetTrue extends PolicySet
    sealed trait PolicySetFalse extends PolicySet

    sealed trait PolicyRequired extends PolicySet
    sealed trait PolicyRequiredTrue extends PolicyRequired
    sealed trait PolicyRequiredFalse extends PolicyRequired

    // The auxiliary evidence trait
    sealed trait ValidPolicyCombinations[
      PR <: PhantomTypes.PolicyRequired,
      PS <: PhantomTypes.PolicySet
    ]
    object ValidPolicyCombinations {
      // Case 1: Standard Key (Policy not required, Policy not set)
      implicit val standard: ValidPolicyCombinations[
        PhantomTypes.PolicyRequiredFalse,
        PhantomTypes.PolicySetFalse
      ] = new ValidPolicyCombinations[
        PhantomTypes.PolicyRequiredFalse,
        PhantomTypes.PolicySetFalse
      ] {}

      // Case 2: Restricted Key (Policy required, Policy IS set)
      implicit val restricted: ValidPolicyCombinations[
        PhantomTypes.PolicyRequiredTrue,
        PhantomTypes.PolicySetTrue
      ] = new ValidPolicyCombinations[
        PhantomTypes.PolicyRequiredTrue,
        PhantomTypes.PolicySetTrue
      ] {}
    }
  }

  final class Builder[
    AS <: PhantomTypes.AccountSidSet,
    KT <: PhantomTypes.KeyTypeSet,
    PS <: PhantomTypes.PolicySet,
    DP <: PhantomTypes.DisallowPolicy,
    PR <: PhantomTypes.PolicyRequired
  ] ... {
    def build()(
      implicit evAccount: AS =:= AccountSidSetTrue,
      evKeyType: KT =:= KeyTypeSetTrue,
      evValid: ValidPolicyCombinations[PR, PS]
    ): KeyCreateRequest = ...
  }
}
```

This approach is highly flexible and keeps the validation logic centralized in the `ValidPolicyCombinations` object rather than cluttering the `build()` method signature.

### Choosing a strategy

| Concern | Strategy 1 (single type param) | Strategy 2 (multiple type params) | Strategy 2 Advanced (evidence trait) |
|---------|-------------------------------|-----------------------------------|--------------------------------------|
| Simple required/optional fields | Yes | Yes (but overkill) | Yes (overkill) |
| Mutual exclusion ("one of X, Y, Z") | No | Yes | Yes |
| Conditional requirements ("A requires B") | No | Yes | Yes |
| Complex state combinations / Truth tables | No | Difficult | **Yes (Best)** |
| Readability | Clean, minimal boilerplate | Verbose | Logic is centralized |

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
