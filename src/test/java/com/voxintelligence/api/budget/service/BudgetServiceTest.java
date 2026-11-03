package com.voxintelligence.api.budget.service;

import com.voxintelligence.api.budget.dto.BudgetRequest;
import com.voxintelligence.api.budget.model.MonthlyBudget;
import com.voxintelligence.api.budget.repository.MonthlyBudgetRepository;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BudgetServiceTest {
    private final MonthlyBudgetRepository budgets=mock(MonthlyBudgetRepository.class);
    private final FinancialTransactionRepository transactions=mock(FinancialTransactionRepository.class);
    private final BudgetService service=new BudgetService(budgets, transactions);

    @Test
    void shouldCreateBudgetAndCalculateCategoryUsage() {
        when(budgets.findByCategoryAndYearAndMonth(TransactionCategory.FOOD, 2026, 11))
                .thenReturn(Optional.empty());
        when(budgets.save(any(MonthlyBudget.class))).thenAnswer(i -> i.getArgument(0));
        when(transactions.sumByTypeCategoryAndPeriod(eq(TransactionType.EXPENSE),
                eq(TransactionCategory.FOOD), any(), any())).thenReturn(new BigDecimal("200.00"));

        var result=service.create(new BudgetRequest(TransactionCategory.FOOD, 2026, 11,
                new BigDecimal("800.00")));

        assertEquals(new BigDecimal("200.00"), result.spentAmount());
        assertEquals(new BigDecimal("600.00"), result.remainingAmount());
        assertEquals(new BigDecimal("25.00"), result.usagePercentage());
        assertFalse(result.exceeded());
    }

    @Test
    void shouldReportExceededBudget() {
        MonthlyBudget budget=new MonthlyBudget(UUID.randomUUID(), TransactionCategory.FOOD,
                2026, 11, new BigDecimal("100.00"));
        when(budgets.findByCategoryAndYearAndMonth(TransactionCategory.FOOD, 2026, 11))
                .thenReturn(Optional.empty());
        when(budgets.save(any(MonthlyBudget.class))).thenReturn(budget);
        when(transactions.sumByTypeCategoryAndPeriod(eq(TransactionType.EXPENSE),
                eq(TransactionCategory.FOOD), any(), any())).thenReturn(new BigDecimal("120.00"));

        assertTrue(service.create(new BudgetRequest(TransactionCategory.FOOD, 2026, 11,
                new BigDecimal("100.00"))).exceeded());
    }
}
