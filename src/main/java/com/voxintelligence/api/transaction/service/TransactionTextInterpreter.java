package com.voxintelligence.api.transaction.service;

import com.voxintelligence.api.transaction.dto.TransactionInterpretationResponse;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TransactionTextInterpreter {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:R\\$\\s*)?(\\d{1,3}(?:\\.\\d{3})*(?:,\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?)\\s*(?:reais?|R\\$)?"
    );

    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "(?i)\\b(?:no|na|em|do|da)\\s+([\\p{L}0-9][\\p{L}0-9 .&'-]*?)(?=\\s+(?:agora|hoje|ontem|amanhã|a prazo|no crédito|no débito)\\b|[,.;]|$)"
    );

    private final Clock clock;

    public TransactionTextInterpreter() {
        this(Clock.systemDefaultZone());
    }

    TransactionTextInterpreter(Clock clock) {
        this.clock = clock;
    }

    public TransactionInterpretationResponse interpret(String input) {
        String text = input == null ? "" : input.trim();
        String normalized = text.toLowerCase(Locale.ROOT);

        TransactionType type = detectType(normalized);
        BigDecimal amount = extractAmount(text);
        String location = extractLocation(text);
        LocalDate date = extractDate(normalized);

        TransactionCategory category = detectCategory(normalized, type);
        String subcategory = detectSubcategory(normalized);
        String description = buildDescription(type, location, category);

        List<String> missingFields = new ArrayList<>();
        if (type == null) {
            missingFields.add("type");
        }
        if (amount == null) {
            missingFields.add("amount");
        }

        String status = missingFields.isEmpty()
                ? "READY_FOR_CONFIRMATION"
                : "NEEDS_INFORMATION";

        return new TransactionInterpretationResponse(
                text,
                type,
                amount,
                "BRL",
                description,
                location,
                category,
                subcategory,
                date,
                status,
                List.copyOf(missingFields)
        );
    }

    private TransactionType detectType(String text) {
        if (containsAny(text, "gastei", "comprei", "paguei", "paguei", "despesa", "usei", "assinei")) {
            return TransactionType.EXPENSE;
        }
        if (containsAny(text, "recebi", "ganhei", "salário", "salario", "rendimento", "vendi")) {
            return TransactionType.INCOME;
        }
        return null;
    }

    private BigDecimal extractAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        String value = matcher.group(1);
        if (value.contains(",") && value.contains(".")) {
            value = value.replace(".", "").replace(",", ".");
        } else if (value.contains(",")) {
            value = value.replace(",", ".");
        }

        try {
            return new BigDecimal(value).setScale(2);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String extractLocation(String text) {
        Matcher matcher = LOCATION_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        String location = matcher.group(1).trim();
        return location.isBlank() ? null : location;
    }

    private LocalDate extractDate(String text) {
        LocalDate today = LocalDate.now(clock);
        if (text.matches("(?s).*\\bontem\\b.*")) {
            return today.minusDays(1);
        }
        if (text.matches("(?s).*\\bamanhã\\b.*")) {
            return today.plusDays(1);
        }
        return today;
    }

    private TransactionCategory detectCategory(String text, TransactionType type) {
        if (type == TransactionType.INCOME && containsAny(text, "salário", "salario")) {
            return TransactionCategory.SALARY;
        }
        if (containsAny(text, "starbucks", "café", "cafe", "restaurante", "almoço", "almoco",
                "jantar", "lanche", "padaria", "mercado", "supermercado", "ifood", "comida")) {
            return TransactionCategory.FOOD;
        }
        if (containsAny(text, "uber", "ônibus", "onibus", "metrô", "metro", "combustível",
                "combustivel", "gasolina", "estacionamento", "99")) {
            return TransactionCategory.TRANSPORT;
        }
        if (containsAny(text, "farmácia", "farmacia", "médico", "medico", "consulta", "hospital")) {
            return TransactionCategory.HEALTH;
        }
        if (containsAny(text, "aluguel", "condomínio", "condominio", "iptu")) {
            return TransactionCategory.HOUSING;
        }
        if (containsAny(text, "cinema", "show", "jogo", "streaming", "netflix")) {
            return TransactionCategory.ENTERTAINMENT;
        }
        if (containsAny(text, "curso", "faculdade", "livro", "escola")) {
            return TransactionCategory.EDUCATION;
        }
        if (containsAny(text, "roupa", "sapato", "eletrônico", "eletronico", "shopping")) {
            return TransactionCategory.SHOPPING;
        }
        return TransactionCategory.OTHER;
    }

    private String detectSubcategory(String text) {
        if (containsAny(text, "starbucks", "café", "cafe")) {
            return "COFFEE";
        }
        if (containsAny(text, "almoço", "almoco", "jantar", "lanche", "restaurante")) {
            return "MEAL";
        }
        if (containsAny(text, "uber", "99")) {
            return "RIDE_HAILING";
        }
        if (containsAny(text, "gasolina", "combustível", "combustivel")) {
            return "FUEL";
        }
        return null;
    }

    private String buildDescription(TransactionType type, String location, TransactionCategory category) {
        if (location != null) {
            return (type == TransactionType.INCOME ? "Recebimento" : "Compra") + " em " + location;
        }
        if (category == TransactionCategory.SALARY) {
            return "Recebimento de salário";
        }
        return type == TransactionType.INCOME ? "Recebimento" : "Despesa";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
