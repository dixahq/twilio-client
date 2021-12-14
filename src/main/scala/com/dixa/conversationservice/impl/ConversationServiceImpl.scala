package com.dixa.conversationservice.impl

import com.dixa.server.util.Logging
import com.dixa.thrift.generated._
import com.dixa.thrift.server.AbstractService
import com.twitter.util.{Future => TFuture}

import scala.collection.Map
import scala.concurrent.ExecutionContext

class ConversationServiceImpl()(implicit val ioEx: ExecutionContext) extends ConversationService.MethodPerEndpoint with AbstractService with Logging {
  override def getConversation(meta: RequestMeta, csid: Long, organizationId: String, resolveAgent: Option[Boolean]): TFuture[GetConversationResponse] = ???

  override def addConversationRating(meta: RequestMeta, csid: Long, organizationId: String, ratingType: ConversationRatingType, score: Int, message: String, ratedByUserId: String): TFuture[ConversationRating] = ???

  override def addMessageToConversation(meta: RequestMeta, csid: Long, organizationId: String, authorId: String, channel: ConversationChannel, attributes: Map[ConversationMessageAttributes, String], changeConversationStateTo: Option[ConversationState]): TFuture[AddMessageToConversationResponse] = ???

  override def markConversationAsSeen(meta: RequestMeta, csid: Long, organizationId: String, userId: String): TFuture[Unit] = ???

  override def markMessageSeenByEnduser(meta: RequestMeta, csid: Long, organizationId: String, messageId: String): TFuture[Unit] = ???

  override def addConversationWrapup(meta: RequestMeta, organizationId: String, csid: Long, authorId: String, message: String): TFuture[ConversationWrapupMessage] = ???

  override def getConversationAggregate(meta: RequestMeta, csid: Long, organizationId: String, isAgent: Option[Boolean]): TFuture[GetConversationAggregateResponse] = ???

  override def getConversationHistory(meta: RequestMeta, organizationId: String, csid: Long, isAgent: Boolean, requestingAgentId: Option[String]): TFuture[GetConversationHistoryResponse] = ???

  override def startCallConversation(meta: RequestMeta, organizationId: String, callSid: String, requestedBy: String, assignedAgent: Option[String], direction: Option[ConversationDirection], conversationAttributes: Map[ConversationAttributes, String]): TFuture[StartConversationResponse] = ???

