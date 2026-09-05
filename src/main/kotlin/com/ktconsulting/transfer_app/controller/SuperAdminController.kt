package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CityRequest
import com.ktconsulting.transfer_app.dto.request.CompanyRequest
import com.ktconsulting.transfer_app.dto.request.CreateCompanyAdminRequest
import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.dto.response.CityResponse
import com.ktconsulting.transfer_app.dto.response.CompanyResponse
import com.ktconsulting.transfer_app.service.CityService
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
class SuperAdminController(
    private val companyService: CompanyService,
    private val cityService: CityService
) {

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

    @PostMapping("/{id}/cities")
    fun createCity(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CityRequest
    ): ResponseEntity<CityResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(cityService.createCity(id, request))

    @PostMapping("/{id}/admin")
    fun createCompanyAdmin(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateCompanyAdminRequest
    ): ResponseEntity<AgentResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(companyService.createCompanyAdmin(id, request))
}
