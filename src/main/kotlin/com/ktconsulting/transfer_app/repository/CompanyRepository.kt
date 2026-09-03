package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.Company
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompanyRepository : JpaRepository<Company, UUID> {
    fun findByCompanyCode(companyCode: String): Company?
    fun existsByCompanyCode(companyCode: String): Boolean
}
