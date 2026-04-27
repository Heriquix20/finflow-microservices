# FinFlow

Backend de gestÃ£o financeira construÃ­do com arquitetura de microsserviÃ§os para estudo avanÃ§ado de Java, Spring e sistemas distribuÃ­dos.

O projeto foi pensado como uma base tÃ©cnica de portfÃ³lio para demonstrar:

- autenticaÃ§Ã£o com JWT
- separaÃ§Ã£o de responsabilidades por serviÃ§o
- comunicaÃ§Ã£o sÃ­ncrona e assÃ­ncrona
- persistÃªncia orientada a documentos
- testes automatizados com cobertura validada no build
- execuÃ§Ã£o local e empacotamento com Docker

## VisÃ£o geral

O FinFlow representa o nÃºcleo backend de uma aplicaÃ§Ã£o financeira capaz de:

- cadastrar usuÃ¡rios com e-mail e senha
- autenticar sessÃµes via JWT
- registrar receitas
- registrar despesas
- consolidar saldo e indicadores financeiros
- gerar relatÃ³rios por perÃ­odo e por categoria

O objetivo do projeto Ã© demonstrar como modelar um domÃ­nio financeiro simples em uma arquitetura distribuÃ­da, com foco em clareza estrutural, seguranÃ§a bÃ¡sica, testabilidade e evoluÃ§Ã£o incremental.

## Arquitetura

```mermaid
flowchart LR
    A["Cliente API"] --> B["Gateway (8080)"]
    B --> C["Auth Service (8084)"]
    B --> D["Income Service (8081)"]
    B --> E["Expense Service (8082)"]
    B --> F["Reports Service (8083)"]
    C --> G["MongoDB"]
    D --> G
    E --> G
    F --> G
    D --> H["Kafka"]
    E --> H
    H --> F
    F --> D
    F --> E
    B --> I["Eureka (8761)"]
    C --> I
    D --> I
    E --> I
    F --> I
```

## MÃ³dulos

| MÃ³dulo | Porta | Responsabilidade |
| --- | --- | --- |
| `finflow-discovery` | `8761` | Registro e descoberta de serviÃ§os com Eureka |
| `finflow-auth` | `8084` | Cadastro, login por e-mail, emissÃ£o de JWT e recuperaÃ§Ã£o de perfil |
| `finflow-gateway` | `8080` | Entrada Ãºnica, validaÃ§Ã£o do token e roteamento para os serviÃ§os internos |
| `finflow-income` | `8081` | CRUD de receitas e publicaÃ§Ã£o de eventos |
| `finflow-expense` | `8082` | CRUD de despesas e publicaÃ§Ã£o de eventos |
| `finflow-reports` | `8083` | ConsolidaÃ§Ã£o financeira, histÃ³rico, saldo e agrupamentos |

## Stack tÃ©cnica

- Java 21
- Spring Boot 3.3.13
- Spring Cloud 2023.0.6
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud OpenFeign
- Spring Data MongoDB
- Spring for Apache Kafka
- Spring Validation
- Spring Security Crypto
- SpringDoc OpenAPI
- Maven multi-mÃ³dulo
- Docker Compose
- MongoDB
- Zookeeper
- Kafka
- JUnit 5
- Mockito
- MockMvc
- JaCoCo
- GitHub Actions

## Principais funcionalidades

### AutenticaÃ§Ã£o

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- cadastro de usuÃ¡rio com e-mail e senha
- autenticaÃ§Ã£o stateless com JWT
- propagaÃ§Ã£o do contexto autenticado para os serviÃ§os de domÃ­nio

### Receitas

- criaÃ§Ã£o, listagem, atualizaÃ§Ã£o e remoÃ§Ã£o de receitas
- consulta de resumo por mÃªs e ano
- publicaÃ§Ã£o de eventos:
  - `income.created`
  - `income.updated`
  - `income.deleted`

### Despesas

- criaÃ§Ã£o, listagem, atualizaÃ§Ã£o e remoÃ§Ã£o de despesas
- consulta de resumo por mÃªs e ano
- publicaÃ§Ã£o de eventos:
  - `expense.created`
  - `expense.updated`
  - `expense.deleted`

### RelatÃ³rios

- saldo consolidado
- resumo mensal
- despesas agrupadas por categoria
- histÃ³rico consolidado por perÃ­odo
- consolidaÃ§Ã£o acionada por eventos Kafka
- leitura complementar via OpenFeign

## Fluxo de autenticaÃ§Ã£o e sessÃ£o

1. o cliente registra uma conta com `POST /api/auth/register`
2. o serviÃ§o de autenticaÃ§Ã£o retorna um `accessToken`
3. o cliente envia `Authorization: Bearer <token>` nas rotas protegidas
4. o gateway valida o token e propaga `X-User-Id` para os microsserviÃ§os internos
5. o cliente pode consultar o perfil autenticado com `GET /api/auth/me`

