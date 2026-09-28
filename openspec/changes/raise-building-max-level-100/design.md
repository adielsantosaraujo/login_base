# Design

## Context

Ver [proposal.md](proposal.md) — Why. Estado atual relevante:

- As regras vivem em enums do pacote `jogo/catalogo` (ADR 0016): `TipoPredio` (`custo(nivel)`,
  `tempoSegundos(nivel)`, `numeroCanteiros`, `nivelMaximoForjavel`, `capacidadeExercito`),
  `TipoRecurso.capacidadeArmazem(nivel)`, `Custo.paraNivel(nivel)` (BigDecimal `1,5^(N−1)` com
  `longValueExact`) e `ModeloItem` (`NIVEL_MAXIMO = 5`, atributos/custo/tempo lineares em L).
- A única configuração do jogo é `JogoProperties` (`app.jogo.velocidade`, `@Validated`, `@Min(1)`),
  injetada em `JogoMapper`, `ConstrucaoService`, `VilaService`, `ForjaService` e `QuartelService`; os
  testes de serviço (`@DataJpaTest`) e de controller (`@WebMvcTest`) importam `JogoProperties.class`
  diretamente, sem `JogoConfig`.
- Tempo: `tempoBase << (N−1)`; capacidade: `500L << (N−1)` (estoura o `long` perto de N55).
- `AplicadorOrdens.aplicarConstrucao` cria um canteiro a cada melhoria da FAZENDA;
  `FazendaService.plantar` valida `posicao ≤ nível da FAZENDA`; `ForjaService.forjar` valida
  `nivel ≤ nível da FORJA` sem usar `nivelMaximoForjavel`.
- `GeradorLoot` limita o nível do item por `min(ModeloItem.NIVEL_MAXIMO, N + d)`; subir o
  `NIVEL_MAXIMO` de item mudaria o loot da masmorra 5 sem querer.
- `CalculadoraProducao.produzirAte` multiplica `taxa × velocidade × dtMs × 1000` sem checagem.
- Há uma edição local não commitada (`TipoPredio.NIVEL_MAXIMO = 100`, literal 100 em
  `TipoRecurso.capacidadeArmazem` e constraint `0..100` editada em `V3__jogo.sql`); `V3` já foi
  aplicada em bancos existentes.

## Goals / Non-Goals

**Goals:**

- Níveis de prédio 1–100 sem overflow em nenhum cálculo, com níveis 1–5 bit a bit iguais aos atuais.
- Expoente `p` configurável, determinístico (BigDecimal) e validado na inicialização.
- Canteiros, nível forjável e nível de item coerentes entre regra, API, banco e frontend.
- Loot e masmorras inalterados.

**Non-Goals:**

- Novas masmorras, novos inimigos ou rebalanceamento do combate.
- Mudar produção (30/20/10 × N), capacidade do exército (3 × N), quantidade máxima de forja (5) ou de
  treino (15).
- Mudar as fórmulas de atributos, custo e tempo de forja de itens (apenas estendê-las até L23).
- Endurecer contra overflow outros produtos que não estouram na faixa válida (ex.: `producaoPorHora ×
  velocidade` no `JogoMapper`, `Custo.multiplicar` da forja).
- Paginar ou reduzir o catálogo.

## Decisions

### D1 — Curva de níveis como objeto de valor em JogoProperties

**Decisão:** criar o record puro `CurvaNiveis(BigDecimal expoente)` em `jogo/catalogo` e expô-lo por
`JogoProperties.curvaNiveis()` (método sem prefixo `get`, que o binder ignora), construído a partir da
nova propriedade `expoenteCurva` (`app.jogo.expoente-curva=${JOGO_EXPOENTE_CURVA:1.5}`). As funções do
catálogo que dependem de `p` passam a recebê-la explicitamente:
`TipoPredio.custo(int nivel, CurvaNiveis curva)`, `TipoPredio.capacidadeRecurso(int, CurvaNiveis)`,
`TipoRecurso.capacidadeArmazem(int, CurvaNiveis)`, `Custo.paraNivel(int, CurvaNiveis)`; as versões de um
argumento são removidas (o compilador aponta todos os chamadores). `CalculadoraProducao` passa a
receber `(int velocidade, CurvaNiveis curva)`. `CurvaNiveis.PADRAO` (p = 1,5) existe só para testes e
valores de referência. `MasmorraService` passa a injetar `JogoProperties` para calcular a capacidade.

