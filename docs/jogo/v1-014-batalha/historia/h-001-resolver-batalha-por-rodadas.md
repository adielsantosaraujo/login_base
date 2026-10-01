# H-001 — Resolver batalha por rodadas

**Épico:** [batalha.md](../batalha.md) · **Domínio:** [batalha.md](../batalha.md)

## História

Como comandante de uma tropa, quero que as batalhas contra masmorras sejam resolvidas automaticamente por rodadas, para que o resultado seja determinístico e justo.

## Contexto

Batalhas ocorrem quando uma tropa chega a uma masmorra após viagem (seção 2.3 do turno). O sistema deve:
1. Calcular atributos iniciais de todos os participantes (seção 10.1);
2. Resolver rodadas sequenciais (seção 10.3) até vitória, derrota ou 30 rodadas (limite de recuo);
3. Determinar consequências (ferimentos, mortes, saque) (seção 10.6);
4. Gravar resultado e log para replay (seção 10.5).

Referências rápidas:
- Atributos: PV, Ataque, Defesa, Iniciativa, Crítico (10.1)
- Dano: fórmula com Defesa efetiva (10.2)
- Fluxo: 30 rodadas máx., ordem por iniciativa, alvos por alcance (10.3)

## Critérios de aceite

### CA1 — Ordem de ação por iniciativa
- **Dado** um guerreiro com VEL 5 (Iniciativa base 10) e um Goblin com INI 10
- **Quando** inicia-se uma rodada com sorteio de 1d6 para ambos
- **Então** o participante com maior (INI base + 1d6) age primeiro; em caso de empate, VEL maior age; se ainda empate, ID menor

### CA2 — Cálculo correto de dano no exemplo 10.4
- **Dado** um Guerreiro: FOR 6, VIT 5, VEL 5, PE Guerreiro 5, Espada L1, sem armadura; Goblin: ATQ 16, DEF 8, PV 40
- **Quando** o Guerreiro ataca com Ataque 29 e o Goblin é atingido
- **Então** dano ≈ 23 (fórmula 29 × 100 ÷ 124 com variação U(0,9; 1,1)); Goblin cai em 2 golpes

### CA3 — Alcance de armas (corpo a corpo vs. distância)
- **Dado** uma tropa com Espada (corpo a corpo) e um inimigo na retaguarda
- **Quando** o portador da espada ataca
- **Então** é rejeitado; só pode atacar alvos na frente (enquanto houver)

### CA4 — Lança pode atacar de qualquer linha
- **Dado** um Guerreiro com Lança na retaguarda
- **Quando** tenta atacar um inimigo na frente
- **Então** é permitido

### CA5 — Besta ignora 25% da defesa
- **Dado** um atacante com Besta e um alvo com DEF 100
- **Quando** calcula o dano
- **Então** usa Defesa_efetiva = 75, não 100

### CA6 — Limite de 30 rodadas com recuo
- **Dado** uma batalha que chega à rodada 30 sem eliminar nenhum lado
- **Quando** termina a rodada 30
- **Então** a tropa recua (resultado = derrota), sem recompensas

### CA7 — Semente reprodutível
- **Dado** uma batalha com semente X
- **Quando** re-executa-se com a mesma semente
- **Então** todos os sorteios (1d6, críticos, dano com U(0,9; 1,1)) produzem o mesmo log

## Tarefas

- [H-001 · Tarefa 001 — Cálculo de atributos de combate](h-001-tarefa-001-calculo-de-atributos-de-combate.md)
- [H-001 · Tarefa 002 — Motor de batalha](h-001-tarefa-002-motor-de-batalha.md)
- [H-001 · Tarefa 003 — Consequências para abatidos](h-001-tarefa-003-consequencias-para-abatidos.md)

## Fora de escopo

- Múltiplos alvos do Dragão (complexidade futura).
- Interfaccia de comando manual durante batalha (automática na v1).
- Replay visual em tempo real (resumido no relatório).