A sessÃ£o Ã© stateless:

- o JWT representa a identidade autenticada
- o gateway nÃ£o mantÃ©m sessÃ£o em memÃ³ria
- os serviÃ§os internos recebem apenas o contexto necessÃ¡rio para autorizaÃ§Ã£o e segregaÃ§Ã£o de dados por usuÃ¡rio

## Estrutura do repositÃ³rio

```text
finflow/
|-- .github/
|-- docs/
|-- finflow-discovery/
|-- finflow-auth/
|-- finflow-gateway/
|-- finflow-income/
|-- finflow-expense/
|-- finflow-reports/
|-- build-finflow.ps1
|-- docker-compose.yml
|-- docker-compose.app.yml
|-- README.md
|-- start-finflow.ps1
|-- start-finflow-all.ps1
|-- stop-finflow.ps1
`-- stop-finflow-all.ps1
```

## Como executar

### PrÃ©-requisitos

- Java 21
- Maven
- Docker Desktop
- PowerShell

### Subir a stack backend

```powershell
.\start-finflow-all.ps1
```

Esse script:

- sobe MongoDB, Zookeeper e Kafka
- inicia `finflow-discovery`
- inicia `finflow-auth`
- inicia `finflow-gateway`
- inicia `finflow-income`
- inicia `finflow-expense`
- inicia `finflow-reports`

### Parar a stack backend

```powershell
.\stop-finflow-all.ps1
```

### Build completo

```powershell
.\build-finflow.ps1
```

Para forÃ§ar limpeza antes:

```powershell
.\build-finflow.ps1 -Clean
```

### Comandos manuais Ãºteis

```powershell
docker compose up -d
mvn verify
```

### Stack containerizada

```powershell
docker compose -f docker-compose.app.yml up --build
```

Guia complementar:

- [docs/deployment/README.md](C:/Users/hcgv1/OneDrive/Ãrea%20de%20Trabalho/Projetos%20-%20Henrique/finFLow/docs/deployment/README.md)

## URLs locais

| Recurso | URL |
| --- | --- |
| Eureka | [http://localhost:8761](http://localhost:8761) |
| Gateway | [http://localhost:8080](http://localhost:8080) |
| Swagger Auth | [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) |
| Swagger Income | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |
| Swagger Expense | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| Swagger Reports | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) |

## Endpoints principais

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Exemplo de cadastro:

```json
{
  "displayName": "Maria Silva",
  "email": "maria@example.com",
  "password": "senha1234"
}
```

Exemplo de login:

```json
{
  "email": "maria@example.com",
  "password": "senha1234"
}
```

### Receitas

- `POST /api/incomes`
- `GET /api/incomes`
- `GET /api/incomes/{id}`
- `PUT /api/incomes/{id}`
- `DELETE /api/incomes/{id}`
- `GET /api/incomes/summary?month=&year=`

### Despesas

- `POST /api/expenses`
- `GET /api/expenses`
- `GET /api/expenses/{id}`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`
- `GET /api/expenses/summary?month=&year=`

### RelatÃ³rios

- `GET /api/reports/monthly-summary?month=&year=`
- `GET /api/reports/balance`
- `GET /api/reports/by-category?month=&year=`
- `GET /api/reports/history`

## Qualidade e testes

O projeto possui:

- testes unitÃ¡rios de service
- testes de controller
- testes do `finflow-auth`
- testes do filtro JWT no gateway
- testes de producers e consumer
- cobertura validada com JaCoCo no `mvn verify`
- cenÃ¡rios BDD para fluxos crÃ­ticos

Cobertura bruta atual:

- `finflow-auth`: `90.00%`
- `finflow-gateway`: `96.61%`
- `finflow-income`: `84.92%`
- `finflow-expense`: `84.42%`
- `finflow-reports`: `92.28%`

DocumentaÃ§Ã£o complementar:

- guia de testes: [docs/testing/README.md](C:/Users/hcgv1/OneDrive/Ãrea%20de%20Trabalho/Projetos%20-%20Henrique/finFLow/docs/testing/README.md)
- baseline de cobertura: [docs/testing/coverage-status.md](C:/Users/hcgv1/OneDrive/Ãrea%20de%20Trabalho/Projetos%20-%20Henrique/finFLow/docs/testing/coverage-status.md)
- cenÃ¡rios BDD: [docs/testing/bdd/README.md](C:/Users/hcgv1/OneDrive/Ãrea%20de%20Trabalho/Projetos%20-%20Henrique/finFLow/docs/testing/bdd/README.md)

## CI e deploy

O projeto inclui:

- workflow de CI em [`.github/workflows/ci.yml`](C:/Users/hcgv1/OneDrive/Ãrea%20de%20Trabalho/Projetos%20-%20Henrique/finFLow/.github/workflows/ci.yml)
- Dockerfiles por serviÃ§o backend
- compose dedicado para a stack completa em contÃªineres
