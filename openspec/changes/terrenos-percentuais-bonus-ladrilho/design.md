# Design

## Context

- Regras de negócio decididas pelo usuário (fonte da verdade): percentuais de terreno no lugar dos bônus de região; sorteio b1 20–60, b2 20–(90 − b1), b3 = 100 − b1 − b2; ladrilhos com terreno na quantidade do percentual (inclusive Urbana); `bonus_base` 0–100, `bonus_adjacente` +25 por vizinho ortogonal igual, `bonus_total` = base + adjacente; bônus do prédio = `bonus_total` da âncora se o terreno bate; Comércio/Desenvolvimento/Militar = média das âncoras do grupo no terreno certo; Quartel na Urbana e no Litoral; fim da soma da vila; endereço (A,1), sigla de 2 letras e cor por terreno; banco limpo; casas iniciais nos 4 primeiros ladrilhos De; Comércio não altera o preço do Mercado.
- Docs de domínio já atualizados: `/docs/jogo/v1-008-vila-e-mapa/regioes.md`, `/docs/jogo/v1-008-vila-e-mapa/vila.md`, `/docs/jogo/v1-010-recursos-e-producao/producao.md`, `/docs/jogo/v1-003-construcoes/*.md`.
- Código atual (resultado da change `redesenho-criacao-vila-populacao`, não arquivada):
  - `GeradorMapaService.gerar(semente)` com `java.util.Random(semente)`: quantidades por tipo (rejeição), layout embaralhado e, por região, ordem dos 3 bônus embaralhada e valor por `FaixaBonusRegiao`. `RegiaoGerada(indice, tipo, List<BonusGerado>)`, `BonusGerado(bonus, posicao, valor)`, `conectadas`, `somarBonus`.
  - `GeradorJazidaService`: 100 jazidas por quantidades fixas, `Collections.shuffle(lista, new Random(misturar(semente, indice)))` (splitmix64); só gravadas para regiões possuídas ≠ Urbana (VilaService, AnexacaoService); MapaService gera na hora se faltar.
  - V17: `regiao_bonus` (CHECK das faixas antigas) e `vila_previa`. V3: `ladrilho_jazida` (PK composta).
  - `BonusRegiaoService`: soma por bônus das regiões possuídas e `fator = 1 + soma/100`; usado por ProducaoService, OuroService, ObraService, TreinamentoQuartelService, VilaService, MapaService, AnexacaoService.
  - `ConstrucaoCatalogo`: `Entrada(tipo, nome, bonusRegiao, profissoes, custoN1, poN1, jazidaAssociada)`; `regioesPermitidas` deriva do bônus (sem bônus → só Urbana).
  - Casas iniciais em (0,0), (2,0), (4,0), (6,0) da 1ª Urbana da seleção.
  - Frontend: `domain/regioes.ts` (BONUS_POR_TIPO, totaisBonus), `components/criacao/*` (BonusLista), `views/Mapa.vue` ("Bônus total da vila"), `components/GradeRegiao.vue` (símbolo = 1ª letra), `composables/useMapa.ts` (ROTULOS_JAZIDA), `useMarcacoes.ts` (JAZIDA_POR_TIPO).
- Flyway até V17 (próxima livre V18); `ddl-auto=validate`; ids bigint identity; testes de integração com Postgres real, um banco por subagente (`DB_NAME`).

## Goals / Non-Goals

**Goals:**
- Implementar as regras 1 a 12 no backend e no frontend.
- Trocar o armazenamento (V18) e os contratos da API de bônus → terrenos.
- Remover todo o código de jazidas e da soma de bônus.
- Permitir execução por subagentes Sonnet em paralelo, com compilação verde ao fim de cada task.

**Non-Goals:**
- Converter dados antigos (banco limpo).
- Teto de bônus (o fator de um prédio vai até 3,0).
- Mudar as quantidades por tipo de região, a grade 4×4, a regra de seleção inicial, o custo de anexação ou a prévia (`vila_previa`).
- Exibir na prévia os ladrilhos das regiões (a prévia mostra só a composição).
- Bônus para prédios sem terreno (Armazém, Ferraria, Alfaiataria, Carpintaria).
- Mudar o preço de venda do Mercado.

## Decisions

### D1. Enum TipoTerreno

- **Renomear** `BonusRegiao` para `TipoTerreno` (mesmo pacote `jogo.modelo`, mesmos 13 valores e mesma ordem, mesmo `getNomeExibicao()`): `FLORESTA`, `BARREIRO`, `PLANTACOES`, `CRIACOES`, `ROCHA`, `FERRO`, `CARVAO`, `SALINAS`, `ENXOFRE`, `MILITAR`, `INDUSTRIA`, `COMERCIO`, `DESENVOLVIMENTO`.
- `TipoRegiao.bonus()` passa a `TipoRegiao.terrenos()` (mesma tabela).
- Por que renomear e não criar um enum novo: os valores e nomes são idênticos (as strings gravadas no banco continuam as mesmas) e manter dois enums equivalentes geraria conversões inúteis. A renomeação é mecânica (substituição de `\bBonusRegiao\b`, que não atinge `BonusRegiaoService`, `RegiaoBonus*`, `FaixaBonusRegiao` nem `bonusRegiao` minúsculo) e roda sozinha na onda 1.
- A sigla de 2 letras é só de exibição: fica no frontend (D14), não no enum Java.

### D2. Sorteio dos percentuais

