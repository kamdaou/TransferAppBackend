package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class ReversalRequest(
    @field:NotNull
    val transactionId: UUID,

    @field:NotBlank
    val reason: String
)
