# Turnos

**Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

O jogo avança por turnos globais sincronizados: um turno representa 1 mês de jogo e é processado a cada 60 minutos reais. Cada vila é processada em sequência através de uma pipeline de 13 etapas determinísticas, com garantias de idempotência para suportar reexecução em caso de falha.

## Regras

- **R1 — Cadência:** Turno global processado a cada 60 minutos reais (configurável em `jogo.turno.intervalo-minutos`); 1 turno = 1 mês; 12 turnos = 1 ano.
- **R2 — Processamento:** Cada vila é processada em sua própria transação; o processamento é idempotente por (vila, número do turno).
- **R3 — Ordens:** Entre turnos, o jogador dá ordens (construir, alocar, casar, fabricar, formar tropa, enviar expedição, comerciar). Ordens que gastam recursos debitam no ato; os efeitos de tempo são resolvidos no processamento do turno.
- **R4 — Concorrência:** Batalhas são resolvidas em rodadas independentes do turno do jogo: uma batalha inteira é resolvida dentro do passo 8 de um turno [req].
- **R5 — Idempotência:** Reexecução de um turno para a mesma vila não altera o resultado (útil para recuperação de falhas).

## Ordem de resolução por vila

A resolução segue 13 passos em sequência:

| Passo | Etapa | Doc de domínio |
|---|---|---|
| 1 | Produção: prédios de coleta e rurais, depois fábricas (Serraria → Olaria → Fundição → Tecelagem → Curtume → Cozinha) | [v1-010-recursos-e-producao](../v1-010-recursos-e-producao/producao.md) |
| 2 | Ouro passivo: imposto e Estalagem | [v1-010-recursos-e-producao](../v1-010-recursos-e-producao/comercio.md) |
| 3 | Consumo de comida e verificação de fome | [v1-010-recursos-e-producao](../v1-010-recursos-e-producao/alimentacao.md) |
| 4 | Limite de armazenamento: excedente acima da capacidade é perdido | [v1-010-recursos-e-producao](../v1-010-recursos-e-producao/recursos.md) |
| 5 | Obras: progresso de construções e upgrades; conclusão | [v1-003-construcoes](../v1-003-construcoes/construcoes.md) |
| 6 | Fabricação: progresso das filas das oficinas; conclusão de itens | [v1-011-itens-e-fabricacao](../v1-011-itens-e-fabricacao/fabricacao.md) |
| 7 | Quartel: XP de treinamento dos guerreiros aquartelados | [v1-013-quartel-e-tropas](../v1-013-quartel-e-tropas/tropas.md) |
| 8 | Movimentação de tropas: avança viagens; ao chegar, batalha resolvida imediatamente; ao voltar, tropa retorna e entrega saque | [v1-013-quartel-e-tropas](../v1-013-quartel-e-tropas/expedicoes.md) |
| 9 | Reprodução: concepções, gestações, nascimentos; imigração pela Estalagem | [v1-002-cidadaos](../v1-002-cidadaos/familias.md) |
| 10 | Envelhecimento: +1 mês para todos; aniversários dão pontos de crescimento; teste de morte | [v1-002-cidadaos](../v1-002-cidadaos/ciclo-de-vida.md) |
| 11 | Recuperação de feridos | [v1-002-cidadaos](../v1-002-cidadaos/ciclo-de-vida.md) |
| 12 | Masmorras: evolução e surgimento | [v1-001-masmorras](../v1-001-masmorras/masmorras.md) |
| 13 | Relatório do turno: grava eventos para o jogador | — |

## Modelo de dados (resumo)

| Tabela | Campos principais |
|---|---|
| jogo_turno | numero, iniciado_em, concluido_em |
| evento_turno | id, vila_id, turno, tipo, mensagem, dados (jsonb) |

- Tabela `jogo_turno` com chave primária em `numero`; grava o estado global do turno (iniciado, concluído, timestamps).
- Tabela `evento_turno` registra os eventos ocorridos em cada turno para exibição no relatório (produção, morte, nascimento, etc.).

## Histórias

- [h-001 — Processar turno global](historia/h-001-processar-turno-global.md)
- [h-002 — Acompanhar relatório do turno](historia/h-002-acompanhar-relatorio-do-turno.md)

## Questões em aberto

- Configuração do intervalo de turno (`jogo.turno.intervalo-minutos`) validada em tempo de inicialização ou durante execução?
