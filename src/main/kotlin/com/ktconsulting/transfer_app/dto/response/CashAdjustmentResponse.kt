package com.ktconsulting.transfer_app.dto.response

import java.time.Instant
import java.util.UUID

data class CashAdjustmentResponse(
    val id: UUID,
    val agentId: UUID,
    val amount: Long,
    val reason: String,
    val performedByAdminId: UUID,
    val performedByAdminName: String,
    val createdAt: Instant
)
