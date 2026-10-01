# Itens

**Épico:** [v1-011-itens-e-fabricacao](../v1-011-itens-e-fabricacao) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Itens são equipamentos e ferramentas que cidadãos podem fabricar nas oficinas e equipar para aumentar sua eficácia em combate ou trabalho. Cada item tem categoria, nível (L1–L10), qualidade (Simples, Boa, Excelente, Divina) e bônus intrínsecos e de pedras de bônus. O sistema é comum a armas, ferramentas, armaduras e joias.

## Regras

### Categorias e slots

- R1: Cada pessoa tem 1 slot de Arma (Arco, Espada, Lança, Besta) [req]
- R2: Cada pessoa tem 1 slot de Ferramenta por profissão não-guerreira [req]
- R3: Cada pessoa tem 6 slots de Armadura: Peitoral, Capacete, Ombreiras, Luvas, Calças, Sapato (1 cada) [req]
- R4: Cada pessoa tem 1 slot de Colar e 2 slots de Anel para Joias [req + proposta]

### Qualidades

- R5: Qualidades: Simples (0 slots de pedra, 0 bônus intrínsecos), Boa (1 slot, 1 bônus), Excelente (3 slots, 2 bônus), Divina (5 slots, 3 bônus) [req + proposta]
- R6: Bônus intrínsecos sorteados ao fabricar, distintos, do subconjunto permitido por categoria [proposta]:
  - Armas: FOR, VEL, INT, ATK, CRIT, INI
  - Armaduras: VIT, DEF, VIDA, VEL
  - Joias: VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD
  - Ferramentas: PROF, PROD, INT, FOR, VEL
- R7: Faixa dos bônus intrínsecos pelo nível do item: L1–4 baixa; L5–7 média; L8–10 alta [proposta]

### Níveis 1–10

- R8: Atributo principal segue fórmula: `base × (1 + 0,2 × (L − 1))` [proposta]
- R9: Multiplicadores: L1 ×1,0; L3 ×1,4; L5 ×1,8; L6 ×2,0; L10 ×2,8 [proposta]
- R10: Ferramentas: bônus de PE = **+L** na profissão da ferramenta (ex.: Enxada L3 → +3 PE de Agricultor → +0,3 de eficiência) [proposta]
- R11: Joias (Colar): +5 × L de Vida [proposta]
- R12: Joias (Anel): + característica escolhida ao fabricar: L1–3 +1, L4–6 +2, L7–9 +3, L10 +4 [proposta]

## Números e tabelas

### Fórmula de nível

| Nível | Multiplicador |
|---|---|
| L1 | 1,0 |
| L2 | 1,2 |
| L3 | 1,4 |
| L4 | 1,6 |
| L5 | 1,8 |
| L6 | 2,0 |
| L7 | 2,2 |
| L8 | 2,4 |
| L9 | 2,6 |
| L10 | 2,8 |

(Seção 7.3 da bíblia)

### Catálogo de bônus

[Ver [bonus.md — catálogo de bônus](../v1-012-pedras-de-bonus/bonus.md)]

| Código | Efeito | Baixa | Média | Alta |
|---|---|---|---|---|
| VIT / FOR / VEL / INT / CAR | + característica | +1 | +2 | +3 |
| ATK | + % de ataque | +3% | +5% | +8% |
| DEF | + % de defesa | +3% | +5% | +8% |
| VIDA | + pontos de vida | +8 | +15 | +25 |
| INI | + iniciativa | +1 | +2 | +3 |
| CRIT | + chance de crítico (pp) | +2 | +3 | +5 |
| PROF | + PE na profissão da ferramenta (em arma/armadura/joia: Guerreiro) | +1 | +2 | +3 |
| PROD | + % de eficiência no trabalho | +3% | +5% | +8% |

(Seção 6.2 da bíblia)

## Exemplos

### Exemplo 1: Espada L5 com bônus

- Base: Ataque 10 (seção 7.8)
- Multiplicador L5: ×1,8
- Ataque L5: 10 × 1,8 = 18
- Se qualidade Excelente: 2 bônus intrínsecos (p. ex., +5% ATK + +1 INI)
- Se 1 pedra Boa engastada: +1 VIDA (+15)

### Exemplo 2: Ferramenta — Enxada L1

- Bônus PE: +1 na profissão Agricultor
- Agricultor com PE base 2 equipando Enxada L1: PE efetivo = 2 + 1 = 3
- Eficiência: 0,5 + 0,1 × 3 = 0,8

### Exemplo 3: Anel L6 de Força

- Receita base: 1 Ferro, 15 Ouro (seção 7.11)
- Custo L6: 6 Ferro, 90 Ouro (×L)
- Efeito: + característica Força L4–6 +2 → +2 FOR

## Interações com outros domínios

- [../v1-010-recursos-e-producao/recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos gastos na fabricação
- [../v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md) — oficinas (Ferraria, Alfaiataria, Carpintaria) onde itens são fabricados
- [../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — cidadãos equipam itens
- [../v1-012-pedras-de-bonus/pedras-de-bonus.md](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — pedras engastadas em itens
- [../../v1-014-batalha/batalha.md](../v1-014-batalha/batalha.md) — bônus de itens afetam combate

## Modelo de dados (resumo)

| Tabela | Campos principais |
|---|---|
| item | id, vila_id, categoria, subtipo, qualidade, nivel, bonus (jsonb), atributo_escolhido, cidadao_id, slot |
| pedra | id, vila_id, qualidade, bonus (jsonb), item_id |

(Seção 11.2 da bíblia)

## Histórias

- [h-001-fabricar-item-na-oficina.md](historia/h-001-fabricar-item-na-oficina.md)
- [h-002-equipar-itens-no-painel-da-pessoa.md](historia/h-002-equipar-itens-no-painel-da-pessoa.md)
- [h-003-gerenciar-inventario-e-aprimorar-itens.md](historia/h-003-gerenciar-inventario-e-aprimorar-itens.md)

## Questões em aberto

- [proposta] Bônus de pedras e itens interagem com bônus do líder? (multiplicam-se ou somam-se?)
- [proposta] Inventário tem limite? (spec 7.6 diz "sem limite")
