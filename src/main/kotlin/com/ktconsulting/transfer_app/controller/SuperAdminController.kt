package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CompanyRequest
import com.ktconsulting.transfer_app.dto.response.CompanyResponse
import com.ktconsulting.transfer_app.service.CompanyService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/companies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
class SuperAdminController(private val companyService: CompanyService) {

    @GetMapping
    fun listCompanies(): ResponseEntity<List<CompanyResponse>> =
        ResponseEntity.ok(companyService.listCompanies())

    @PostMapping
    fun createCompany(@Valid @RequestBody request: CompanyRequest): ResponseEntity<CompanyResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompany(request))

    @PutMapping("/{id}")
    fun updateCompany(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CompanyRequest
    ): ResponseEntity<CompanyResponse> =
        ResponseEntity.ok(companyService.updateCompany(id, request))
}
