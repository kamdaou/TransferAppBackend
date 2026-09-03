package com.ktconsulting.transfer_app.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            try {
                val claims = jwtUtil.validateToken(token)
                val agentId = jwtUtil.getAgentId(claims)
                val companyId = jwtUtil.getCompanyId(claims)
                val role = jwtUtil.getRole(claims)

                val userDetails = AgentUserDetails(agentId, companyId, role)
                val auth = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (_: Exception) {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }
}
