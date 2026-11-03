package com.voxintelligence.api.budget.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BudgetRequest(
        @NotNull TransactionCategory category,
        @Min(2000) @Max(9999) int year,
        @Min(1) @Max(12) int month,
        @NotNull @DecimalMin("0.01") @Digits(integer=17, fraction=2) BigDecimal limitAmount
) {}
