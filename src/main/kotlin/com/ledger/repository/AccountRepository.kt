package com.ledger.repository

import com.ledger.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByCode(code: String): Optional<Account>
    fun findByParentIsNull(): List<Account>
}
