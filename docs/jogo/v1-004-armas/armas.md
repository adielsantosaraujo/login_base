# Armas

**Épico:** v1-004-armas · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo
O jogo conta com 4 tipos de armas: espada, lança, arco e besta. Cada uma tem características únicas de alcance, ataque, iniciativa e função tática em batalha. Todas podem ser fabricadas em oficinas específicas e se beneficiam de níveis (L1–L10) e qualidades (Simples, Boa, Excelente, Divina).

## Regras
- R1: Existem 4 categorias de armas: Espada, Lança, Arco e Besta (seção 7.8).
- R2: Cada arma tem uma oficina onde é fabricada (Ferraria ou Carpintaria).
- R3: Todas as armas seguem o sistema comum de níveis (L1–L10) e qualidades (seção 7.2).
- R4: Armas têm atributo-chave que influencia seu ataque: FOR (Espada, Lança), VEL (Arco), INT (Besta).
- R5: A fórmula de ataque incorpora o atributo-chave com modificador +5% por ponto (seção 10.1).
- R6: Alcance e posicionamento definem quem pode atacar quem em batalha (seção 10.3).

## Números e tabelas

Tabela 7.8 — Armas (fonte: seção 7.8 da bíblia):

| Arma | Oficina | Receita base (×L) | Ataque base | Atributo-chave | Mod. iniciativa | Alcance | Especial |
|---|---|---|---|---|---|---|---|
| Espada | Ferraria | 3 Ferro, 1 Tábua | 10 | FOR | 0 | Corpo a corpo, só da linha de frente | +2 Defesa ao portador |
| Lança | Ferraria | 2 Ferro, 2 Tábua | 9 | média de FOR e VEL | +1 | Corpo a corpo, da frente ou da retaguarda | — |
| Arco | Carpintaria | 3 Tábua, 1 Tecido | 8 | VEL | +2 | À distância, qualquer alvo | — |
| Besta | Carpintaria | 2 Ferro, 3 Tábua, 1 Tecido | 13 | INT | −4 | À distância, qualquer alvo | Ignora 25% da defesa do alvo |

Exemplos de custo × nível (L1, L5, L10):
- Espada L1: 3 Ferro + 1 Tábua; L5: 15 Ferro + 5 Tábua; L10: 30 Aço + 10 Tábua.
- Besta L1: 2 Ferro + 3 Tábua + 1 Tecido; L5: 10 Ferro + 15 Tábua + 5 Tecido; L10: 20 Aço + 30 Tábua + 10 Tecido.

## Exemplos
**Exemplo de Ataque (N1)** — Guerreiro com Espada L1 contra Goblin:
- Guerreiro: FOR 6, VIT 5, VEL 5, PE Guerreiro base 5 → G = 5 + 1 (FOR) + 1 (VIT) + 1 (VEL) = 8.
- Espada L1 ataque 10.
- Ataque = 10 × (1 + 0,05 × 6) + 2 × 8 = 10 × 1,30 + 16 = 29.
- Goblin defesa 8 (valores da seção 8.3): 29 × 100 ÷ (100 + 3 × 8) = 29 × 100 ÷ 124 ≈ 23 dano → Goblin (40 PV) cai em 2 golpes.

## Interações com outros domínios
- [v1-011-itens-e-fabricacao/itens.md](../v1-011-itens-e-fabricacao/itens.md) — Sistema comum de qualidades (7.2), níveis (7.3), fórmula de atributos e bônus intrínsecos.
- [v1-011-itens-e-fabricacao/fabricacao.md](../v1-011-itens-e-fabricacao/fabricacao.md) — Requisitos de fabricação (7.4), requisitos de PE, qualidade sorteada.
- [v1-011-itens-e-fabricacao/equipamento.md](../v1-011-itens-e-fabricacao/equipamento.md) — Requisitos para equipar (7.5), inventário (7.6), equipar/trocar (7.7).
- [v1-003-construcoes/oficinas.md](../v1-003-construcoes/oficinas.md) — Oficinas fabricantes: Ferraria e Carpintaria.
- [v1-014-batalha/batalha.md](../v1-014-batalha/batalha.md) — Atributos de combate (10.1), dano (10.2), fluxo e alcance (10.3).

## Modelo de dados (resumo)
Armas são um subtipo de `item` com:
- `categoria` = "ARMA"
- `subtipo` = "ESPADA", "LANCA", "ARCO" ou "BESTA"
- `nivel` L1..L10
- `qualidade`: Simples, Boa, Excelente, Divina
- `bonus`: bônus intrínsecos sorteados (FOR, VEL, INT, ATK, CRIT, INI)
- `cidadao_id`: quem está portando (opcional)
- `slot`: "ARMA" (cada pessoa 1 por vez)

O catálogo de armas (receitas, custos, atributos) fica centralizado em `jogo.catalogo`.

## Histórias
- [h-001-fabricar-armas.md](historia/h-001-fabricar-armas.md)
- [h-002-usar-arma-em-combate.md](historia/h-002-usar-arma-em-combate.md)

## Questões em aberto
- Nenhuma deste domínio.
