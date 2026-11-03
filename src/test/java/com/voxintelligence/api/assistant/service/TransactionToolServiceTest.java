package com.voxintelligence.api.assistant.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TransactionToolServiceTest {

    private final TransactionToolService service = new TransactionToolService(
            Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    );

    @Test
    void shouldRegisterExpenseAndIncludeItInBalance() {
        String result = service.registerTransaction(
                "EXPENSE", new BigDecimal("42.50"), "Almoço", "FOOD", "2026-11-03"
        );

        assertTrue(result.contains("Movimentação registrada com sucesso"));
        assertTrue(service.listTransactions("EXPENSE").contains("Almoço"));
        assertTrue(service.getBalance().contains("saldo = R$ -42.50"));
    }

    @Test
    void shouldRegisterIncomeAndCalculateNetBalance() {
        service.registerTransaction("INCOME", new BigDecimal("2500.00"),
                "Salário", "SALARY", "2026-11-03");
        service.registerTransaction("EXPENSE", new BigDecimal("500.00"),
                "Aluguel", "HOUSING", "2026-11-03");

        assertTrue(service.getBalance().contains("saldo = R$ 2000.00"));
    }

    @Test
    void shouldRejectInvalidAmount() {
        String result = service.registerTransaction(
                "EXPENSE", BigDecimal.ZERO, "Compra", "SHOPPING", "2026-11-03"
        );

        assertTrue(result.contains("valor deve ser maior que zero"));
        assertTrue(service.listTransactions("").contains("Nenhuma movimentação"));
    }

    @Test
    void shouldUseCurrentDateWhenDateIsMissing() {
        String result = service.registerTransaction(
                "INCOME", new BigDecimal("100"), "Venda", "OTHER", ""
        );

        assertTrue(result.contains("2026-11-03"));
    }
}
