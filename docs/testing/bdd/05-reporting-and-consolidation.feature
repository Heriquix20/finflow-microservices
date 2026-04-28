@smoke @api @manual @regression
Feature: Relatorios e consolidacao financeira
  Como usuario do FinFlow
  Quero visualizar consolidacoes financeiras confiaveis
  Para entender meu saldo e comportamento por periodo

  Background:
    Given que o reports service esta ativo
    And existem receitas e despesas para o usuario "user-123"

  Scenario: Atualizar resumo mensal a partir dos eventos financeiros
    When uma receita ou despesa e criada ou atualizada
    Then o reports service deve processar o evento correspondente
    And o resumo mensal do periodo afetado deve ser recalculado

  Scenario: Consultar resumo mensal consolidado
    When envio um GET para "/reports/monthly-summary?month=4&year=2026"
    Then a resposta deve ser 200
    And o payload deve conter "totalIncome", "totalExpense" e "balance"

  Scenario: Consultar saldo consolidado atual
    When envio um GET para "/reports/balance"
    Then a resposta deve ser 200
    And o valor retornado deve representar receitas menos despesas do usuario

  Scenario: Consultar despesas agrupadas por categoria
    When envio um GET para "/reports/by-category?month=4&year=2026"
    Then a resposta deve ser 200
    And a resposta deve trazer uma lista de categorias com seus totais

  Scenario: Consultar historico consolidado sem duplicidade por periodo
    When envio um GET para "/reports/history"
    Then a resposta deve ser 200
    And o historico deve representar no maximo um resumo por combinacao de mes e ano

  Scenario: Retornar resposta vazia para periodo sem dados
    Given que nao existem dados no periodo solicitado
    When consulto os endpoints de relatorio para esse periodo
    Then a API deve retornar respostas vazias coerentes com o contrato
