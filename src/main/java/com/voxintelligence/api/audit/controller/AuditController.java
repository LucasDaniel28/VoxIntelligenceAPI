package com.voxintelligence.api.audit.controller;

import com.voxintelligence.api.audit.model.AuditEvent;
import com.voxintelligence.api.audit.repository.AuditEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditEventRepository repository;

    public AuditController(AuditEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/transactions/{transactionId}")
    public List<AuditEvent> findTransactionHistory(@PathVariable UUID transactionId) {
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                "FINANCIAL_TRANSACTION", transactionId);
    }

    @GetMapping("/budgets/{budgetId}")
    public List<AuditEvent> findBudgetHistory(@PathVariable UUID budgetId) {
        return repository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                "MONTHLY_BUDGET", budgetId);
    }
}
