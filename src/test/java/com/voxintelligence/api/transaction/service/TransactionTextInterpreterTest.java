package com.voxintelligence.api.transaction.service;

import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTextInterpreterTest {

    private TransactionTextInterpreter interpreter;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-10-30T12:00:00Z"),
                ZoneId.of("America/Belem")
        );
        interpreter = new TransactionTextInterpreter(clock);
    }

    @Test
    void shouldInterpretExpenseAtStarbucks() {
        TransactionInterpretationResponse result =
                interpreter.interpret("Gastei 50 reais no Starbucks agora");

        assertEquals(TransactionType.EXPENSE, result.type());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertEquals("Starbucks", result.location());
        assertEquals(TransactionCategory.FOOD, result.category());
        assertEquals("COFFEE", result.subcategory());
        assertEquals("READY_FOR_CONFIRMATION", result.status());
        assertTrue(result.missingFields().isEmpty());
    }

    @Test
    void shouldInterpretBrazilianAmountWithThousandsSeparator() {
        TransactionInterpretationResponse result =
                interpreter.interpret("Paguei R$ 1.250,90 no mercado hoje");

        assertEquals(new BigDecimal("1250.90"), result.amount());
        assertEquals(TransactionCategory.FOOD, result.category());
    }

    @Test
    void shouldIdentifyIncome() {
        TransactionInterpretationResponse result =
                interpreter.interpret("Recebi 3500 reais de salário");

        assertEquals(TransactionType.INCOME, result.type());
        assertEquals(TransactionCategory.SALARY, result.category());
        assertEquals(new BigDecimal("3500.00"), result.amount());
    }

    @Test
    void shouldRequestMissingAmount() {
        TransactionInterpretationResponse result =
                interpreter.interpret("Gastei no Starbucks agora");

        assertEquals("NEEDS_INFORMATION", result.status());
        assertTrue(result.missingFields().contains("amount"));
        assertNull(result.amount());
    }

    @Test
    void shouldUseYesterdayWhenMentioned() {
        TransactionInterpretationResponse result =
                interpreter.interpret("Gastei 25 reais no almoço ontem");

        assertEquals("2026-10-29", result.date().toString());
    }
}
