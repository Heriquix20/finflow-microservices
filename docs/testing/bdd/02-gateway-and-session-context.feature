@smoke @api @manual @regression
Feature: Gateway e contexto de sessao
  Como plataforma de entrada do sistema
  Quero validar autenticacao, roteamento e propagacao de contexto
  Para garantir seguranca e consistencia entre os microsservicos

  Scenario: Bloquear rota protegida sem autenticacao
    Given que o gateway esta ativo
    When envio uma requisicao protegida sem header Authorization
    Then a resposta deve ser 401
    And a mensagem deve indicar falha de autenticacao

  Scenario: Permitir rota publica de autenticacao
    Given que o gateway esta ativo
    When envio uma requisicao para uma rota publica em "/api/auth/**"
    Then a requisicao nao deve ser bloqueada pelo filtro JWT

  Scenario: Repassar o userId para os servicos de dominio
    Given que o gateway recebeu um token JWT valido emitido pelo auth service com subject "user-123"
    When a requisicao e encaminhada para income, expense ou reports
    Then o header "X-User-Id" deve ser propagado com o valor "user-123"

  Scenario: Reescrever rotas externas para rotas internas dos servicos
    Given que o gateway esta ativo
    When acesso "/api/auth/**", "/api/incomes/**", "/api/expenses/**" ou "/api/reports/**"
    Then o roteamento deve apontar para os endpoints internos esperados de cada servico
