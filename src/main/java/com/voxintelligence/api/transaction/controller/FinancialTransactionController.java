package com.voxintelligence.api.transaction.controller;

import com.voxintelligence.api.transaction.dto.*;
import com.voxintelligence.api.transaction.service.FinancialTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class FinancialTransactionController {

    private final FinancialTransactionService service;

    public FinancialTransactionController(FinancialTransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionCreateRequest request) {
        return service.create(request);
    }

    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse confirm(@Valid @RequestBody TransactionCreateRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TransactionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody TransactionCreateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
