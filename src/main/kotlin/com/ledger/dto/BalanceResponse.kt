package com.ledger.dto

import com.ledger.entity.AccountType
import java.math.BigDecimal

data class BalanceResponse(
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val totalDebit: BigDecimal,
    val totalCredit: BigDecimal,
    val balance: BigDecimal
)

data class TrialBalanceResponse(
    val accounts: List<BalanceResponse>,
    val totalDebitSum: BigDecimal,
    val totalCreditSum: BigDecimal,
    val balanceSum: BigDecimal
)
