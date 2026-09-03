package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.City
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CityRepository : JpaRepository<City, UUID> {
    fun findByCompanyIdAndIsActiveTrue(companyId: UUID): List<City>
    fun findByCompanyId(companyId: UUID): List<City>
    fun existsByCompanyIdAndName(companyId: UUID, name: String): Boolean
}
