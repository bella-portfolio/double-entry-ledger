package com.ledger.service

import com.ledger.dto.EntryRequest
import com.ledger.dto.EntryResponse
import com.ledger.dto.JournalRequest
import com.ledger.dto.JournalResponse
import com.ledger.entity.Entry
import com.ledger.entity.Journal
import com.ledger.entity.Side
import com.ledger.repository.AccountRepository
import com.ledger.repository.JournalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class JournalService(
    private val journalRepository: JournalRepository,
    private val accountRepository: AccountRepository
) {

    /**
     * Create a journal with entries, enforcing the double-entry invariant (total DEBIT = total CREDIT).
     */
    @Transactional
    fun createJournal(request: JournalRequest): JournalResponse {
        validateDoubleEntry(request.entries)

        val journalDate = request.journalDate?.let {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        } ?: LocalDate.now()

        val journal = Journal(
            description = request.description,
            journalDate = journalDate
        )

        for (entryReq in request.entries) {
            val account = accountRepository.findByCode(entryReq.accountCode)
                .orElseThrow { IllegalArgumentException("계정코드를 찾을 수 없습니다: ${entryReq.accountCode}") }

            val entry = Entry(
                account = account,
                journal = journal,
                amount = entryReq.amount,
                side = entryReq.side
            )
            journal.addEntry(entry)
        }

        val savedJournal = journalRepository.save(journal)
        return toResponse(savedJournal)
    }

    /**
     * Validate that total DEBIT equals total CREDIT (double-entry invariant).
     */
    private fun validateDoubleEntry(entries: List<EntryRequest>) {
        val totalDebit = entries
            .filter { it.side == Side.DEBIT }
            .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }

        val totalCredit = entries
            .filter { it.side == Side.CREDIT }
            .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw IllegalArgumentException(
                "복식부기 원칙 위반: 차변 합계($totalDebit)와 대변 합계($totalCredit)가 일치하지 않습니다. " +
                "차변과 대변의 합계는 항상 같아야 합니다."
            )
        }

        if (totalDebit.compareTo(BigDecimal.ZERO) == 0) {
            throw IllegalArgumentException("차변과 대변 합계가 0입니다. 유효한 금액을 입력하세요.")
        }
    }

    @Transactional(readOnly = true)
    fun getJournal(id: Long): JournalResponse {
        val journal = journalRepository.findById(id)
            .orElseThrow { IllegalArgumentException("분개를 찾을 수 없습니다: id=$id") }
        return toResponse(journal)
    }

    private fun toResponse(journal: Journal): JournalResponse {
        return JournalResponse(
            id = journal.id,
            description = journal.description,
            journalDate = journal.journalDate,
            entries = journal.entries.map { entry ->
                EntryResponse(
                    id = entry.id,
                    accountCode = entry.account!!.code,
                    accountName = entry.account!!.name,
                    amount = entry.amount,
                    side = entry.side
                )
            },
            createdAt = journal.createdAt
        )
    }
}
