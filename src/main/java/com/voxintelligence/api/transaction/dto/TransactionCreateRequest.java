package com.voxintelligence.api.transaction.dto;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionCreateRequest(
        @NotNull TransactionType type,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 255) String description,
        @Size(max = 255) String location,
        @NotNull TransactionCategory category,
        @Size(max = 80) String subcategory,
        @NotNull LocalDate date
) {}
