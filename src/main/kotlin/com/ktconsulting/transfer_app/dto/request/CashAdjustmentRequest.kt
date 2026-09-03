package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotBlank

data class CashAdjustmentRequest(
    val amount: Long,

    @field:NotBlank
    val reason: String
)
