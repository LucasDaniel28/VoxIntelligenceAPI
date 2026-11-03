package com.voxintelligence.api.transaction.repository;

import com.voxintelligence.api.transaction.model.FinancialTransaction;
import com.voxintelligence.api.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findAllByOrderByDateDescCreatedAtDesc();

    List<FinancialTransaction> findByTypeOrderByDateDescCreatedAtDesc(TransactionType type);

    List<FinancialTransaction> findByDateBetweenOrderByDateDescCreatedAtDesc(
            LocalDate start, LocalDate end);

    @Query("""
            select coalesce(sum(t.amount), 0) from FinancialTransaction t
            where t.type = :type and t.date between :start and :end
            """)
    BigDecimal sumByTypeAndPeriod(@Param("type") TransactionType type,
                                  @Param("start") LocalDate start,
                                  @Param("end") LocalDate end);

    List<FinancialTransaction> findByCategoryAndDateBetween(
            com.voxintelligence.api.transaction.model.TransactionCategory category,
            LocalDate start, LocalDate end);
}
