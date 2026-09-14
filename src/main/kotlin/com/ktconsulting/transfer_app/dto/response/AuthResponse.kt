package com.ktconsulting.transfer_app.dto.response

import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import java.util.UUID

data class AuthResponse(
    val token: String,
    val agentId: UUID,
    val role: UserRole,
    val approvalStatus: ApprovalStatus,
    val companyId: UUID,
    val name: String,
    val adminSecret: String? = null
)