**Alternativas:** (a) bean Spring `CurvaNiveis` em `JogoConfig` — exigiria registrar o bean em
`JogoTestConfig` e em todos os `@Import` de `@WebMvcTest`/`@DataJpaTest`; (b) holder estático
preenchido na inicialização — estado global mutável, testes frágeis; (c) serviço `RegrasProgressao`
envolvendo os enums — duplica a API do catálogo. A opção escolhida mantém o catálogo puro (ADR 0016)
e não muda nenhuma configuração de teste, porque `JogoProperties` já é importada em todas as fatias.

### D2 — Potência determinística com expoente múltiplo de 0,25

**Decisão:** `p` é restrito a múltiplos de 0,25 em [1,0; 2,0] (1,0 · 1,25 · 1,5 · 1,75 · 2,0).
`CurvaNiveis.fator(int nivel)` calcula `(N/5)^p` só com BigDecimal e `MathContext.DECIMAL128`:

```text
x = N / 5                      (exato: N × 0,2)
q = p × 4 (inteiro 4..8); k = q / 4; f = q % 4
r = x^k                        (BigDecimal.pow exato, k ∈ {1, 2})
s = sqrt(x, DECIMAL128); t = sqrt(s, DECIMAL128)
se f ≥ 2: r = r × s  (DECIMAL128)
se f ímpar: r = r × t (DECIMAL128)
```

`BigDecimal.sqrt` devolve o valor exato quando ele cabe na precisão (ex.: `fator(20)` com p = 1,5 é
exatamente 8), então os empates de `HALF_UP` são resolvidos sem erro de ponto flutuante. Custo acima
de 5: `round_half_up(base × 5,0625 × fator(N))` (5,0625 = 1,5^4 exato); capacidade:
`round_half_up(8000 × fator(N))`; ambos com `setScale(0, HALF_UP).longValueExact()`.

**Alternativas:** (a) `Math.pow`/`StrictMath.pow` em `double` — reprodutível com `StrictMath`, mas
arredondamentos de `N/5` e de `pow` podem virar empates `.5` para o lado errado; (b) `exp(p × ln x)`
implementado em BigDecimal por séries — aceita qualquer decimal, mas é código numérico próprio, mais
difícil de testar; (c) só múltiplos de 0,5 — apenas 3 valores (1,0 · 1,5 · 2,0), pouco para ajuste
fino. Múltiplos de 0,25 dão 5 curvas com duas raízes quadradas da biblioteca padrão.

### D3 — Validação do expoente na inicialização

**Decisão:** em `JogoProperties`: `@NotNull @DecimalMin("1.0") @DecimalMax("2.0") BigDecimal
expoenteCurva = new BigDecimal("1.5")` e um `@AssertTrue public boolean isCurvaNiveisValida()` que
constrói `CurvaNiveis` (o construtor compacto rejeita `null`, faixa e múltiplo de 0,25 com
`IllegalArgumentException`) e chama `curva.verificarLimites()`; qualquer exceção devolve `false`. Como
`JogoProperties` é `@Validated`, o contexto Spring falha na inicialização. `verificarLimites()` percorre
`TipoPredio.values()` × níveis 1..`TipoPredio.NIVEL_MAXIMO` calculando `custo(n, this)` e
`TipoRecurso.capacidadeArmazem(n, this)` × 1000 com `Math.multiplyExact` (lança `ArithmeticException`
se estourar). Na faixa válida os máximos são 303.750 (custo) e 3.200.000 unidades (capacidade), longe do
limite do `long`: a verificação é um guarda-corpo barato para mudanças futuras.

**Alternativas:** validar em `@PostConstruct` de um bean — mensagem de erro menos padronizada; validar
de forma preguiçosa no primeiro uso — o erro original (overflow no catálogo) reapareceria em runtime.

### D4 — Tempo linear acima do nível 5

