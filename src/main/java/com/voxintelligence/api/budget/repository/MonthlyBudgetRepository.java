package com.voxintelligence.api.budget.repository;

import com.voxintelligence.api.budget.model.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, UUID> {
    List<MonthlyBudget> findByYearAndMonthOrderByCategory(int year, int month);
    Optional<MonthlyBudget> findByCategoryAndYearAndMonth(
            com.voxintelligence.api.transaction.model.TransactionCategory category, int year, int month);
}
