package com.ktconsulting.transfer_app.websocket

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionManager {

    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<UUID, WebSocketSession>()

    fun register(agentId: UUID, session: WebSocketSession) {
        sessions[agentId]?.let {
            if (it.isOpen) it.close()
        }
        sessions[agentId] = session
        log.info("WebSocket session registered for agent {}", agentId)
    }

    fun remove(agentId: UUID) {
        sessions.remove(agentId)
        log.info("WebSocket session removed for agent {}", agentId)
    }

    fun getSession(agentId: UUID): WebSocketSession? =
        sessions[agentId]?.takeIf { it.isOpen }

    fun isOnline(agentId: UUID): Boolean =
        sessions[agentId]?.isOpen == true

    fun allSessions(): Collection<Map.Entry<UUID, WebSocketSession>> =
        sessions.entries
}
