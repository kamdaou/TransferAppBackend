package com.ktconsulting.transfer_app.dto.response

import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import java.time.Instant
import java.util.UUID

data class AgentResponse(
    val id: UUID,
    val name: String,
    val phone: String,
    val cityId: UUID,
    val cityName: String,
    val role: UserRole,
    val approvalStatus: ApprovalStatus,
    val initialCash: Long,
    val isActive: Boolean,
    val createdAt: Instant
)
