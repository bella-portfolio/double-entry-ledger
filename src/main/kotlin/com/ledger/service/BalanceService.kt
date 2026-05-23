package com.ledger.service

import com.ledger.dto.BalanceResponse
import com.ledger.dto.TrialBalanceResponse
import com.ledger.entity.AccountType
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class BalanceService(
    private val entityManager: EntityManager
) {

    /**
     * Get the balance for a specific account by account code.
     * Calculates debit/credit totals via native SQL and applies normal-balance sign convention.
     */
    @Transactional(readOnly = true)
    fun getBalance(accountCode: String): BalanceResponse {
        val sql = """
            SELECT 
                a.code,
                a.name,
                a.type,
                COALESCE(SUM(CASE WHEN e.side = 'DEBIT' THEN e.amount ELSE 0 END), 0) as total_debit,
                COALESCE(SUM(CASE WHEN e.side = 'CREDIT' THEN e.amount ELSE 0 END), 0) as total_credit
            FROM account a
            LEFT JOIN entry e ON a.id = e.account_id
            WHERE a.code = :code
            GROUP BY a.id, a.code, a.name, a.type
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(sql)
            .setParameter("code", accountCode)
            .resultList

        if (rows.isEmpty()) {
            throw IllegalArgumentException("계정코드를 찾을 수 없습니다: $accountCode")
        }

        val row = rows[0] as Array<*>
        val code = row[0] as String
        val name = row[1] as String
        val typeStr = row[2] as String
        val type = AccountType.valueOf(typeStr)
        val totalDebit = BigDecimal((row[3] as Number).toString()).setScale(2, RoundingMode.HALF_UP)
        val totalCredit = BigDecimal((row[4] as Number).toString()).setScale(2, RoundingMode.HALF_UP)

        val balance = calculateBalance(type, totalDebit, totalCredit)

        return BalanceResponse(
            accountCode = code,
            accountName = name,
            accountType = type,
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            balance = balance
        )
    }

    /**
     * Get the trial balance (합계잔액시산표) — all accounts with debit/credit totals and balance.
     */
    @Transactional(readOnly = true)
    fun getTrialBalance(): TrialBalanceResponse {
        val sql = """
            SELECT 
                a.code,
                a.name,
                a.type,
                COALESCE(SUM(CASE WHEN e.side = 'DEBIT' THEN e.amount ELSE 0 END), 0) as total_debit,
                COALESCE(SUM(CASE WHEN e.side = 'CREDIT' THEN e.amount ELSE 0 END), 0) as total_credit
            FROM account a
            LEFT JOIN entry e ON a.id = e.account_id
            GROUP BY a.id, a.code, a.name, a.type
            ORDER BY a.code
        """.trimIndent()

        @Suppress("UNCHECKED_CAST")
        val rows = entityManager.createNativeQuery(sql).resultList

        var totalDebitSum = BigDecimal.ZERO.setScale(2)
        var totalCreditSum = BigDecimal.ZERO.setScale(2)
        var balanceSum = BigDecimal.ZERO.setScale(2)

        val accounts = rows.map { row ->
            val arr = row as Array<*>
            val code = arr[0] as String
            val name = arr[1] as String
            val typeStr = arr[2] as String
            val type = AccountType.valueOf(typeStr)
            val totalDebit = BigDecimal((arr[3] as Number).toString()).setScale(2, RoundingMode.HALF_UP)
            val totalCredit = BigDecimal((arr[4] as Number).toString()).setScale(2, RoundingMode.HALF_UP)
            val balance = calculateBalance(type, totalDebit, totalCredit)

            totalDebitSum = totalDebitSum.add(totalDebit)
            totalCreditSum = totalCreditSum.add(totalCredit)
            balanceSum = balanceSum.add(balance)

            BalanceResponse(
                accountCode = code,
                accountName = name,
                accountType = type,
                totalDebit = totalDebit,
                totalCredit = totalCredit,
                balance = balance
            )
        }

        return TrialBalanceResponse(
            accounts = accounts,
            totalDebitSum = totalDebitSum,
            totalCreditSum = totalCreditSum,
            balanceSum = balanceSum
        )
    }

    /**
     * Calculate balance based on account type's normal balance side.
     * ASSET and EXPENSE have normal DEBIT balance.
     * LIABILITY, EQUITY, and REVENUE have normal CREDIT balance.
     */
    private fun calculateBalance(type: AccountType, debit: BigDecimal, credit: BigDecimal): BigDecimal {
        return when (type) {
            AccountType.ASSET, AccountType.EXPENSE -> debit.subtract(credit)
            AccountType.LIABILITY, AccountType.EQUITY, AccountType.REVENUE -> credit.subtract(debit)
        }.setScale(2, RoundingMode.HALF_UP)
    }
}