- `GeradorMapaService` mantém o algoritmo e a ordem de consumo do `Random(semente)` (quantidades → layout → por região, na ordem dos índices 1..16) e troca só o sorteio dos valores. Por região:
  1. `ordem = new ArrayList<>(tipo.terrenos()); embaralhar(rng, ordem)` (Fisher–Yates atual);
  2. `b1 = inteiro(rng, 20, 60)`;
  3. `b2 = inteiro(rng, 20, 90 - b1)`;
  4. `b3 = 100 - b1 - b2` (não consome RNG; sempre entre 10 e 60).
- Records: `TerrenoGerado(TipoTerreno terreno, int posicao, int percentual)` e `RegiaoGerada(int indice, TipoRegiao tipo, List<TerrenoGerado> terrenos)` (ordenados por posição 1..3). Constantes: `PERCENTUAL_1_MIN = 20`, `PERCENTUAL_1_MAX = 60`, `PERCENTUAL_2_MIN = 20`, `SOMA_1_2_MAX = 90`, `TOTAL_PERCENTUAL = 100`.
- `somarBonus` é removido; `FaixaBonusRegiao` é apagado. `conectadas` continua.
- Mesma semente gera os mesmos tipos de antes, mas percentuais diferentes dos valores antigos (sem compatibilidade, banco limpo).
- Testes (sementes 0..999): 3 terrenos distintos do tipo; b1 ∈ [20,60]; b2 ∈ [20, 90 − b1]; b3 = 100 − b1 − b2 e ≥ 10; soma 100; b1 atinge 20 e 60; b3 atinge 10; cada terreno aparece em cada posição ao longo das sementes; determinismo.

### D3. Geração dos ladrilhos

- `GeradorLadrilhoService` (novo, `jogo.servico`, `@Service` puro, sem banco) substitui `GeradorJazidaService`. Constantes `LADO = 10`, `TOTAL_LADRILHOS = 100`.
- Entrada: `record PercentualTerreno(TipoTerreno terreno, int percentual)` em lista na ordem de posição (1º, 2º, 3º). Valida: 3 itens, terrenos distintos, soma 100 (`IllegalArgumentException` se não).
- Saída: `record LadrilhoGerado(int x, int y, TipoTerreno terreno, int bonusBase, int bonusAdjacente)` com `int bonusTotal()`; lista de 100 na ordem de varredura (y = 0..9; dentro da linha, x = 0..9).
- Algoritmo `List<LadrilhoGerado> gerar(long semente, int indiceRegiao, List<PercentualTerreno> percentuais)`:
  1. `Random rng = new Random(misturar(semente, indiceRegiao))` — `misturar` é o splitmix64 copiado de `GeradorJazidaService` (sementes próximas → grades bem distintas);
  2. `lista` = cada terreno repetido `percentual` vezes, na ordem de posição;
  3. `Collections.shuffle(lista, rng)`;
  4. terreno de (x, y) = `lista.get(y * 10 + x)`;
  5. `bonusBase` de cada ladrilho, na ordem de varredura: `rng.nextInt(101)` (mesmo `rng`, depois do shuffle);
  6. `bonusAdjacente` = 25 × número de vizinhos ortogonais (x±1, y) e (x, y±1) **dentro de 0..9** com o mesmo terreno.
- Valida o índice 1..16 como hoje. Determinístico: mesma entrada → mesma lista.

### D4. bonus_adjacente gravado

- **Decisão: gravar `bonus_base` e `bonus_adjacente` na tabela `ladrilho`; `bonus_total` não é gravado (calculado: `getBonusTotal()` na entidade e campo no DTO).**
- Justificativa: o terreno dos ladrilhos é imutável depois de gerado, então o `bonus_adjacente` nunca fica desatualizado; gravá-lo faz o bônus de um prédio ser uma leitura de **uma linha** (`findByRegiaoIdAndXAndY`) no turno, sem carregar os 100 ladrilhos da região para contar vizinhos; o CHECK do banco garante o domínio (0, 25, 50, 75, 100); e bate com o modelo de dados de `regioes.md`/`vila.md`. Gravar também o `bonus_total` seria redundante (derivado trivial, risco de divergência).
- O cálculo é feito uma única vez, no gerador puro (D3), e testado lá.

### D5. Migration V18 e entidades

Conteúdo **final** de `/src/main/resources/db/migration/V18__terrenos_e_ladrilhos.sql`:

```sql
-- V18: percentuais de tipos de terreno por região e ladrilhos com terreno e bônus.
-- Banco limpo: os dados antigos (regiao_bonus, ladrilho_jazida) não são convertidos.
drop table regiao_bonus;
drop table ladrilho_jazida;

create table regiao_terreno (
    id         bigint generated always as identity,
    regiao_id  bigint      not null,
    terreno    varchar(20) not null,
    posicao    smallint    not null,
    percentual smallint    not null,
    constraint pk_regiao_terreno primary key (id),
    constraint fk_regiao_terreno_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_regiao_terreno_terreno unique (regiao_id, terreno),
    constraint uk_regiao_terreno_posicao unique (regiao_id, posicao),
    constraint ck_regiao_terreno_posicao check (posicao between 1 and 3),
    constraint ck_regiao_terreno_percentual check ((posicao = 1 and percentual between 20 and 60)
        or (posicao = 2 and percentual between 20 and 70) or (posicao = 3 and percentual between 10 and 60)),
    constraint ck_regiao_terreno_terreno check (terreno in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES',
        'ROCHA', 'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'))
);

create table ladrilho (
    id              bigint generated always as identity,
    regiao_id       bigint      not null,
    x               smallint    not null,
    y               smallint    not null,
    terreno         varchar(20) not null,
    bonus_base      smallint    not null,
    bonus_adjacente smallint    not null,
    constraint pk_ladrilho primary key (id),
    constraint fk_ladrilho_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_ladrilho_posicao unique (regiao_id, x, y),
    constraint ck_ladrilho_x check (x between 0 and 9),
    constraint ck_ladrilho_y check (y between 0 and 9),
    constraint ck_ladrilho_terreno check (terreno in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES',
        'ROCHA', 'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO')),
    constraint ck_ladrilho_bonus_base check (bonus_base between 0 and 100),
    constraint ck_ladrilho_bonus_adjacente check (bonus_adjacente in (0, 25, 50, 75, 100))
);
```

