# H-001 · Tarefa 002 — Geração do mapa por semente

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend · **Spec:** [../specs/jogo-criacao-vila/spec.md#requirement-geração-do-mapa-por-semente](../specs/jogo-criacao-vila/spec.md#requirement-geração-do-mapa-por-semente)

## Objetivo

Portar o algoritmo de geração do mapa (tipos de região + 3 bônus cada) do JavaScript para Java, implementando `GeradorMapaService` que segue as regras R8–R16 do handoff e passa por ~1000 sementes de teste. As jazidas continuam sendo geradas pelo `GeradorJazidaService` existente.

## Contexto necessário

- [../vila.md](../vila.md) — R8–R10, e nova regra bônus da vila
  > R9: semente gera os 5 tipos + 3 bônus de cada região, determinísticos.
  > R10: prévia regenerável no servidor sem limite.

- [/docs/designe/handoff/referencia/geracao-mapa.js](/docs/designe/handoff/referencia/geracao-mapa.js) — algoritmo JS a portar para Java
  > Ordem de consumo do RNG: amostragem por rejeição (5 inteiros 2..4), layout, embaralhamento Fisher–Yates, sorteios de bônus por faixa.

- [/docs/designe/handoff/regras/regras-regioes-v2.md](/docs/designe/handoff/regras/regras-regioes-v2.md) — R8–R16 (tipos, bônus, faixas, distribuição)
  > Exemplo 2: região 06 Urbana, 07 Litoral, 10 Planície (vizinhas).

## Backend

**Enums (novos):**
- [/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java) — `FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA`; cada um com `List<BonusRegiao> bonus()`.
- [/src/main/java/com/example/loginbase/jogo/modelo/BonusRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/BonusRegiao.java) — 13 valores: `FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO`.
- [/src/main/java/com/example/loginbase/jogo/modelo/FaixaBonusRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/FaixaBonusRegiao.java) — `POSICAO_1 (35–50), POSICAO_2 (16–34), POSICAO_3 (5–15)`.

**Records (novos):**
- `record RegiaoGerada(int indice, TipoRegiao tipo, List<BonusGerado> bonus)`.
- `record BonusGerado(BonusRegiao bonus, int posicao, int valor)`.

**Serviço (novo):**
- [/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java) — `@Service`, sem banco.
  - `List<RegiaoGerada> gerar(long semente)` — retorna 16 regiões com tipos e bônus gerados da semente.
  - Implementação: **portar do JS** (`geracao-mapa.js`).
    - Amostragem por rejeição: 5 inteiros 2..4 (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA); soma = 16, ≤ 2 valores = 4.
    - Layout: lista de tipos repetidos, embaralhada (Fisher–Yates).
    - Para cada região (índice 1..16): embaralha a lista de 3 bônus do tipo, sorteia valor de cada posição pela faixa.
  - `boolean conectadas(List<Integer> indices)` — BFS ortogonal usando `GradeRegioes.adjacente`; valida que os 3 índices formam grafo ligado.
  - `Map<BonusRegiao, Integer> somarBonus(List<RegiaoGerada> mapa, Collection<Integer> indices)` — soma os 3 bônus das regiões em `indices`.

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
- [/src/main/java/com/example/loginbase/jogo/modelo/BonusRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/BonusRegiao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/FaixaBonusRegiao.java](/src/main/java/com/example/loginbase/jogo/modelo/FaixaBonusRegiao.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java](/src/main/java/com/example/loginbase/jogo/modelo/GradeRegioes.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java](/src/main/java/com/example/loginbase/jogo/servico/GeradorMapaService.java) (novo)

## Testes

~1000 sementes (determinismo, distribuição, exemplo 2):
- **R8:** 5 quantidades por tipo (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA) entre 2–4; soma = 16.
- **R9:** soma = 16 ✓.
- **R10:** ≤ 2 valores iguais a 4 ✓.
- **R13:** cada região tem 3 bônus distintos do tipo ✓.
- **R14/R15:** valores em faixas corretas por posição ✓.
- **R16:** ao longo das sementes, cada bônus do tipo aparece em cada posição ✓.
- **Determinismo:** `gerar(semente)` duas vezes → mesma lista.
- **Ambas as distribuições:** ao longo das sementes, ocorrem as duas famílias (4,4,3,3,2 e 4,3,3,3,3).
- **Existe seleção válida:** ao longo das sementes, sempre há ≥1 Urbana e alguma seleção conexa de 3.
- **`conectadas` e `somarBonus`:** Exemplo 2 do handoff (região 6 Urbana, 7 Litoral, 10 Planície).

## Definição de pronto

- Build (`./mvnw verify`) sem erros.
- ~1000 sementes testadas (determinismo, distribuição, requisitos).
- `GeradorMapaService` com paridade ao JS.

## Fora de escopo

- Persistência do mapa (task 003).
- Geração de jazidas (continua em `GeradorJazidaService`, da tarefa anterior).
