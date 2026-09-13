package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.dto.response.CashAdjustmentResponse
import com.ktconsulting.transfer_app.dto.response.PairingResponse
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CashService
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
    private val cashService: CashService
) {

    @GetMapping("/me")
    fun getProfile(): ResponseEntity<AgentResponse> {
        val agent = agentRepository.findById(SecurityUtil.currentAgentId())
            .orElseThrow { ResourceNotFoundException("error.agent.not_found") }

        return ResponseEntity.ok(
            AgentResponse(
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
        )
    }

    @GetMapping("/me/pairings")
    fun getMyPairings(): ResponseEntity<List<PairingResponse>> =
        ResponseEntity.ok(pairingService.getAgentPairings(SecurityUtil.currentAgentId()))

    @GetMapping("/me/adjustments")
    fun getMyAdjustments(): ResponseEntity<List<CashAdjustmentResponse>> =
        ResponseEntity.ok(cashService.getAdjustments(SecurityUtil.currentAgentId(), SecurityUtil.currentCompanyId()))
}
