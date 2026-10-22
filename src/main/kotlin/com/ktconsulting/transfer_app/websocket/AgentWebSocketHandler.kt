package com.ktconsulting.transfer_app.websocket

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.UUID

@Component
class AgentWebSocketHandler(
    private val sessionManager: WebSocketSessionManager
) : TextWebSocketHandler() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val agentId = session.attributes["agentId"] as? UUID
        if (agentId == null) {
            session.close(CloseStatus.POLICY_VIOLATION)
            return
        }
        sessionManager.register(agentId, session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Client sends PONG in response to server PING — no action needed
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val agentId = session.attributes["agentId"] as? UUID ?: return
        sessionManager.remove(agentId)
        log.debug("Agent {} disconnected: {}", agentId, status)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val agentId = session.attributes["agentId"] as? UUID
        log.warn("WebSocket transport error for agent {}: {}", agentId, exception.message)
        if (agentId != null) sessionManager.remove(agentId)
        if (session.isOpen) session.close(CloseStatus.SERVER_ERROR)
    }
}
