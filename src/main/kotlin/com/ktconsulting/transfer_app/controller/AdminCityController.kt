package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CityRequest
import com.ktconsulting.transfer_app.dto.response.CityResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CityService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/cities")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminCityController(private val cityService: CityService) {

    @GetMapping
    fun getCities(): ResponseEntity<List<CityResponse>> =
        ResponseEntity.ok(cityService.getCities(SecurityUtil.currentCompanyId()))

    @PostMapping
    fun createCity(@Valid @RequestBody request: CityRequest): ResponseEntity<CityResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(cityService.createCity(SecurityUtil.currentCompanyId(), request))

    @PutMapping("/{id}")
    fun updateCity(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CityRequest
    ): ResponseEntity<CityResponse> =
        ResponseEntity.ok(cityService.updateCity(id, SecurityUtil.currentCompanyId(), request))
}