- O índice único `uk_ladrilho_posicao (regiao_id, x, y)` já serve às consultas por região; não há índice extra.
- A soma 100 por região e o limite b2 ≤ 90 − b1 não cabem em CHECK de linha: são garantidos pelo gerador (D2) e testados.
- **Escrita em duas etapas (D18):** a task 2.1 cria a V18 **sem** as duas linhas `drop table` (as tabelas antigas continuam enquanto o código legado existe); a task 6.1 acrescenta os `drop table` no topo, junto com a remoção das entidades antigas. Editar a V18 é seguro porque ela só existe nesta branch e todo banco de teste é recriado; o banco do desenvolvedor deve ser recriado ao fim da change (já exigido: banco limpo).
- Entidades (`jogo.modelo`):
  - `RegiaoTerreno`: `Long id` (identity), `long regiaoId`, `TipoTerreno terreno` (`@Enumerated(STRING)`, length 20), `int posicao`, `int percentual` (os dois com `@JdbcTypeCode(SqlTypes.SMALLINT)`, como `RegiaoBonus.posicao` hoje). Construtor `(regiaoId, terreno, posicao, percentual)`.
  - `Ladrilho`: `Long id` (identity), `long regiaoId`, `int x`, `int y`, `TipoTerreno terreno`, `int bonusBase`, `int bonusAdjacente` (smallint com `@JdbcTypeCode(SqlTypes.SMALLINT)`), `int getBonusTotal()` (`@Transient`/método simples, não mapeado). Construtor `(regiaoId, x, y, terreno, bonusBase, bonusAdjacente)`.
  - Id identity (e não PK composta como em `ladrilho_jazida`): o `save` de entidade nova com id gerado não faz `select` prévio de merge, e segue o padrão bigint identity do projeto.
- Repositórios (`jogo.repositorio`):
  - `RegiaoTerrenoRepository extends JpaRepository<RegiaoTerreno, Long>`: `List<RegiaoTerreno> findByRegiaoIdOrderByPosicao(long regiaoId)`, `List<RegiaoTerreno> findByRegiaoIdIn(Collection<Long> regiaoIds)`.
  - `LadrilhoRepository extends JpaRepository<Ladrilho, Long>`: `List<Ladrilho> findByRegiaoIdOrderByYAscXAsc(long regiaoId)`, `Optional<Ladrilho> findByRegiaoIdAndXAndY(long regiaoId, int x, int y)`, `List<Ladrilho> findByRegiaoIdAndTerrenoOrderByYAscXAsc(long regiaoId, TipoTerreno terreno)`, `boolean existsByRegiaoId(long regiaoId)`.

### D6. TerrenoRegiaoService

`TerrenoRegiaoService` (novo, `jogo.servico`, `@Service` com repositórios) concentra a leitura dos terrenos e a geração persistida dos ladrilhos, usada por VilaService (3.1) e AnexacaoService (4.1):

- `Map<Long, List<RegiaoTerrenoDTO>> terrenosDasRegioes(Collection<Long> regiaoIds)` — ordenados por posição; região sem linhas fica fora do mapa (chamador usa `getOrDefault(id, List.of())`).
- `List<RegiaoTerrenoDTO> terrenosDaRegiao(long regiaoId)`.
- `void gerarLadrilhosSeAusentes(long semente, Regiao regiao)` — se `!ladrilhoRepository.existsByRegiaoId(id)`: lê `regiao_terreno` da região (ordem de posição), chama `GeradorLadrilhoService.gerar(semente, regiao.getIndice(), percentuais)` e grava as 100 entidades `Ladrilho`. Vale para **todos os tipos, inclusive Urbana**. (As consultas JPQL fazem auto-flush, então os `regiao_terreno` salvos antes na mesma transação são lidos.)
- `List<Ladrilho> primeirosLadrilhos(long regiaoId, TipoTerreno terreno, int quantidade)` — os primeiros na ordem de varredura (y, depois x).
- Os ladrilhos são gerados **quando a região passa a ser possuída** (criação e anexação), como hoje. Região não possuída não tem ladrilhos e o detalhe devolve lista vazia. Como o gerador é determinístico, gerar na anexação dá o mesmo resultado que gerar na criação. O MapaService deixa de gerar ladrilhos na hora.

### D7. Contratos da API

Records Java (pacote `jogo.dto`, salvo indicação):

