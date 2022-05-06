package com.dixa.twilio.model.twiml

/** Phantom types used for constructing Twiml with compile time constraints.
  *
  * Phantom types are types that are never instantiated. They are only used to make the type system
  * enforce some constraints on the builders of TwimlElements at compile time, instead of runtime.
  *
  * The types are placed in this object, as some of them are shared by multiple TwimlElement
  * implementations.
  */
object PhantomTypes {
  sealed trait Buildable
  sealed trait BuildableTrue  extends Buildable
  sealed trait BuildableFalse extends Buildable

  sealed trait Verified
  sealed trait VerifiedTrue  extends Verified
  sealed trait VerifiedFalse extends Verified

  sealed trait HasSingleAllowedValueAlready
  sealed trait HasSingleAllowedValueAlreadyTrue  extends HasSingleAllowedValueAlready
  sealed trait HasSingleAllowedValueAlreadyFalse extends HasSingleAllowedValueAlready
}
