package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.CommissionRate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CommissionRateRepository : JpaRepository<CommissionRate, UUID> {
    fun findByCompanyId(companyId: UUID): List<CommissionRate>

    @Query(
        "SELECT cr FROM CommissionRate cr WHERE cr.company.id = :companyId " +
        "AND cr.sendingCity.id = :sendingCityId AND cr.receivingCity.id = :receivingCityId " +
        "AND :amount BETWEEN cr.minAmount AND cr.maxAmount"
    )
    fun findRate(companyId: UUID, sendingCityId: UUID, receivingCityId: UUID, amount: Long): CommissionRate?
}
