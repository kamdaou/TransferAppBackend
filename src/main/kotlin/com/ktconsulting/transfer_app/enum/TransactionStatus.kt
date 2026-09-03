package com.ktconsulting.transfer_app.enum

enum class TransactionStatus {
    PENDING,
    SENT,
    CONFIRMED,
    COLLECTED,
    REVERSAL_REQUESTED,
    REVERSAL_APPROVED,
    REVERSED,
    COLLECTION_PENDING_APPROVAL
}
