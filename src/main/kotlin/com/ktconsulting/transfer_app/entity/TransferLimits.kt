package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "transfer_limits")
class TransferLimits(

    @Id
    val companyId: UUID,

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    val company: Company,

    @Column(nullable = false)
    var minPerTransaction: Long = 0,

    @Column(nullable = false)
    var maxPerTransaction: Long = Long.MAX_VALUE,

    @Column(nullable = false)
    var dailyCap: Long = Long.MAX_VALUE
)
