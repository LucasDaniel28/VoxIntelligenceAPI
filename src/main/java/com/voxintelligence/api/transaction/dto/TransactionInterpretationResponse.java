package com.voxintelligence.api.transaction.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TransactionInterpretationResponse(
        String originalText,
        TransactionType type,
        BigDecimal amount,
        String currency,
        String description,
        String location,
        TransactionCategory category,
        String subcategory,
        LocalDate date,
        String status,
        List<String> missingFields
) {
}
