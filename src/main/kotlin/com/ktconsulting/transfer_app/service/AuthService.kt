package com.ktconsulting.transfer_app.service

import com.ktconsulting.transfer_app.dto.request.LoginRequest
import com.ktconsulting.transfer_app.dto.request.RegisterRequest
import com.ktconsulting.transfer_app.dto.response.AuthResponse
import com.ktconsulting.transfer_app.entity.Agent
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import com.ktconsulting.transfer_app.exception.BusinessRuleException
import com.ktconsulting.transfer_app.exception.DuplicateResourceException
import com.ktconsulting.transfer_app.exception.InvalidCredentialsException
import com.ktconsulting.transfer_app.exception.ResourceNotFoundException
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import com.ktconsulting.transfer_app.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val agentRepository: AgentRepository,
    private val companyRepository: CompanyRepository,
    private val cityRepository: CityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val company = companyRepository.findByCompanyCode(request.companyCode)
            ?: throw ResourceNotFoundException("error.company.not_found")

        val city = cityRepository.findById(request.cityId)
            .filter { it.company.id == company.id }
            .orElseThrow { ResourceNotFoundException("error.city.not_found") }

        if (agentRepository.existsByCompanyIdAndPhone(company.id!!, request.phone)) {
            throw DuplicateResourceException("error.agent.already_registered")
        }

        val agent = agentRepository.save(
            Agent(
                company = company,
                city = city,
                name = request.name,
                phone = request.phone,
                pin = passwordEncoder.encode(request.pin)!!,
                role = UserRole.AGENT,
                approvalStatus = ApprovalStatus.PENDING
            )
        )

        val token = jwtUtil.generateToken(agent.id!!, company.id, agent.role)

        return AuthResponse(
            token = token,
            agentId = agent.id!!,
            role = agent.role,
            approvalStatus = agent.approvalStatus,
            companyId = company.id!!,
            name = agent.name,
            phone = agent.phone,
            cityId = agent.city.id!!,
            cityName = agent.city.name,
            initialCash = agent.initialCash,
            adminSecret = agent.adminSecret
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val company = companyRepository.findByCompanyCode(request.companyCode)
            ?: throw InvalidCredentialsException()

        val agent = agentRepository.findByCompanyIdAndPhone(company.id!!, request.phone)
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(request.pin, agent.pin)) {
            throw InvalidCredentialsException()
        }

        if (!agent.isActive) {
            throw BusinessRuleException("error.auth.account_inactive")
        }

        val token = jwtUtil.generateToken(agent.id!!, company.id, agent.role)

        return AuthResponse(
            token = token,
            agentId = agent.id!!,
            role = agent.role,
            approvalStatus = agent.approvalStatus,
            companyId = company.id!!,
            name = agent.name,
            phone = agent.phone,
            cityId = agent.city.id!!,
            cityName = agent.city.name,
            initialCash = agent.initialCash,
            adminSecret = agent.adminSecret
        )
    }
}
