package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.CashAdjustment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CashAdjustmentRepository : JpaRepository<CashAdjustment, UUID> {
    @Query("SELECT COALESCE(SUM(ca.amount), 0) FROM CashAdjustment ca WHERE ca.agent.id = :agentId")
    fun sumByAgentId(agentId: UUID): Long

    fun findByAgentIdAndCompanyId(agentId: UUID, companyId: UUID): List<CashAdjustment>
}
