package com.voxintelligence.api.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantMessageRequest(
        @NotBlank(message = "A mensagem não pode estar vazia.")
        @Size(max = 4000, message = "A mensagem deve ter no máximo 4000 caracteres.")
        String message
) {
}
