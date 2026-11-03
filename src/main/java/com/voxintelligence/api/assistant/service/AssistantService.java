package com.voxintelligence.api.assistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AssistantService {

    private static final String SYSTEM_INSTRUCTIONS = """
            Você é um assistente de controle financeiro pessoal.
            Converse sempre em português brasileiro, com respostas claras e objetivas.
            Você pode usar ferramentas para consultar movimentações, calcular o saldo,
            registrar receitas ou despesas e consultar ou definir limites mensais por categoria.
            
            Regras obrigatórias:
            - Nunca invente movimentações, valores ou resultados de consultas.
            - Para registrar, exija um pedido explícito do usuário para salvar/registrar.
            - Antes de registrar, confirme que tipo, valor e descrição estão claros.
            - Se faltar informação essencial, pergunte ao usuário em vez de chamar a ferramenta.
            - Não registre apenas porque o usuário descreveu um gasto; interpretar não significa confirmar.
            - Use a ferramenta de consulta quando perguntarem sobre lançamentos ou saldo.
            - Os registros são persistidos no banco de dados; nunca diga que são temporários.
            - Valores monetários devem ser apresentados em reais (BRL).
            """;

    private final ChatClient chatClient;

    public AssistantService(
            ChatClient.Builder chatClientBuilder,
            TransactionToolService transactionToolService,
            com.voxintelligence.api.budget.service.BudgetToolService budgetToolService
    ) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_INSTRUCTIONS)
                .defaultTools(transactionToolService, budgetToolService)
                .build();
    }

    public String chat(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("A mensagem não pode estar vazia.");
        }

        String response = chatClient.prompt()
                .user(message.trim())
                .call()
                .content();

        return response == null || response.isBlank()
                ? "Não consegui gerar uma resposta. Tente novamente."
                : response.trim();
    }
}