**Decisão:** `tempoSegundos(N)` = `tempoBase << (N−1)` para N ≤ 5 e `ceil(tempoBase × 16 × N / 5)`
= `(tempoBase × 16L × N + 4) / 5` para N > 5, só com aritmética inteira. É contínuo em N5 (16 ×
tempoBase) e cresce 20% de N5 para N6. Não usa `p`. A velocidade continua sendo aplicada depois
(`ceil(tempo / velocidade)`) por `JogoMapper` e `ConstrucaoService`.

### D5 — Canteiros por faixa e criação condicional

**Decisão:** `TipoPredio.numeroCanteiros(N)` = N para N ≤ 5 e `5 + (N − 5) / 5` (divisão inteira)
para N > 5; nova constante `TipoPredio.NUMERO_MAXIMO_CANTEIROS = 24` (= `numeroCanteiros(100)`, garantido
por teste). `AplicadorOrdens.aplicarConstrucao` (FAZENDA) cria canteiros TRIGO nas posições
`existentes + 1 .. numeroCanteiros(novoNível)`, em laço (0 ou 1 por melhoria na prática, robusto a dados
inconsistentes), com `plantadoEm = concluiEm`. `FazendaService.plantar` valida
`1 ≤ posicao ≤ numeroCanteiros(nívelFazenda)` (nível 0 → 0 canteiros) com `CANTEIRO_INEXISTENTE`.

**Alternativa:** manter um canteiro por nível até 100 — rejeitada pelo usuário (tela e produção de
comida ficariam desproporcionais).

### D6 — Nível forjável por faixas e itens até o nível 23

**Decisão:** `TipoPredio.nivelMaximoForjavel(N)` = N (N ≤ 10); `10 + (N − 10) / 5` (11 ≤ N ≤ 50);
`18 + (N − 50) / 10` (51 ≤ N ≤ 100). `ModeloItem.NIVEL_MAXIMO` passa a 23 (= `nivelMaximoForjavel(100)`,
garantido por teste). `ForjaService.forjar` troca `nivel > nivelForja` por
`nivel > TipoPredio.FORJA.nivelMaximoForjavel(nivelForja)`. `ForjarRequest`/`TreinarRequest` já usam
`@Max(ModeloItem.NIVEL_MAXIMO)` e passam a aceitar 1–23 (24 → 400). Atributos, custo
(`custoBase × L × quantidade`) e tempo (`ceil(tempoBase × L × quantidade / velocidade)`) de item seguem
as fórmulas lineares atuais até L23 (ex.: ESPADA N23 ataque 50; 5 ARMADURA_FERRO N23 custam M1150 F4600
e 10350 s). `VilaDto` ganha `int nivelMaximoForjavel` (logo após `capacidadeExercito`; 0 com FORJA no
nível 0) para o frontend não reimplementar a regra.

**Suposição registrada (interpretação da regra do usuário):** "até 10: N; de 11 a 50: +1 a cada 5
níveis; de 51 a 100: +1 a cada 10 níveis" foi lido como degraus que contam a partir do início de cada
faixa: N11–N14 → 10, N15 → 11, N20 → 12, N50 → 18, N51–N59 → 18, N60 → 19, N100 → 23. Se a intenção
for outra (ex.: N11 já forjar 11), só mudam `nivelMaximoForjavel`, `ModeloItem.NIVEL_MAXIMO`, a
constraint de item e os cenários.

### D7 — Loot e masmorras continuam limitados a 5

**Decisão:** `GeradorLoot` passa a usar uma constante própria `NIVEL_MAXIMO_ITEM_LOOT = 5` em
`min(NIVEL_MAXIMO_ITEM_LOOT, N + d)`, desacoplada de `ModeloItem.NIVEL_MAXIMO`, preservando o
comportamento atual e a spec `game-dungeon-loot` (sem delta). `CatalogoMasmorras.NIVEL_MAXIMO` e
`ck_jogo_vilas_masmorra_nivel` (1–5) não mudam.

**Alternativa:** deixar o loot acompanhar o novo máximo — mudaria a spec de loot e o balanceamento sem
pedido do usuário.

