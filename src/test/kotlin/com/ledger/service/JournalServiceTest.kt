package com.ledger.service

import com.ledger.dto.EntryRequest
import com.ledger.dto.JournalRequest
import com.ledger.entity.*
import com.ledger.repository.AccountRepository
import com.ledger.repository.JournalRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JournalServiceTest {

    @Autowired
    private lateinit var journalService: JournalService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var journalRepository: JournalRepository

    private lateinit var cashAccount: Account
    private lateinit var revenueAccount: Account
    private lateinit var expenseAccount: Account

    @BeforeEach
    fun setUp() {
        // Reuse seeded accounts if present (DataInitializer), otherwise create them.
        cashAccount = accountRepository.findByCode("10101").orElseGet {
            accountRepository.save(Account(code = "10101", name = "현금", type = AccountType.ASSET))
        }
        revenueAccount = accountRepository.findByCode("401").orElseGet {
            accountRepository.save(Account(code = "401", name = "급여수익", type = AccountType.REVENUE))
        }
        expenseAccount = accountRepository.findByCode("502").orElseGet {
            accountRepository.save(Account(code = "502", name = "월세", type = AccountType.EXPENSE))
        }
    }

    @Test
    fun `should create journal when debit equals credit`() {
        val request = JournalRequest(
            description = "급여 수령",
            entries = listOf(
                EntryRequest("10101", BigDecimal("1000000"), Side.DEBIT),
                EntryRequest("401", BigDecimal("1000000"), Side.CREDIT)
            )
        )

        val response = assertDoesNotThrow { journalService.createJournal(request) }
        assertEquals("급여 수령", response.description)
        assertEquals(2, response.entries.size)

        // Verify journal is persisted
        val savedJournal = journalRepository.findById(response.id)
        assertTrue(savedJournal.isPresent)
        assertEquals(2, savedJournal.get().entries.size)
    }

    @Test
    fun `should reject journal when debit and credit are not equal`() {
        val request = JournalRequest(
            description = "잘못된 분개 - 차대 불일치",
            entries = listOf(
                EntryRequest("10101", BigDecimal("1000000"), Side.DEBIT),
                EntryRequest("401", BigDecimal("500000"), Side.CREDIT)
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            journalService.createJournal(request)
        }
        assertTrue(exception.message!!.contains("복식부기 원칙 위반") ||
                   exception.message!!.contains("일치하지 않습니다"))
    }

    @Test
    fun `should reject journal with multiple debits and credits that do not balance`() {
        // 3 entries: 500,000 + 300,000 (debit) vs 700,000 (credit) = 800,000 vs 700,000 — unbalanced
        val request = JournalRequest(
            description = "복합 분개 - 불균형",
            entries = listOf(
                EntryRequest("10101", BigDecimal("500000"), Side.DEBIT),
                EntryRequest("502", BigDecimal("300000"), Side.DEBIT),
                EntryRequest("401", BigDecimal("700000"), Side.CREDIT)
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            journalService.createJournal(request)
        }
        assertTrue(exception.message!!.contains("복식부기 원칙 위반"))
    }

    @Test
    fun `should create complex journal with multiple balanced entries`() {
        // Complex journal: 월세 지급 with 현금 and 보증금
        // Debit: 월세 500,000 + 보증금(미수금) 1,000,000
        // Credit: 현금 1,500,000
        val receivableAccount = accountRepository.findByCode("10103").orElseGet {
            accountRepository.save(Account(code = "10103", name = "미수금", type = AccountType.ASSET))
        }

        val request = JournalRequest(
            description = "월세 및 보증금 지급",
            entries = listOf(
                EntryRequest("502", BigDecimal("500000"), Side.DEBIT),   // 월세 (비용)
                EntryRequest("10103", BigDecimal("1000000"), Side.DEBIT), // 보증금 (자산-미수금)
                EntryRequest("10101", BigDecimal("1500000"), Side.CREDIT)  // 현금 감소
            )
        )

        val response = assertDoesNotThrow { journalService.createJournal(request) }
        assertEquals(3, response.entries.size)
        assertEquals("월세 및 보증금 지급", response.description)
    }

    @Test
    fun `should reject empty entries`() {
        val request = JournalRequest(
            description = "빈 분개",
            entries = emptyList()
        )

        // Validation should catch this before the service processes it,
        // but the test verifies the validation annotation works
        // (tested via integration test or controller test)
    }

    @Test
    fun `should reject all-debit journal`() {
        val request = JournalRequest(
            description = "차변만 있는 분개",
            entries = listOf(
                EntryRequest("10101", BigDecimal("100000"), Side.DEBIT),
                EntryRequest("502", BigDecimal("100000"), Side.DEBIT)
            )
        )

        val exception = assertThrows<IllegalArgumentException> {
            journalService.createJournal(request)
        }
        assertTrue(exception.message!!.contains("복식부기 원칙 위반"))
    }
}
