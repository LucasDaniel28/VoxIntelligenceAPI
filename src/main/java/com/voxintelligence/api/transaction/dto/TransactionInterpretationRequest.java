package com.voxintelligence.api.transaction.dto;

import jakarta.validation.constraints.NotBlank;

public record TransactionInterpretationRequest(
        @NotBlank(message = "O texto da movimentação é obrigatório.")
        String text
) {
}
