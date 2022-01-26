//package com.dixa.twilio.client.twilioClient.messaging
//
//import com.dixa.twilio.client.twilioClient.TwilioClientTest
//import com.github.tomakehurst.wiremock.client.WireMock
//import com.github.tomakehurst.wiremock.client.WireMock.aResponse
//
//final class ReadServicesTest extends TwilioClientTest {
//
//  classOf[TwilioClientMessaging].getSimpleName when {
//
//    "asked to read all service" should {
//
//      "return all the services it gets from twilio" in {
//
//        wireMockServer.stubFor(
//          WireMock
//            .get(WireMock.urlPathEqualTo("/v1/Services"))
//            .withBasicAuth("testUsername", "testPassword")
//            .willReturn(
//              aResponse()
//                .withStatus(200)
//                .withHeader("Content-Type", "application/json")
//                .withBodyFile(getAccountsRequest1JsonResponse)
//            )
//        )
//        wireMockServer.stubFor(
//          WireMock
//            .get(WireMock.urlPathEqualTo("/2010-04-01/Accounts.json"))
//            .withQueryParam("Status", WireMock.equalTo("active"))
//            .withQueryParam("Page", WireMock.equalTo("1"))
//            .withQueryParam(
//              "PageToken",
//              WireMock.equalTo(
//                "PTJTdCJTIyZnJpZW5kbHlOYW1lJTIyJTNBbnVsbCUyQyUyMnN0YXR1cyUyMiUzQSUyMmFjdGl2ZSUyMiU3RCwyMDIxLTA5LTI3VDA1JTNBMTQlM0EwNS0wNyUzQTAwLCU1QiUyMkFDMzE4M2E3NDFmMWJhYjQ3NjRkYWMyNDkyYzhkMWZkODklMjIlNUQ6MTo0MjMy"
//              )
//            )
//            .withBasicAuth("testUsername", "testPassword")
//            .willReturn(
//              aResponse()
//                .withStatus(200)
//                .withHeader("Content-Type", "application/json")
//                .withBody(getAccountsRequest2JsonResponse)
//            )
//        )
//      }
//    }
//  }
//}
