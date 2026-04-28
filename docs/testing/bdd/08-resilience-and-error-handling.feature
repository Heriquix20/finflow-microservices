@api @manual @regression
Feature: Resiliencia e tratamento de erros
  Como equipe responsavel pelo produto
  Quero validar degradacao controlada e mensagens consistentes
  Para reduzir falhas opacas para usuario e time de manutencao

  Scenario: Retornar erro padronizado para validacao invalida no income
    Given que o income service esta ativo
    When envio um payload invalido para criacao de receita
    Then a resposta deve ser 400
    And o corpo deve seguir o padrao ProblemDetail
    And os erros por campo devem estar presentes

  Scenario: Retornar erro padronizado para recurso inexistente no expense
    Given que o expense service esta ativo
    When consulto um identificador inexistente
    Then a resposta deve ser 404
    And o erro deve informar que a despesa nao foi encontrada

  Scenario: Reagir com seguranca a token invalido no gateway
    Given que o gateway esta ativo
    When envio uma requisicao com token JWT invalido
    Then a resposta deve ser 401
    And a requisicao nao deve ser encaminhada ao servico de destino

  Scenario: Bloquear cadastro com email ja existente
    Given que o auth service esta ativo
    And que o email "duplicado@example.com" ja foi registrado
    When envio novo cadastro com o mesmo email
    Then a resposta deve ser 409
    And o erro deve informar conflito de email
