package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotBlank

data class CityRequest(
    @field:NotBlank
    val name: String
)
