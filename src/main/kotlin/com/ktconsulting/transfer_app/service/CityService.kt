package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CityRequest
import com.ktconsulting.transfer_app.dto.response.CityResponse
import com.ktconsulting.transfer_app.entity.City
import com.ktconsulting.transfer_app.exception.DuplicateResourceException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CityService(
    private val cityRepository: CityRepository,
    private val companyRepository: CompanyRepository
) {

    fun getCities(companyId: UUID): List<CityResponse> =
        cityRepository.findByCompanyId(companyId).map { it.toResponse() }

    @Transactional
    fun createCity(companyId: UUID, request: CityRequest): CityResponse {
        if (cityRepository.existsByCompanyIdAndName(companyId, request.name)) {
            throw DuplicateResourceException("error.city.duplicate")
        }

        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        val city = cityRepository.save(
            City(company = company, name = request.name)
        )

        return city.toResponse()
    }

    @Transactional
    fun updateCity(cityId: UUID, companyId: UUID, request: CityRequest): CityResponse {
        val city = cityRepository.findById(cityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        if (city.name != request.name && cityRepository.existsByCompanyIdAndName(companyId, request.name)) {
            throw DuplicateResourceException("error.city.duplicate")
        }

        city.name = request.name
        return cityRepository.save(city).toResponse()
    }

    private fun City.toResponse() = CityResponse(
        id = id!!,
        name = name,
        isActive = isActive
    )
}
