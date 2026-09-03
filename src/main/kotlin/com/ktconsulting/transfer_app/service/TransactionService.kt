package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.TransactionItem
import com.ktconsulting.transfer_app.dto.request.TransactionSyncRequest
import com.ktconsulting.transfer_app.dto.response.SyncError
import com.ktconsulting.transfer_app.dto.response.SyncedTransaction
import com.ktconsulting.transfer_app.dto.response.TransactionSyncResponse
import com.ktconsulting.transfer_app.entity.Transaction
import com.ktconsulting.transfer_app.enum.TransactionDirection
import com.ktconsulting.transfer_app.enum.TransactionStatus
import com.ktconsulting.transfer_app.repository.*
import org.springframework.context.MessageSource
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
    private val messageSource: MessageSource
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

    private fun resolveMessage(e: Exception): String {
        val locale = LocaleContextHolder.getLocale()
        return try {
            messageSource.getMessage(e.message ?: "error.internal", null, locale)
        } catch (_: Exception) {
            e.message ?: "Unknown error"
        }
    }
}