- `RegiaoTerrenoDTO(TipoTerreno terreno, int posicao, int percentual)` (novo; substitui `RegiaoBonusDTO`).
- `RegiaoPreviaDTO(int indice, TipoRegiao tipo, List<RegiaoTerrenoDTO> terrenos)`.
- `VilaResumoDTO(Long vilaId, String nome, Long semente, Integer turnoCriacao, List<Regiao> regioes, Map<String, BigDecimal> estoque, boolean populacaoConfirmada)` com `Regiao(int indice, TipoRegiao tipo, boolean possuida, List<RegiaoTerrenoDTO> terrenos)` — sem `bonusRegiao`.
- `MapaDTO(VilaResumoDTO vila, List<RegiaoResumoDTO> regioes)` com `VilaResumoDTO(Long id, String nome)` — sem `bonusRegiao`.
- `RegiaoResumoDTO(int indice, TipoRegiao tipo, boolean possuida, boolean masmorraAtiva, Integer nivelMasmorra, Long masmorraId, List<RegiaoTerrenoDTO> terrenos)`.
- `RegiaoDetalheDTO(RegiaoDTO regiao, List<LadrilhoDTO> ladrilhos)` com `RegiaoDTO(Long id, int indice, TipoRegiao tipo, boolean possuida, List<RegiaoTerrenoDTO> terrenos)`.
- `LadrilhoDTO(int x, int y, TipoTerreno terreno, int bonusBase, int bonusAdjacente, int bonusTotal, ConstrucaoLadrilhoDTO construcao)` (o record interno `ConstrucaoLadrilhoDTO` não muda).
- `AnexacaoDTO.RegiaoAnexadaDTO(int indice, TipoRegiao tipo, boolean possuida, List<RegiaoTerrenoDTO> terrenos)`.
- `construcao.CatalogoConstrucaoDTO(TipoConstrucao tipo, String nome, List<TipoRegiao> regioes, TipoTerreno terreno, Map<String, Integer> custoN1, int tamanho, int poN1, List<Profissao> profissoes)`.

JSON:

`POST/GET /api/jogo/vila/previa`
```json
{
  "previaId": "5f0c2a1e-8d7b-4c1a-9e1f-2b3c4d5e6f70",
  "rodada": 2,
  "regioes": [
    { "indice": 1, "tipo": "FLORESTA",
      "terrenos": [
        { "terreno": "FLORESTA", "posicao": 1, "percentual": 40 },
        { "terreno": "PLANTACOES", "posicao": 2, "percentual": 35 },
        { "terreno": "BARREIRO", "posicao": 3, "percentual": 25 } ] }
  ]
}
```

`GET /api/jogo/vila`
```json
{
  "vilaId": 7, "nome": "Vila de Ana", "semente": 123456789, "turnoCriacao": 1,
  "regioes": [
    { "indice": 6, "tipo": "URBANA", "possuida": true,
      "terrenos": [
        { "terreno": "INDUSTRIA", "posicao": 1, "percentual": 45 },
        { "terreno": "COMERCIO", "posicao": 2, "percentual": 30 },
        { "terreno": "DESENVOLVIMENTO", "posicao": 3, "percentual": 25 } ] }
  ],
  "estoque": { "MADEIRA": 200, "OURO": 200 },
  "populacaoConfirmada": false
}
```

`GET /api/jogo/vila/mapa`
```json
{
  "vila": { "id": 7, "nome": "Vila de Ana" },
  "regioes": [
    { "indice": 6, "tipo": "URBANA", "possuida": true, "masmorraAtiva": false,
      "nivelMasmorra": null, "masmorraId": null,
      "terrenos": [ { "terreno": "INDUSTRIA", "posicao": 1, "percentual": 45 } ] }
  ]
}
```

`GET /api/jogo/regioes/{indice}`
```json
{
  "regiao": { "id": 31, "indice": 6, "tipo": "URBANA", "possuida": true,
              "terrenos": [ { "terreno": "INDUSTRIA", "posicao": 1, "percentual": 45 } ] },
  "ladrilhos": [
    { "x": 0, "y": 0, "terreno": "DESENVOLVIMENTO", "bonusBase": 30, "bonusAdjacente": 50, "bonusTotal": 80,
      "construcao": { "id": 3, "tipo": "CASA", "nivel": "N1", "tamanho": 1, "estado": "ATIVA", "poAtual": 0, "poTotal": 0 } },
    { "x": 1, "y": 0, "terreno": "INDUSTRIA", "bonusBase": 12, "bonusAdjacente": 25, "bonusTotal": 37, "construcao": null }
  ]
}
```
Região não possuída: `possuida: false`, `terrenos` preenchidos e `ladrilhos: []`.

`POST /api/jogo/regioes/{indice}/anexar` (rota atual)
```json
{ "regiao": { "indice": 7, "tipo": "LITORAL", "possuida": true, "terrenos": [ ... ] },
  "estoque": { "OURO": 50 }, "custo": { "ouro": 150, "madeira": 50, "pedra": 50 } }
```

`GET /api/jogo/construcoes/catalogo`, item:
```json
{ "tipo": "QUARTEL", "nome": "Quartel", "regioes": ["URBANA", "LITORAL"], "terreno": "MILITAR",
  "custoN1": { "PEDRA": 40, "TABUA": 30, "FERRO": 10 }, "tamanho": 1, "poN1": 8, "profissoes": ["GUERREIRO"] }
```

### D8. Catálogo de construções

