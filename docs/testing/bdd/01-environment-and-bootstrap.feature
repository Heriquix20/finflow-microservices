@smoke @manual @regression
Feature: Bootstrap do ambiente local
  Como pessoa desenvolvedora ou QA
  Quero subir o ambiente completo localmente
  Para validar a disponibilidade basica do ecossistema FinFlow

  Scenario: Subir todos os servicos com o script principal
    Given que estou na raiz do projeto
    When executo o script "start-finflow-all.ps1"
    Then o Docker Compose deve subir MongoDB, Zookeeper e Kafka
    And o discovery deve responder na porta 8761
    And o auth service deve responder na porta 8084
    And o gateway deve responder na porta 8080
    And o income service deve responder na porta 8081
    And o expense service deve responder na porta 8082
    And o reports service deve responder na porta 8083

  Scenario: Parar o ambiente completo com o script principal
    Given que todos os servicos do FinFlow estao ativos
    When executo o script "stop-finflow-all.ps1"
    Then os processos backend devem ser encerrados
    And os containers da infraestrutura devem ser finalizados

  Scenario: Registrar servicos no Eureka
    Given que o ambiente completo esta ativo
    When acesso a interface do Eureka
    Then devo visualizar os servicos "finflow-auth", "finflow-gateway", "finflow-income", "finflow-expense" e "finflow-reports" registrados
