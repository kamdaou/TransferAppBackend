package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "cash_adjustments")
class CashAdjustment(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    val agent: Agent,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false)
    val reason: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_admin_id", nullable = false)
    val performedByAdmin: Agent,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
