package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.enum.TransactionStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CollectionService(
    private val transactionRepository: TransactionRepository
) {

    fun getPendingCollections(companyId: UUID): List<TransactionResponse> =
        transactionRepository.findByCompanyIdAndStatus(companyId, TransactionStatus.COLLECTION_PENDING_APPROVAL)
            .map { it.toResponse() }

    @Transactional
    fun approveCollection(txnId: UUID, companyId: UUID): TransactionResponse {
        val txn = transactionRepository.findById(txnId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        if (txn.status != TransactionStatus.COLLECTION_PENDING_APPROVAL) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }

        txn.status = TransactionStatus.COLLECTED
        txn.updatedAt = Instant.now()

        return transactionRepository.save(txn).toResponse()
    }

    @Transactional
    fun rejectCollection(txnId: UUID, companyId: UUID): TransactionResponse {
        val txn = transactionRepository.findById(txnId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        if (txn.status != TransactionStatus.COLLECTION_PENDING_APPROVAL) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }

        txn.status = TransactionStatus.CONFIRMED
        txn.updatedAt = Instant.now()

        return transactionRepository.save(txn).toResponse()
    }

    private fun com.ktconsulting.transfer_app.entity.Transaction.toResponse() = TransactionResponse(
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
