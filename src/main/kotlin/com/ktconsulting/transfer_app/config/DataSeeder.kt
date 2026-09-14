package com.ktconsulting.transfer_app.config

import com.ktconsulting.transfer_app.entity.Agent
import com.ktconsulting.transfer_app.entity.City
import com.ktconsulting.transfer_app.entity.Company
import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import com.ktconsulting.transfer_app.repository.AgentRepository
import com.ktconsulting.transfer_app.repository.CityRepository
import com.ktconsulting.transfer_app.repository.CompanyRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class DataSeeder(
    private val companyRepository: CompanyRepository,
    private val cityRepository: CityRepository,
    private val agentRepository: AgentRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.super-admin.phone:+23500000000}") private val superAdminPhone: String,
    @Value("\${app.super-admin.pin:000000}") private val superAdminPin: String
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val systemCompany = companyRepository.findByCompanyCode("__SYSTEM__")
            ?: companyRepository.save(
                Company(
                    companyCode = "__SYSTEM__",
                    name = "System",
                    isActive = true
                )
            ).also { log.info("Created __SYSTEM__ company") }

        val systemCity = cityRepository.findByCompanyId(systemCompany.id!!).firstOrNull()
            ?: cityRepository.save(
                City(
                    company = systemCompany,
                    name = "System",
                    isActive = true
                )
            ).also { log.info("Created system city") }

        val existingAdmin = agentRepository.findByCompanyIdAndPhone(systemCompany.id!!, superAdminPhone)
        if (existingAdmin == null) {
            agentRepository.save(
                Agent(
                    company = systemCompany,
                    city = systemCity,
                    name = "Super Admin",
                    phone = superAdminPhone,
                    pin = passwordEncoder.encode(superAdminPin)!!,
                    role = UserRole.SUPER_ADMIN,
                    approvalStatus = ApprovalStatus.APPROVED,
                    isActive = true
                )
            )
            log.info("Created super admin with phone: {}", superAdminPhone)
        }

        backfillAdminSecrets()
    }

    private fun backfillAdminSecrets() {
        val agents = agentRepository.findByApprovalStatusAndAdminSecretIsNull(ApprovalStatus.APPROVED)
        if (agents.isNotEmpty()) {
            agents.forEach { it.adminSecret = generateAdminSecret() }
            agentRepository.saveAll(agents)
            log.info("Backfilled adminSecret for {} existing approved agents", agents.size)
        }
    }

    private fun generateAdminSecret(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
