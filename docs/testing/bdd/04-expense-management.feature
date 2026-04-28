@smoke @api @manual @regression
Feature: Gestao de despesas
  Como usuario do FinFlow
  Quero gerenciar despesas
  Para acompanhar saidas financeiras do meu periodo

  Background:
    Given que o expense service esta ativo
    And uso o header "X-User-Id" com valor "user-123"

  Scenario: Criar uma despesa valida
    When envio um POST para "/expenses" com descricao, valor, categoria e data validos
    Then a resposta deve ser 201
    And o corpo deve conter um identificador gerado
    And o recurso deve ficar disponivel para consultas futuras

  Scenario: Listar despesas do usuario
    Given que ja existe ao menos uma despesa para "user-123"
    When envio um GET para "/expenses"
    Then a resposta deve ser 200
    And a lista deve conter apenas despesas do usuario informado

  Scenario: Buscar uma despesa existente por id
    Given que existe uma despesa cadastrada para "user-123"
    When envio um GET para "/expenses/{id}"
    Then a resposta deve ser 200
    And o item retornado deve corresponder ao identificador informado

  Scenario: Atualizar uma despesa existente
    Given que existe uma despesa cadastrada para "user-123"
    When envio um PUT para "/expenses/{id}" com novos dados validos
    Then a resposta deve ser 200
    And os campos editaveis devem refletir os novos valores

  Scenario: Remover uma despesa existente
    Given que existe uma despesa cadastrada para "user-123"
    When envio um DELETE para "/expenses/{id}"
    Then a resposta deve ser 204
    And o recurso nao deve mais aparecer na listagem

  Scenario: Obter resumo mensal de despesas
    Given que existem despesas no mes e ano consultados
    When envio um GET para "/expenses/summary?month=4&year=2026"
    Then a resposta deve ser 200
    And o valor retornado deve ser a soma das despesas do periodo

  Scenario: Rejeitar payload invalido
    When envio um POST para "/expenses" com campos obrigatorios ausentes
    Then a resposta deve ser 400
    And a resposta deve conter detalhes padronizados de validacao
