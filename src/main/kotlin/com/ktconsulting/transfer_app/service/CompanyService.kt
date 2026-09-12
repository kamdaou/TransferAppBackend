package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.CompanyRequest
import com.ktconsulting.transfer_app.dto.request.CreateCompanyAdminRequest
import com.ktconsulting.transfer_app.dto.response.AgentResponse
import com.ktconsulting.transfer_app.dto.response.CompanyResponse
import com.ktconsulting.transfer_app.entity.Agent
import com.ktconsulting.transfer_app.entity.Company
import com.ktconsulting.transfer_app.entity.TransferLimits
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import com.ktconsulting.transfer_app.exception.DuplicateResourceException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import com.ktconsulting.transfer_app.repository.TransferLimitsRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CompanyService(
    private val companyRepository: CompanyRepository,
    private val transferLimitsRepository: TransferLimitsRepository,
    private val agentRepository: AgentRepository,
    private val cityRepository: CityRepository,
    private val passwordEncoder: PasswordEncoder
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
            TransferLimits(company = company)
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

    @Transactional
    fun createCompanyAdmin(companyId: UUID, request: CreateCompanyAdminRequest): AgentResponse {
        val company = companyRepository.findById(companyId)
            .orElseThrow { ResourceNotFoundException("error.company.not_found") }

        val city = cityRepository.findById(request.cityId)
            .filter { it.company.id == companyId }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        if (agentRepository.existsByCompanyIdAndPhone(companyId, request.phone)) {
            throw DuplicateResourceException("error.agent.already_registered")
        }

        val admin = agentRepository.save(
            Agent(
                company = company,
                city = city,
                name = request.name,
                phone = request.phone,
                pin = passwordEncoder.encode(request.pin)!!,
                role = UserRole.COMPANY_ADMIN,
                approvalStatus = ApprovalStatus.APPROVED,
                isActive = true
            )
        )

        return AgentResponse(
            id = admin.id!!,
            name = admin.name,
            phone = admin.phone,
            cityId = admin.city.id!!,
            cityName = admin.city.name,
            role = admin.role,
            approvalStatus = admin.approvalStatus,
            initialCash = admin.initialCash,
            adminSecret = admin.adminSecret,
            isActive = admin.isActive,
            createdAt = admin.createdAt
        )
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
