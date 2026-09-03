package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "cities")
class City(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var isActive: Boolean = true
)
