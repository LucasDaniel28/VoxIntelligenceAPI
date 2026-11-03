# VoxIntelligenceAPI

API REST para registrar movimentações financeiras a partir de descrições em texto ou gravações de áudio. O objetivo é reduzir o preenchimento manual: o usuário informa uma despesa ou receita em linguagem natural e a aplicação prepara os dados para conferência.

## Funcionalidades disponíveis

- Recebimento de descrições financeiras por JSON.
- Interpretação de tipo, valor, moeda, estabelecimento, categoria, subcategoria e data.
- Transcrição de arquivos de áudio.
- Encaminhamento automático da transcrição para interpretação financeira.
- Identificação de informações obrigatórias ausentes.
- Preparação da movimentação para confirmação antes do registro.

## Tecnologias

- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Validation
- Maven
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

Os testes automatizados cobrem a validação de arquivos de áudio e os endpoints HTTP de interpretação por texto e áudio. Eles utilizam dublês para não realizar chamadas pagas ao provedor externo.

Inicie a aplicação:

```bash
mvn spring-boot:run
```

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
- [ ] Implementar ferramentas para registrar e consultar movimentações.
- [ ] Persistir movimentações em PostgreSQL.
- [ ] Implementar confirmação e edição de movimentações.
- [ ] Criar limites e metas orçamentárias.
- [ ] Implementar consultas e relatórios de orçamento.
- [ ] Adicionar respostas em áudio.

## Status

Projeto em desenvolvimento. Nesta etapa, a API interpreta descrições textuais e gravações de áudio, retornando dados estruturados para conferência. A persistência financeira ainda não está habilitada.
