package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.entity.Transaction
import com.ktconsulting.transfer_app.enum.TransactionStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ReversalService(
    private val transactionRepository: TransactionRepository
) {

    fun getPendingReversals(companyId: UUID): List<TransactionResponse> =
        transactionRepository.findByCompanyIdAndStatus(companyId, TransactionStatus.REVERSAL_REQUESTED)
            .map { it.toResponse() }

    @Transactional
    fun approveReversal(txnId: UUID, companyId: UUID): TransactionResponse {
        val txn = findTransactionInCompany(txnId, companyId)

        if (txn.status != TransactionStatus.REVERSAL_REQUESTED) {
            throw BusinessRuleException("error.reversal.invalid_state")
        }

        txn.status = TransactionStatus.REVERSAL_APPROVED
        txn.updatedAt = Instant.now()

        return transactionRepository.save(txn).toResponse()
    }

    @Transactional
    fun rejectReversal(txnId: UUID, companyId: UUID): TransactionResponse {
        val txn = findTransactionInCompany(txnId, companyId)

        if (txn.status != TransactionStatus.REVERSAL_REQUESTED) {
            throw BusinessRuleException("error.reversal.invalid_state")
        }

        txn.status = TransactionStatus.COLLECTED
        txn.reversalReason = null
        txn.updatedAt = Instant.now()

        return transactionRepository.save(txn).toResponse()
    }

    private fun findTransactionInCompany(txnId: UUID, companyId: UUID): Transaction =
        transactionRepository.findById(txnId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

    private fun Transaction.toResponse() = TransactionResponse(
        id = id!!,
        agentId = agent.id!!,
        agentName = agent.name,
        partnerId = partner.id!!,
        partnerName = partner.name,
        amount = amount,
        fee = fee,
        feeIncluded = feeIncluded,
        senderCityId = senderCity.id!!,
        senderCityName = senderCity.name,
        receiverCityId = receiverCity.id!!,
        receiverCityName = receiverCity.name,
        senderPhone = senderPhone,
        senderName = senderName,
        receiverPhone = receiverPhone,
        receiverName = receiverName,
        direction = direction,
        status = status,
        reversalReason = reversalReason,
        createdAt = createdAt
    )
}
