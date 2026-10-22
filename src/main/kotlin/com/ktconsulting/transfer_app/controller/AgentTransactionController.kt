package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.CreateTransactionRequest
import com.ktconsulting.transfer_app.dto.request.TransactionSyncRequest
import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.dto.response.TransactionSyncResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/transactions")
@PreAuthorize("hasRole('AGENT')")
class AgentTransactionController(private val transactionService: TransactionService) {

    @PostMapping
    fun sendTransaction(@Valid @RequestBody request: CreateTransactionRequest): ResponseEntity<TransactionResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(
            transactionService.sendTransaction(
                SecurityUtil.currentAgentId(),
                SecurityUtil.currentCompanyId(),
                request
            )
        )

    @PostMapping("/{id}/confirm")
    fun confirmTransaction(@PathVariable id: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(
            transactionService.confirmTransaction(id, SecurityUtil.currentAgentId(), SecurityUtil.currentCompanyId())
        )

    @PostMapping("/{id}/collected")
    fun collectTransaction(@PathVariable id: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(
            transactionService.collectTransaction(id, SecurityUtil.currentAgentId(), SecurityUtil.currentCompanyId())
        )

    @PostMapping("/sync")
    fun syncTransactions(@Valid @RequestBody request: TransactionSyncRequest): ResponseEntity<TransactionSyncResponse> =
        ResponseEntity.ok(
            transactionService.syncTransactions(
                SecurityUtil.currentAgentId(),
                SecurityUtil.currentCompanyId(),
                request
            )
        )
}
