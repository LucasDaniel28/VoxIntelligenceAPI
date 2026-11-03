package com.voxintelligence.api.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpeechRequest(
        @NotBlank(message = "O texto não pode estar vazio.")
        @Size(max = 4000, message = "O texto deve ter no máximo 4000 caracteres.")
        String text
) {}
