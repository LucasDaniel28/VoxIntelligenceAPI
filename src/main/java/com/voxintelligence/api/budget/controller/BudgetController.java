package com.voxintelligence.api.budget.controller;

import com.voxintelligence.api.budget.dto.*;
import com.voxintelligence.api.budget.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@Validated
public class BudgetController {
    private final BudgetService service;
    public BudgetController(BudgetService service) { this.service=service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetSummaryResponse create(@Valid @RequestBody BudgetRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<BudgetSummaryResponse> list(
            @RequestParam @Min(2000) @Max(9999) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return service.list(year, month);
    }

    @PutMapping("/{id}")
    public BudgetSummaryResponse update(@PathVariable UUID id,
            @RequestBody @Valid AmountRequest request) {
        return service.update(id, request.limitAmount());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }

    public record AmountRequest(@NotNull @DecimalMin("0.01") @Digits(integer=17, fraction=2)
                                BigDecimal limitAmount) {}
}
