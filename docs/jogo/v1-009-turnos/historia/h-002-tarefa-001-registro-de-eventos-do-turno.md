# H-002 · Tarefa 001 — Registro de eventos do turno

**História:** [H-002 — Acompanhar relatório do turno](h-002-acompanhar-relatorio-do-turno.md) · **Domínio:** [Turnos](../turnos.md) · **Depende de:** [H-001 · Tarefa 002](h-001-tarefa-002-pipeline-de-resolucao-por-vila.md) · **Camada:** Backend

## Objetivo

Implementar os endpoints REST para consultar o número do turno atual e a lista de eventos do último turno de uma vila, com suporte a filtro opcional por turno.

## Contexto necessário

- Seção 11.2 — Modelo de dados
  > evento_turno: id, vila_id, turno, tipo, mensagem, dados (jsonb)

- Seção 2.2 — Passo 13
  > Relatório do turno: grava eventos para o jogador.

## Backend

- **Endpoints REST:**
  - `GET /api/jogo/turno` — Retorna `{ "numero": 15, "proximoEm": "59 minutos" }`
  - `GET /api/jogo/turno/eventos` — Retorna lista de eventos do último turno da vila.
  - `GET /api/jogo/turno/eventos?turno=10` — Filtro opcional por número de turno.

- **Resposta de eventos:**
  ```json
  {
    "turno": 15,
    "eventos": [
      { "id": 1, "tipo": "NASCIMENTO", "mensagem": "João nasceu", "timestamp": "2026-09-30T12:00:00Z" },
      { "id": 2, "tipo": "MORTE", "mensagem": "Maria faleceu de velhice", "timestamp": "2026-09-30T12:00:05Z" }
    ]
  }
  ```

- **Entity `EventoTurno`** (se não existir):
  - `id`, `vilaId`, `turno`, `tipo` (enum: PRODUCAO, MORTE, NASCIMENTO, MASMORRA, FERIDO, RECUPERADO, ...), `mensagem`, `dados` (jsonb), `criadoEm` (timestamp).

- **Service `TurnoService`:**
  - `getTurnoAtual(): TurnoDTO` — consulta `jogo_turno` e calcula contagem regressiva.
  - `getEventos(Vila vila, Integer filtroTurno): List<EventoTurnoDTO>` — consulta `evento_turno` com filtro.

- **Autenticação:** Endpoints protegidos; o `vila_id` é extraído do usuário logado.

## Frontend

Não se aplica (implementado em tarefa 002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/turno/TurnoController.java](/src/main/java/com/example/loginbase/jogo/turno/TurnoController.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/TurnoService.java](/src/main/java/com/example/loginbase/jogo/turno/TurnoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EventoTurnoDTO.java](/src/main/java/com/example/loginbase/jogo/turno/EventoTurnoDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/TurnoDTO.java](/src/main/java/com/example/loginbase/jogo/turno/TurnoDTO.java) (novo)

## Testes

- Teste unitário: mock do `TurnoRepository` e `EventoTurnoRepository`; verificar cálculo de contagem regressiva.
- Teste de integração: inserir eventos; chamar endpoints; validar resposta.
- Teste de segurança: tentar acessar turno de outra vila; verificar que é negado.

## Definição de pronto

- Critérios de aceite CA1, CA2 (barra e lista).
- Endpoints testados e documentados.
- Build sem erros.

## Fora de escopo

- Renderização no frontend (tarefa 002).
- Eventos específicos de cada domínio (registrados pelas próprias etapas).
