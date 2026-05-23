package com.ledger.controller

import com.ledger.repository.AccountRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AccountResponse(
    val id: Long,
    val code: String,
    val name: String,
    val type: String,
    val parentCode: String?
)

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountRepository: AccountRepository
) {

    @GetMapping
    fun listAccounts(): ResponseEntity<List<AccountResponse>> {
        val accounts = accountRepository.findAll().map { account ->
            AccountResponse(
                id = account.id,
                code = account.code,
                name = account.name,
                type = account.type.name,
                parentCode = account.parent?.code
            )
        }
        return ResponseEntity.ok(accounts)
    }
}
