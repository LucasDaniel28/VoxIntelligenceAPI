package com.voxintelligence.api.transaction.service;

import com.voxintelligence.api.transaction.dto.TransactionExtraction;
import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionInterpretationService {

    private static final String SYSTEM_INSTRUCTIONS = """
            Você interpreta descrições de movimentações financeiras pessoais.
            Extraia somente informações explicitamente presentes ou claramente inferíveis
            pelo contexto da frase. Não invente valores, locais ou datas específicas.

            Regras:
            - Identifique o tipo como EXPENSE ou INCOME.
            - O valor deve ser numérico e positivo. Se não estiver informado, use null.
            - A moeda padrão é BRL, salvo indicação explícita de outra moeda.
            - A data deve estar no formato ISO yyyy-MM-dd.
            - Para expressões como hoje/agora, use a data atual informada na mensagem.
            - Para ontem, use um dia antes da data atual.
            - Para amanhã, use um dia depois da data atual.
            - Categorias permitidas: FOOD, TRANSPORT, HOUSING, HEALTH, EDUCATION,
              ENTERTAINMENT, SHOPPING, SERVICES, SALARY, INVESTMENTS, OTHER.
            - Se não for possível classificar com segurança, use OTHER.
            - location deve conter o estabelecimento ou local citado, sem preposições.
            - description deve ser curta e objetiva.
            - Não inclua explicações, markdown ou texto fora do objeto estruturado.
            """;

    private final ChatClient chatClient;
    private final Clock clock;

    public TransactionInterpretationService(ChatClient.Builder chatClientBuilder, Clock clock) {
        this.chatClient = chatClientBuilder.build();
        this.clock = clock;
    }

    public TransactionInterpretationResponse interpret(String input) {
        if (input == null || input.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A descrição da movimentação não pode estar vazia.");
        }

        LocalDate today = LocalDate.now(clock);

        TransactionExtraction extraction = chatClient.prompt()
                .system(SYSTEM_INSTRUCTIONS)
                .user(userPrompt(input.trim(), today))
                .call()
                .entity(TransactionExtraction.class);

        if (extraction == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Não foi possível interpretar a movimentação.");
        }

        return toResponse(input.trim(), extraction, today);
    }

    private String userPrompt(String input, LocalDate today) {
        return """
                Data atual: %s
                Idioma: português brasileiro
                Descrição informada: %s
                """.formatted(today, input);
    }

    private TransactionInterpretationResponse toResponse(
            String originalText,
            TransactionExtraction extraction,
            LocalDate today
    ) {
        BigDecimal amount = extraction.amount();
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = null;
        }

        List<String> missingFields = new ArrayList<>();
        if (extraction.type() == null) {
            missingFields.add("type");
        }
        if (amount == null) {
            missingFields.add("amount");
        }

        String status = missingFields.isEmpty()
                ? "READY_FOR_CONFIRMATION"
                : "NEEDS_INFORMATION";

        String currency = extraction.currency() == null || extraction.currency().isBlank()
                ? "BRL"
                : extraction.currency().trim().toUpperCase();

        String description = extraction.description();
        if (description == null || description.isBlank()) {
            description = extraction.type() == null
                    ? "Movimentação financeira"
                    : extraction.type() == com.voxintelligence.api.transaction.model.TransactionType.INCOME
                    ? "Recebimento"
                    : "Despesa";
        }

        TransactionCategory category = extraction.category() == null
                ? TransactionCategory.OTHER
                : extraction.category();

        return new TransactionInterpretationResponse(
                originalText,
                extraction.type(),
                amount,
                currency,
                description,
                extraction.location(),
                category,
                extraction.subcategory(),
                extraction.date() == null ? today : extraction.date(),
                status,
                List.copyOf(missingFields)
        );
    }
}
