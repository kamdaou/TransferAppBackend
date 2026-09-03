package com.ktconsulting.transfer_app.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "agent_pairings")
class AgentPairing(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent1_id", nullable = false)
    val agent1: Agent,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent2_id", nullable = false)
    val agent2: Agent,

    @Column(nullable = false)
    var sharedSecret: String,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
