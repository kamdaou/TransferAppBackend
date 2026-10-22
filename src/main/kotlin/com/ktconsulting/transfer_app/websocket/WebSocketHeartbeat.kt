package com.ktconsulting.transfer_app.websocket

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.PingMessage

@Component
class WebSocketHeartbeat(
    private val sessionManager: WebSocketSessionManager
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 30000)
    fun sendPings() {
        for (entry in sessionManager.allSessions()) {
            try {
                if (entry.value.isOpen) {
                    entry.value.sendMessage(PingMessage())
                }
            } catch (e: Exception) {
                log.warn("Ping failed for agent {}, removing session", entry.key)
                sessionManager.remove(entry.key)
            }
        }
    }
}
