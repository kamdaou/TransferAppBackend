package com.ktconsulting.transfer_app.config

import com.ktconsulting.transfer_app.websocket.AgentWebSocketHandler
import com.ktconsulting.transfer_app.websocket.JwtHandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val agentWebSocketHandler: AgentWebSocketHandler,
    private val jwtHandshakeInterceptor: JwtHandshakeInterceptor
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(agentWebSocketHandler, "/ws/agent")
            .addInterceptors(jwtHandshakeInterceptor)
            .setAllowedOrigins("*")
    }
}
