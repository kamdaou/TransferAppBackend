package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "companies")
class Company(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var companyCode: String,

    @Column(nullable = false)
    var name: String,

    var logoUrl: String? = null,

    var primaryColor: String? = null,

    var contacts: String? = null,

    @Column(nullable = false)
    var collectionApprovalThreshold: Long = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
