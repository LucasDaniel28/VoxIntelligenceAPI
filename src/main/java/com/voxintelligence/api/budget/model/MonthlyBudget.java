package com.voxintelligence.api.budget.model;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monthly_budgets",
       uniqueConstraints = @UniqueConstraint(name = "uk_monthly_budget_category_period",
               columnNames = {"category", "year", "month"}))
public class MonthlyBudget {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 40)
    private TransactionCategory category;
    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;
    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal limitAmount;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected MonthlyBudget() {}
    public MonthlyBudget(UUID id, TransactionCategory category, int year, int month, BigDecimal limitAmount) {
        this.id=id; this.category=category; this.year=year; this.month=month; this.limitAmount=limitAmount;
    }
    @PrePersist void onCreate() { createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate void onUpdate() { updatedAt=Instant.now(); }
    public void update(BigDecimal amount) { this.limitAmount=amount; }
    public UUID getId(){return id;}
    public TransactionCategory getCategory(){return category;}
    public int getYear(){return year;}
    public int getMonth(){return month;}
    public BigDecimal getLimitAmount(){return limitAmount;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getUpdatedAt(){return updatedAt;}
}
