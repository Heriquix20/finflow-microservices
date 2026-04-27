# Testing Guide

Documentacao central de testes do FinFlow.

Esta pasta organiza a estrategia de qualidade do projeto em quatro frentes:

- testes automatizados de backend
- validacao de cobertura com JaCoCo no ciclo `mvn verify`
- roteiros BDD para validacao funcional fim a fim

## Objetivo

Servir como referencia unica para:

- entender como a qualidade do projeto e validada
- executar os checks principais localmente
- guiar testes manuais e exploratorios
- registrar cenarios criticos de negocio em formato BDD

## Estrutura

```text
docs/testing/
|-- README.md
|-- coverage-status.md
`-- bdd/
    |-- README.md
    |-- 01-environment-and-bootstrap.feature
    |-- 02-gateway-and-session-context.feature
    |-- 03-income-management.feature
    |-- 04-expense-management.feature
    |-- 05-reporting-and-consolidation.feature
    |-- 06-auth-registration-and-login.feature
    |-- 08-resilience-and-error-handling.feature
    `-- ...
```

## Como executar a validacao automatizada

### Backend

```powershell
mvn verify
```

Esse comando:

- compila os modulos backend
- executa os testes automatizados
- gera relatorios JaCoCo
- aplica o gate de cobertura configurado no build

## Como usar os roteiros BDD

Os arquivos `.feature` desta pasta ainda nao estao plugados a um runner Cucumber no build.

Eles funcionam como:

- documentacao executavel de negocio
- roteiro padrao para QA manual
- base futura para automacao com Cucumber ou outra estrategia API-first

Fluxo recomendado:

1. subir o ambiente com `.\start-finflow-all.ps1`
2. garantir que os servicos backend respondem nas portas esperadas
3. executar os cenarios por prioridade:
   - `@smoke`
   - `@api`
   - `@regression`
4. registrar evidencias quando necessario:
   - resposta HTTP
   - log de servico

## Escopo atual de cobertura funcional

Os cenarios BDD cobrem:

- bootstrap do ambiente local
- cadastro e login por email
- roteamento e protecao no gateway
- CRUD de receitas
- CRUD de despesas
- consolidacao de relatorios
- erros esperados e degradacao controlada

## Referencias rapidas

- baseline de cobertura: [coverage-status.md](./coverage-status.md)
- cenarios BDD: [bdd/README.md](./bdd/README.md)
