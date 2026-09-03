package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.Transaction
import com.ktconsulting.transfer_app.enum.TransactionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

interface TransactionRepository : JpaRepository<Transaction, UUID> {
    fun findByCompanyIdAndStatus(companyId: UUID, status: TransactionStatus): List<Transaction>
    fun findByAgentIdAndCompanyId(agentId: UUID, companyId: UUID): List<Transaction>

    @Query(
        "SELECT COALESCE(SUM(t.amount + t.fee), 0) FROM Transaction t " +
        "WHERE t.agent.id = :agentId AND t.direction = com.ktconsulting.transfer_app.enum.TransactionDirection.OUTGOING " +
        "AND t.status IN (com.ktconsulting.transfer_app.enum.TransactionStatus.SENT, " +
        "com.ktconsulting.transfer_app.enum.TransactionStatus.CONFIRMED, " +
        "com.ktconsulting.transfer_app.enum.TransactionStatus.COLLECTED, " +
        "com.ktconsulting.transfer_app.enum.TransactionStatus.REVERSAL_REQUESTED, " +
        "com.ktconsulting.transfer_app.enum.TransactionStatus.REVERSAL_APPROVED)"
    )
    fun sumOutgoingCashCollected(agentId: UUID): Long

    @Query(
        "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.agent.id = :agentId AND t.direction = com.ktconsulting.transfer_app.enum.TransactionDirection.INCOMING " +
        "AND t.status = com.ktconsulting.transfer_app.enum.TransactionStatus.COLLECTED"
    )
    fun sumIncomingCashPaid(agentId: UUID): Long

    @Query(
        "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.agent.id = :agentId AND t.direction = com.ktconsulting.transfer_app.enum.TransactionDirection.INCOMING " +
        "AND t.status = com.ktconsulting.transfer_app.enum.TransactionStatus.CONFIRMED"
    )
    fun sumPendingPayouts(agentId: UUID): Long

    @Query(
        "SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
        "WHERE t.agent.id = :agentId AND t.direction = com.ktconsulting.transfer_app.enum.TransactionDirection.OUTGOING " +
        "AND t.createdAt >= :startOfDay"
    )
    fun sumDailyOutgoing(agentId: UUID, startOfDay: Instant): Long
}
