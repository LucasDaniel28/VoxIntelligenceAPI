package com.voxintelligence.api.transaction.service;

import com.voxintelligence.api.transaction.dto.*;
import com.voxintelligence.api.transaction.model.FinancialTransaction;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FinancialTransactionService {

    private final FinancialTransactionRepository repository;

    public FinancialTransactionService(FinancialTransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        FinancialTransaction saved = repository.save(new FinancialTransaction(
                UUID.randomUUID(), request.type(), request.amount(), "BRL",
                request.description().trim(), request.location(), request.category(),
                request.subcategory(), request.date()));
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return repository.findAllByOrderByDateDescCreatedAtDesc()
                .stream().map(TransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID id) {
        return TransactionResponse.from(getEntity(id));
    }

    @Transactional
    public TransactionResponse update(UUID id, TransactionCreateRequest request) {
        FinancialTransaction transaction = getEntity(id);
        transaction.update(request.amount(), request.description().trim(),
                request.location(), request.category(), request.date());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(getEntity(id));
    }

    private FinancialTransaction getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Movimentação não encontrada."));
    }
}
