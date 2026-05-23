package com.ledger.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class JournalRequest(
    @field:NotBlank(message = "적요는 필수입니다")
    val description: String,

    val journalDate: String? = null, // yyyy-MM-dd, defaults to today

    @field:NotEmpty(message = "분개항목은 최소 1개 이상이어야 합니다")
    @field:Valid
    val entries: List<EntryRequest>
)
