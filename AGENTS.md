# Agent Guide

This file contains context for AI coding agents working on this
codebase. Read this before making changes.

## Build & Test

- Build tool: **SBT**
- Scala version: **2.13.18**
- Java target: **17** (`-release 17`)
- Compile: `sbt compile`
- Test: `sbt "+test"`
- The compiler runs with `-Xfatal-warnings`, so all warnings are
  errors.

## Code Formatting

Scalafmt is enforced. Tests will not compile if code is not formatted
correctly. Run `sbt scalafmtAll` to format, or `sbt scalafmtCheckAll`
to check. Key settings:

- Max column width: **100**
- Indent: **2 spaces**
- Import sorting is enabled via `rewrite.rules = [SortImports]`

See `.scalafmt.conf` for full configuration.

## Project Structure

The project has two logical modules in a single SBT project:

- **Models** (`com.dixa.twilio.model`) — Scala representations of
  Twilio entities.
- **Client** (`com.dixa.twilio.client`) — Request execution logic.

```
src/main/scala/com/dixa/twilio/
├── model/              # Data types and domain models
│   ├── messaging/
│   ├── voice/
│   ├── twiml/
│   └── ...
└── client/             # Public API (traits)
    ├── messaging/
    ├── voice/
    ├── impl/           # Implementations (private[impl])
    │   ├── messaging/
    │   └── ...
    └── ...
```

## Visibility Conventions

This project uses strict scope control:

- **RequestExecutor implementations** are `private[impl]` — they live
  in `client.impl.{subdomain}` and are not visible to consumers.
- **JSON representation classes** (`*JsonRep`) are scoped to their
  subdomain (e.g. `private[messaging]`).
- **Sid constructors** are private to their companion object
  (e.g. `private[Message]`), forcing construction through `safe` /
  `unsafe` factory methods.
- **Public traits** (RequestExecutors, sub-clients) live in
  `client.{subdomain}` packages.

## JSON Serialization

JSON is handled by **upickle** with a custom pickler:
`com.dixa.twilio.client.impl.TwilioClientPickler`.

- Response parsing uses `*JsonRep` case classes (e.g.
  `MessageJsonRep`) that map Twilio's snake_case JSON fields.
- These are implementation details, located in `client.impl.*`
  packages.
- Use `implicit val reader: Reader[X] = macroR[X]` for derivation.
- The custom pickler treats `null` JSON values as `None` for
  `Option` fields.

## Dependencies

- **Apache Pekko** (HTTP, Actor, Stream) is declared as `Provided`.
  Do not add it as a direct dependency — consumers supply their own
  version.
- **upickle** for JSON serialization.
- **enumeratum** for type-safe enums.

## Testing

- Framework: **ScalaTest** (`AnyWordSpec` style)
- Mocking: **ScalaMock**
- HTTP stubbing: **WireMock**
- Tests mirror the production package structure under `src/test/`.

## Implementing New Requests

You **MUST** read
[doc/client-implementation-doc.md](doc/client-implementation-doc.md)
before implementing any new request executors. It is the authoritative
guide covering:

- The `RequestExecutor` hierarchy (`SingleRequestExecutor` vs
  `MultipleResponseRequestExecutor`)
- The type-safe builder pattern (both strategies)
- Exception ADT conventions
- Naming conventions (follow Twilio's endpoint names)
- Streaming vs non-streaming requests
