package com.ledger.controller

import com.ledger.dto.JournalRequest
import com.ledger.dto.JournalResponse
import com.ledger.service.JournalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/journals")
class JournalController(
    private val journalService: JournalService
) {

    @PostMapping
    fun createJournal(@Valid @RequestBody request: JournalRequest): ResponseEntity<JournalResponse> {
        val response = journalService.createJournal(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getJournal(@PathVariable id: Long): ResponseEntity<JournalResponse> {
        val response = journalService.getJournal(id)
        return ResponseEntity.ok(response)
    }
}
