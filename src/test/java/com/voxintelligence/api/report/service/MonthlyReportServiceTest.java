package com.voxintelligence.api.report.service;

import com.voxintelligence.api.transaction.model.*;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonthlyReportServiceTest {
    private final FinancialTransactionRepository repository=mock(FinancialTransactionRepository.class);
    private final MonthlyReportService service=new MonthlyReportService(repository);

    @Test
    void shouldCalculateMonthlyTotalsAndCategoryBreakdown() {
        var food=new FinancialTransaction(UUID.randomUUID(), TransactionType.EXPENSE,
                new BigDecimal("100.00"), "BRL", "Mercado", null,
                TransactionCategory.FOOD, null, LocalDate.of(2026,11,2));
        var salary=new FinancialTransaction(UUID.randomUUID(), TransactionType.INCOME,
                new BigDecimal("3000.00"), "BRL", "Salário", null,
                TransactionCategory.SALARY, null, LocalDate.of(2026,11,1));
        when(repository.findByDateBetweenOrderByDateDescCreatedAtDesc(any(),any()))
                .thenReturn(List.of(food,salary));

        var report=service.get(2026,11);

        assertEquals(new BigDecimal("3000.00"), report.income());
        assertEquals(new BigDecimal("100.00"), report.expenses());
        assertEquals(new BigDecimal("2900.00"), report.balance());
        assertEquals(new BigDecimal("100.00"), report.expensesByCategory().get(TransactionCategory.FOOD));
    }
}
