package com.voxintelligence.api.assistant.service;

import com.voxintelligence.api.transaction.model.TransactionCategory;
import com.voxintelligence.api.transaction.model.TransactionType;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TransactionToolService {

    private final List<StoredTransaction> transactions = new CopyOnWriteArrayList<>();
    private final Clock clock;

    public TransactionToolService(Clock clock) {
        this.clock = clock;
    }

    @Tool(description = "Registra uma despesa ou receita confirmada pelo usuário. Use somente quando o usuário pedir explicitamente para registrar/salvar e todos os dados necessários estiverem claros.")
    public String registerTransaction(
            @ToolParam(description = "Tipo da movimentação: EXPENSE para despesa ou INCOME para receita.") String type,
            @ToolParam(description = "Valor numérico positivo, em reais.") BigDecimal amount,
            @ToolParam(description = "Descrição curta da movimentação.") String description,
            @ToolParam(description = "Categoria: FOOD, TRANSPORT, HOUSING, HEALTH, EDUCATION, ENTERTAINMENT, SHOPPING, SERVICES, SALARY, INVESTMENTS ou OTHER.") String category,
            @ToolParam(description = "Data no formato yyyy-MM-dd. Use a data atual quando o usuário não especificar outra.") String date
    ) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Não foi possível registrar: o valor deve ser maior que zero.";
        }

        TransactionType parsedType;
        try {
            parsedType = TransactionType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return "Não foi possível registrar: tipo deve ser EXPENSE ou INCOME.";
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
            return "Não foi possível registrar: data inválida. Use yyyy-MM-dd.";
        }

        if (description == null || description.isBlank()) {
            return "Não foi possível registrar: informe uma descrição.";
        }

        StoredTransaction transaction = new StoredTransaction(
                UUID.randomUUID(), parsedType, amount, description.trim(),
                parsedCategory, parsedDate
        );
        transactions.add(transaction);

        return "Movimentação registrada com sucesso. ID: %s | Tipo: %s | Valor: R$ %s | Descrição: %s | Categoria: %s | Data: %s"
                .formatted(transaction.id(), transaction.type(), transaction.amount(),
                        transaction.description(), transaction.category(), transaction.date());
    }

    @Tool(description = "Consulta as movimentações financeiras registradas. Pode filtrar por tipo EXPENSE ou INCOME. Deixe o filtro vazio para listar todas.")
    public String listTransactions(
            @ToolParam(description = "Filtro opcional: EXPENSE, INCOME ou vazio para todas.", required = false) String type
    ) {
        List<StoredTransaction> result = transactions.stream()
                .filter(transaction -> type == null || type.isBlank()
                        || transaction.type().name().equalsIgnoreCase(type.trim()))
                .sorted(Comparator.comparing(StoredTransaction::date).reversed())
                .toList();

        if (result.isEmpty()) {
            return "Nenhuma movimentação encontrada.";
        }

        return result.stream()
                .map(transaction -> "ID: %s | %s | R$ %s | %s | %s | %s".formatted(
                        transaction.id(), transaction.type(), transaction.amount(),
                        transaction.description(), transaction.category(), transaction.date()))
                .reduce("Movimentações encontradas:\n", (left, right) -> left + right + "\n");
    }

    @Tool(description = "Calcula o saldo das movimentações registradas, somando receitas e subtraindo despesas.")
    public String getBalance() {
        BigDecimal income = transactions.stream()
                .filter(transaction -> transaction.type() == TransactionType.INCOME)
                .map(StoredTransaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = transactions.stream()
                .filter(transaction -> transaction.type() == TransactionType.EXPENSE)
                .map(StoredTransaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = income.subtract(expenses);
        return "Resumo financeiro: receitas = R$ %s; despesas = R$ %s; saldo = R$ %s."
                .formatted(income, expenses, balance);
    }

    private record StoredTransaction(
            UUID id,
            TransactionType type,
            BigDecimal amount,
            String description,
            TransactionCategory category,
            LocalDate date
    ) {
    }
}
