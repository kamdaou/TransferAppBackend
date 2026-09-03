package com.ktconsulting.transfer_app.dto.response

import java.util.UUID

data class CommissionRateResponse(
    val id: UUID,
    val sendingCityId: UUID,
    val sendingCityName: String,
    val receivingCityId: UUID,
    val receivingCityName: String,
    val minAmount: Long,
    val maxAmount: Long,
    val fee: Long
)
