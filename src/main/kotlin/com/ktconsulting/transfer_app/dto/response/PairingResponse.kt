package com.ktconsulting.transfer_app.dto.response

import java.time.Instant
import java.util.UUID

data class PairingResponse(
    val id: UUID,
    val agent1Id: UUID,
    val agent1Name: String,
    val agent1City: String,
    val agent1Phone: String,
    val agent2Id: UUID,
    val agent2Name: String,
    val agent2City: String,
    val agent2Phone: String,
    val sharedSecret: String,
    val isActive: Boolean,
    val createdAt: Instant
)
