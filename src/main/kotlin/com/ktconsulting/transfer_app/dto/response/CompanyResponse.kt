package com.ktconsulting.transfer_app.dto.response

import java.time.Instant
import java.util.UUID

data class CompanyResponse(
    val id: UUID,
    val companyCode: String,
    val name: String,
    val logoUrl: String?,
    val primaryColor: String?,
    val contacts: String?,
    val collectionApprovalThreshold: Long,
    val isActive: Boolean,
    val createdAt: Instant
)
