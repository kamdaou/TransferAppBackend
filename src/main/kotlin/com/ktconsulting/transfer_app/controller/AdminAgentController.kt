package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.ApproveAgentRequest
import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.AgentService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/agents")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminAgentController(private val agentService: AgentService) {

    @GetMapping("/pending")
    fun getPendingAgents(): ResponseEntity<List<AgentResponse>> =
        ResponseEntity.ok(agentService.getPendingAgents(SecurityUtil.currentCompanyId()))

    @GetMapping
    fun getAllAgents(): ResponseEntity<List<AgentResponse>> =
        ResponseEntity.ok(agentService.getAllAgents(SecurityUtil.currentCompanyId()))

    @PostMapping("/{id}/approve")
    fun approveAgent(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ApproveAgentRequest
    ): ResponseEntity<AgentResponse> =
        ResponseEntity.ok(agentService.approveAgent(id, SecurityUtil.currentCompanyId(), request))

    @PostMapping("/{id}/reject")
    fun rejectAgent(@PathVariable id: UUID): ResponseEntity<AgentResponse> =
        ResponseEntity.ok(agentService.rejectAgent(id, SecurityUtil.currentCompanyId()))
}
