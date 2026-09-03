package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CreatePairingRequest
import com.ktconsulting.transfer_app.dto.response.PairingResponse
import com.ktconsulting.transfer_app.entity.AgentPairing
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.DuplicateResourceException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentPairingRepository
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.util.UUID

@Service
class PairingService(
    private val pairingRepository: AgentPairingRepository,
    private val agentRepository: AgentRepository,
    private val companyRepository: CompanyRepository
) {

    fun getPairings(companyId: UUID): List<PairingResponse> =
        pairingRepository.findByCompanyIdAndIsActiveTrue(companyId).map { it.toResponse() }

    @Transactional
    fun createPairing(companyId: UUID, request: CreatePairingRequest): PairingResponse {
        if (request.agent1Id == request.agent2Id) {
            throw BusinessRuleException("error.pairing.self")
        }

        val agent1 = agentRepository.findById(request.agent1Id)
            .filter { it.company.id == companyId && it.approvalStatus == ApprovalStatus.APPROVED }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val agent2 = agentRepository.findById(request.agent2Id)
            .filter { it.company.id == companyId && it.approvalStatus == ApprovalStatus.APPROVED }
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        if (pairingRepository.findActivePairing(companyId, request.agent1Id, request.agent2Id) != null) {
            throw DuplicateResourceException("error.pairing.duplicate")
        }

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        val sharedSecret = generateSharedSecret()

        val pairing = pairingRepository.save(
            AgentPairing(
                company = company,
                agent1 = agent1,
                agent2 = agent2,
                sharedSecret = sharedSecret
            )
        )

        return pairing.toResponse()
    }

    @Transactional
    fun deactivatePairing(pairingId: UUID, companyId: UUID) {
        val pairing = pairingRepository.findById(pairingId)
            .filter { it.company.id == companyId && it.isActive }
            .orElseThrow { ResourceNotFoundException("error.pairing.not_found") }

        pairing.isActive = false
        pairingRepository.save(pairing)
    }

    private fun generateSharedSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun AgentPairing.toResponse() = PairingResponse(
        id = id!!,
        agent1Id = agent1.id!!,
        agent1Name = agent1.name,
        agent2Id = agent2.id!!,
        agent2Name = agent2.name,
        sharedSecret = sharedSecret,
        isActive = isActive,
        createdAt = createdAt
    )
}
