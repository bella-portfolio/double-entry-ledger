package com.ledger.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "account")
class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 20)
    val code: String,

    @Column(nullable = false, length = 100)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: AccountType,

    // Self-referencing hierarchy for 계정과목 트리 구조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: Account? = null,

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    val children: MutableList<Account> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    // Required by JPA
    protected constructor() : this(code = "", name = "", type = AccountType.ASSET)
}
