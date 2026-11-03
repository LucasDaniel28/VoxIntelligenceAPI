package com.voxintelligence.api.report.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import java.math.BigDecimal;
import java.util.Map;

public record MonthlyReportResponse(int year, int month, BigDecimal income,
        BigDecimal expenses, BigDecimal balance, Map<TransactionCategory, BigDecimal> expensesByCategory) {}
