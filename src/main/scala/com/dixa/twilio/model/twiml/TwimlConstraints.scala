// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.twiml

/** Phantom types used for constructing Twiml with compile time constraints.
  *
  * Phantom types are types that are never instantiated. They are only used to make the type system
  * enforce some constraints on the builders of TwimlElements at compile time, instead of runtime.
  *
  * The types are placed in this object, as some of them are shared by multiple TwimlElement
  * implementations.
  */
object TwimlConstraints {
  sealed trait Buildable
  sealed trait BuildableTrue  extends Buildable
  sealed trait BuildableFalse extends Buildable

  sealed trait Verified
  sealed trait VerifiedTrue  extends Verified
  sealed trait VerifiedFalse extends Verified

  sealed trait HasSingleAllowedValueAlready
  sealed trait HasSingleAllowedValueAlreadyTrue  extends HasSingleAllowedValueAlready
  sealed trait HasSingleAllowedValueAlreadyFalse extends HasSingleAllowedValueAlready

  sealed trait LastAddedVerbProhibitMoreVerbs
  sealed trait LastAddedVerbProhibitMoreVerbsTrue  extends LastAddedVerbProhibitMoreVerbs
  sealed trait LastAddedVerbProhibitMoreVerbsFalse extends LastAddedVerbProhibitMoreVerbs
}
