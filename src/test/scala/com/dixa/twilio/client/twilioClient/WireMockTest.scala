package com.dixa.twilio.client.twilioClient

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

trait WireMockTest extends BeforeAndAfterAll with BeforeAndAfterEach { this: Suite =>

  protected val wireMockServer = new WireMockServer(0)
  wireMockServer.start()

  override protected def beforeEach(): Unit = {
    wireMockServer.resetAll()
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }
}
