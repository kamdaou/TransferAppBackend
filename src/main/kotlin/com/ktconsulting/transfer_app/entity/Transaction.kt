package com.ktconsulting.transfer_app.entity

import com.ktconsulting.transfer_app.enum.TransactionDirection
import com.ktconsulting.transfer_app.enum.TransactionStatus
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "transactions")
class Transaction(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    val company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    val agent: Agent,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    val partner: Agent,

    @Column(nullable = false)
    var amount: Long,

    @Column(nullable = false)
    var fee: Long,

    @Column(nullable = false)
    var feeIncluded: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_city_id", nullable = false)
    val senderCity: City,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_city_id", nullable = false)
    val receiverCity: City,

    @Column(nullable = false)
    var senderPhone: String,

    @Column(nullable = false)
    var senderName: String,

    @Column(nullable = false)
    var receiverPhone: String,

    @Column(nullable = false)
    var receiverName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var direction: TransactionDirection,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TransactionStatus = TransactionStatus.PENDING,

    var reversalReason: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
)
