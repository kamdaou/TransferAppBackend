package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.dto.response.EncryptedResponse
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CashService
import com.ktconsulting.transfer_app.service.EncryptionService
import com.ktconsulting.transfer_app.service.PairingService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/agents")
@PreAuthorize("hasRole('AGENT')")
class AgentProfileController(
    private val agentRepository: AgentRepository,
    private val pairingService: PairingService,
    private val cashService: CashService,
    private val encryptionService: EncryptionService
) {

    @GetMapping("/me")
    fun getProfile(): ResponseEntity<EncryptedResponse> {
        val agent = agentRepository.findById(SecurityUtil.currentAgentId())
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val profile = AgentResponse(
            id = agent.id!!,
            name = agent.name,
            phone = agent.phone,
            cityId = agent.city.id!!,
            cityName = agent.city.name,
            role = agent.role,
            approvalStatus = agent.approvalStatus,
            initialCash = agent.initialCash,
            adminSecret = agent.adminSecret,
            isActive = agent.isActive,
            createdAt = agent.createdAt
        )

        return ResponseEntity.ok(encrypt(profile))
    }

    @GetMapping("/me/pairings")
    fun getMyPairings(): ResponseEntity<EncryptedResponse> {
        val pairings = pairingService.getAgentPairings(SecurityUtil.currentAgentId())
        return ResponseEntity.ok(encrypt(pairings))
    }

    @GetMapping("/me/adjustments")
    fun getMyAdjustments(): ResponseEntity<EncryptedResponse> {
        val adjustments = cashService.getAdjustments(SecurityUtil.currentAgentId(), SecurityUtil.currentCompanyId())
        return ResponseEntity.ok(encrypt(adjustments))
    }

    private fun encrypt(data: Any): EncryptedResponse {
        val agent = agentRepository.findById(SecurityUtil.currentAgentId())
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        val adminSecret = agent.adminSecret
            ?: throw BusinessRuleException("error.agent.no_admin_secret")

        return EncryptedResponse(encrypted = encryptionService.encrypt(data, adminSecret))
    }
}
