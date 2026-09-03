package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.Min

data class ApproveAgentRequest(
    @field:Min(0)
    val initialCash: Long
)
