package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "commission_rates")
class CommissionRate(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sending_city_id", nullable = false)
    var sendingCity: City,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiving_city_id", nullable = false)
    var receivingCity: City,

    @Column(nullable = false)
    var minAmount: Long,

    @Column(nullable = false)
    var maxAmount: Long,

    @Column(nullable = false)
    var fee: Long
)
