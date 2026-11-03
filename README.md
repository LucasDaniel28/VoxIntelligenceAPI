# VoxIntelligenceAPI

API REST para registrar movimentações financeiras a partir de descrições em texto ou gravações de áudio. O objetivo é reduzir o preenchimento manual: o usuário informa uma despesa ou receita em linguagem natural e a aplicação prepara os dados para conferência.

## Funcionalidades disponíveis

- Recebimento de descrições financeiras por JSON.
- Interpretação de tipo, valor, moeda, estabelecimento, categoria, subcategoria e data.
- Transcrição de arquivos de áudio.
- Encaminhamento automático da transcrição para interpretação financeira.
- Identificação de informações obrigatórias ausentes.
- Preparação da movimentação para confirmação antes do registro.
- Assistente conversacional com chamada de ferramentas para registrar e consultar movimentações e calcular o saldo.

## Tecnologias

- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Validation
- Serviços externos de processamento de texto, transcrição e síntese de áudio
- Maven
- Spring Data JPA
- PostgreSQL 16
- Flyway (migrações de banco)
- Docker Compose
- JUnit 5

## Requisitos

- JDK 21
- Maven 3.9 ou superior
- Credencial do serviço externo configurado para processamento de texto e áudio

## Configuração

A chave de acesso não deve ser inserida diretamente nos arquivos do projeto.

Configure as variáveis `OPENAI_API_KEY` e `API_ACCESS_KEY`. A variável `OPENAI_API_KEY` contém a credencial do serviço externo utilizado nos recursos de texto e áudio. A variável `API_ACCESS_KEY` é uma chave privada definida por você para proteger todos os endpoints HTTP da aplicação. Não reutilize a chave do provedor como chave de acesso da API.

O arquivo `.env.example` apresenta os nomes das variáveis, sem conter credenciais. Se preferir usar um arquivo `.env`, utilize uma ferramenta de carregamento de variáveis compatível; o Spring Boot não carrega esse arquivo automaticamente por padrão.

No Windows PowerShell:

```powershell
$env:OPENAI_API_KEY="sua-chave-do-provedor"
$env:API_ACCESS_KEY="crie-uma-chave-longa-e-aleatoria"
```

No Linux ou macOS:

```bash
export OPENAI_API_KEY="sua-chave-do-provedor"
export API_ACCESS_KEY="crie-uma-chave-longa-e-aleatoria"
```

Todas as requisições à API devem enviar o cabeçalho `X-API-KEY` com o valor de `API_ACCESS_KEY`. Sem a variável configurada, a aplicação responde `503 Service Unavailable`; com uma chave ausente ou incorreta, responde `401 Unauthorized`. Os testes unitários validam esses cenários. Não publique a chave no GitHub, em capturas de tela ou no código-fonte.

Os serviços e parâmetros de processamento de texto, transcrição e síntese de áudio estão configurados em `src/main/resources/application.yml`.

## Executar localmente

Clone o repositório:

```bash
git clone https://github.com/LucasDaniel28/VoxIntelligenceAPI.git
cd VoxIntelligenceAPI
```

Execute os testes:

```bash
mvn clean verify
```

Os testes unitários utilizam mocks para não realizar chamadas externas. A suíte também executa testes de integração com PostgreSQL 16 em contêiner, validando as três migrações Flyway, a compatibilidade do esquema com as entidades JPA e operações reais de persistência. O Docker deve estar disponível para executar esses testes.

Inicie o PostgreSQL com Docker Compose (em outro terminal):

```bash
docker compose up -d postgres
```

Aguarde o container ficar saudável. O Flyway cria as tabelas automaticamente na primeira inicialização. Depois inicie a API:

```bash
mvn spring-boot:run
```

Para encerrar o banco:

```bash
docker compose down
```

Os dados permanecem no volume `vox_postgres_data`. Para apagar também os dados locais, use `docker compose down -v`.

A aplicação ficará disponível em `http://localhost:8080`. Inclua o cabeçalho `X-API-KEY` nas chamadas protegidas. Exemplo:

```bash
curl http://localhost:8080/api/v1/transactions \\
  -H "X-API-KEY: sua-chave-de-acesso"
```

## Auditoria

As operações de criação, atualização e exclusão de movimentações financeiras e orçamentos mensais geram eventos na tabela `audit_events`. Cada evento registra o tipo e identificador da entidade, a ação, a data/hora e os dados anteriores e/ou posteriores em JSON.

Consulte o histórico de uma movimentação:

`GET /api/v1/audit/transactions/{transactionId}`

Consulte o histórico de um orçamento:

`GET /api/v1/audit/budgets/{budgetId}`

As respostas são ordenadas do evento mais recente para o mais antigo. A auditoria é gravada na mesma transação de banco da operação financeira ou orçamentária: se a operação principal falhar, o evento também não será persistido.

A migração `V3__create_audit_events.sql` cria a estrutura e os índices necessários.

## Endpoints

### 1. Interpretar uma movimentação por texto

`POST /api/v1/transactions/interpret`

Requisição:

```json
{
  "text": "Gastei 50 reais no Starbucks agora"
}
```

Exemplo de resposta:

```json
{
  "originalText": "Gastei 50 reais no Starbucks agora",
  "type": "EXPENSE",
  "amount": 50.00,
  "currency": "BRL",
  "description": "Compra no Starbucks",
  "location": "Starbucks",
  "category": "FOOD",
  "subcategory": "COFFEE",
  "date": "2026-11-03",
  "status": "READY_FOR_CONFIRMATION",
  "missingFields": []
}
```

### 2. Interpretar uma movimentação por áudio

