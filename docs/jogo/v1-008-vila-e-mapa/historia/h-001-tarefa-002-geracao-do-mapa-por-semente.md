# H-001 · Tarefa 002 — Geração do mapa por semente

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend · **Spec:** [../specs/jogo-criacao-vila/spec.md#requirement-geração-do-mapa-por-semente](../specs/jogo-criacao-vila/spec.md#requirement-geração-do-mapa-por-semente)

## Objetivo

Portar o algoritmo de geração do mapa (tipos de região + 3 terrenos com percentuais de cada + 100 ladrilhos por região com terreno e bônus) do JavaScript para Java, implementando `GeradorMapaService` que segue as regras R8–R16 de [../regioes.md](../regioes.md) e passa por ~1000 sementes de teste.

## Contexto necessário

- [../vila.md](../vila.md) — R8–R10, e nova regra percentuais de terreno
  > R9: semente gera os 5 tipos, a ordem e os percentuais dos 3 terrenos de cada região, determinísticos.
  > R10: prévia regenerável no servidor sem limite.

- [/docs/designe/handoff/referencia/geracao-mapa.js](/docs/designe/handoff/referencia/geracao-mapa.js) — algoritmo JS a portar para Java
  > Ordem de consumo do RNG: amostragem por rejeição (5 inteiros 2..4), layout, embaralhamento Fisher–Yates, sorteios de bônus por faixa.

- [/docs/designe/handoff/regras/regras-regioes-v2.md](/docs/designe/handoff/regras/regras-regioes-v2.md) — R8–R16 (tipos, bônus, faixas, distribuição)
  > Exemplo 2: região 06 Urbana, 07 Litoral, 10 Planície (vizinhas).

## Backend

**Enums (novos):**
- [/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java) — `FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA`; cada um com `List<TipoTerreno> terrenos()`.
- [/src/main/java/com/example/loginbase/jogo/modelo/TipoTerreno.java](/src/main/java/com/example/loginbase/jogo/modelo/TipoTerreno.java) — 13 valores: `FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO`.

**Records (novos):**
- `record RegiaoGerada(int indice, TipoRegiao tipo, List<TerrenoGerado> terrenos)`.
- `record TerrenoGerado(TipoTerreno terreno, int posicao, int percentual)`.

**Serviço (novo):**
- [/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java) — `@Service`, sem banco.
  - `List<RegiaoGerada> gerar(long semente)` — retorna 16 regiões com tipos e 3 percentuais de terreno gerados da semente.
  - Implementação: **portar do JS** (`geracao-mapa.js`).
    - Amostragem por rejeição: 5 inteiros 2..4 (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA); soma = 16, ≤ 2 valores = 4.
    - Layout: lista de tipos repetidos, embaralhada (Fisher–Yates).
    - Para cada região (índice 1..16): embaralha a lista de 3 terrenos do tipo, sorteia percentual de cada posição (1º: 20–60, 2º: 20–(90−b1), 3º: 100−(b1+b2)).
  - `boolean conectadas(List<Integer> indices)` — BFS ortogonal usando `GradeRegioes.adjacente`; valida que os 3 índices formam grafo ligado.
  - `Map<TipoTerreno, Integer> totalLadrilhosPorTerreno(List<RegiaoGerada> mapa, Collection<Integer> indices)` — total de ladrilhos por terreno nas regiões escolhidas (para o painel; não é bônus).

**Classe auxiliar (nova):**
- [/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java](/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java) — constantes e métodos para a grade 4×4.
  - `static int linha(int indice)` → `(indice - 1) / 4`.
  - `static int coluna(int indice)` → `(indice - 1) % 4`.
  - `static List<Integer> adjacentes(int indice)` — retorna 2–4 vizinhos ortogonais.
  - `static boolean adjacente(int a, int b)` — true se vizinhos.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/TipoTerreno.java](/src/main/java/com/example/loginbase/jogo/modelo/TipoTerreno.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java](/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java) (novo)

## Testes

~1000 sementes (determinismo, distribuição, exemplo 2):
- **R8:** 5 quantidades por tipo (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA) entre 2–4; soma = 16.
- **R9:** soma = 16 ✓.
- **R10:** ≤ 2 valores iguais a 4 ✓.
- **R13:** cada região tem 3 terrenos distintos do tipo ✓.
- **R14/R15:** percentuais em faixas corretas por posição (1º: 20–60, 2º: 20–(90−b1), 3º: 10–60) ✓.
- **R16:** ao longo das sementes, cada terreno do tipo aparece em cada posição ✓.
- **Ladrilhos:** contagem exata por terreno = percentual; bonus_adjacente ∈ {0, 25, 50, 75, 100} e coerente com os vizinhos; bonus_total ≤ 200.
- **Determinismo:** `gerar(semente)` duas vezes → mesma lista, ladrilhos inclusive.
- **Ambas as distribuições:** ao longo das sementes, ocorrem as duas famílias (4,4,3,3,2 e 4,3,3,3,3).
- **Existe seleção válida:** ao longo das sementes, sempre há ≥1 Urbana e alguma seleção conexa de 3.
- **`conectadas` e `totalLadrilhosPorTerreno`:** Exemplo 2 do handoff (região 6 Urbana, 7 Litoral, 10 Planície).

## Definição de pronto

- Build (`./mvnw verify`) sem erros.
- ~1000 sementes testadas (determinismo, distribuição, requisitos).
- `GeradorMapaService` com paridade ao JS.

## Fora de escopo

- Persistência do mapa (task 003).
