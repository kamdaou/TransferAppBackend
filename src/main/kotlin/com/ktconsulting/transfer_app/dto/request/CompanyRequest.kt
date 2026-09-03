package com.ktconsulting.transfer_app.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CompanyRequest(
    @field:NotBlank
    val companyCode: String,

    @field:NotBlank
    val name: String,

    val logoUrl: String? = null,

    val primaryColor: String? = null,

    val contacts: String? = null,

    @field:Min(0)
    val collectionApprovalThreshold: Long = 0
)
