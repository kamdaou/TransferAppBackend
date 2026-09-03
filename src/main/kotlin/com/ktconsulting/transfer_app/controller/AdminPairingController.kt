package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CreatePairingRequest
import com.ktconsulting.transfer_app.dto.response.PairingResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.PairingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/pairings")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminPairingController(private val pairingService: PairingService) {

    @GetMapping
    fun getPairings(): ResponseEntity<List<PairingResponse>> =
        ResponseEntity.ok(pairingService.getPairings(SecurityUtil.currentCompanyId()))

    @PostMapping
    fun createPairing(@Valid @RequestBody request: CreatePairingRequest): ResponseEntity<PairingResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(pairingService.createPairing(SecurityUtil.currentCompanyId(), request))

    @DeleteMapping("/{id}")
    fun deactivatePairing(@PathVariable id: UUID): ResponseEntity<Void> {
        pairingService.deactivatePairing(id, SecurityUtil.currentCompanyId())
        return ResponseEntity.noContent().build()
    }
}
