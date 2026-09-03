package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CompanyRequest
import com.ktconsulting.transfer_app.dto.response.CompanyResponse
import com.ktconsulting.transfer_app.entity.Company
import com.ktconsulting.transfer_app.entity.TransferLimits
import com.ktconsulting.transfer_app.exception.DuplicateResourceException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.CompanyRepository
import com.ktconsulting.transfer_app.repository.TransferLimitsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val transferLimitsRepository: TransferLimitsRepository
) {

    fun listCompanies(): List<CompanyResponse> =
        companyRepository.findAll()
            .filter { it.companyCode != "__SYSTEM__" }
            .map { it.toResponse() }

    @Transactional
    fun createCompany(request: CompanyRequest): CompanyResponse {
        if (companyRepository.existsByCompanyCode(request.companyCode)) {
            throw DuplicateResourceException("error.resource.duplicate", "Company code")
        }

        val company = companyRepository.save(
            Company(
                companyCode = request.companyCode,
                name = request.name,
                logoUrl = request.logoUrl,
                primaryColor = request.primaryColor,
                contacts = request.contacts,
                collectionApprovalThreshold = request.collectionApprovalThreshold
            )
        )

        transferLimitsRepository.save(
            TransferLimits(companyId = company.id!!, company = company)
        )

        return company.toResponse()
    }

    @Transactional
    fun updateCompany(companyId: UUID, request: CompanyRequest): CompanyResponse {
        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        if (company.companyCode != request.companyCode && companyRepository.existsByCompanyCode(request.companyCode)) {
            throw DuplicateResourceException("error.resource.duplicate", "Company code")
        }

        company.companyCode = request.companyCode
        company.name = request.name
        company.logoUrl = request.logoUrl
        company.primaryColor = request.primaryColor
        company.contacts = request.contacts
        company.collectionApprovalThreshold = request.collectionApprovalThreshold
        company.updatedAt = Instant.now()

        return companyRepository.save(company).toResponse()
    }

    private fun Company.toResponse() = CompanyResponse(
        id = id!!,
        companyCode = companyCode,
        name = name,
        logoUrl = logoUrl,
        primaryColor = primaryColor,
        contacts = contacts,
        collectionApprovalThreshold = collectionApprovalThreshold,
        isActive = isActive,
        createdAt = createdAt
    )
}
