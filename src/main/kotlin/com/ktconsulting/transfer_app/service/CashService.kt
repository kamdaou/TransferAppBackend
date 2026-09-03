package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CashAdjustmentRequest
import com.ktconsulting.transfer_app.dto.response.BalanceResponse
import com.ktconsulting.transfer_app.entity.CashAdjustment
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.repository.CashAdjustmentRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import com.ktconsulting.transfer_app.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CashService(
    private val agentRepository: AgentRepository,
    private val transactionRepository: TransactionRepository,
    private val cashAdjustmentRepository: CashAdjustmentRepository,
    private val companyRepository: CompanyRepository
) {

    fun getBalance(agentId: UUID, companyId: UUID): BalanceResponse {
        val agent = agentRepository.findById(agentId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val cashBalance = agent.initialCash +
            transactionRepository.sumOutgoingCashCollected(agentId) -
            transactionRepository.sumIncomingCashPaid(agentId) +
            cashAdjustmentRepository.sumByAgentId(agentId)

        val pendingPayouts = transactionRepository.sumPendingPayouts(agentId)

        return BalanceResponse(
            agentId = agentId,
            cashBalance = cashBalance,
            pendingPayouts = pendingPayouts,
            availableCash = cashBalance - pendingPayouts
        )
    }

    @Transactional
    fun adjustCash(agentId: UUID, companyId: UUID, adminId: UUID, request: CashAdjustmentRequest): CashAdjustment {
        val agent = agentRepository.findById(agentId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val admin = agentRepository.findById(adminId)
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        return cashAdjustmentRepository.save(
            CashAdjustment(
                agent = agent,
                company = company,
                amount = request.amount,
                reason = request.reason,
                performedByAdmin = admin
            )
        )
    }
}
