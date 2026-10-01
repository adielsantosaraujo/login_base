# H-001 — Processar turno global

**Épico:** [../turnos.md](../turnos.md) · **Domínio:** [Turnos](../turnos.md)

## História

Como administrador do jogo, quero que o turno global seja processado automaticamente a cada intervalo configurável, para que todas as vilas avancem juntas no tempo de jogo.

## Contexto

O sistema utiliza um agendador baseado em `@Scheduled` para disparar o processamento do turno de forma periódica. A cadência é de 60 minutos reais (configurável), e cada turno representa 1 mês de jogo. O processamento deve garantir que todas as vilas sejam processadas exatamente uma vez por turno, com isolamento transacional para evitar inconsistências.

> Turno global (o mesmo número para todas as vilas), processado por um agendador a cada **60 minutos reais** (configurável em `jogo.turno.intervalo-minutos`). (Seção 2.1 da bíblia)

## Critérios de aceite

### CA1 — Agendador dispara a cada intervalo configurável
- **Dado** que `jogo.turno.intervalo-minutos` está definido como 60
- **Quando** o sistema aguarda 60 minutos
- **Então** a etapa de processamento do turno é acionada uma única vez

### CA2 — Cada vila processada uma única vez por turno
- **Dado** que existem 3 vilas (vilasA, B, C) no banco de dados
- **Quando** o turno é processado
- **Então** cada vila recebe turno número N exatamente uma vez e nenhuma é processada duas vezes no mesmo turno

### CA3 — Falha em uma vila não impede as demais
- **Dado** que durante o processamento a vila A lança uma exceção (ex.: erro em cálculo de produção)
- **Quando** o agendador trata a exceção e continua
- **Então** as vilas B e C são processadas normalmente, e o evento de falha é registrado

### CA4 — Ordem dos passos é respeitada
- **Dado** que os 13 passos estão implementados (produção, ouro, comida, ..., masmorras, relatório)
- **Quando** uma vila é processada
- **Então** os passos são executados na ordem exata: 1→2→3→...→13, sem saltos

### CA5 — Trava previne execução simultânea
- **Dado** que a aplicação está em cluster (múltiplas instâncias)
- **Quando** o agendador de duas instâncias dispara no mesmo minuto
- **Então** uma adquire a trava (ex.: ShedLock ou versão na tabela `jogo_turno`) e a outra aguarda; nenhum turno é processado duas vezes

## Tarefas

- [H-001 · Tarefa 001 — Agendador do turno global](h-001-tarefa-001-agendador-do-turno-global.md)
- [H-001 · Tarefa 002 — Pipeline de resolução por vila](h-001-tarefa-002-pipeline-de-resolucao-por-vila.md)

## Fora de escopo

- Notificação em tempo real para o cliente (push, WebSocket).
- Simulação de turnos futuros ("fast-forward").
