package com.voxintelligence.api.assistant.service;

import com.voxintelligence.api.transaction.model.*;
import com.voxintelligence.api.transaction.repository.FinancialTransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransactionToolService {

    private final FinancialTransactionRepository repository;
    private final Clock clock;

    public TransactionToolService(FinancialTransactionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Tool(description = "Registra uma despesa ou receita somente quando o usuário pedir explicitamente para salvar e os dados estiverem claros.")
    @Transactional
    public String registerTransaction(
            @ToolParam(description = "EXPENSE para despesa ou INCOME para receita.") String type,
            @ToolParam(description = "Valor positivo em reais.") BigDecimal amount,
            @ToolParam(description = "Descrição curta.") String description,
            @ToolParam(description = "Categoria financeira válida.") String category,
            @ToolParam(description = "Data yyyy-MM-dd; use a data atual se omitida.") String date
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2) {
            return "Não foi possível registrar: informe um valor positivo com no máximo duas casas decimais.";
        }
        if (description == null || description.isBlank()) {
            return "Não foi possível registrar: informe uma descrição.";
        }

        TransactionType parsedType;
        try {
            parsedType = TransactionType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return "Tipo inválido. Use EXPENSE ou INCOME.";
        }

        TransactionCategory parsedCategory;
        try {
            parsedCategory = TransactionCategory.valueOf(category == null
                    ? "OTHER" : category.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            parsedCategory = TransactionCategory.OTHER;
        }

        LocalDate parsedDate;
        try {
            parsedDate = date == null || date.isBlank()
                    ? LocalDate.now(clock) : LocalDate.parse(date.trim());
        } catch (Exception exception) {
            return "Data inválida. Use yyyy-MM-dd.";
        }

        FinancialTransaction saved = repository.save(new FinancialTransaction(
                UUID.randomUUID(), parsedType, amount, "BRL", description.trim(),
                null, parsedCategory, null, parsedDate
        ));
        return "Movimentação salva permanentemente. ID: %s | Tipo: %s | Valor: R$ %s | Descrição: %s | Categoria: %s | Data: %s"
                .formatted(saved.getId(), saved.getType(), saved.getAmount(),
                        saved.getDescription(), saved.getCategory(), saved.getDate());
    }

    @Tool(description = "Lista movimentações persistidas. Filtro opcional EXPENSE ou INCOME; vazio lista todas.")
    @Transactional(readOnly = true)
    public String listTransactions(
            @ToolParam(description = "EXPENSE, INCOME ou vazio para todas.", required = false) String type
    ) {
        List<FinancialTransaction> result;
        if (type == null || type.isBlank()) {
            result = repository.findAllByOrderByDateDescCreatedAtDesc();
        } else {
            try {
                result = repository.findByTypeOrderByDateDescCreatedAtDesc(
                        TransactionType.valueOf(type.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return "Filtro inválido. Use EXPENSE ou INCOME.";
            }
        }

        if (result.isEmpty()) return "Nenhuma movimentação encontrada.";
        return result.stream()
                .map(t -> "ID: %s | %s | R$ %s | %s | %s | %s".formatted(
                        t.getId(), t.getType(), t.getAmount(), t.getDescription(),
                        t.getCategory(), t.getDate()))
                .reduce("Movimentações encontradas:\n", (a, b) -> a + b + "\n");
    }

    @Tool(description = "Calcula o total de receitas, despesas e saldo líquido de todas as movimentações persistidas.")
    @Transactional(readOnly = true)
    public String getBalance() {
        BigDecimal income = repository.sumByTypeAndPeriod(
                TransactionType.INCOME, LocalDate.of(1900, 1, 1), LocalDate.of(9999, 12, 31));
        BigDecimal expenses = repository.sumByTypeAndPeriod(
                TransactionType.EXPENSE, LocalDate.of(1900, 1, 1), LocalDate.of(9999, 12, 31));
        return "Resumo financeiro: receitas = R$ %s; despesas = R$ %s; saldo = R$ %s."
                .formatted(income, expenses, income.subtract(expenses));
    }
}
