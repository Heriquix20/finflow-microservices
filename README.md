# FinFlow

Backend de gestão financeira construído com arquitetura de microsserviços para estudo avançado de Java, Spring Boot e sistemas distribuídos.

O projeto foi estruturado como uma base técnica de portfólio para demonstrar, de forma prática:

- autenticação com JWT
- gateway como ponto único de entrada
- separação de responsabilidades por serviço
- comunicação síncrona com OpenFeign
- comunicação assíncrona com Kafka
- persistência orientada a documentos com MongoDB
- testes automatizados com cobertura validada no build
- execução local e empacotamento com Docker

## Visão geral

O FinFlow representa o núcleo backend de uma aplicação financeira capaz de:

- cadastrar usuários com e-mail e senha
- autenticar sessões via JWT
- registrar receitas
- registrar despesas
- consolidar saldo e indicadores financeiros
- gerar relatórios por período e por categoria

O foco do projeto é mostrar como modelar um domínio financeiro simples em uma arquitetura distribuída, com atenção a clareza estrutural, testabilidade, segurança básica e evolução incremental.

## Arquitetura

```mermaid
flowchart LR
    Client["Cliente API"] --> Gateway["Gateway (8080)"]
    Gateway --> Auth["Auth Service (8084)"]
    Gateway --> Income["Income Service (8081)"]
    Gateway --> Expense["Expense Service (8082)"]
    Gateway --> Reports["Reports Service (8083)"]

    Auth --> Mongo["MongoDB"]
    Income --> Mongo
    Expense --> Mongo
    Reports --> Mongo

    Income --> Kafka["Kafka"]
    Expense --> Kafka
    Kafka --> Reports

    Reports --> Income
    Reports --> Expense

    Gateway --> Eureka["Eureka (8761)"]
    Auth --> Eureka
    Income --> Eureka
    Expense --> Eureka
    Reports --> Eureka
```

## Módulos

| Módulo | Porta | Responsabilidade |
| --- | --- | --- |
| `finflow-discovery` | `8761` | Registro e descoberta de serviços com Eureka |
| `finflow-auth` | `8084` | Cadastro, login por e-mail, emissão de JWT e recuperação de perfil |
| `finflow-gateway` | `8080` | Entrada única, validação do token e roteamento para os serviços internos |
| `finflow-income` | `8081` | CRUD de receitas e publicação de eventos |
| `finflow-expense` | `8082` | CRUD de despesas e publicação de eventos |
| `finflow-reports` | `8083` | Consolidação financeira, histórico, saldo e agrupamentos |

## Stack técnica

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
- Maven multi-módulo
- Docker Compose
- MongoDB
- Kafka
- Zookeeper
- JUnit 5
- Mockito
- MockMvc
- JaCoCo
- GitHub Actions

## Principais funcionalidades

### Autenticação

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- cadastro de usuário com e-mail e senha
- autenticação stateless com JWT
- propagação do contexto autenticado para os serviços de domínio

### Receitas

- criação, listagem, atualização e remoção de receitas
- filtros por categoria e intervalo de datas
- endpoint paginado para consulta operacional
- consulta de resumo por mês e ano
- publicação de eventos:
  - `income.created`
  - `income.updated`
  - `income.deleted`

### Despesas

- criação, listagem, atualização e remoção de despesas
- filtros por categoria e intervalo de datas
- endpoint paginado para consulta operacional
- consulta de resumo por mês e ano
- publicação de eventos:
  - `expense.created`
  - `expense.updated`
  - `expense.deleted`

### Relatórios

- saldo consolidado
- resumo mensal
- despesas agrupadas por categoria
- histórico consolidado por período
- consolidação acionada por eventos Kafka
- leitura complementar via OpenFeign

## Fluxo de autenticação e sessão

1. O cliente registra uma conta com `POST /api/auth/register`.
2. O serviço de autenticação retorna um `accessToken`.
3. O cliente envia `Authorization: Bearer <token>` nas rotas protegidas.
4. O gateway valida o token e propaga `X-User-Id` para os microsserviços internos.
5. O cliente pode consultar o perfil autenticado com `GET /api/auth/me`.

A sessão é stateless:

- o JWT representa a identidade autenticada
- o gateway não mantém sessão em memória
- os serviços internos recebem apenas o contexto necessário para autorização e segregação de dados por usuário

## Estrutura do repositório

```text
finflow/
├── .github/
├── docs/
├── finflow-discovery/
├── finflow-auth/
├── finflow-gateway/
├── finflow-income/
├── finflow-expense/
├── finflow-reports/
├── build-finflow.ps1
├── docker-compose.yml
├── docker-compose.app.yml
├── README.md
├── start-finflow.ps1
└── stop-finflow.ps1
```

## Como executar

### Pré-requisitos

- Java 21
- Maven
- Docker Desktop
- PowerShell

