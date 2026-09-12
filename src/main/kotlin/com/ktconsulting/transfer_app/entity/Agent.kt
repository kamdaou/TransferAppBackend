package com.ktconsulting.transfer_app.entity

import com.ktconsulting.transfer_app.enum.ApprovalStatus
import com.ktconsulting.transfer_app.enum.UserRole
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "agents",
    uniqueConstraints = [UniqueConstraint(columnNames = ["company_id", "phone"])]
)
class Agent(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    var city: City,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var phone: String,

    @Column(nullable = false)
    var pin: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.AGENT,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var approvalStatus: ApprovalStatus = ApprovalStatus.PENDING,

    @Column(nullable = false)
    var initialCash: Long = 0,

    @Column
    var adminSecret: String? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
