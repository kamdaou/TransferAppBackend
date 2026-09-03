package com.ktconsulting.transfer_app.dto.request

import com.ktconsulting.transfer_app.enum.TransactionDirection
import com.ktconsulting.transfer_app.enum.TransactionStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class TransactionSyncRequest(
    @field:Valid
    val transactions: List<TransactionItem>
)

data class TransactionItem(
    val localId: String? = null,

    @field:NotNull
    val partnerId: UUID,

    @field:Min(1)
    val amount: Long,

    val feeIncluded: Boolean = false,

    @field:NotNull
    val senderCityId: UUID,

    @field:NotNull
    val receiverCityId: UUID,

    @field:NotBlank
    val senderPhone: String,

    @field:NotBlank
    val senderName: String,

    @field:NotBlank
    val receiverPhone: String,

    @field:NotBlank
    val receiverName: String,

    @field:NotNull
    val direction: TransactionDirection,

    @field:NotNull
    val status: TransactionStatus
)
