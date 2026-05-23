package com.ledger.dto

import com.ledger.entity.Side
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class EntryRequest(
    @field:NotBlank(message = "계정코드는 필수입니다")
    val accountCode: String,

    @field:NotNull(message = "금액은 필수입니다")
    @field:DecimalMin(value = "0.01", message = "금액은 0.01 이상이어야 합니다")
    val amount: BigDecimal,

    @field:NotNull(message = "차변/대변 구분은 필수입니다")
    val side: Side
)
