package com.voxintelligence.api.transaction.dto;

import com.voxintelligence.api.transaction.model.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id, TransactionType type, BigDecimal amount, String currency,
        String description, String location, TransactionCategory category,
        String subcategory, LocalDate date, Instant createdAt, Instant updatedAt
) {
    public static TransactionResponse from(FinancialTransaction t) {
        return new TransactionResponse(t.getId(), t.getType(), t.getAmount(),
                t.getCurrency(), t.getDescription(), t.getLocation(), t.getCategory(),
                t.getSubcategory(), t.getDate(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
