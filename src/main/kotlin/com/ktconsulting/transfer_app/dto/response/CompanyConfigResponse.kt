package com.ktconsulting.transfer_app.dto.response

import java.util.UUID

data class CompanyConfigResponse(
    val companyId: UUID,
    val companyCode: String,
    val name: String,
    val logoUrl: String?,
    val primaryColor: String?,
    val contacts: String?,
    val collectionApprovalThreshold: Long,
    val cities: List<CityDto>,
    val commissionRates: List<CommissionRateDto>,
    val transferLimits: TransferLimitsDto?
)

data class CityDto(
    val id: UUID,
    val name: String
)

data class CommissionRateDto(
    val id: UUID,
    val sendingCityId: UUID,
    val receivingCityId: UUID,
    val minAmount: Long,
    val maxAmount: Long,
    val fee: Long
)

data class TransferLimitsDto(
    val minPerTransaction: Long,
    val maxPerTransaction: Long,
    val dailyCap: Long
)
