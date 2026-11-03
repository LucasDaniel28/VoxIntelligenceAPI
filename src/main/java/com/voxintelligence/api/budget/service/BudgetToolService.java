package com.voxintelligence.api.budget.service;

import com.voxintelligence.api.budget.dto.BudgetRequest;
import com.voxintelligence.api.transaction.model.TransactionCategory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class BudgetToolService {
    private final BudgetService budgetService;

    public BudgetToolService(BudgetService budgetService) {
        this.budgetService=budgetService;
    }

    @Tool(description = "Cria um limite mensal de gastos para uma categoria. Só execute quando o usuário pedir explicitamente para definir ou criar o limite.")
    public String createMonthlyBudget(
            @ToolParam(description = "Categoria: FOOD, TRANSPORT, HOUSING, HEALTH, EDUCATION, ENTERTAINMENT, SHOPPING, SERVICES, SALARY, INVESTMENTS ou OTHER.") String category,
            @ToolParam(description = "Ano com quatro dígitos.") int year,
            @ToolParam(description = "Mês de 1 a 12.") int month,
            @ToolParam(description = "Limite positivo em reais.") BigDecimal limitAmount) {
        try {
            TransactionCategory parsed=TransactionCategory.valueOf(category.trim().toUpperCase(Locale.ROOT));
            var result=budgetService.create(new BudgetRequest(parsed, year, month, limitAmount));
            return "Limite criado: %s/%02d/%d | teto R$ %s."
                    .formatted(result.category(), result.month(), result.year(), result.limitAmount());
        } catch (Exception exception) {
            return "Não foi possível criar o limite: " + exception.getMessage();
        }
    }

    @Tool(description = "Consulta os limites mensais e quanto já foi gasto em cada categoria no período informado.")
    public String getMonthlyBudgetStatus(
            @ToolParam(description = "Ano com quatro dígitos.") int year,
            @ToolParam(description = "Mês de 1 a 12.") int month) {
        var results=budgetService.list(year, month);
        if (results.isEmpty()) return "Não existem limites cadastrados para esse mês.";
        return results.stream().map(b -> "%s: gasto R$ %s de R$ %s (%.2f%%); restante R$ %s; ultrapassado: %s"
                .formatted(b.category(), b.spentAmount(), b.limitAmount(), b.usagePercentage(),
                        b.remainingAmount(), b.exceeded() ? "sim" : "não"))
                .reduce("Orçamento mensal:\n", (a,b) -> a+b+"\n");
    }
}
