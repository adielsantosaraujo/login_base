# H-001 · Tarefa 001 — Cálculo de atributos de combate

**História:** [H-001 — Resolver batalha por rodadas](h-001-resolver-batalha-por-rodadas.md) · **Domínio:** [batalha.md](../batalha.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar serviço puro que calcula PV máximo, Ataque, Defesa, Iniciativa e Crítico de guerreiros e inimigos, com testes que reproduzem o exemplo 10.4.

## Contexto necessário

- Fórmulas de atributos de combate (seção 10.1)
  > - **PV máx.** = `30 + 5 × VIT + 3 × G + Σ VIDA`
  > - **Ataque** = `[ATQarma(L) × (1 + 0,05 × atributo-chave) + 2 × G] × (1 + Σ ATK%)`
  > - **Defesa** = `[Σ DEFpeça(L) + VIT + G + 2 (se espada)] × (1 + Σ DEF%)`
  > - **Iniciativa** = `2 × VEL + G + mod. arma + Σ INI`; +1d6 a cada rodada
  > - **Crítico** = `5% + 0,5% × VEL + Σ CRIT` (pp)

- PE efetivo de Guerreiro (seção 5.2)
  > PE base + Σ floor(característica ÷ 5) das ligadas (FOR, VIT, VEL) + bônus de ferramenta + bônus PROF de itens

- Bônus de itens (6.2, 7.2, 7.10, 7.11)
  > Armas: FOR, VEL, INT, ATK, CRIT, INI
  > Armaduras: VIT, DEF, VIDA, VEL
  > Joias: VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD

- Exemplo 10.4 (valores esperados)
  > Guerreiro: FOR 6, VIT 5, VEL 5, PE Guerreiro 5, Espada L1, sem armadura
  > Esperado: Ataque 29, Defesa 15, PV 79, Iniciativa base 10

## Backend

**Serviço puro (testável)**: `CombateAtributosService`

Métodos:
- `calcularAtributosGuerreiro(guerreiro: Cidadao, arma: Item, armaduras: List<Item>, joias: List<Item>) → AtributosCombate`
  - Retorna: struct {pv_max, ataque, defesa, iniciativa_base, critico_chance_pp}
  - Calcula PE efetivo de Guerreiro (soma bônus de FOR/VIT/VEL)
  - Acumula bônus de itens por tipo (ATK%, DEF%, VIDA, INI, CRIT, VIT)
  - Aplica fórmulas exatas (seção 10.1)

- `calcularAtributosInimigo(inimigo: TipoInimigo, nivel_masmorra: int) → AtributosInimigo`
  - Aplica multiplicador M(N) = 1 + 0,15 × (N − 1) aos atributos base
  - Retorna PV, ATQ, DEF, INI

- `calcularDano(ataque: double, defesa_efetiva: double, rng: Random) → int`
  - Fórmula: max(1; round(ataque × 100 ÷ (100 + 3 × defesa_efetiva) × U(0,9; 1,1)))
  - U(0,9; 1,1) = valor aleatório uniforme em [0,9; 1,1)

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/batalha/CombateAtributosService.java](/src/main/java/com/example/loginbase/jogo/batalha/CombateAtributosService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/AtributosCombate.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/AtributosCombate.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/dto/AtributosInimigo.java](/src/main/java/com/example/loginbase/jogo/batalha/dto/AtributosInimigo.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/batalha/CombateAtributosServiceTest.java](/src/test/java/com/example/loginbase/jogo/batalha/CombateAtributosServiceTest.java) (novo)

## Testes

1. **Teste do exemplo 10.4**: guerreiro com FOR 6, VIT 5, VEL 5, PE 5, Espada L1, sem armadura
   - Esperado: Ataque 29, Defesa 15, PV 79, Iniciativa base 10
   
2. **Teste com armadura**: adicionar Peitoral L1 (DEF 8) ao guerreiro acima
   - Esperado: Defesa = 8 + 5 + 8 + 2 = 23

3. **Teste com bônus de item**: Espada com bônus +3% ATK
   - Esperado: Ataque = (10 × 1,30 + 16) × 1,03 ≈ 30

4. **Cálculo de dano**: Ataque 29, DEF 8, sem modificadores
   - Esperado: 29 × 100 ÷ (100 + 3 × 8) ≈ 23 com margem U(0,9; 1,1)

5. **Multiplicador de inimigo**: Goblin (base 40 PV) em N5
   - Esperado: 40 × 1,6 = 64 PV

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2, CA5 (parcialmente)
- Build do backend (`./mvnw verify`) sem erros
- Todos os testes listados passando, incluindo reprodução exata do exemplo 10.4
- Bônus [proposta] aplicados via catálogo centralizado (jogo.catalogo)

## Fora de escopo

- Integração com banco de dados (DTOs apenas)
- Persistência de atributos calculados
- Replay visual
