package com.ktconsulting.transfer_app.websocket

import com.ktconsulting.transfer_app.security.JwtUtil
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder

@Component
class JwtHandshakeInterceptor(
    private val jwtUtil: JwtUtil
) : HandshakeInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = extractToken(request)
        if (token == null) {
            log.warn("WebSocket handshake rejected: no token")
            return false
        }

        return try {
            val claims = jwtUtil.validateToken(token)
            attributes["agentId"] = jwtUtil.getAgentId(claims)
            attributes["companyId"] = jwtUtil.getCompanyId(claims) ?: return false
            attributes["role"] = jwtUtil.getRole(claims)
            true
        } catch (e: Exception) {
            log.warn("WebSocket handshake rejected: invalid token — {}", e.message)
            false
        }
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // no-op
    }

    private fun extractToken(request: ServerHttpRequest): String? {
        // Try query param: ?token=<jwt>
        val params = UriComponentsBuilder.fromUri(request.uri).build().queryParams
        params.getFirst("token")?.let { return it }

        // Try Authorization header
        val authHeader = request.headers.getFirst("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }

        return null
    }
}
