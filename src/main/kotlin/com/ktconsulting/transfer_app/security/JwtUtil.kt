package com.ktconsulting.transfer_app.security

import com.ktconsulting.transfer_app.enum.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-ms}") private val expirationMs: Long
) {

    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateToken(agentId: UUID, companyId: UUID?, role: UserRole): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        val builder = Jwts.builder()
            .subject(agentId.toString())
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)

        if (companyId != null) {
            builder.claim("companyId", companyId.toString())
        }

        return builder.compact()
    }

    fun validateToken(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    fun getAgentId(claims: Claims): UUID = UUID.fromString(claims.subject)

    fun getCompanyId(claims: Claims): UUID? =
        claims["companyId"]?.let { UUID.fromString(it as String) }

    fun getRole(claims: Claims): UserRole = UserRole.valueOf(claims["role"] as String)
}
