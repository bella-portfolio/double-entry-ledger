package com.ledger.controller

import com.ledger.dto.BalanceResponse
import com.ledger.dto.TrialBalanceResponse
import com.ledger.service.BalanceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/balances")
class BalanceController(
    private val balanceService: BalanceService
) {

    @GetMapping("/{accountCode}")
    fun getBalance(@PathVariable accountCode: String): ResponseEntity<BalanceResponse> {
        val balance = balanceService.getBalance(accountCode)
        return ResponseEntity.ok(balance)
    }

    @GetMapping("/trial-balance")
    fun getTrialBalance(): ResponseEntity<TrialBalanceResponse> {
        val trialBalance = balanceService.getTrialBalance()
        return ResponseEntity.ok(trialBalance)
    }
}
