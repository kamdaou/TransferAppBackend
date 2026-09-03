package com.ktconsulting.transfer_app.dto.response

import com.ktconsulting.transfer_app.enum.TransactionDirection
import com.ktconsulting.transfer_app.enum.TransactionStatus
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val agentId: UUID,
    val agentName: String,
    val partnerId: UUID,
    val partnerName: String,
    val amount: Long,
    val fee: Long,
    val feeIncluded: Boolean,
    val senderCityId: UUID,
    val senderCityName: String,
    val receiverCityId: UUID,
    val receiverCityName: String,
    val senderPhone: String,
    val senderName: String,
    val receiverPhone: String,
    val receiverName: String,
    val direction: TransactionDirection,
    val status: TransactionStatus,
    val reversalReason: String?,
    val createdAt: Instant
)
