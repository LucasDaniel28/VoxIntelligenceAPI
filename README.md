# VoxIntelligenceAPI

API REST para registrar e organizar movimentações financeiras descritas em linguagem natural. A proposta é reduzir o preenchimento manual: o usuário informa uma movimentação em uma frase e a aplicação identifica os principais dados para preparar o registro.

## Objetivo

Receber descrições como:

> Gastei 50 reais no Starbucks agora.

E retornar uma estrutura com tipo da movimentação, valor, moeda, estabelecimento, categoria, subcategoria, data e situação da interpretação.

## Tecnologias

- Java 21
- Spring Boot 3.5
- Spring Web
- Bean Validation
- Maven
- JUnit 5

## Requisitos

- JDK 21
- Maven 3.9 ou superior

## Executar localmente

Clone o repositório:

```bash
git clone https://github.com/LucasDaniel28/VoxIntelligenceAPI.git
cd VoxIntelligenceAPI
```

Execute os testes:

```bash
mvn test
```

Inicie a aplicação:

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em `http://localhost:8080`.

## Endpoint disponível

### Interpretar movimentação

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
  "description": "Compra em Starbucks",
  "location": "Starbucks",
  "category": "FOOD",
  "subcategory": "COFFEE",
  "date": "2026-10-30",
  "status": "READY_FOR_CONFIRMATION",
  "missingFields": []
}
```

Quando informações obrigatórias não forem identificadas, a resposta utiliza o status `NEEDS_INFORMATION` e informa os campos ausentes em `missingFields`.

Exemplo com cURL:

```bash
curl -X POST http://localhost:8080/api/v1/transactions/interpret \
  -H "Content-Type: application/json" \
  -d '{"text":"Gastei 50 reais no Starbucks agora"}'
```

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

## Roadmap

- [x] Estrutura inicial com Spring Boot.
- [x] Endpoint para receber descrições em texto.
- [x] Extração inicial de valor, local e data.
- [x] Classificação inicial de tipo e categoria.
- [x] Retorno de dados estruturados.
- [ ] Aprimorar interpretação de linguagem natural.
- [ ] Persistir movimentações em banco de dados.
- [ ] Implementar confirmação e edição de movimentações.
- [ ] Receber arquivos de áudio e realizar transcrição.
- [ ] Adicionar interação por voz.
- [ ] Criar consultas e relatórios de orçamento.

## Status

Projeto em desenvolvimento. A versão atual realiza uma interpretação inicial baseada em regras e ainda não persiste movimentações financeiras.
