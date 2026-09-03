package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.ReversalService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reversals")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminReversalController(private val reversalService: ReversalService) {

    @GetMapping("/pending")
    fun getPendingReversals(): ResponseEntity<List<TransactionResponse>> =
        ResponseEntity.ok(reversalService.getPendingReversals(SecurityUtil.currentCompanyId()))

    @PostMapping("/{id}/approve")
    fun approveReversal(@PathVariable id: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(reversalService.approveReversal(id, SecurityUtil.currentCompanyId()))

    @PostMapping("/{id}/reject")
    fun rejectReversal(@PathVariable id: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(reversalService.rejectReversal(id, SecurityUtil.currentCompanyId()))
}
