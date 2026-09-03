package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CommissionRateRequest
import com.ktconsulting.transfer_app.dto.response.CommissionRateResponse
import com.ktconsulting.transfer_app.entity.CommissionRate
import com.ktconsulting.transfer_app.exception.ConflictException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CommissionRateRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CommissionService(
    private val commissionRateRepository: CommissionRateRepository,
    private val companyRepository: CompanyRepository,
    private val cityRepository: CityRepository
) {

    fun getRates(companyId: UUID): List<CommissionRateResponse> =
        commissionRateRepository.findByCompanyId(companyId).map { it.toResponse() }

    @Transactional
    fun createRate(companyId: UUID, request: CommissionRateRequest): CommissionRateResponse {
        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        val sendingCity = cityRepository.findById(request.sendingCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        val receivingCity = cityRepository.findById(request.receivingCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        checkOverlap(companyId, request.sendingCityId, request.receivingCityId, request.minAmount, request.maxAmount, excludeId = null)

        val rate = commissionRateRepository.save(
            CommissionRate(
                company = company,
                sendingCity = sendingCity,
                receivingCity = receivingCity,
                minAmount = request.minAmount,
                maxAmount = request.maxAmount,
                fee = request.fee
            )
        )

        return rate.toResponse()
    }

    @Transactional
    fun updateRate(rateId: UUID, companyId: UUID, request: CommissionRateRequest): CommissionRateResponse {
        val rate = commissionRateRepository.findById(rateId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.commission.not_found") }

        val sendingCity = cityRepository.findById(request.sendingCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        val receivingCity = cityRepository.findById(request.receivingCityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        checkOverlap(companyId, request.sendingCityId, request.receivingCityId, request.minAmount, request.maxAmount, excludeId = rateId)

        rate.sendingCity = sendingCity
        rate.receivingCity = receivingCity
        rate.minAmount = request.minAmount
        rate.maxAmount = request.maxAmount
        rate.fee = request.fee

        return commissionRateRepository.save(rate).toResponse()
    }

    @Transactional
    fun deleteRate(rateId: UUID, companyId: UUID) {
        val rate = commissionRateRepository.findById(rateId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.commission.not_found") }

        commissionRateRepository.delete(rate)
    }

    private fun checkOverlap(companyId: UUID, sendingCityId: UUID, receivingCityId: UUID, minAmount: Long, maxAmount: Long, excludeId: UUID?) {
        val existing = commissionRateRepository.findByCompanyId(companyId)
            .filter { it.sendingCity.id == sendingCityId && it.receivingCity.id == receivingCityId }
            .filter { excludeId == null || it.id != excludeId }
            .any { it.minAmount <= maxAmount && it.maxAmount >= minAmount }

        if (existing) {
            throw ConflictException("error.commission.overlap")
        }
    }

    private fun CommissionRate.toResponse() = CommissionRateResponse(
        id = id!!,
        sendingCityId = sendingCity.id!!,
        sendingCityName = sendingCity.name,
        receivingCityId = receivingCity.id!!,
        receivingCityName = receivingCity.name,
        minAmount = minAmount,
        maxAmount = maxAmount,
        fee = fee
    )
}