- `ConstrucaoCatalogo.Entrada` passa a `Entrada(TipoConstrucao tipo, String nome, TipoTerreno terreno, List<TipoRegiao> regioes, boolean coleta, List<Profissao> profissoes, Map<Recurso,Integer> custoN1, int poN1, Jazida jazidaAssociada)`; `jazidaAssociada` fica `@Deprecated` até a task 6.1, que a remove.
- Terreno por prédio: Acampamento de lenhadores e Cabana de caça → FLORESTA; Barreiro → BARREIRO; Pedreira → ROCHA; Mina de ferro → FERRO; Mina de carvão → CARVAO; Salina → SALINAS; Mina de enxofre → ENXOFRE; Fazenda de plantio → PLANTACOES; Fazenda de criação → CRIACOES; Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha → INDUSTRIA; Mercado, Estalagem → COMERCIO; Casa → DESENVOLVIMENTO; Quartel → MILITAR; Armazém, Ferraria, Alfaiataria, Carpintaria → sem terreno (`null`).
- Regiões permitidas: sem terreno → `[URBANA]`; com terreno → tipos de `TipoRegiao.atuais()` cujos `terrenos()` contêm o terreno (ordem do enum); **exceção explícita: QUARTEL → `[URBANA, LITORAL]`**. Resultado: Casa, Mercado, Estalagem e fábricas → Urbana; fazendas, coleta como hoje.
- `coleta = true` para os 8 prédios de coleta (os mesmos que hoje têm jazida).
- API: `Optional<TipoTerreno> terreno(TipoConstrucao)`; `regioesPermitidas`, `permiteRegiao`, `ehPredioDeColeta` (passa a ler `coleta`); `@Deprecated bonusRegiao(tipo)` delega a `terreno(tipo)` e `@Deprecated jazida(tipo)` continua, ambos até 6.1 (ProducaoService e MarcacaoService ainda os usam nas ondas 3–5).
- `ConstrucaoService.criar`: mensagem de região inválida continua "X só pode ser construído em região A ou B" (ex.: "Quartel só pode ser construído em região Urbana ou Litoral"); `GeradorJazidaService.LADO` → `GeradorLadrilhoService.LADO`.
- Prédios com terreno podem ser construídos em qualquer ladrilho livre da região permitida; fora do terreno certo só não recebem bônus.

### D9. Bônus do prédio pela âncora

`BonusTerrenoService` (novo, `jogo.servico`):

- `int bonusDoPredio(Long vilaId, Construcao c)`: terreno do prédio = `ConstrucaoCatalogo.terreno(c.getTipo())`; se vazio → 0; região = `regiaoRepository.findByVilaIdAndIndice(vilaId, c.getRegiaoIndice())`; âncora = `ladrilhoRepository.findByRegiaoIdAndXAndY(regiao.id, c.getX(), c.getY())`; se a âncora existe **e** `ancora.terreno == terreno do prédio` → `ancora.getBonusTotal()`; senão 0 (região ou ladrilho ausentes também dão 0).
- `BigDecimal fatorDoPredio(Long vilaId, Construcao c)` = `1 + bonus/100`, escala 2 (ex.: 80 → 1,80).
- A âncora é sempre o (x, y) **atual** do prédio (canto superior esquerdo do quadrado 1/2/3). No aprimoramento com nova posição, a âncora muda junto.

### D10. Bônus de grupo pela média

- `GrupoBonusVila` (novo enum, `jogo.servico`): `COMERCIO(TipoTerreno.COMERCIO, MERCADO, ESTALAGEM)`, `DESENVOLVIMENTO(TipoTerreno.DESENVOLVIMENTO, CASA)`, `MILITAR(TipoTerreno.MILITAR, QUARTEL)`, com `terreno()` e `Set<TipoConstrucao> tipos()`.
- `BigDecimal media(Long vilaId, GrupoBonusVila g)`: prédios da vila com `tipo ∈ g.tipos()` e estado **ATIVA ou EM_UPGRADE** (prédio existente; EM_OBRA ainda não existe) cuja âncora tem terreno `g.terreno()`; média aritmética dos `bonus_total` dessas âncoras, escala 2 HALF_UP; sem nenhum → 0. Um prédio no terreno certo com `bonus_total` 0 **entra** na média (com 0); um prédio fora do terreno certo **não entra**.
- `BigDecimal fator(Long vilaId, GrupoBonusVila g)` = `1 + media/100`, escala 4 HALF_UP (ex.: média 50,00 → 1,5000; 45,50 → 1,4550).
- Carrega os ladrilhos-âncora com uma consulta por região envolvida (prédios agrupados por `regiaoIndice`).

### D11. Consumidores do bônus

- `ProducaoService` (coleta, fazendas e fábricas): fator = `bonusTerrenoService.fatorDoPredio(vila, c)` no lugar da soma da vila; aplicado como hoje (depois da eficiência e do nível, 2 casas). `marcadosValidos` passa a contar marcações em ladrilhos cujo terreno é `ConstrucaoCatalogo.terreno(tipo)` (via `LadrilhoRepository.findByRegiaoIdAndTerrenoOrderByYAscXAsc`).
- `OuroService`: imposto e receita da Estalagem × `fator(vila, COMERCIO)`. O Mercado só contribui com a âncora para a média; `MercadoService` não muda.
- `ObraService`: `ganho *= fator(vila, DESENVOLVIMENTO).doubleValue()`.
- `TreinamentoQuartelService`: `xpPorTurno(nivel) × fator(vila, MILITAR)`, 2 casas.
- Eventos: a chave de dados `bonusRegiao` é trocada por `bonusTerreno` — no evento de produção do prédio, o `bonus_total` aplicado (inteiro, só quando > 0); nos eventos de imposto e Estalagem, a média de Comércio (número com 2 casas, só quando > 0).
- Exemplos: Acampamento N1 com 2 trabalhadores de eficiência 1,0 (base 10 Madeira) e âncora Floresta com `bonus_total` 80 → 18,00 Madeira; âncora Barreiro → 10,00. Serraria N1 com 1 trabalhador (3 ciclos base) e âncora Indústria 50 → 4,50 ciclos. Imposto de 16 adultos (8 Ouro) com média Comércio 50 → 12,00. Obra com 1 Construtor de eficiência 1,0 e média Desenvolvimento 50 → 1,5 PO. Quartel N1 (0,5 XP) com média Militar 40 → 0,70 XP.

