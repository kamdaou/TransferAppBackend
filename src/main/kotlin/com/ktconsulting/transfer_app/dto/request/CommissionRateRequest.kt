package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CommissionRateRequest(
    @field:NotNull
    val sendingCityId: UUID,

    @field:NotNull
    val receivingCityId: UUID,

    @field:Min(0)
    val minAmount: Long,

    @field:Min(1)
    val maxAmount: Long,

    @field:Min(0)
    val fee: Long
)
