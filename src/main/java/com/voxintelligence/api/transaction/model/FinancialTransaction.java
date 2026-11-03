package com.voxintelligence.api.transaction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "BRL";

    @Column(nullable = false, length = 255)
    private String description;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransactionCategory category;

    @Column(length = 80)
    private String subcategory;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FinancialTransaction() {
    }

    public FinancialTransaction(UUID id, TransactionType type, BigDecimal amount, String currency,
                                String description, String location, TransactionCategory category,
                                String subcategory, LocalDate date) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.currency = currency == null ? "BRL" : currency;
        this.description = description;
        this.location = location;
        this.category = category;
        this.subcategory = subcategory;
        this.date = date;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(TransactionType type, BigDecimal amount, String description, String location,
                       TransactionCategory category, LocalDate date) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.location = location;
        this.category = category;
        this.date = date;
    }

    public UUID getId() { return id; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public TransactionCategory getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public LocalDate getDate() { return date; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
