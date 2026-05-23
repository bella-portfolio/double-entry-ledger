package com.ledger.repository

import com.ledger.entity.Entry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EntryRepository : JpaRepository<Entry, Long>
