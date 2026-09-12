package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.ApproveAgentRequest
import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.entity.Agent
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

@Service
class AgentService(
    private val agentRepository: AgentRepository
) {

    fun getPendingAgents(companyId: UUID): List<AgentResponse> =
        agentRepository.findByCompanyIdAndApprovalStatus(companyId, ApprovalStatus.PENDING)
            .map { it.toResponse() }

    fun getAllAgents(companyId: UUID): List<AgentResponse> =
        agentRepository.findByCompanyId(companyId)
            .map { it.toResponse() }

    @Transactional
    fun approveAgent(agentId: UUID, companyId: UUID, request: ApproveAgentRequest): AgentResponse {
        val agent = findAgentInCompany(agentId, companyId)

        if (agent.approvalStatus != ApprovalStatus.PENDING) {
            throw BusinessRuleException("error.agent.already_approved")
        }

        agent.approvalStatus = ApprovalStatus.APPROVED
        agent.initialCash = request.initialCash
        agent.adminSecret = generateAdminSecret()
        agent.updatedAt = Instant.now()

        return agentRepository.save(agent).toResponse()
    }

    @Transactional
    fun rejectAgent(agentId: UUID, companyId: UUID): AgentResponse {
        val agent = findAgentInCompany(agentId, companyId)

        if (agent.approvalStatus != ApprovalStatus.PENDING) {
            throw BusinessRuleException("error.agent.already_approved")
        }

        agent.approvalStatus = ApprovalStatus.REJECTED
        agent.updatedAt = Instant.now()

        return agentRepository.save(agent).toResponse()
    }

    private fun findAgentInCompany(agentId: UUID, companyId: UUID): Agent =
        agentRepository.findById(agentId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

    private fun generateAdminSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun Agent.toResponse() = AgentResponse(
        id = id!!,
        name = name,
        phone = phone,
        cityId = city.id!!,
        cityName = city.name,
        role = role,
        approvalStatus = approvalStatus,
        initialCash = initialCash,
        adminSecret = adminSecret,
        isActive = isActive,
        createdAt = createdAt
    )
}
