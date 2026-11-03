package com.voxintelligence.api.transaction.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Estrutura intermediária preenchida a partir da descrição enviada pelo usuário.
 */
public record TransactionExtraction(
        TransactionType type,
        BigDecimal amount,
        String currency,
        String description,
        String location,
        TransactionCategory category,
        String subcategory,
        LocalDate date
) {
}
