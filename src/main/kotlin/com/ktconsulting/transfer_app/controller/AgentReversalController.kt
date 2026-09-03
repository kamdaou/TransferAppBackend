package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.ReversalRequest
import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.enum.TransactionStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.TransactionRepository
import com.ktconsulting.transfer_app.security.SecurityUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reversals")
@PreAuthorize("hasRole('AGENT')")
class AgentReversalController(
    private val transactionRepository: TransactionRepository
) {

    @PostMapping
    @Transactional
    fun requestReversal(@Valid @RequestBody request: ReversalRequest): ResponseEntity<TransactionResponse> {
        val companyId = SecurityUtil.currentCompanyId()
        val agentId = SecurityUtil.currentAgentId()

        val txn = transactionRepository.findById(request.transactionId)
            .filter { it.company.id == companyId && it.agent.id == agentId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        val allowedStatuses = setOf(TransactionStatus.SENT, TransactionStatus.CONFIRMED, TransactionStatus.COLLECTED)
        if (txn.status !in allowedStatuses) {
            throw BusinessRuleException("error.reversal.invalid_state")
        }

        txn.status = TransactionStatus.REVERSAL_REQUESTED
        txn.reversalReason = request.reason
        txn.updatedAt = Instant.now()

        val saved = transactionRepository.save(txn)

        return ResponseEntity.status(HttpStatus.CREATED).body(saved.toResponse())
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    fun confirmReversal(@PathVariable id: UUID): ResponseEntity<TransactionResponse> {
        val companyId = SecurityUtil.currentCompanyId()
        val agentId = SecurityUtil.currentAgentId()

        val txn = transactionRepository.findById(id)
            .filter { it.company.id == companyId && it.agent.id == agentId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        if (txn.status != TransactionStatus.REVERSAL_APPROVED) {
            throw BusinessRuleException("error.reversal.invalid_state")
        }

        txn.status = TransactionStatus.REVERSED
        txn.updatedAt = Instant.now()

        return ResponseEntity.ok(transactionRepository.save(txn).toResponse())
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
