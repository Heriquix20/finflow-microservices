@smoke @api @manual @regression
Feature: Gestao de receitas
  Como usuario do FinFlow
  Quero gerenciar receitas
  Para acompanhar entradas financeiras do meu periodo

  Background:
    Given que o income service esta ativo
    And uso o header "X-User-Id" com valor "user-123"

  Scenario: Criar uma receita valida
    When envio um POST para "/incomes" com descricao, valor, categoria e data validos
    Then a resposta deve ser 201
    And o corpo deve conter um identificador gerado
    And o corpo deve refletir o "userId" da requisicao

  Scenario: Listar receitas do usuario
    Given que ja existe ao menos uma receita para "user-123"
    When envio um GET para "/incomes"
    Then a resposta deve ser 200
    And a lista deve conter apenas receitas do usuario informado

  Scenario: Buscar uma receita existente por id
    Given que existe uma receita cadastrada para "user-123"
    When envio um GET para "/incomes/{id}"
    Then a resposta deve ser 200
    And o item retornado deve corresponder ao identificador informado

  Scenario: Atualizar uma receita existente
    Given que existe uma receita cadastrada para "user-123"
    When envio um PUT para "/incomes/{id}" com novos dados validos
    Then a resposta deve ser 200
    And os campos editaveis devem refletir os novos valores

  Scenario: Remover uma receita existente
    Given que existe uma receita cadastrada para "user-123"
    When envio um DELETE para "/incomes/{id}"
    Then a resposta deve ser 204
    And o recurso nao deve mais aparecer na listagem

  Scenario: Obter resumo mensal de receitas
    Given que existem receitas no mes e ano consultados
    When envio um GET para "/incomes/summary?month=4&year=2026"
    Then a resposta deve ser 200
    And o valor retornado deve ser a soma das receitas do periodo

  Scenario: Rejeitar payload invalido
    When envio um POST para "/incomes" com campos obrigatorios ausentes
    Then a resposta deve ser 400
    And a resposta deve conter detalhes padronizados de validacao
