# What is this?

This is two libraries in 1:

1. Scala model representation of Twilio
2. General purpose Twilio scala client library

The model part is usefully in scala application working with twilio, needing model classes
for representing the different Twilio entities.

The client is usefully if you need to communicate with twilio from you scala application.

In the future, we might separate it into two libraries, where the client would then depend on the
model, but not the other way around. 

By general purpose is meant a library, that is not filled with Dixa specific stuff, but instead would
be usable by anyone needing to use the Twilio APIs from scala. We are planning to open source
this at some point in time, when it has matured a bit. That said we at time of writing, only implement
the calls that we need at Dixa. 

# Why?

The newest version of the Twilio SDK (version 8.x at time of writing), has many flaws in our eyes:

1. It is really hard to stub out in tests. It uses classes that cannot be extended, so your cannot stub
   with tools like scalamock, and it also uses hardcoded hostnames, so it's also impossible to stub it
   with tools like wiremock.
2. It is using static state for authentication. So you set the credentials as static state, and all
   subsequent calls, will use the credentials. This is really problematic for us, as we perform a
   lot of concurrent calls on different sub-accounts, using different credentials. This would force
   us to use locking to ensure that a call uses the expected credentials.
3. It's hard to know when the SDK actually performs blocking calls, and this can be a problem in
   async applications.

Besides the clear disadvantages in the Twilio SDK, doing our own client also allows us to add in
some extra sugar. For example, by hiding paging logic behind reactive streams using Akka-streams.

# How

The library is using akka-http and akka-streams for communication with Twilio. Akka-http is
an implementation detail, but akka-stream is part of the public API, as it has plenty of
endpoint returning results as a Source.

# Developing

## Documentation

The code should be pretty well-documented with scaladoc explaining most classes' purpose.
Each package also has a package-object with scaladoc, describing what each package represents.
So if you are in doubt how to implement something, try reading these comments and look at
an existing implementation, and you should be going strong.
Remember that all new stuff should be documented just as well as the existing code.

For a detailed guide on the client architecture and how to implement new
requests, see [Client Implementation Guide](doc/client-implementation-doc.md).

## Versioning

This library uses semantic versioning.

## Publishing new version

Create a PR for merging into `master`. Once that is done, CI pipeline will make sure to
release a new minor version. If you want to increment major or middle version, you need
to upate the `version.sbt` file as part of you pull request.
