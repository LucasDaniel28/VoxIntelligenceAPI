package com.voxintelligence.api.report.service;

import com.voxintelligence.api.report.dto.MonthlyReportResponse;
import com.voxintelligence.api.transaction.model.*;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonthlyReportService {
    private final FinancialTransactionRepository repository;
    public MonthlyReportService(FinancialTransactionRepository repository) { this.repository=repository; }

    @Transactional(readOnly=true)
    public MonthlyReportResponse get(int year, int month) {
        LocalDate start=LocalDate.of(year, month, 1);
        LocalDate end=start.withDayOfMonth(start.lengthOfMonth());
        var records=repository.findByDateBetweenOrderByDateDescCreatedAtDesc(start, end);
        BigDecimal income=records.stream().filter(t -> t.getType()==TransactionType.INCOME)
                .map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses=records.stream().filter(t -> t.getType()==TransactionType.EXPENSE)
                .map(FinancialTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<TransactionCategory, BigDecimal> byCategory=records.stream()
                .filter(t -> t.getType()==TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(FinancialTransaction::getCategory,
                        Collectors.mapping(FinancialTransaction::getAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        return new MonthlyReportResponse(year, month, income, expenses, income.subtract(expenses), byCategory);
    }
}