### D8 — Produção sem overflow com saturação

**Decisão:** em `produzirAte`, calcular `taxa × velocidade × dtMs × 1000` com `Math.multiplyExact`; se
lançar `ArithmeticException`, o recurso vai direto para `capacidade`. Depois,
`novo = ganho >= capacidade − atual ? capacidade : atual + ganho` (evita overflow na soma). A divisão
por `3_600_000` continua após a multiplicação (mesmo arredondamento para baixo de hoje, sem mudar
resultados na faixa normal).

**Alternativa:** `BigInteger`/`BigDecimal` no laço quente de sincronização — mais caro e mais verboso;
reordenar a divisão mudaria o arredondamento dos testes atuais.

### D9 — Migração V5 e reversão do V3

**Decisão:** reverter a edição local de `V3__jogo.sql` (voltar ao conteúdo do commit, `0..5`) e criar
`V5__niveis_estendidos.sql`:

```sql
alter table jogo_predios drop constraint ck_jogo_predios_nivel;
alter table jogo_predios add constraint ck_jogo_predios_nivel check (nivel between 0 and 100);
alter table jogo_canteiros drop constraint ck_jogo_canteiros_posicao;
alter table jogo_canteiros add constraint ck_jogo_canteiros_posicao check (posicao between 1 and 24);
alter table jogo_itens drop constraint ck_jogo_itens_nivel;
alter table jogo_itens add constraint ck_jogo_itens_nivel check (nivel between 1 and 23);
```

Nomes de constraint mantidos; nenhum dado precisa ser migrado (as faixas só se ampliam).

**Alternativa:** manter a edição no V3 e rodar `flyway repair` — reescreve histórico já aplicado; o
usuário decidiu não usar repair.

### D10 — Frontend guiado por nivelMaximoForjavel e inventário

**Decisão:** `ForjaView` usa `vila.nivelMaximoForjavel` como `:max` do nível e no bloqueio/aviso
(em vez de `:max="5"` e do nível da forja); `QuartelView` troca `NIVEIS = [1..5]` por níveis derivados
do inventário (níveis distintos com ≥ 1 item `DISPONIVEL` do modelo, em ordem crescente), evitando
listas de 23 opções vazias. `tipos.ts` ganha `nivelMaximoForjavel: number` em `VilaDto`.

### D11 — Catálogo com os 100 níveis

**Decisão:** `GET /api/jogo/catalogo` continua listando todos os níveis de todos os prédios (8 × 100 =
800 entradas `ProximoNivelDto`); o frontend já guarda o catálogo em cache (`useVila`). **Alternativa:**
limitar/paginar — mudaria o contrato sem necessidade.

## Tabela de valores de referência

Expoente p = 1,5, velocidade 1. Custos em unidades; "Nível forjável" = nível máximo de item com a FORJA
nesse nível.

| N | Custo CENTRO_VILA (M = P) | Custo FORJA (M / P / F) | Tempo CENTRO_VILA (s) | Tempo ARMAZEM (s) | Capacidade do armazém | Canteiros | Nível forjável |
|---|---|---|---|---|---|---|---|
| 1 | 150 | 120 / 100 / 40 | 120 | 60 | 500 | 1 | 1 |
| 5 | 759 | 608 / 506 / 203 | 1920 | 960 | 8000 | 5 | 5 |
| 6 | 998 | 799 / 665 / 266 | 2304 | 1152 | 10516 | 5 | 6 |
| 10 | 2148 | 1718 / 1432 / 573 | 3840 | 1920 | 22627 | 6 | 10 |
| 25 | 8490 | 6792 / 5660 / 2264 | 9600 | 4800 | 89443 | 9 | 13 |
| 50 | 24014 | 19211 / 16009 / 6404 | 19200 | 9600 | 252982 | 14 | 18 |
| 100 | 67921 | 54336 / 45280 / 18112 | 38400 | 19200 | 715542 | 24 | 23 |

Outros pontos úteis: SERRARIA N6 = M399 P266; ARMAZEM N6 = M665 P399, N10 = M1432 P859, N100 = M45280
P27168; nível forjável N11 = 10, N14 = 10, N15 = 11, N20 = 12, N51 = 18, N59 = 18, N60 = 19, N99 = 22;
canteiros N9 = 5, N14 = 6, N15 = 7, N99 = 23.

