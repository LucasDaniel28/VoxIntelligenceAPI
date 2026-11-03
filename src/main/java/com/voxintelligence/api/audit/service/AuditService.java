package com.voxintelligence.api.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxintelligence.api.audit.model.AuditEvent;
import com.voxintelligence.api.audit.repository.AuditEventRepository;
import com.voxintelligence.api.budget.model.MonthlyBudget;
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
        record("FINANCIAL_TRANSACTION", transaction.getId(), "CREATE", null, transactionSnapshot(transaction));
    }

    public void recordTransactionUpdated(FinancialTransaction before, FinancialTransaction after) {
        record("FINANCIAL_TRANSACTION", after.getId(), "UPDATE",
                transactionSnapshot(before), transactionSnapshot(after));
    }

    public void recordTransactionDeleted(FinancialTransaction transaction) {
        record("FINANCIAL_TRANSACTION", transaction.getId(), "DELETE", transactionSnapshot(transaction), null);
    }

    public void recordBudgetCreated(MonthlyBudget budget) {
        record("MONTHLY_BUDGET", budget.getId(), "CREATE", null, budgetSnapshot(budget));
    }

    public void recordBudgetUpdated(MonthlyBudget before, MonthlyBudget after) {
        record("MONTHLY_BUDGET", after.getId(), "UPDATE", budgetSnapshot(before), budgetSnapshot(after));
    }

    public void recordBudgetDeleted(MonthlyBudget budget) {
        record("MONTHLY_BUDGET", budget.getId(), "DELETE", budgetSnapshot(budget), null);
    }

    private void record(String entityType, UUID entityId, String action,
                        Map<String, Object> before, Map<String, Object> after) {
        repository.save(new AuditEvent(UUID.randomUUID(), entityType, entityId, action,
                serialize(before), serialize(after), Instant.now()));
    }

    private Map<String, Object> transactionSnapshot(FinancialTransaction transaction) {
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

    private Map<String, Object> budgetSnapshot(MonthlyBudget budget) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("category", budget.getCategory());
        data.put("year", budget.getYear());
        data.put("month", budget.getMonth());
        data.put("limitAmount", budget.getLimitAmount());
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
