package com.voxintelligence.api.budget.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import java.math.BigDecimal;
import java.util.UUID;

public record BudgetSummaryResponse(UUID id, TransactionCategory category, int year, int month,
        BigDecimal limitAmount, BigDecimal spentAmount, BigDecimal remainingAmount,
        BigDecimal usagePercentage, boolean exceeded) {}
