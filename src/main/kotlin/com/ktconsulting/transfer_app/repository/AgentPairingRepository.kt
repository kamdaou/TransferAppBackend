package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.AgentPairing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AgentPairingRepository : JpaRepository<AgentPairing, UUID> {
    fun findByCompanyIdAndIsActiveTrue(companyId: UUID): List<AgentPairing>

    @Query(
        "SELECT ap FROM AgentPairing ap WHERE ap.company.id = :companyId " +
        "AND ap.isActive = true " +
        "AND ((ap.agent1.id = :agent1Id AND ap.agent2.id = :agent2Id) " +
        "OR (ap.agent1.id = :agent2Id AND ap.agent2.id = :agent1Id))"
    )
    fun findActivePairing(companyId: UUID, agent1Id: UUID, agent2Id: UUID): AgentPairing?

    @Query(
        "SELECT ap FROM AgentPairing ap WHERE ap.isActive = true " +
        "AND (ap.agent1.id = :agentId OR ap.agent2.id = :agentId)"
    )
    fun findActiveByAgentId(agentId: UUID): List<AgentPairing>
}
