# Oficinas

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Oficinas fabricam itens: armas, armaduras, ferramentas e joias. Existem 3 oficinas principais (Ferraria, Alfaiataria, Carpintaria), cada uma com profissão e nível máximo de item que pode fabricar. Ferraria também realiza engaste de pedras em itens.

## Regras

- **R1**: Oficinas fabricam itens (ver seção 7 do game-design) (seção 4.11) [proposta].
- **R2**: Vagas de artesão: N1 = 2, N2 = 5, N3 = 10 (seção 4.11) [proposta].
- **R3**: Cada artesão trabalha 1 item por vez (seção 4.11) [proposta].
- **R4**: Nível máximo de item fabricável: N1 até L3, N2 até L6, N3 até L10 (seção 4.11) [proposta].
- **R5**: Ferraria também faz engaste de pedras (seção 4.11, 6.1) [proposta].
- **R6**: Oficinas ocupam 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].
- **R7**: Fabricação segue regras de seção 7.4 (requisitos, custo ×L, qualidade, aprimoramento) (seção 7.4) [proposta].

## Números e tabelas

### Oficinas e sua fabricação [proposta]

| Oficina | Profissão | O que fabrica | Nível máx. item |
|---|---|---|---|
| Ferraria | Ferreiro | Espadas, Lanças, Martelos, Picaretas, Machados, Malhos, Cutelos, Minas de ouro, Peitorais, Capacetes, Ombreiras, Colares, Anéis, Engaste de pedras | N1: L3, N2: L6, N3: L10 |
| Alfaiataria | Costureiro | Kits de costura, Luvas, Calças, Sapatos | N1: L3, N2: L6, N3: L10 |
| Carpintaria | Madeireiro | Carrinhos de mão, Arcos, Bestas | N1: L3, N2: L6, N3: L10 |

### Custos por nível [proposta]

| Oficina | Nível | Madeira | Pedra | Tábua | Tjolo | Ferro | Tecido | PO |
|---|---|---|---|---|---|---|---|---|
| Ferraria | N1 | — | — | 20 | 20 | 10 | — | 8 |
| Alfaiataria | N1 | — | — | 20 | 10 | 5 | 5 | 6 |
| Carpintaria | N1 | — | 10 | 30 | — | — | — | 6 |

Custos N2 = 2,5×, N3 = 5×.

## Exemplos

**Exemplo 1: Fabricar Espada L1 em Ferraria N1**
- Artesão Ferreiro de PE 5 (mínimo 2×1 − 2 = 0, atende).
- Receita: 3 Ferro, 1 Tábua.
- PF: 1 + 1 = 2. Progresso/turno: eficiência 1,0 × mult. 1,0 = 1 PF → 2 turnos.
- Qualidade sorteada no término.

**Exemplo 2: Fabricar Joia L5 em Ferraria N3**
- Artesão com PE 12 (mínimo 2×5 − 2 = 8, atende).
- Receita Colar L5: 1 Ferro × 5 + 20 Ouro × 5 = 5 Ferro, 100 Ouro.
- PF: 1 + 5 = 6. Progresso/turno: eficiência 1,0 × mult. 1,5 = 1,5 PF → 4 turnos.

**Exemplo 3: Engaste de pedra**
- Ferraria N1 com 1 Ferreiro. Pedra Boa (25 Ouro de engaste).
- Engaste imediato; item recebe slot de pedra.
- Se remover, a pedra é destruída.

**Exemplo 4: Limite de nível da oficina**
- Alfaiataria N1. Jogador tenta fabricar Sapato L4.
- Rejeitado: oficina permite até L3 (nível máximo = 3).

## Interações com outros domínios

- [itens.md](../v1-011-itens-e-fabricacao/itens.md) — sistema de itens, fabricação
- [armas.md](../v1-004-armas/armas.md) — receita de armas
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — receita de ferramentas
- [armaduras.md](../v1-006-Armaduras/armaduras.md) — receita de armaduras
- [joias.md](../v1-007-Joias/joias.md) — receita de joias
- [pedras-de-bonus.md](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — engaste na Ferraria
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissão Ferreiro, Costureiro, Madeireiro
- [construcoes.md](construcoes.md) — custos, PO, alocação

## Modelo de dados

Oficina é um tipo de `construcao`; fila de fabricação na tabela `fabricacao` com campos: construcao_id, artesao_id, tipo, nivel, pf_total, pf_atual.

## Questões em aberto

- Artesão parado durante obra da oficina: fica ocioso ou trocamos para outra oficina?
- Limite de filas por oficina: há limite?
