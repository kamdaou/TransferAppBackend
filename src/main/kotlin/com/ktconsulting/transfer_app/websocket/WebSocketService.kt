package com.ktconsulting.transfer_app.websocket

import com.ktconsulting.transfer_app.dto.response.WebSocketEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class WebSocketService(
    private val sessionManager: WebSocketSessionManager,
    private val objectMapper: ObjectMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendToAgent(agentId: UUID, type: String, payload: Any) {
        val session = sessionManager.getSession(agentId) ?: return

        try {
            val event = WebSocketEvent(type = type, payload = payload)
            val json = objectMapper.writeValueAsString(event)
            session.sendMessage(TextMessage(json))
            log.debug("Sent {} event to agent {}", type, agentId)
        } catch (e: Exception) {
            log.warn("Failed to send {} event to agent {}: {}", type, agentId, e.message)
            sessionManager.remove(agentId)
        }
    }

    fun sendToAgents(agentIds: List<UUID>, type: String, payload: Any) {
        agentIds.forEach { sendToAgent(it, type, payload) }
    }
}
