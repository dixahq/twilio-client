package com.dixa.conversationservice.init


import java.net.InetSocketAddress
import java.util.concurrent.Executors

import com.dixa.conversationservice.impl.ConversationServiceImpl
import com.dixa.conversationservice.init.Config.AppConfig
import com.dixa.dynamoutil.DynamoUtil
import com.dixa.server.util.{Logging, Stoppable}
import com.dixa.threadfactory.NamedThreadFactory
import com.dixa.thrift.server
import com.dixa.thrift.server.ServerFactory

import scala.concurrent.{ExecutionContext, Future}

class Application(config: AppConfig) extends Logging with Stoppable {


  //TODO replace ThreadFactory name patterns with that that suit your service
  // Threadpool for all blocking IO ops
  implicit val ec: ExecutionContext = NamedThreadFactory("conversation-service")(Executors.newCachedThreadPool)

  //TODO instantiate all dependencies using the config to pass them to the implementation of your service
  val dynamoUtil: DynamoUtil = DynamoUtil.fromConfig(config.dynamodb)



  val conversationServiceImpl = new ConversationServiceImpl()

  val conversationServiceServer: server.Stoppable = ServerFactory.expose(
    address = new InetSocketAddress(
      config.service.bindHost,
      config.service.bindPort
    ),
    serverIface = conversationServiceImpl,
    //TODO replace with your service
    label = "conversation"
  )

  override def stop(): Future[Unit] = {
    logger.info("The application has stopped")
    Future(conversationServiceServer.stop())
  }
}

object Application {
  def apply(config: AppConfig): Application = new Application(config)
}
