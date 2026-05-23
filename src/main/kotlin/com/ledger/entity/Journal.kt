package com.ledger.entity

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "journal")
class Journal(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 500)
    val description: String = "",

    @Column(name = "journal_date", nullable = false)
    val journalDate: LocalDate = LocalDate.now(),

    @OneToMany(mappedBy = "journal", cascade = [CascadeType.ALL], orphanRemoval = true)
    val entries: MutableList<Entry> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    /**
     * Add an entry to this journal. Maintains bidirectional consistency.
     */
    fun addEntry(entry: Entry) {
        entries.add(entry)
        entry.journal = this
    }

    /**
     * Prevent mutation after creation — journals are append-only by design.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Journal) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
