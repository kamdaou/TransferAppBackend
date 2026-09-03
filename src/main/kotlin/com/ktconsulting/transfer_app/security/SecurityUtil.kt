package com.ktconsulting.transfer_app.security

import com.ktconsulting.transfer_app.enum.UserRole
import com.ktconsulting.transfer_app.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

object SecurityUtil {

    fun currentUserDetails(): AgentUserDetails =
        SecurityContextHolder.getContext().authentication?.principal as? AgentUserDetails
            ?: throw UnauthorizedException()

    fun currentAgentId(): UUID = currentUserDetails().agentId

    fun currentCompanyId(): UUID = currentUserDetails().companyId
        ?: throw UnauthorizedException()

    fun currentRole(): UserRole = currentUserDetails().role
}
