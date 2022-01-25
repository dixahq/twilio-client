package com.dixa.twilio.client

import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/** Trait that can be mixed into tests that needs an actor system. */
trait TestActorSystem extends BeforeAndAfterAll { this: Suite =>

  protected implicit val actorSystem: ActorSystem = ActorSystem()

  abstract override protected def afterAll(): Unit = {
    Await.result(actorSystem.terminate(), 15.seconds)
    super.afterAll()
  }
}
