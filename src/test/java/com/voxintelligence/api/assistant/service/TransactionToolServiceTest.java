package com.voxintelligence.api.assistant.service;

import com.voxintelligence.api.transaction.model.*;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionToolServiceTest {

    @Mock private FinancialTransactionRepository repository;
    private TransactionToolService service;

    private void initialize() {
        service = new TransactionToolService(repository,
                Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneId.of("America/Sao_Paulo")));
    }

    @Test
    void shouldPersistExpense() {
        initialize();
        when(repository.save(any(FinancialTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result=service.registerTransaction("EXPENSE", new BigDecimal("42.50"),
                "Almoço", "FOOD", "2026-11-03");

        assertTrue(result.contains("Movimentação salva permanentemente"));
        verify(repository).save(any(FinancialTransaction.class));
    }

    @Test
    void shouldRejectInvalidAmount() {
        initialize();
        String result=service.registerTransaction("EXPENSE", BigDecimal.ZERO,
                "Compra", "SHOPPING", "2026-11-03");
        assertTrue(result.contains("valor positivo"));
        verifyNoInteractions(repository);
    }

    @Test
    void shouldListPersistedTransactions() {
        initialize();
        FinancialTransaction transaction=new FinancialTransaction(UUID.randomUUID(),
                TransactionType.EXPENSE, new BigDecimal("42.50"), "BRL", "Almoço",
                null, TransactionCategory.FOOD, null, LocalDate.of(2026, 11, 3));
        when(repository.findAllByOrderByDateDescCreatedAtDesc()).thenReturn(List.of(transaction));

        assertTrue(service.listTransactions("").contains("Almoço"));
    }

    @Test
    void shouldCalculateBalanceFromDatabaseAggregates() {
        initialize();
        when(repository.sumByTypeAndPeriod(eq(TransactionType.INCOME), any(), any()))
                .thenReturn(new BigDecimal("2500.00"));
        when(repository.sumByTypeAndPeriod(eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(new BigDecimal("500.00"));

        assertTrue(service.getBalance().contains("saldo = R$ 2000.00"));
    }
}