  override def startChatConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], widgetId: String, queueId: Option[String], preConversationActions: Option[Seq[PreConversationAction]], preConversationId: Option[String]): TFuture[StartConversationResponse] = ???

  override def startEmailConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String]): TFuture[StartConversationResponse] = ???

  override def startCallbackConversation(meta: RequestMeta, organizationId: String, requesterId: String, conversationAttributes: Map[ConversationAttributes, String], queueId: String, direction: ConversationDirection): TFuture[StartConversationResponse] = ???

  override def forwardConversationToEmail(meta: RequestMeta, organizationId: String, forwardedBy: String, forwardToEmail: String, sendFromEmail: String, csid: Long, subject: Option[String], message: Option[String], ccList: Option[Seq[EmailWithOptionalName]], bcclist: Option[Seq[EmailWithOptionalName]]): TFuture[ForwardConversationResponse] = ???

  override def startOutboundEmailConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], assignedAgentId: Option[String]): TFuture[StartConversationResponse] = ???

  override def startContactFormConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], widgetId: Option[String], queueId: Option[String], preConversationActions: Option[Seq[PreConversationAction]], preConversationId: Option[String]): TFuture[StartConversationResponse] = ???

  override def startFacebookMessengerConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], queueId: Option[String]): TFuture[StartConversationResponse] = ???

  override def startWhatsAppConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], queueId: Option[String]): TFuture[StartConversationResponse] = ???

  override def startTelegramConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], queueId: Option[String]): TFuture[StartConversationResponse] = ???

  override def putConversationInQueueAndOffer(meta: RequestMeta, organizationId: String, csid: Long, queueId: String, queuedAt: Option[String], offerType: Option[OfferType], personalQueueAgentId: Option[String]): TFuture[Unit] = ???

  override def tryAssignConversation(meta: RequestMeta, organizationId: String, csid: Long, agentId: String, channel: ConversationChannel, conversationReceivedType: ConversationReceivedType, reassignment: Boolean, ignorePresence: Boolean, direction: ConversationDirection, wasClaimed: Boolean): TFuture[TryAssignConversationResponse] = ???

  override def closeConversation(meta: RequestMeta, csid: Long, organizationId: String, endedByUserId: String, endCall: Boolean, force: Boolean): TFuture[CloseConversationResponse] = ???

  override def putConversationPending(meta: RequestMeta, csid: Long, organizationId: String, pendingByUserId: String, timestamp: Option[String], message: Option[String], queueId: Option[String]): TFuture[PutConversationInPendingResponse] = ???

  override def cancelPending(meta: RequestMeta, csid: Long, organizationId: String, canceledByUserId: Option[String]): TFuture[CancelPendingResponse] = ???

  override def respondToOffer(meta: RequestMeta, csid: Long, organizationId: String, agentId: String, didAccept: Boolean, reassignment: Boolean, conversationReceived: ConversationReceivedType): TFuture[RespondToOfferResponse] = ???

  override def claimConversation(meta: RequestMeta, csid: Long, organizationId: String, agentId: String, claimedIn: ConversationReceivedType): TFuture[RespondToOfferResponse] = ???

  override def getAssignedAgent(meta: RequestMeta, csid: Long, organizationId: String): TFuture[GetAssignedAgentResponse] = ???

  override def getConversationCallSidByCombinedId(meta: RequestMeta, organizationId: String, csid: Long): TFuture[GetConversationCallSidResponse] = ???

  override def getConversationCallSid(meta: RequestMeta, organizationId: String, csid: Long, userId: String, isEnduser: Boolean): TFuture[GetConversationCallSidResponse] = ???

  override def getConversationCallSidByCallSid(meta: RequestMeta, callSid: String): TFuture[GetConversationCallSidResponse] = ???

  override def getConversationCallSidsByUser(meta: RequestMeta, organizationId: String, csid: Long, userId: String): TFuture[GetConversationCallSidsResponse] = ???

  override def getConversationCallSids(meta: RequestMeta, organizationId: String, csid: Long): TFuture[GetConversationCallSidsResponse] = ???

  override def insertConversationCallSid(meta: RequestMeta, organizationId: String, csid: Long, userId: String, callSid: String, isEndUser: Boolean): TFuture[InsertConversationCallSidResponse] = ???

  override def addConversationAttributes(meta: RequestMeta, organizationId: String, csid: Long, attributes: Map[ConversationAttributes, String]): TFuture[AddConversationAttributesResponse] = ???

  override def listConversationAggregates(meta: RequestMeta, organizationId: String, attributes: Map[ListConversationAttributes, String], paginationAttributes: Map[PaginationAttributes, String]): TFuture[ListConversationAggregatesResponse] = ???

  override def listConversations(meta: RequestMeta, orgId: String, attributes: Map[ListConversationAttributes, String], paginationAttributes: Map[PaginationAttributes, String]): TFuture[ListConversationsResponse] = ???

  override def getBulkConversations(meta: RequestMeta, organizationId: String, csids: Seq[Long]): TFuture[ListConversationsResponse] = ???

  override def getRequesterConversations(meta: RequestMeta, orgId: String, requesterId: String, paginationAttributes: Map[PaginationAttributes, String]): TFuture[ListConversationsResponse] = ???

  override def updateChannel(meta: RequestMeta, orgId: String, csid: Long, channel: ConversationChannel): TFuture[Unit] = ???

  override def unassignConversation(meta: RequestMeta, organizationId: String, csid: Long): TFuture[Unit] = ???

  override def updateEnduserConnected(meta: RequestMeta, orgId: String, csid: Long, isConnected: Boolean): TFuture[Unit] = ???

  override def setDurationToCallMessage(meta: RequestMeta, orgId: String, csid: Long, duration: String): TFuture[Unit] = ???

  override def reopenConversation(meta: RequestMeta, orgId: String, csid: Long, reopenRequestedBy: Option[String], force: Option[Boolean]): TFuture[ReopenConversationResponse] = ???

  override def sendCancelOfferToAllAgents(meta: RequestMeta, organizationId: String, csid: Long, queueId: Option[String], agentId: Option[String]): TFuture[Unit] = ???

  override def completeACW(meta: RequestMeta, organizationId: String, csid: Long, agentId: String): TFuture[ACWResponse] = ???

  override def extendACW(meta: RequestMeta, organizationId: String, csid: Long, agentId: String, extendedDuration: Int): TFuture[ACWResponse] = ???

  override def getMessages(meta: RequestMeta, organizationId: String, csid: Long): TFuture[GetMessagesResponse] = ???

  override def addMessageAttributes(meta: RequestMeta, organizationId: String, csid: Long, messageId: String, attributes: Map[ConversationMessageAttributes, String]): TFuture[AddMessageAttributesResponse] = ???

  override def ping(meta: RequestMeta): TFuture[String] = ???

  override def linkConversations(meta: RequestMeta, organizationId: String, csidChild: Long, csidParent: Long, linkType: Option[ConversationLinkType]): TFuture[LinkConversationsResponse] = ???

  override def unlinkConversation(meta: RequestMeta, organizationId: String, csidChild: Long): TFuture[Unit] = ???

  override def getConversationHistoryV2(meta: RequestMeta, organizationId: String, csid: Long, requestingAgentId: String): TFuture[ConversationHistoryV2Response] = ???

  override def findMessageByEmailProviderTransactionId(meta: RequestMeta, emailProviderTransactionId: String): TFuture[GetMessageResponse] = ???

  override def addEmailProviderTransactionIdToMessage(meta: RequestMeta, organizationId: String, messageId: String, emailProviderTransactionId: String, csid: Long): TFuture[UpdateMessageResponse] = ???

  override def addEmailIdToMessage(meta: RequestMeta, organizationId: String, messageId: String, emailId: String, csid: Long): TFuture[UpdateMessageResponse] = ???

  override def getMessagesByEmailId(meta: RequestMeta, organizationId: String, emailId: String): TFuture[GetMessagesResponse] = ???

  override def getPreferredAgent(meta: RequestMeta, organizationId: String, requesterId: String, queueId: Option[String]): TFuture[GetPreferredAgentResponse] = ???

  override def startSMSConversation(meta: RequestMeta, organizationId: String, requestedBy: String, conversationAttributes: Map[ConversationAttributes, String], queueId: Option[String], direction: ConversationDirection): TFuture[StartConversationResponse] = ???
}