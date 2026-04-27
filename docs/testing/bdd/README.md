# BDD Scenarios

Colecao de cenarios BDD do FinFlow em formato Gherkin.

## Convencoes adotadas

- idioma: portugues
- formato: `Given / When / Then`
- foco: fluxo de negocio e comportamento observavel
- tags:
  - `@smoke` para fluxo minimo essencial
  - `@api` para validacao orientada a endpoint
  - `@regression` para cobertura de regressao
  - `@manual` para execucao manual atual

## Ordem sugerida de execucao

1. `01-environment-and-bootstrap.feature`
2. `06-auth-registration-and-login.feature`
3. `02-gateway-and-session-context.feature`
4. `03-income-management.feature`
5. `04-expense-management.feature`
6. `05-reporting-and-consolidation.feature`
7. `08-resilience-and-error-handling.feature`

## Preparacao recomendada

Antes de executar os cenarios:

```powershell
.\start-finflow-all.ps1
```

Para parar tudo ao final:

```powershell
.\stop-finflow-all.ps1
```

## Usuario de referencia

Os cenarios devem preferir cadastro real via `POST /api/auth/register`.

Se o ambiente tiver muitos dados acumulados, prefira limpar as colecoes Mongo antes da rodada atual.
