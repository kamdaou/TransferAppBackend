package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    val phone: String,

    @field:NotBlank
    val pin: String,

    @field:NotBlank
    val companyCode: String
)