### D12. Marcação por terreno

- `MarcacaoService.marcar`: a validação de jazida vira "o ladrilho (x, y) da região tem o terreno `ConstrucaoCatalogo.terreno(tipo)`", lida com `ladrilhoRepository.findByRegiaoIdAndXAndY`. Mensagem: `"Ladrilho sem o terreno do prédio (Floresta)"` (nome de exibição do terreno). As demais regras (limite por nível, conexão ortogonal, ocupação) não mudam.
- `GeradorJazidaService.LADO` → `GeradorLadrilhoService.LADO`.

### D13. Casas iniciais em Desenvolvimento

- `VilaService.criarVilaComSemente`: depois de gravar `regiao_terreno` de todas as regiões e os ladrilhos das possuídas, pega a **1ª região Urbana na ordem da seleção** (como hoje) e cria as 4 casas N1 ATIVA (tamanho 1) nos 4 primeiros ladrilhos DESENVOLVIMENTO em ordem de varredura (`terrenoRegiaoService.primeirosLadrilhos(regiaoId, DESENVOLVIMENTO, 4)`). Como b3 ≥ 10, há sempre ao menos 10 ladrilhos De.
- `X_CASAS_INICIAIS` é removido. As famílias continuam associadas às 4 casas na mesma ordem.

### D14. Frontend domínio de terrenos

`/frontend/src/domain/terrenos.ts` (novo):

- `type TipoTerreno` (13 valores do backend) e `TERRENOS` (ordem do enum Java).
- `SIGLA_TERRENO`: FLORESTA Fl, BARREIRO Ba, PLANTACOES Pl, CRIACOES Cr, ROCHA Ro, FERRO Fe, CARVAO Ca, SALINAS Sa, ENXOFRE En, MILITAR Mi, INDUSTRIA In, COMERCIO Co, DESENVOLVIMENTO De.
- `ROTULO_TERRENO`: Floresta, Barreiro, Plantações, Criações, Rocha, Ferro, Carvão, Salinas, Enxofre, Militar, Indústria, Comércio, Desenvolvimento.
- `COR_TERRENO[t] = var(--vl-terreno-<sigla minúscula>)` (ex.: `var(--vl-terreno-fl)`).
- `TERRENOS_POR_TIPO: Record<TipoRegiao, TipoTerreno[]>` (mesma tabela do backend).
- `interface TerrenoDaRegiao { terreno: TipoTerreno; posicao: number; percentual: number }`.
- `ordenarTerrenos(t)`: percentual decrescente, desempate por posição crescente (b2 pode ser maior que b1).
- `composicaoTexto(t)`: `"Floresta 40% · Plantações 35% · Barreiro 25%"` (ordem de `ordenarTerrenos`, separador `" · "`).
- `totaisLadrilhos(sel: number[], regioes: { indice: number; terrenos: TerrenoDaRegiao[] }[]): Record<TipoTerreno, number>` — soma dos percentuais (= ladrilhos) das regiões selecionadas; as 13 chaves presentes (0 quando ausente).
- `COLUNAS_LADRILHO = 'ABCDEFGHIJ'` e `enderecoLadrilho(x, y)` → `"(A,1)"` (coluna = letra de x, linha = y + 1); `(9,9)` → `"(J,10)"`.
- Tokens em `/frontend/src/styles/tokens.css` (bloco "Tipos de terreno"), com os mesmos valores dos `--vl-bonus-*` atuais: `--vl-terreno-fl: oklch(0.72 0.13 145)`, `-ba: oklch(0.68 0.11 45)`, `-pl: oklch(0.80 0.13 120)`, `-cr: oklch(0.76 0.09 70)`, `-ro: oklch(0.74 0.02 250)`, `-fe: oklch(0.66 0.07 230)`, `-ca: oklch(0.60 0.01 260)`, `-sa: oklch(0.86 0.04 210)`, `-en: oklch(0.86 0.14 100)`, `-mi: oklch(0.66 0.13 20)`, `-in: oklch(0.72 0.11 50)`, `-co: oklch(0.80 0.13 75)`, `-de: oklch(0.76 0.11 290)`. Cores parecidas (Ba/In, Cr/Co, Pl/En) nunca aparecem na mesma região, e a sigla desfaz a ambiguidade.

### D15. Frontend grade de ladrilhos