### Subir a stack backend

```powershell
.\start-finflow.ps1
```

Esse script:

- sobe MongoDB, Zookeeper e Kafka
- inicia `finflow-discovery`
- inicia `finflow-auth`
- inicia `finflow-gateway`
- inicia `finflow-income`
- inicia `finflow-expense`
- inicia `finflow-reports`
- aguarda o Eureka responder
- aguarda os serviços ficarem saudáveis
- só retorna o controle quando a rota de autenticação já está disponível pelo gateway

### Parar a stack backend

```powershell
.\stop-finflow.ps1
```

### Build completo

```powershell
.\build-finflow.ps1
```

Para forçar limpeza antes:

```powershell
.\build-finflow.ps1 -Clean
```

### Execução manual rápida

```powershell
docker compose up -d
mvn verify
```

### Stack containerizada

```powershell
docker compose -f docker-compose.app.yml up --build
```

Guia complementar:

- [Guia de deploy](docs/deployment/README.md)

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
- `GET /api/incomes/paged?page=&size=&category=&startDate=&endDate=`
- `GET /api/incomes/{id}`
- `PUT /api/incomes/{id}`
- `DELETE /api/incomes/{id}`
- `GET /api/incomes/summary?month=&year=`

### Despesas

- `POST /api/expenses`
- `GET /api/expenses`
- `GET /api/expenses/paged?page=&size=&category=&startDate=&endDate=`
- `GET /api/expenses/{id}`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`
- `GET /api/expenses/summary?month=&year=`

### Relatórios

- `GET /api/reports/monthly-summary?month=&year=`
- `GET /api/reports/balance`
- `GET /api/reports/by-category?month=&year=`
- `GET /api/reports/history`

## Padrão de erro

Os serviços HTTP retornam erros padronizados com `ProblemDetail` enriquecido.

Campos esperados:

- `status`
- `title`
- `detail`
- `errorCode`
- `timestamp`
- `path`
- `errors` quando houver falha de validação

Alguns `errorCode` relevantes:

- `VALIDATION_ERROR`
- `RESOURCE_NOT_FOUND`
- `REQUEST_ERROR`
- `METHOD_NOT_ALLOWED`
- `MALFORMED_REQUEST`
- `UNSUPPORTED_MEDIA_TYPE`
- `INTERNAL_ERROR`

Exemplo:

```json
{
  "type": "https://finflow/errors/validation",
  "title": "Validation failed.",
  "status": 400,
  "detail": "One or more request fields are invalid.",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2026-04-27T15:30:00-03:00",
  "path": "/incomes",
  "errors": {
    "description": "must not be blank"
  }
}
```

## Paginação e filtros

Os serviços de receitas e despesas suportam:

- filtro por `category`
- filtro por `startDate`
- filtro por `endDate`
- paginação com `page` e `size`

O endpoint paginado retorna:

- `items`
- `page`
- `size`
- `totalItems`
- `totalPages`
- `hasNext`
- `hasPrevious`

## Validação funcional

O fluxo principal foi validado localmente com a stack em execução:

1. cadastro de usuário com `POST /api/auth/register`
2. login com `POST /api/auth/login`
3. consulta do perfil com `GET /api/auth/me`
4. criação, consulta, atualização e remoção de receitas
5. criação, consulta, atualização e remoção de despesas
6. filtros por categoria e intervalo de datas
7. paginação operacional em receitas e despesas
8. consolidação assíncrona de relatórios por Kafka
9. consultas de saldo, resumo mensal, histórico e agrupamento por categoria
10. validação dos casos de erro mais importantes, como payload inválido, recurso inexistente e intervalo de datas inconsistente

## Qualidade e testes

O projeto possui:

- testes unitários de service
- testes de controller
- testes do `finflow-auth`
- testes do filtro JWT no gateway
- testes de producers e consumer
- cobertura validada com JaCoCo no `mvn verify`
- cenários BDD para fluxos críticos

O build principal é:

```powershell
mvn verify
```

O gate de cobertura está configurado para exigir, no build, pelo menos `80%` de cobertura de linhas no escopo considerado.

Documentação complementar:

- [Guia de testes](docs/testing/README.md)
- [Baseline de cobertura](docs/testing/coverage-status.md)
- [Cenários BDD](docs/testing/bdd/README.md)

## CI e deploy

O projeto inclui:

- workflow de CI em [`.github/workflows/ci.yml`](.github/workflows/ci.yml)
- Dockerfiles por serviço backend
- compose dedicado para a stack completa em contêineres

## Objetivo do projeto

O FinFlow foi construído como projeto de estudo avançado e portfólio técnico, com foco em:

- arquitetura de microsserviços no ecossistema Spring
- autenticação e roteamento centralizado
- integração entre serviços por HTTP e eventos
- disciplina de testes e qualidade
- organização de backend com cara de produto real
