# H-002 — Acompanhar relatório do turno

**Épico:** [../turnos.md](../turnos.md) · **Domínio:** [Turnos](../turnos.md)

## História

Como jogador, quero ver o número do turno atual e a lista de eventos que aconteceram no último turno, para acompanhar o progresso da minha vila.

## Contexto

Após cada processamento de turno, a vila deve exibir uma barra com o número do turno, contagem regressiva até o próximo, e um painel com os eventos ocorridos (nascimentos, mortes, produções, masmorras, etc.) registrados em `evento_turno`.

> Relatório do turno: grava eventos para o jogador. (Seção 2.2, passo 13 da bíblia)

## Critérios de aceite

### CA1 — Barra do turno mostra número e contagem regressiva
- **Dado** que o turno 15 foi processado
- **Quando** o jogador abre a tela principal
- **Então** a barra exibe "Turno 15" e contagem regressiva para o turno 16 (ex.: "59 minutos")

### CA2 — Lista de eventos do último turno
- **Dado** que ocorreram eventos no turno 15 (1 nascimento, 1 morte, 5 produções)
- **Quando** o jogador abre a aba de Relatório do turno
- **Então** vê uma lista com 7 eventos, cada um com tipo, descrição e timestamp

### CA3 — Eventos distintos por tipo
- **Dado** que o turno incluiu produção, morte, nascimento e masmorra surgindo
- **Quando** a lista é exibida
- **Então** cada tipo tem um ícone ou cor diferente; é possível filtrar por tipo

### CA4 — Relatório pode ser consultado retroativamente
- **Dado** que o turno 10 foi processado há 5 turnos
- **Quando** o jogador consulta o histórico
- **Então** consegue ver os eventos do turno 10 (via API com filtro por turno)

## Tarefas

- [H-002 · Tarefa 001 — Registro de eventos do turno](h-002-tarefa-001-registro-de-eventos-do-turno.md)
- [H-002 · Tarefa 002 — Barra do turno e relatório](h-002-tarefa-002-barra-do-turno-e-relatorio.md)

## Fora de escopo

- Notificações push ou em tempo real.
- Filtro avançado por data ou intervalo grande de turnos.
