package com.ktconsulting.transfer_app.repository

import com.ktconsulting.transfer_app.entity.TransferLimits
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransferLimitsRepository : JpaRepository<TransferLimits, UUID> {
    fun findByCompanyId(companyId: UUID): TransferLimits?
}
