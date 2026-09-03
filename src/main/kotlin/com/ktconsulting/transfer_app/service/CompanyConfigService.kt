package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.response.CityDto
import com.ktconsulting.transfer_app.dto.response.CommissionRateDto
import com.ktconsulting.transfer_app.dto.response.CompanyConfigResponse
import com.ktconsulting.transfer_app.dto.response.TransferLimitsDto
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CommissionRateRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import com.ktconsulting.transfer_app.repository.TransferLimitsRepository
import org.springframework.stereotype.Service

@Service
class CompanyConfigService(
    private val companyRepository: CompanyRepository,
    private val cityRepository: CityRepository,
    private val commissionRateRepository: CommissionRateRepository,
    private val transferLimitsRepository: TransferLimitsRepository
) {

    fun getConfig(companyCode: String): CompanyConfigResponse {
        val company = companyRepository.findByCompanyCode(companyCode)
            ?: throw ResourceNotFoundException("error.company.not_found")

        val cities = cityRepository.findByCompanyIdAndIsActiveTrue(company.id!!).map {
            CityDto(id = it.id!!, name = it.name)
        }

        val rates = commissionRateRepository.findByCompanyId(company.id!!).map {
            CommissionRateDto(
                id = it.id!!,
                sendingCityId = it.sendingCity.id!!,
                receivingCityId = it.receivingCity.id!!,
                minAmount = it.minAmount,
                maxAmount = it.maxAmount,
                fee = it.fee
            )
        }

        val limits = transferLimitsRepository.findByCompanyId(company.id!!)?.let {
            TransferLimitsDto(
                minPerTransaction = it.minPerTransaction,
                maxPerTransaction = it.maxPerTransaction,
                dailyCap = it.dailyCap
            )
        }

        return CompanyConfigResponse(
            companyId = company.id!!,
            companyCode = company.companyCode,
            name = company.name,
            logoUrl = company.logoUrl,
            primaryColor = company.primaryColor,
            contacts = company.contacts,
            collectionApprovalThreshold = company.collectionApprovalThreshold,
            cities = cities,
            commissionRates = rates,
            transferLimits = limits
        )
    }
}
