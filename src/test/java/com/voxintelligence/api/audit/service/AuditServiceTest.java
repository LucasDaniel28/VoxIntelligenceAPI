package com.voxintelligence.api.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxintelligence.api.audit.model.AuditEvent;
import com.voxintelligence.api.audit.repository.AuditEventRepository;
import com.voxintelligence.api.transaction.model.FinancialTransaction;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditService service = new AuditService(repository, new ObjectMapper());

    @Test
    void shouldRecordCreationWithAfterSnapshot() throws Exception {
        FinancialTransaction transaction = transaction(new BigDecimal("45.90"), "Almoço");

        service.recordTransactionCreated(transaction);

        AuditEvent event = capturedEvent();
        assertEquals("FINANCIAL_TRANSACTION", event.getEntityType());
        assertEquals(transaction.getId(), event.getEntityId());
        assertEquals("CREATE", event.getAction());
        assertNull(event.getBeforeData());
        assertEquals("45.90", new ObjectMapper().readTree(event.getAfterData()).get("amount").asText());
        assertEquals("Almoço", new ObjectMapper().readTree(event.getAfterData()).get("description").asText());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void shouldRecordUpdateWithBeforeAndAfterSnapshots() throws Exception {
        FinancialTransaction before = transaction(new BigDecimal("45.90"), "Almoço");
        FinancialTransaction after = transactionWithId(before.getId(), new BigDecimal("55.00"), "Almoço executivo");

        service.recordTransactionUpdated(before, after);

        AuditEvent event = capturedEvent();
        assertEquals("UPDATE", event.getAction());
        assertEquals("45.90", new ObjectMapper().readTree(event.getBeforeData()).get("amount").asText());
        assertEquals("55.00", new ObjectMapper().readTree(event.getAfterData()).get("amount").asText());
    }

    @Test
    void shouldRecordDeletionWithBeforeSnapshot() throws Exception {
        FinancialTransaction transaction = transaction(new BigDecimal("45.90"), "Almoço");

        service.recordTransactionDeleted(transaction);

        AuditEvent event = capturedEvent();
        assertEquals("DELETE", event.getAction());
        assertNotNull(event.getBeforeData());
        assertNull(event.getAfterData());
    }

    private AuditEvent capturedEvent() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private FinancialTransaction transaction(BigDecimal amount, String description) {
        return transactionWithId(UUID.randomUUID(), amount, description);
    }

    private FinancialTransaction transactionWithId(UUID id, BigDecimal amount, String description) {
        return new FinancialTransaction(id, TransactionType.EXPENSE, amount, "BRL",
                description, "Restaurante", TransactionCategory.FOOD, "RESTAURANT",
                LocalDate.of(2026, 11, 3));
    }
}
