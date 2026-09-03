package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:NotBlank
    val name: String,

    @field:NotBlank
    val phone: String,

    @field:NotBlank
    @field:Size(min = 4, max = 6)
    val pin: String,

    @field:NotBlank
    val companyCode: String,

    @field:NotNull
    val cityId: UUID
)
