# H-001 · Tarefa 001 — Agendador do turno global

**História:** [H-001 — Processar turno global](h-001-processar-turno-global.md) · **Domínio:** [Turnos](../turnos.md) · **Depende de:** — · **Camada:** Backend

## Objetivo

Implementar um agendador Spring que dispara o processamento do turno global a cada intervalo configurável (padrão 60 minutos), garantindo execução única via trava (ShedLock ou controle na tabela jogo_turno).

## Contexto necessário

- Seção 2.1 da bíblia — Cadência
  > Turno global (o mesmo número para todas as vilas), processado por um agendador a cada **60 minutos reais** (configurável em `jogo.turno.intervalo-minutos`).

- Seção 11.1 — Multiusuário
  > Turno global com agendador (`@Scheduled`); se houver mais de uma instância, trava distribuída (ex.: ShedLock) ou tabela de controle `jogo_turno`.

## Backend

- **Service `AgendadorTurnoService`** com método anotado `@Scheduled(fixedDelayString = "${jogo.turno.intervalo-minutos:60}m")`.
  - Método chamado `processarTurnoGlobal()` que:
    - Adquire a trava (ShedLock ou `@Version` em `jogo_turno`).
    - Incrementa o número do turno.
    - Delegua a execução para `TurnoProcessor` (tarefa 002).
    - Registra início e fim em `jogo_turno`.

- **Tabela `jogo_turno` com migração Flyway V3:**
  - Campos: `numero` (PK), `iniciado_em` (timestamp), `concluido_em` (timestamp), `status` (PROCESSANDO/CONCLUIDO).
  - Sem índices adicionais nesta tarefa.

- **Configuração em `application.properties`:**
  - `jogo.turno.intervalo-minutos=60` (padrão).
  - `jogo.turno.habilitado=true` (permitir desabilitar para testes).

- **Tratamento de exceção:** registra erro, não falha o agendador; continua na próxima execução.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/turno/AgendadorTurnoService.java](/src/main/java/com/example/loginbase/jogo/turno/AgendadorTurnoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/TurnoRepository.java](/src/main/java/com/example/loginbase/jogo/turno/TurnoRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/Turno.java](/src/main/java/com/example/loginbase/jogo/turno/Turno.java) (novo)
- [/src/main/resources/db/migration/V3__criar_tabelas_de_turno.sql](/src/main/resources/db/migration/V3__criar_tabelas_de_turno.sql) (novo)
- [/src/main/resources/application.properties](/src/main/resources/application.properties) (existente, adicionar configurações)

## Testes

- Teste unitário: mock do `TurnoProcessor` e `TurnoRepository`; verificar que `processarTurnoGlobal()` é chamado.
- Teste de integração: habilitar `@Scheduled` em contexto de teste; aguardar execução; validar que `jogo_turno.numero` foi incrementado.
- Teste de trava: simular duas instâncias disparando simultaneamente; verificar que apenas uma incrementa o turno.

## Definição de pronto

- Critérios de aceite CA1, CA5 (agendador e trava).
- Build sem erros.
- Testes passando.

## Fora de escopo

- Implementação da interface `EtapaTurno` (tarefa 002).
- Processamento de vilas individuais.