`POST /api/v1/transactions/interpret-audio`

Envie uma requisição `multipart/form-data` com o campo `file`.

Formatos aceitos:

- MP3
- MP4
- MPEG
- MPGA
- M4A
- WAV
- WEBM
- FLAC

Tamanho máximo: 25 MB.

Exemplo com cURL:

```bash
curl -X POST http://localhost:8080/api/v1/transactions/interpret-audio \\
  -H "X-API-KEY: sua-chave-de-acesso" \\
  -F "file=@./gasto.wav"
```

O áudio é transcrito e o texto resultante passa pelo mesmo fluxo de interpretação utilizado pelo endpoint de texto.

### 3. Gerenciar movimentações financeiras

A API oferece operações REST para criar, consultar, editar e excluir movimentações:

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/transactions` | Cria uma movimentação confirmada |
| `POST` | `/api/v1/transactions/confirm` | Confirma e persiste uma movimentação revisada |
| `GET` | `/api/v1/transactions` | Lista movimentações |
| `GET` | `/api/v1/transactions/{id}` | Consulta uma movimentação |
| `PUT` | `/api/v1/transactions/{id}` | Atualiza uma movimentação |
| `DELETE` | `/api/v1/transactions/{id}` | Exclui uma movimentação |

Exemplo de criação:

```json
{
  "type": "EXPENSE",
  "amount": 42.50,
  "description": "Almoço",
  "location": "Restaurante",
  "category": "FOOD",
  "subcategory": "RESTAURANT",
  "date": "2026-11-03"
}
```

### 4. Gerenciar limites e orçamentos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/budgets` | Cria limite mensal por categoria |
| `GET` | `/api/v1/budgets?year=2026&month=11` | Consulta limites e consumo do mês |
| `PUT` | `/api/v1/budgets/{id}` | Atualiza o valor do limite |
| `DELETE` | `/api/v1/budgets/{id}` | Exclui o limite |

Exemplo de criação:

```json
{
  "category": "FOOD",
  "year": 2026,
  "month": 11,
  "limitAmount": 800.00
}
```

O resumo informa o total gasto naquela categoria no mês, o valor restante, o percentual utilizado e se o limite foi ultrapassado.

### 5. Relatório mensal

`GET /api/v1/reports/monthly?year=2026&month=11`

Retorna receitas, despesas, saldo líquido e a distribuição das despesas por categoria para o mês consultado.

### 6. Conversar com o assistente financeiro

`POST /api/v1/assistant/message`

Requisição:

```json
{
  "message": "Registre uma despesa de 42,50 reais com almoço hoje, na categoria FOOD."
}
```

O assistente pode acionar ferramentas para:

- Registrar uma receita ou despesa quando houver pedido explícito e os dados estiverem claros.
- Listar movimentações, opcionalmente filtrando por receita ou despesa.
- Calcular o total de receitas, despesas e o saldo líquido.
- Criar limites mensais por categoria mediante solicitação explícita.
- Consultar o consumo e o percentual utilizado dos limites.

Exemplo de consulta: `Qual é meu saldo atual?`.

As movimentações e os limites são persistidos no PostgreSQL. As alterações de esquema são controladas pelo Flyway.

### 7. Gerar resposta em áudio

`POST /api/v1/assistant/speech`

Requisição:

```json
{
  "text": "Seu saldo deste mês é de 2.500 reais."
}
```

A resposta é um arquivo MP3 (`audio/mpeg`). A síntese utiliza o serviço e a voz configurados em `application.yml`.

## Categorias iniciais

- `FOOD`
- `TRANSPORT`
- `HOUSING`
- `HEALTH`
- `EDUCATION`
- `ENTERTAINMENT`
- `SHOPPING`
- `SERVICES`
- `SALARY`
- `INVESTMENTS`
- `OTHER`

## Situação da interpretação

- `READY_FOR_CONFIRMATION`: tipo e valor foram identificados; a movimentação está preparada para conferência.
- `NEEDS_INFORMATION`: faltam dados obrigatórios, informados em `missingFields`.

A interpretação não grava automaticamente. Após revisar os dados extraídos, envie-os para `POST /api/v1/transactions/confirm` para persistir a movimentação.

## Roadmap

- [x] Estrutura inicial com Spring Boot.
- [x] Endpoint para receber descrições em texto.
- [x] Integração com serviço externo de interpretação de linguagem.
- [x] Extração estruturada dos dados financeiros.
- [x] Integração com serviço externo de transcrição de áudio.
- [x] Endpoint para receber gravações e encaminhar o texto transcrito.
- [x] Testes automatizados para validação de áudio e endpoints REST.
- [x] Implementar ferramentas para registrar e consultar movimentações e calcular o saldo.
- [x] Persistir movimentações em PostgreSQL com Flyway.
- [x] Disponibilizar operações REST de criação, consulta, edição e exclusão.
- [x] Criar limites mensais por categoria.
- [x] Calcular consumo, saldo restante e percentual do orçamento.
- [x] Gerar relatórios mensais por categoria.
- [x] Integrar ferramentas de orçamento ao assistente.
- [x] Adicionar respostas em áudio (texto para fala).

## Status

Projeto em desenvolvimento e planejado para uso individual. Não há cadastro nem gerenciamento de usuários. O acesso aos endpoints é protegido por uma chave única (`API_ACCESS_KEY`), enviada no cabeçalho `X-API-KEY`. A chave protege o acesso à API, mas não substitui HTTPS, proteção de rede ou boas práticas de operação. O projeto também requer configuração das credenciais e do banco antes de executar todos os fluxos.
