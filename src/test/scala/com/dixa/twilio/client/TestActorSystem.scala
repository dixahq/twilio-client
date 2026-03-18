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

package com.dixa.twilio.client

import org.apache.pekko.actor.{ActorSystem, ClassicActorSystemProvider}
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/** Trait that can be mixed into tests that needs an actor system. */
trait TestActorSystem extends BeforeAndAfterAll { this: Suite =>

  // Nothing in this this library should require a ActorSystem directly but a ClassicActorSystemProvider,
  // to more easy interop with typed actors. So hide the actor system, and only expose
  // it as a ClassicActorSystemProvider to enforce it.
  private val actorSystem: ActorSystem                                   = ActorSystem()
  protected implicit val actorSystemProvider: ClassicActorSystemProvider = actorSystem

  abstract override protected def afterAll(): Unit = {
    Await.result(actorSystem.terminate(), 15.seconds)
    super.afterAll()
  }
}
