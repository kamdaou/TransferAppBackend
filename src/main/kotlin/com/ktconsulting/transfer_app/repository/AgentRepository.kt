package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.Agent
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AgentRepository : JpaRepository<Agent, UUID> {
    fun findByCompanyIdAndPhone(companyId: UUID, phone: String): Agent?
    fun findByCompanyIdAndApprovalStatus(companyId: UUID, status: ApprovalStatus): List<Agent>
    fun findByCompanyId(companyId: UUID): List<Agent>
    fun existsByCompanyIdAndPhone(companyId: UUID, phone: String): Boolean
    fun findByApprovalStatusAndAdminSecretIsNull(status: ApprovalStatus): List<Agent>
}
