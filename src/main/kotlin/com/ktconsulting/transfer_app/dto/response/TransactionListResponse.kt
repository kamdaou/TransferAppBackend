package com.ktconsulting.transfer_app.dto.response

data class TransactionListResponse(
    val transactions: List<TransactionResponse>,
    val total: Long
)
