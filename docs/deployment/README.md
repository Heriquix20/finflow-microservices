# Deployment Guide

Guia de execucao e preparo de deploy do FinFlow como plataforma backend.

## Artefatos incluidos

O projeto possui:

- workflow CI em `.github/workflows/ci.yml`
- Dockerfiles para `finflow-discovery`, `finflow-auth`, `finflow-gateway`, `finflow-income`, `finflow-expense` e `finflow-reports`
- `docker-compose.app.yml` para subir a stack backend completa em containers

## Pipeline CI

O workflow executa:

- `mvn verify`

Arquivo:

- [../../.github/workflows/ci.yml](../../.github/workflows/ci.yml)

## Stack containerizada

Para subir a aplicacao com containers:

```powershell
docker compose -f docker-compose.app.yml up --build
```

Essa stack sobe:

- Zookeeper
- Kafka
- MongoDB
- finflow-discovery
- finflow-auth
- finflow-gateway
- finflow-income
- finflow-expense
- finflow-reports

## URLs esperadas na stack containerizada

- eureka: `http://localhost:8761`
- auth: `http://localhost:8084`
- gateway: `http://localhost:8080`
- income swagger: `http://localhost:8081/swagger-ui.html`
- expense swagger: `http://localhost:8082/swagger-ui.html`
- reports swagger: `http://localhost:8083/swagger-ui.html`

## Observacoes

- o auth service centraliza cadastro, login por email e emissao de JWT
- o gateway atua como porta de entrada e validador do token
- para ambiente produtivo real, o proximo passo natural e separar identidade e persistencia de usuarios em um servico dedicado