Sensibilidade ao expoente (CENTRO_VILA, armazém):

| p | Capacidade N6 | Capacidade N10 | Capacidade N100 | Custo CENTRO N10 | Custo CENTRO N100 |
|---|---|---|---|---|---|
| 1,0 | 9600 | 16000 | 160000 | 1519 | 15188 |
| 1,25 | 10048 | — | 338359 | 1806 | 32118 |
| 1,5 | 10516 | 22627 | 715542 | 2148 | 67921 |
| 1,75 | 11007 | — | 1513187 | 2554 | 143635 |
| 2,0 | 11520 | 32000 | 3200000 | 3038 | 303750 |

`fator(N)` exato/aproximado (DECIMAL128): p = 1,5 → `fator(10)` = 2.828427124746190097603377448419396,
`fator(6)` = 1.314534138012398672296727478721925, `fator(20)` = 8, `fator(100)` =
89.44271909999158785636694674925104; p = 1,25 → `fator(80)` = 32; p = 1,75 → `fator(80)` = 128;
p = 2,0 → `fator(10)` = 4; p = 1,0 → `fator(10)` = 2. Custo CENTRO N10 com p = 2,0 é o empate
3037,5 → 3038 (HALF_UP).

Viabilidade: como custo e capacidade usam o mesmo `(N/5)^p`, a capacidade do armazém no nível N−1
continua pelo menos ~6,9× o maior custo de um recurso para o nível N (p = 2,0, N = 6); nenhum nível fica
inalcançável por falta de armazém.

## Risks / Trade-offs

- [Itens N6–N23 muito fortes contra masmorras 1–5 (dano mínimo 1 dos inimigos)] → aceito nesta change;
  registrado em Open Questions e em `docs/17` como risco de balanceamento para uma change futura de
  masmorras.
- [Expoente mal configurado derruba a aplicação] → falha rápida e explícita na inicialização com
  mensagem de Bean Validation; faixa documentada em `.env.example` e ADR 0025.
- [Payload do catálogo ~20× maior] → aceitável (cache no cliente, uma chamada por sessão).
- [Checksum do V3 divergente em bancos que aplicaram a edição local] → o usuário limpa o banco de
  desenvolvimento (ver Migration Plan); nenhum `flyway repair`.
- [`BigDecimal.sqrt` com erro de 1 ulp na 34ª casa] → irrelevante para o arredondamento a inteiro; casos
  exatos (empates) são exatos por construção.
- [Contrato da API muda (catálogo, `VilaDto`, faixas de nível)] → frontend ajustado na mesma change.

## Migration Plan

1. Reverter `src/main/resources/db/migration/V3__jogo.sql` ao conteúdo do `HEAD`.
2. Adicionar `V5__niveis_estendidos.sql` (D9).
3. **Nota operacional — banco de desenvolvimento:** se o banco local já aplicou o `V3` editado, o
   Flyway acusará checksum divergente. **Não** rodar `flyway repair`: o usuário limpará o banco de
   desenvolvimento por conta própria (o mesmo Postgres do compose é usado pelos testes, ADR 0022) antes
   de rodar a aplicação ou `./mvnw test`. Subagentes que encontrarem esse erro devem parar e avisar o
   orquestrador.
4. Deploy: nenhuma ação extra; V5 só amplia faixas. Rollback: reverter o código e remover a V5 num
   banco descartável (não há produção com dados a preservar).

## Open Questions

(dúvidas de game design que não mudam specs nem tasks; as opções recomendadas já estão aplicadas)

- Atributos de itens 6–23 seguem a extrapolação linear atual? (recomendado: sim)
- Masmorras e loot continuam limitados a 5 até uma change própria de conteúdo? (recomendado: sim)
- A interpretação das faixas da forja (D6) está correta?
- Múltiplos de 0,25 no expoente bastam para o ajuste fino? (recomendado: sim)
- O quartel deve listar só os níveis com itens em estoque? (recomendado: sim)
