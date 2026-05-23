package com.ledger.config

import com.ledger.entity.Account
import com.ledger.entity.AccountType
import com.ledger.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Seed Korean accounting chart of accounts (계정과목).
 * Standard Korean financial accounting 계정과목 hierarchy.
 */
@Component
class DataInitializer(
    private val accountRepository: AccountRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DataInitializer::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        if (accountRepository.count() > 0) {
            log.info("Accounts already exist. Skipping seed data.")
            return
        }

        log.info("Seeding chart of accounts...")

        // --- 자산 (ASSET) ---
        val currentAssets = Account(code = "101", name = "유동자산", type = AccountType.ASSET)
        accountRepository.save(currentAssets)

        accountRepository.save(Account(code = "10101", name = "현금", type = AccountType.ASSET, parent = currentAssets))
        accountRepository.save(Account(code = "10102", name = "은행예금", type = AccountType.ASSET, parent = currentAssets))
        accountRepository.save(Account(code = "10103", name = "미수금", type = AccountType.ASSET, parent = currentAssets))

        val nonCurrentAssets = Account(code = "102", name = "비유동자산", type = AccountType.ASSET)
        accountRepository.save(nonCurrentAssets)
        accountRepository.save(Account(code = "10201", name = "유형자산", type = AccountType.ASSET, parent = nonCurrentAssets))

        // --- 부채 (LIABILITY) ---
        val currentLiabilities = Account(code = "201", name = "유동부채", type = AccountType.LIABILITY)
        accountRepository.save(currentLiabilities)
        accountRepository.save(Account(code = "20101", name = "미지급금", type = AccountType.LIABILITY, parent = currentLiabilities))
        accountRepository.save(Account(code = "20102", name = "단기차입금", type = AccountType.LIABILITY, parent = currentLiabilities))

        // --- 자본 (EQUITY) ---
        val equity = Account(code = "301", name = "자본", type = AccountType.EQUITY)
        accountRepository.save(equity)
        accountRepository.save(Account(code = "30101", name = "자본금", type = AccountType.EQUITY, parent = equity))
        accountRepository.save(Account(code = "30102", name = "이익잉여금", type = AccountType.EQUITY, parent = equity))

        // --- 수익 (REVENUE) ---
        accountRepository.save(Account(code = "401", name = "급여수익", type = AccountType.REVENUE))
        accountRepository.save(Account(code = "402", name = "이자수익", type = AccountType.REVENUE))

        // --- 비용 (EXPENSE) ---
        accountRepository.save(Account(code = "501", name = "식비", type = AccountType.EXPENSE))
        accountRepository.save(Account(code = "502", name = "월세", type = AccountType.EXPENSE))
        accountRepository.save(Account(code = "503", name = "통신비", type = AccountType.EXPENSE))
        accountRepository.save(Account(code = "504", name = "교통비", type = AccountType.EXPENSE))

        log.info("Seeded ${accountRepository.count()} accounts.")
    }
}
