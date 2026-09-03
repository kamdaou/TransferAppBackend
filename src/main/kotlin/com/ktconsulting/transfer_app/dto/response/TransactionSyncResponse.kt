package com.ktconsulting.transfer_app.dto.response

import java.util.UUID

data class TransactionSyncResponse(
    val synced: List<SyncedTransaction>,
    val errors: List<SyncError>
)

data class SyncedTransaction(
    val localId: String?,
    val serverId: UUID
)

data class SyncError(
    val localId: String?,
    val message: String
)
