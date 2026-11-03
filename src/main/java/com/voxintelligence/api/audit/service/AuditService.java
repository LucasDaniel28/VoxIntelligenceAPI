package com.voxintelligence.api.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxintelligence.api.audit.model.AuditEvent;
import com.voxintelligence.api.audit.repository.AuditEventRepository;
import com.voxintelligence.api.transaction.model.FinancialTransaction;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void recordTransactionCreated(FinancialTransaction transaction) {
        record(transaction, "CREATE", null, snapshot(transaction));
    }

    public void recordTransactionUpdated(FinancialTransaction before, FinancialTransaction after) {
        record(after, "UPDATE", snapshot(before), snapshot(after));
    }

    public void recordTransactionDeleted(FinancialTransaction transaction) {
        record(transaction, "DELETE", snapshot(transaction), null);
    }

    private void record(FinancialTransaction transaction, String action,
                        Map<String, Object> before, Map<String, Object> after) {
        repository.save(new AuditEvent(
                UUID.randomUUID(),
                "FINANCIAL_TRANSACTION",
                transaction.getId(),
                action,
                serialize(before),
                serialize(after),
                Instant.now()
        ));
    }

    private Map<String, Object> snapshot(FinancialTransaction transaction) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", transaction.getType());
        data.put("amount", transaction.getAmount());
        data.put("currency", transaction.getCurrency());
        data.put("description", transaction.getDescription());
        data.put("location", transaction.getLocation());
        data.put("category", transaction.getCategory());
        data.put("subcategory", transaction.getSubcategory());
        data.put("date", transaction.getDate());
        return data;
    }

    private String serialize(Map<String, Object> data) {
        if (data == null) return null;
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o evento de auditoria.", exception);
        }
    }
}
