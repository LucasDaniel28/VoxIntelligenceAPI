package com.voxintelligence.api.budget.service;

import com.voxintelligence.api.budget.dto.*;
import com.voxintelligence.api.budget.model.MonthlyBudget;
import com.voxintelligence.api.budget.repository.MonthlyBudgetRepository;
import com.voxintelligence.api.transaction.model.TransactionType;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BudgetService {
    private final MonthlyBudgetRepository budgets;
    private final FinancialTransactionRepository transactions;

    public BudgetService(MonthlyBudgetRepository budgets, FinancialTransactionRepository transactions) {
        this.budgets=budgets; this.transactions=transactions;
    }

    @Transactional
    public BudgetSummaryResponse create(BudgetRequest request) {
        if (budgets.findByCategoryAndYearAndMonth(request.category(), request.year(), request.month()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um limite para essa categoria e período.");
        MonthlyBudget saved=budgets.save(new MonthlyBudget(UUID.randomUUID(), request.category(),
                request.year(), request.month(), request.limitAmount()));
        return summarize(saved);
    }

    @Transactional(readOnly=true)
    public List<BudgetSummaryResponse> list(int year, int month) {
        return budgets.findByYearAndMonthOrderByCategory(year, month).stream().map(this::summarize).toList();
    }

    @Transactional
    public BudgetSummaryResponse update(UUID id, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O limite deve ser positivo e ter no máximo duas casas decimais.");
        MonthlyBudget budget=budgets.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Limite não encontrado."));
        budget.update(amount);
        return summarize(budget);
    }

    @Transactional
    public void delete(UUID id) {
        budgets.delete(budgets.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Limite não encontrado.")));
    }

    private BudgetSummaryResponse summarize(MonthlyBudget budget) {
        LocalDate start=LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate end=start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent=transactions.sumByTypeAndPeriod(TransactionType.EXPENSE, start, end);
        BigDecimal remaining=budget.getLimitAmount().subtract(spent);
        BigDecimal percentage=spent.multiply(BigDecimal.valueOf(100))
                .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);
        return new BudgetSummaryResponse(budget.getId(), budget.getCategory(), budget.getYear(),
                budget.getMonth(), budget.getLimitAmount(), spent, remaining, percentage,
                spent.compareTo(budget.getLimitAmount()) > 0);
    }
}
