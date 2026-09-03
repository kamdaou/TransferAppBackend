package com.ktconsulting.transfer_app.security

import com.ktconsulting.transfer_app.enum.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class AgentUserDetails(
    val agentId: UUID,
    val companyId: UUID?,
    val role: UserRole,
    private val password: String = "",
    private val phone: String = ""
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password

    override fun getUsername(): String = phone

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