- Tipos em `/frontend/src/composables/useMapa.ts`: `Ladrilho { x; y; terreno: TipoTerreno; bonusBase: number; bonusAdjacente: number; bonusTotal: number; construcao: Construcao | null }`; `RegiaoResumo.terrenos: TerrenoDaRegiao[]` (sai `bonus`); `MapaVila.vila { id; nome }` (sai `bonusRegiao`); `RegiaoDetalhe.regiao.terrenos?: TerrenoDaRegiao[]`. `ROTULOS_JAZIDA` é removido.
- `GradeRegiao.vue`: cada célula mostra duas linhas — em cima o endereço (`.lad-endereco`, fonte mono pequena) e embaixo a sigla do terreno (`.lad-sigla`); fundo = `COR_TERRENO[terreno]` (classe `ladrilho-terreno-<sigla minúscula>` ou `style`). Ladrilho com construção: classe `ladrilho-construcao` (contorno interno `--vl-accent`) e um selo com a 1ª letra do rótulo do prédio (`.lad-predio`, canto superior direito); endereço e sigla continuam visíveis. `max-width` da grade sobe de 480px para 560px.
- Tooltip (`title`) e `aria-label`: partes unidas por `" - "`: endereço; se houver construção, `"<Prédio> nível N"`, estado e `"PO a/b"` em obra (como hoje); depois `"<Terreno> (<Sigla>)"`, `"base B"`, `"adjacente +A"`, `"total T"`. Ex.: `"(C,5) - Floresta (Fl) - base 30 - adjacente +50 - total 80"`.
- Posição sem ladrilho na lista (região sem dados): célula vazia, tooltip só com o endereço e "Vazio".
- `useMarcacoes.ts`: `TERRENO_POR_TIPO` (8 prédios de coleta → terreno) no lugar de `JAZIDA_POR_TIPO`; computed `terreno` no lugar de `jazida`; candidatos filtram `l.terreno === terreno`. `PainelMarcacao.vue`: dica `"Terreno: Floresta (Fl) - N ladrilho(s) disponível(is). Clique para marcar ou desmarcar."`.

### D16. Frontend criação mapa e anexação

- `domain/regioes.ts`: `RegiaoPrevia { indice; tipo; terrenos: TerrenoDaRegiao[] }`; `totaisBonus` removido (substituído por `totaisLadrilhos`). Os exports de bônus ainda usados por Mapa/Anexação (`BonusRegiao`, `BONUS`, `ROTULO_BONUS`, `COR_BONUS`, `BONUS_POR_TIPO`, `BonusDaRegiao`, `bonusOrdenados`) ficam até a task 6.2.
- `components/criacao/ListaTerrenos.vue` (novo, substitui `BonusLista.vue`): props `itens: { terreno: TipoTerreno; valor: number }[]`, `maximo: number`, `sufixo?: string` (padrão `''`), `espessura?: 'fina' | 'normal'`; cada item mostra chip com a sigla (fundo `COR_TERRENO`), rótulo, `BarraValor` e o valor com o sufixo; `data-testid="terreno-<TERRENO>"`.
- `RegiaoTile.vue` / `RegiaoFoco.vue`: composição com `ListaTerrenos` (`ordenarTerrenos`, `maximo` 100, sufixo `%`); `aria-label` do tile: `"Região 06 · Urbana · Indústria 45% · Comércio 30% · Desenvolvimento 25%"`.
- `SelecaoPainel.vue`: seção "Ladrilhos por terreno" (substitui "Bônus de região"), com `totaisLadrilhos` só dos terrenos com total > 0, em ordem decrescente, `maximo = max(100, maior total)`, sem sufixo; sem seleção mostra "Selecione regiões para ver os ladrilhos". A prop `totais` passa a `Record<TipoTerreno, number>`. `useVila` expõe `totaisLadrilhos` no lugar de `totaisBonus`; `CriacaoVila.vue` repassa.
- `views/Mapa.vue`: remove a seção "Bônus total da vila" (`data-testid="bonus-vila"`); cada célula lista os 3 terrenos (`ordenarTerrenos`) como ponto colorido + `"<Sigla> <percentual>%"` (`data-testid="terrenos-regiao-<indice>"`); o `aria-label` da célula inclui `composicaoTexto`; o detalhe da região mostra chips `"<Rótulo> <percentual>%"` (`data-testid="terrenos-detalhe"`).
- `DialogoAnexacao.vue`: lista os terrenos da região (`"<Rótulo> <percentual>%"`, `data-testid="terreno-<TERRENO>"`) no lugar dos bônus.
- `useAnexacao.ts`: `ResultadoAnexacao.regiao.terrenos`. `useConstrucoes.ts`: `CatalogoConstrucao.terreno: TipoTerreno | null` no lugar de `bonusRegiao`. `SeletorConstrucao.vue`: o item escolhido mostra `"Terreno: <Rótulo> (<Sigla>)"` quando `terreno` não é nulo, ou `"Sem terreno (sem bônus)"`.

### D17. Remoção do legado

- Backend (task 6.1): apagar `BonusRegiaoService` (+ `BonusRegiaoServiceIntegrationTest`), `RegiaoBonus`, `RegiaoBonusRepository`, `RegiaoBonusDTO`, `LadrilhoJazida`, `LadrilhoJazidaRepository`, `Jazida`, `GeradorJazidaService` (+ `GeradorJazidaServiceTest`); remover de `ConstrucaoCatalogo` o campo `jazidaAssociada` e os métodos `jazida()` e `bonusRegiao()`; acrescentar os `drop table` à V18; limpar `ModeloJogoBaseIntegrationTest`. `FaixaBonusRegiao` já sai na 3.1. Varredura: `grep -rnE "BonusRegiao|RegiaoBonus|Jazida|jazida|FaixaBonus|bonusRegiao|GeradorJazida" src/main/java src/test/java` vazio.
- Frontend (task 6.2): remover de `domain/regioes.ts` `BonusRegiao`, `BONUS`, `ROTULO_BONUS`, `COR_BONUS`, `BONUS_POR_TIPO`, `BonusDaRegiao`, `bonusOrdenados`; remover de `tokens.css` os blocos `--vl-bonus-*` e `--vl-jazida-*`; `MasmorraIndicador.vue` usa `--vl-terreno-mi`; `.vitoria` em `BatalhaDetalhe.vue` e `JogoBatalhas.vue` usa `--vl-terreno-fl`. Varredura: `grep -rnE "bonusRegiao|BonusRegiao|ROTULO_BONUS|COR_BONUS|BONUS_POR_TIPO|BonusDaRegiao|bonusOrdenados|jazida|JAZIDA|vl-bonus|vl-jazida" frontend/src` vazio.

