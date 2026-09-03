package com.ktconsulting.transfer_app.dto.response

import java.util.UUID

data class BalanceResponse(
    val agentId: UUID,
    val cashBalance: Long,
    val pendingPayouts: Long,
    val availableCash: Long
)
