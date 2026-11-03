package com.voxintelligence.api.budget.service;

import com.voxintelligence.api.audit.service.AuditService;
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
    private final MonthlyBudgetRepository budgets = mock(MonthlyBudgetRepository.class);
    private final FinancialTransactionRepository transactions = mock(FinancialTransactionRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final BudgetService service = new BudgetService(budgets, transactions, auditService);

    @Test
    void shouldCreateBudgetAndCalculateCategoryUsage() {
        when(budgets.findByCategoryAndYearAndMonth(TransactionCategory.FOOD, 2026, 11))
                .thenReturn(Optional.empty());
        when(budgets.save(any(MonthlyBudget.class))).thenAnswer(i -> i.getArgument(0));
        when(transactions.sumByTypeCategoryAndPeriod(eq(TransactionType.EXPENSE),
                eq(TransactionCategory.FOOD), any(), any())).thenReturn(new BigDecimal("200.00"));

        var result = service.create(new BudgetRequest(TransactionCategory.FOOD, 2026, 11,
                new BigDecimal("800.00")));

        assertEquals(new BigDecimal("200.00"), result.spentAmount());
        assertEquals(new BigDecimal("600.00"), result.remainingAmount());
        assertEquals(new BigDecimal("25.00"), result.usagePercentage());
        assertFalse(result.exceeded());
        verify(auditService).recordBudgetCreated(any(MonthlyBudget.class));
    }

    @Test
    void shouldReportExceededBudget() {
        MonthlyBudget budget = new MonthlyBudget(UUID.randomUUID(), TransactionCategory.FOOD,
                2026, 11, new BigDecimal("100.00"));
        when(budgets.findByCategoryAndYearAndMonth(TransactionCategory.FOOD, 2026, 11))
                .thenReturn(Optional.empty());
        when(budgets.save(any(MonthlyBudget.class))).thenReturn(budget);
        when(transactions.sumByTypeCategoryAndPeriod(eq(TransactionType.EXPENSE),
                eq(TransactionCategory.FOOD), any(), any())).thenReturn(new BigDecimal("120.00"));

        assertTrue(service.create(new BudgetRequest(TransactionCategory.FOOD, 2026, 11,
                new BigDecimal("100.00"))).exceeded());
    }

    @Test
    void shouldAuditBudgetUpdateWithPreviousAndNewValues() {
        UUID id = UUID.randomUUID();
        MonthlyBudget budget = new MonthlyBudget(id, TransactionCategory.FOOD,
                2026, 11, new BigDecimal("100.00"));
        when(budgets.findById(id)).thenReturn(Optional.of(budget));
        when(transactions.sumByTypeCategoryAndPeriod(eq(TransactionType.EXPENSE),
                eq(TransactionCategory.FOOD), any(), any())).thenReturn(BigDecimal.ZERO);

        service.update(id, new BigDecimal("150.00"));

        var captor = org.mockito.ArgumentCaptor.forClass(MonthlyBudget.class);
        verify(auditService).recordBudgetUpdated(captor.capture(), eq(budget));
        assertEquals(new BigDecimal("100.00"), captor.getValue().getLimitAmount());
        assertEquals(new BigDecimal("150.00"), budget.getLimitAmount());
    }

    @Test
    void shouldAuditBudgetDeletionBeforeRemovingIt() {
        UUID id = UUID.randomUUID();
        MonthlyBudget budget = new MonthlyBudget(id, TransactionCategory.FOOD,
                2026, 11, new BigDecimal("100.00"));
        when(budgets.findById(id)).thenReturn(Optional.of(budget));

        service.delete(id);

        verify(auditService).recordBudgetDeleted(budget);
        verify(budgets).delete(budget);
    }
}
