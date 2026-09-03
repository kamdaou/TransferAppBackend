package com.ktconsulting.transfer_app.dto.response

import java.util.UUID

data class CityResponse(
    val id: UUID,
    val name: String,
    val isActive: Boolean
)
