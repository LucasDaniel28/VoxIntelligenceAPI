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
- Maven
- Spring Data JPA
- PostgreSQL 16
- Flyway (migrações de banco)
- Docker Compose
- JUnit 5

## Requisitos

- JDK 21
- Maven 3.9 ou superior
- Chave de acesso do provedor configurado para processamento de texto e transcrição de áudio

## Configuração

A chave de acesso não deve ser inserida diretamente nos arquivos do projeto.

Configure a variável de ambiente `OPENAI_API_KEY`. O arquivo `.env.example` apresenta o nome da variável esperada, sem conter credenciais. Se preferir usar um arquivo `.env`, utilize uma ferramenta de carregamento de variáveis compatível; o Spring Boot não carrega esse arquivo automaticamente por padrão.

No Windows PowerShell:

```powershell
$env:OPENAI_API_KEY="sua-chave"
```

No Linux ou macOS:

```bash
export OPENAI_API_KEY="sua-chave"
```

O modelo de texto e o serviço de transcrição estão configurados em `src/main/resources/application.yml`. Os nomes dos modelos podem ser alterados nessa configuração.

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

Os testes automatizados utilizam mocks para não realizar chamadas pagas ao provedor externo. O PostgreSQL é necessário para executar a aplicação completa.

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

A aplicação ficará disponível em `http://localhost:8080`.

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

A resposta é um arquivo MP3 (`audio/mpeg`). A síntese utiliza o modelo e a voz configurados em `application.yml` e gera consumo adicional no provedor.

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

A aplicação não grava automaticamente uma movimentação nesta etapa. A confirmação e a persistência serão implementadas nas próximas entregas.

## Roadmap

- [x] Estrutura inicial com Spring Boot.
- [x] Endpoint para receber descrições em texto.
- [x] Integração com o serviço de interpretação de linguagem.
- [x] Extração estruturada dos dados financeiros.
- [x] Integração com transcrição de áudio.
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

Projeto em desenvolvimento. A API interpreta texto e áudio, disponibiliza gerenciamento REST de movimentações, persiste dados em PostgreSQL e oferece ferramentas conversacionais para lançamentos, saldo e orçamento. **Limitação importante:** ainda não há autenticação, autorização ou isolamento de dados por usuário. Não exponha a API publicamente nem a utilize com dados financeiros de múltiplas pessoas até implementar essa camada. O projeto também requer configuração de chave e banco antes de executar todos os fluxos.
