# Masmorras

**Épico:** [masmorras.md](masmorras.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Masmorras surgem eventualmente em regiões não possuídas da vila, evoluindo em nível a cada 18 turnos sem ataque. Tropas podem atacá-las em expedição para obter recompensas valiosas. Uma masmorra limpa bloqueia o surgimento de novas masmorras naquela região por 6 turnos.

## Regras

- **R1** — Surgimento: cada região elegível (não possuída, sem masmorra, fora do período de 6 turnos após limpeza) tem **1% de chance por turno** de gerar masmorra nível 1 [proposta] (seção 8.1).
- **R2** — Limite: a vila pode ter no máximo **3 masmorras ativas** ao mesmo tempo [proposta] (seção 8.1).
- **R3** — Carência: vilas com menos de 12 turnos de vida não recebem masmorras [proposta] (seção 8.1).
- **R4** — Evolução: masmorra sobe **+1 nível a cada 18 turnos sem ataque**; máximo nível 10 [proposta] (seção 8.2).
- **R5** — Ataque: qualquer ataque zera o contador de turnos sem ataque; derrota restaura os inimigos com vida cheia [proposta] (seção 8.2).
- **R6** — Vitória: remove a masmorra e bloqueia o surgimento naquela região por 6 turnos [proposta] (seção 8.2, 1.6).
- **R7** — Bloqueio de anexação: região com masmorra ativa não pode ser anexada [proposta] (seção 1.6).

## Números e tabelas

### Evolução de nível
| Turnos sem ataque | Nível da masmorra |
|---|---|
| 0–17 | N (atual) |
| 18–35 | N + 1 |
| 36–53 | N + 2 |
| ... | ... |
| 162+ | N + 10 (máximo) |

Fonte: seção 8.2 (bíblia).

## Exemplos

- Masmorra nível 1 surge em região elegível → após 18 turnos sem ataque, sobe para nível 2.
- Tropa derrota uma masmorra N3 → masmorra removida; região fica 6 turnos "limpa" (8.2, 1.6).
- Região elegível com 1% chance × 100 turnos ≈ 63% de chance acumulada de gerar masmorra (limite de 3).

## Interações com outros domínios

- [vila.md](../v1-008-vila-e-mapa/vila.md) — regiões bloqueadas por masmorra ativa não podem ser anexadas.
- [regioes.md](../v1-008-vila-e-mapa/regioes.md) — índice das regiões e distância de Manhattan.
- [tropas.md](../v1-013-quartel-e-tropas/tropas.md) — expedição de tropa tem destino masmorra; comida e viagem.
- [expedições.md](../v1-013-quartel-e-tropas/expedicoes.md) — turnos de viagem.
- [batalha.md](../v1-014-batalha/batalha.md) — combate contra inimigos da masmorra.

## Modelo de dados (resumo)

| Tabela | Campos |
|---|---|
| masmorra | id, vila_id, regiao_indice (1–16), nivel (1–10), turno_surgimento, turnos_sem_ataque (0+), ativa (bool) |

Fonte: seção 11.2 (bíblia).

## Histórias

- [h-001 — Surgimento e evolução de masmorras](historia/h-001-surgimento-e-evolucao-de-masmorras.md)
- [h-002 — Atacar masmorra](historia/h-002-atacar-masmorra.md)
- [h-003 — Receber recompensas da masmorra](historia/h-003-receber-recompensas-da-masmorra.md)

## Questões em aberto

- Nenhuma [proposta] adicional neste épico.
