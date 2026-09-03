package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.request.TransactionSyncRequest
import com.ktconsulting.transfer_app.dto.response.TransactionSyncResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.TransactionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/transactions")
@PreAuthorize("hasRole('AGENT')")
class AgentTransactionController(private val transactionService: TransactionService) {

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
