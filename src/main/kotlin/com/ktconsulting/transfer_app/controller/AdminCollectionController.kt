package com.ktconsulting.transfer_app.controller

import com.ktconsulting.transfer_app.dto.response.TransactionResponse
import com.ktconsulting.transfer_app.security.SecurityUtil
import com.ktconsulting.transfer_app.service.CollectionService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/collections")
@PreAuthorize("hasRole('COMPANY_ADMIN')")
class AdminCollectionController(private val collectionService: CollectionService) {

    @GetMapping("/pending")
    fun getPendingCollections(): ResponseEntity<List<TransactionResponse>> =
        ResponseEntity.ok(collectionService.getPendingCollections(SecurityUtil.currentCompanyId()))

    @PostMapping("/{txnId}/approve")
    fun approveCollection(@PathVariable txnId: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(collectionService.approveCollection(txnId, SecurityUtil.currentCompanyId()))

    @PostMapping("/{txnId}/reject")
    fun rejectCollection(@PathVariable txnId: UUID): ResponseEntity<TransactionResponse> =
        ResponseEntity.ok(collectionService.rejectCollection(txnId, SecurityUtil.currentCompanyId()))
}
