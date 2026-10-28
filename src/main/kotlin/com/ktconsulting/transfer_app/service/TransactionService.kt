package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CreateTransactionRequest
import com.ktconsulting.transfer_app.dto.request.TransactionItem
import com.ktconsulting.transfer_app.dto.request.TransactionSyncRequest
import com.ktconsulting.transfer_app.dto.response.SyncError
import com.ktconsulting.transfer_app.dto.response.SyncedTransaction
import com.ktconsulting.transfer_app.dto.response.TransactionListResponse
import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.dto.response.TransactionSyncResponse
import com.ktconsulting.transfer_app.entity.Transaction
import com.ktconsulting.transfer_app.enum.TransactionDirection
import com.ktconsulting.transfer_app.enum.TransactionStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.*
import com.ktconsulting.transfer_app.websocket.WebSocketService
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val agentRepository: AgentRepository,
    private val agentPairingRepository: AgentPairingRepository,
    private val commissionRateRepository: CommissionRateRepository,
    private val companyRepository: CompanyRepository,
    private val cityRepository: CityRepository,
    private val transferLimitsRepository: TransferLimitsRepository,
    private val messageSource: MessageSource,
    private val webSocketService: WebSocketService
) {

    @Transactional
    fun syncTransactions(agentId: UUID, companyId: UUID, request: TransactionSyncRequest): TransactionSyncResponse {
        val synced = mutableListOf<SyncedTransaction>()
        val errors = mutableListOf<SyncError>()

        val agent = agentRepository.findById(agentId).orElse(null) ?: return TransactionSyncResponse(synced, errors)
        val company = companyRepository.findById(companyId).orElse(null) ?: return TransactionSyncResponse(synced, errors)
        val limits = transferLimitsRepository.findByCompanyId(companyId)

        for (item in request.transactions) {
            try {
                val txn = processItem(item, agentId, companyId, agent, company, limits)
                synced.add(SyncedTransaction(localId = item.localId, serverId = txn.id!!))
            } catch (e: Exception) {
                errors.add(SyncError(localId = item.localId, message = resolveMessage(e)))
            }
        }

        return TransactionSyncResponse(synced, errors)
    }

    private fun processItem(
        item: TransactionItem,
        agentId: UUID,
        companyId: UUID,
        agent: com.ktconsulting.transfer_app.entity.Agent,
        company: com.ktconsulting.transfer_app.entity.Company,
        limits: com.ktconsulting.transfer_app.entity.TransferLimits?
    ): Transaction {
        val partner = agentRepository.findById(item.partnerId)
            .filter { it.company.id == companyId }
            .orElseThrow { RuntimeException("error.agent.not_found") }

        agentPairingRepository.findActivePairing(companyId, agentId, item.partnerId)
            ?: throw RuntimeException("error.transaction.agents_not_paired")

        val senderCity = cityRepository.findById(item.senderCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { RuntimeException("error.city.not_found") }

        val receiverCity = cityRepository.findById(item.receiverCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { RuntimeException("error.city.not_found") }

        var amount = item.amount
        var fee = 0L

        if (item.direction == TransactionDirection.OUTGOING) {
            val rate = commissionRateRepository.findRate(companyId, item.senderCityId, item.receiverCityId, item.amount)
                ?: throw RuntimeException("error.transaction.no_commission_rate")
            fee = rate.fee

            if (item.feeIncluded) {
                amount = item.amount - fee
                if (amount <= 0) throw RuntimeException("error.transaction.no_commission_rate")
            }
        }

        if (limits != null) {
            val effectiveAmount = if (item.feeIncluded) item.amount else amount
            if (effectiveAmount < limits.minPerTransaction) throw RuntimeException("error.transaction.limit_exceeded")
            if (effectiveAmount > limits.maxPerTransaction) throw RuntimeException("error.transaction.limit_exceeded")

            if (item.direction == TransactionDirection.OUTGOING) {
                val startOfDay = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
                    .atStartOfDay(ZoneOffset.UTC).toInstant()
                val dailyTotal = transactionRepository.sumDailyOutgoing(agentId, startOfDay)
                if (dailyTotal + amount > limits.dailyCap) throw RuntimeException("error.transaction.limit_exceeded")
            }
        }

        var status = item.status

        if (item.direction == TransactionDirection.INCOMING && status == TransactionStatus.COLLECTED) {
            if (amount >= company.collectionApprovalThreshold && company.collectionApprovalThreshold > 0) {
                status = TransactionStatus.COLLECTION_PENDING_APPROVAL
            }
        }

        return transactionRepository.save(
            Transaction(
                company = company,
                agent = agent,
                partner = partner,
                amount = amount,
                fee = fee,
                feeIncluded = item.feeIncluded,
                senderCity = senderCity,
                receiverCity = receiverCity,
                senderPhone = item.senderPhone,
                senderName = item.senderName,
                receiverPhone = item.receiverPhone,
                receiverName = item.receiverName,
                direction = item.direction,
                status = status
            )
        )
    }

    @Transactional
    fun sendTransaction(agentId: UUID, companyId: UUID, request: CreateTransactionRequest): TransactionResponse {
        val agent = agentRepository.findById(agentId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val partner = agentRepository.findById(request.partnerId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        agentPairingRepository.findActivePairing(companyId, agentId, request.partnerId)
            ?: throw BusinessRuleException("error.transaction.agents_not_paired")

        val senderCity = cityRepository.findById(request.senderCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        val receiverCity = cityRepository.findById(request.receiverCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        val rate = commissionRateRepository.findRate(companyId, request.senderCityId, request.receiverCityId, request.amount)
            ?: throw BusinessRuleException("error.transaction.no_commission_rate")
        val fee = rate.fee

        var amount = request.amount
        if (request.feeIncluded) {
            amount = request.amount - fee
            if (amount <= 0) throw BusinessRuleException("error.transaction.no_commission_rate")
        }

        val limits = transferLimitsRepository.findByCompanyId(companyId)
        if (limits != null) {
            val effectiveAmount = if (request.feeIncluded) request.amount else amount
            if (effectiveAmount < limits.minPerTransaction) throw BusinessRuleException("error.transaction.limit_exceeded")
            if (effectiveAmount > limits.maxPerTransaction) throw BusinessRuleException("error.transaction.limit_exceeded")

            val startOfDay = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant()
            val dailyTotal = transactionRepository.sumDailyOutgoing(agentId, startOfDay)
            if (dailyTotal + amount > limits.dailyCap) throw BusinessRuleException("error.transaction.limit_exceeded")
        }

        // Save outgoing transaction for sender
        val outgoing = transactionRepository.save(
            Transaction(
                company = company,
                agent = agent,
                partner = partner,
                amount = amount,
                fee = fee,
                feeIncluded = request.feeIncluded,
                senderCity = senderCity,
                receiverCity = receiverCity,
                senderPhone = request.senderPhone,
                senderName = request.senderName,
                receiverPhone = request.receiverPhone,
                receiverName = request.receiverName,
                direction = TransactionDirection.OUTGOING,
                status = TransactionStatus.SENT
            )
        )

        // Save incoming transaction for receiver
        val incoming = transactionRepository.save(
            Transaction(
                company = company,
                agent = partner,
                partner = agent,
                amount = amount,
                fee = fee,
                feeIncluded = request.feeIncluded,
                senderCity = senderCity,
                receiverCity = receiverCity,
                senderPhone = request.senderPhone,
                senderName = request.senderName,
                receiverPhone = request.receiverPhone,
                receiverName = request.receiverName,
                direction = TransactionDirection.INCOMING,
                status = TransactionStatus.SENT
            )
        )

        // Notify partner via WebSocket
        webSocketService.sendToAgent(partner.id!!, "NEW_TRANSACTION", incoming.toResponse())

        return outgoing.toResponse()
    }

    @Transactional
    fun confirmTransaction(transactionId: UUID, agentId: UUID, companyId: UUID): TransactionResponse {
        val txn = transactionRepository.findById(transactionId)
            .filter { it.company.id == companyId && it.agent.id == agentId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        if (txn.direction != TransactionDirection.INCOMING) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }
        if (txn.status != TransactionStatus.SENT) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }

        txn.status = TransactionStatus.CONFIRMED
        txn.updatedAt = Instant.now()
        val saved = transactionRepository.save(txn)

        // Notify sender
        webSocketService.sendToAgent(txn.partner.id!!, "STATUS_CHANGED",
            mapOf("transactionId" to transactionId, "newStatus" to "CONFIRMED"))

        return saved.toResponse()
    }

    @Transactional
    fun collectTransaction(transactionId: UUID, agentId: UUID, companyId: UUID): TransactionResponse {
        val txn = transactionRepository.findById(transactionId)
            .filter { it.company.id == companyId && it.agent.id == agentId }
            .orElseThrow { ResourceNotFoundException("error.transaction.not_found") }

        if (txn.direction != TransactionDirection.INCOMING) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }
        if (txn.status != TransactionStatus.CONFIRMED) {
            throw BusinessRuleException("error.transaction.invalid_status")
        }

        val company = txn.company
        if (txn.amount >= company.collectionApprovalThreshold && company.collectionApprovalThreshold > 0) {
            txn.status = TransactionStatus.COLLECTION_PENDING_APPROVAL
        } else {
            txn.status = TransactionStatus.COLLECTED
        }
        txn.updatedAt = Instant.now()
        val saved = transactionRepository.save(txn)

        // Notify sender
        webSocketService.sendToAgent(txn.partner.id!!, "STATUS_CHANGED",
            mapOf("transactionId" to transactionId, "newStatus" to saved.status.name))

        return saved.toResponse()
    }

    fun getAgentTransactions(
        agentId: UUID,
        since: Instant?,
        statuses: List<TransactionStatus>?,
        limit: Int,
        offset: Int
    ): TransactionListResponse {
        val page = transactionRepository.findByAgentFiltered(
            agentId = agentId,
            since = since,
            statuses = statuses?.takeIf { it.isNotEmpty() },
            pageable = PageRequest.of(offset / limit.coerceAtLeast(1), limit.coerceAtLeast(1))
        )
        return TransactionListResponse(
            transactions = page.content.map { it.toResponse() },
            total = page.totalElements
        )
    }

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

    private fun resolveMessage(e: Exception): String {
        val locale = LocaleContextHolder.getLocale()
        return try {
            messageSource.getMessage(e.message ?: "error.internal", null, locale)
        } catch (_: Exception) {
            e.message ?: "Unknown error"
        }
    }
}
