package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CommissionRateRequest
import com.ktconsulting.transfer_app.dto.response.CommissionRateResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CommissionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/commissions")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminCommissionController(private val commissionService: CommissionService) {

    @GetMapping
    fun getRates(): ResponseEntity<List<CommissionRateResponse>> =
        ResponseEntity.ok(commissionService.getRates(SecurityUtil.currentCompanyId()))

    @PostMapping
    fun createRate(@Valid @RequestBody request: CommissionRateRequest): ResponseEntity<CommissionRateResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(commissionService.createRate(SecurityUtil.currentCompanyId(), request))

    @PutMapping("/{id}")
    fun updateRate(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CommissionRateRequest
    ): ResponseEntity<CommissionRateResponse> =
        ResponseEntity.ok(commissionService.updateRate(id, SecurityUtil.currentCompanyId(), request))

    @DeleteMapping("/{id}")
    fun deleteRate(@PathVariable id: UUID): ResponseEntity<Void> {
        commissionService.deleteRate(id, SecurityUtil.currentCompanyId())
        return ResponseEntity.noContent().build()
    }
}
