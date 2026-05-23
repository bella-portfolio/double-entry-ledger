package com.ledger.dto

import com.ledger.entity.Side
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant

data class EntryResponse(
    val id: Long,
    val accountCode: String,
    val accountName: String,
    val amount: BigDecimal,
    val side: Side
)

data class JournalResponse(
    val id: Long,
    val description: String,
    val journalDate: LocalDate,
    val entries: List<EntryResponse>,
    val createdAt: Instant
)
