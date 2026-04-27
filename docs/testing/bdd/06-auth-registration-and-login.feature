@smoke @api @manual @regression
Feature: Cadastro, login e sessao autenticada
  Como pessoa usuaria da plataforma
  Quero criar conta, entrar com email e senha e recuperar meu perfil
  Para acessar os demais recursos de forma autenticada

  Scenario: Registrar nova conta com email e senha
    Given que o auth service esta ativo
    And que o email "maria@example.com" ainda nao foi cadastrado
    When envio um POST para "/api/auth/register" com displayName, email e password validos
    Then a resposta deve ser 201
    And o payload deve conter accessToken, userId, email e displayName

  Scenario: Entrar com credenciais validas
    Given que existe uma conta cadastrada com email "maria@example.com"
    When envio um POST para "/api/auth/login" com email e senha corretos
    Then a resposta deve ser 200
    And o payload deve conter um JWT valido

  Scenario: Recuperar perfil autenticado
    Given que possuo um JWT valido emitido pelo auth service
    When envio um GET para "/api/auth/me"
    Then a resposta deve ser 200
    And o payload deve conter userId, email e displayName do usuario autenticado

  Scenario: Bloquear cadastro duplicado
    Given que o email "maria@example.com" ja esta cadastrado
    When tento registrar novamente a mesma conta
    Then a resposta deve ser 409
    And a mensagem deve informar que o email ja esta em uso
