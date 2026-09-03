package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreatePairingRequest(
    @field:NotNull
    val agent1Id: UUID,

    @field:NotNull
    val agent2Id: UUID
)
