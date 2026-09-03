package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.response.CompanyConfigResponse
import com.ktconsulting.transfer_app.service.CompanyConfigService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/config")
class CompanyConfigController(private val companyConfigService: CompanyConfigService) {

    @GetMapping("/{companyCode}")
    fun getConfig(@PathVariable companyCode: String): ResponseEntity<CompanyConfigResponse> =
        ResponseEntity.ok(companyConfigService.getConfig(companyCode))
}
