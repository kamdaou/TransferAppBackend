package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CashAdjustmentRequest
import com.ktconsulting.transfer_app.dto.response.BalanceResponse
import com.ktconsulting.transfer_app.dto.response.CashAdjustmentResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CashService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/agents")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminCashController(private val cashService: CashService) {

    @GetMapping("/{id}/balance")
    fun getBalance(@PathVariable id: UUID): ResponseEntity<BalanceResponse> =
        ResponseEntity.ok(cashService.getBalance(id, SecurityUtil.currentCompanyId()))

    @GetMapping("/{id}/adjustments")
    fun getAdjustments(@PathVariable id: UUID): ResponseEntity<List<CashAdjustmentResponse>> =
        ResponseEntity.ok(cashService.getAdjustments(id, SecurityUtil.currentCompanyId()))

    @PostMapping("/{id}/cash-adjustment")
    fun adjustCash(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CashAdjustmentRequest
    ): ResponseEntity<CashAdjustmentResponse> {
        val adjustment = cashService.adjustCash(id, SecurityUtil.currentCompanyId(), SecurityUtil.currentAgentId(), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(adjustment)
    }
}