### D18. Ondas e coexistência temporária

- Para que vários Sonnet trabalhem em paralelo na mesma árvore sem quebrar a compilação dos outros, a troca é **aditiva primeiro e destrutiva por último**:
  1. Onda 1: renomeação mecânica `BonusRegiao → TipoTerreno` (sozinha no backend), domínio TS e docs.
  2. Onda 2: V18 só com `create table` + entidades/repositórios novos; gerador de ladrilhos puro; frontend da grade e da criação.
  3. Onda 3: gerador por percentuais + prévia + criação da vila (passa a gravar `regiao_terreno`/`ladrilho`); catálogo; frontend mapa/anexação/catálogo.
  4. Onda 4: mapa/detalhe/anexação na API; marcação; serviço de bônus por âncora.
  5. Onda 5: produção, ouro, obras, treino.
  6. Onda 6: remoção do legado (backend e frontend) e V18 final.
  7. Onda 7: verificação integrada.
- Entre as ondas 3 e 6 as tabelas antigas continuam existindo; o código que ainda as usa (BonusRegiaoService, MarcacaoService, ProducaoService) segue compilando, e os testes desse código montam seus próprios dados. Testes de outra task podem ficar vermelhos até a task dona deles (ex.: `MapaControladorIntegrationTest` entre 3.1 e 4.1); cada task roda só os seus testes; `./mvnw verify` completo roda na 6.1 e na 7.1.
- Cada task backend usa seu próprio banco (`DB_NAME=login_base_t<id>`), recriado antes do teste. Proibido `git stash`, `git checkout -- .`, `git reset` (há tasks em paralelo).

### D19. Capabilities próprias

- Capabilities novas (`jogo-terrenos-regiao`, `jogo-bonus-terreno`, `jogo-ui-terrenos`) com requisitos ADDED, em vez de MODIFIED/REMOVED sobre `jogo-bonus-regiao` e `jogo-criacao-vila`.
- Motivo: essas duas capabilities só existem na change `redesenho-criacao-vila-populacao`, que não foi arquivada; `openspec/specs/` não as tem, então um delta MODIFIED/REMOVED não teria base para ser aplicado e prenderia esta change à ordem de arquivamento da outra.
- Consequência no arquivamento: arquivar primeiro `redesenho-criacao-vila-populacao` e depois esta change; em seguida, numa change de limpeza (ou `openspec-update-change`), remover das specs principais os requisitos superados: em `jogo-bonus-regiao` — "Bônus de região da vila", "Efeito dos bônus na produção", "Construções permitidas pelo tipo da região", "Mapa e região exibem tipo e bônus", "Anexação mantém tipo e bônus sorteados"; em `jogo-criacao-vila` — os cenários de faixas 35–50/16–34/5–15 de "Geração do mapa por semente" e a soma de bônus de "Painel Sua seleção e criação pela tela".

## Risks / Trade-offs

- [Editar a V18 na 6.1 muda o checksum] → só existe nesta branch; todo banco de teste é recriado; o banco do desenvolvedor precisa ser recriado ao fim (já exigido por "banco limpo").
- [Casas iniciais em posições que variam com a semente] → testes que constroem na Urbana de uma vila criada pelo VilaService podem cair num ladrilho ocupado; usar regiões montadas à mão (como `ConstrucaoServiceIntegrationTest`) ou procurar ladrilho livre.
- [Testes vermelhos fora da task entre ondas] → esperado e documentado em cada task ("Fora de escopo"); o `verify` completo fica para 6.1 e 7.1.
- [Balanceamento] → bônus de até +200% por prédio, sem teto; média do grupo pode chegar a 200. Questão em aberto (como nos docs).
- [Desempenho do turno] → uma consulta de âncora por prédio; volume pequeno (≤ 16 regiões). Se crescer, carregar as âncoras por região em lote (D10 já faz isso na média).
- [Legibilidade da grade] → endereço em fonte pequena; a grade cresce para 560px; tooltip completo.
- [Docs × regras] → `regioes.md` diz que Comércio afeta a "produção de Mercado" e que ladrilhos só são gerados para tipos ≠ Urbana; valem as regras do usuário (Comércio só no ouro passivo; ladrilhos em todos os tipos); a task 1.1 corrige os docs.

## Migration Plan

1. Rodar as ondas 1–7.
2. Recriar o banco local (dropdb/createdb) antes de subir a aplicação; o Flyway aplica V1..V18.
3. Rollback: reverter os commits da change e recriar o banco (não há dados a preservar).

## Open Questions

- Estados que entram na média de grupo: decidido ATIVA + EM_UPGRADE; confirmar com o usuário se Casas/Quartéis em aprimoramento devem contar.
- Teto para o fator de bônus (hoje até 3,0 por prédio).
- Prédios sem terreno (Armazém, Ferraria, Alfaiataria, Carpintaria) seguem sem bônus — confirmar.