# Design

## Context

- Handoff em `/docs/designe/handoff/` (README, `regras/regras-regioes-v2.md`, `regras/regras-populacao-v1.md`, `telas/tela-01-criar-vila.md`, `telas/tela-02-distribuir-populacao.md`, `telas/design-tokens.md`, `api/contratos-api.md`, `referencia/geracao-mapa.js`, `referencia/distribuicao-populacao.js`, protótipos).
- Backend atual (Spring Boot, `com.example.loginbase.jogo`): `VilaService.criarVila(usuarioId, indices, tipos, semente)`, `GET /api/jogo/vila/preview` (contagem de jazidas), `PopulacaoService` com máximos 10/5 por atributo e pontos incrementais, `ConstrucaoCatalogo` com uma `TipoRegiao regiaoPermitida` por prédio (RURAL/COLETA/URBANA), `ProducaoService` (coleta/rural/fábricas), `OuroService`, `ObraService`, `TreinamentoQuartelService`, erros `{ "erro": "..." }`. Ids bigint identity. Flyway até V16; `ddl-auto=validate`.
- Ladrilhos 10×10 com jazidas (`ladrilho_jazida`, `GeradorJazidaService`, marcação e produção de coleta) continuam.
- Frontend: Vue 3 + PrimeVue 5 (Aura claro, sem darkMode), vue-router 5 com base `/app/`, guarda `guardaVila.ts` (rota de população `/jogo/populacao`), `http.ts` lança `Error(dados.erro)` sem status/código, telas com cores fixas.
- O usuário vai recriar o banco: não há dados a converter.

## Goals / Non-Goals

**Goals:**
- Implementar as telas 1 e 2 do handoff (alta fidelidade) e os contratos §1–§5 ajustados por este design.
- Portar `geracao-mapa.js` para Java e `distribuicao-populacao.js` para Java e TypeScript, com paridade JS↔Java↔TS da distribuição garantida por fixture.
- Dar efeito aos 13 bônus de região como % de produção.
- Derivar construções permitidas do bônus do tipo.
- Aplicar o tema "Vilarejo" (tokens + preset PrimeVue escuro + cabeçalho) a todas as telas do jogo.
- Atualizar `docs/jogo/**`.

**Non-Goals:**
- Converter dados antigos (RURAL/COLETA, regiões sem bônus) — banco será recriado.
- Remover ou alterar ladrilhos 10×10/jazidas, marcação de ladrilhos e `GeradorJazidaService`.
- Limite ou custo para "Gerar novo mapa"; plano inicial variável pelas regiões; nomes fixos das famílias (continuam gerados por `NomesFixos` com a semente).
- Endpoint `POST /api/jogo/vila/populacao/sugestao` (não será criado).
- Teto de balanceamento para bônus acumulados ou para atributos (sem máximo por atributo, como o handoff pede).
- Telas fora do SPA (login/cadastro Thymeleaf).

## Decisions

### D1. Tipos e bônus de região

- `TipoRegiao { FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA }` em `/src/main/java/com/example/loginbase/jogo/modelo/TipoRegiao.java`, com `List<BonusRegiao> bonus()` (ordem da tabela) e `String nomeExibicao` (Floresta, Planície, Urbana, Litoral, Montanha). Durante a change, `RURAL` e `COLETA` ficam `@Deprecated` (não usados em código novo) e são removidos na task 5.6.
- `BonusRegiao` (novo, mesmo pacote), 13 valores nesta ordem, com `nomeExibicao`: `FLORESTA` Floresta, `BARREIRO` Barreiro, `PLANTACOES` Plantações, `CRIACOES` Criações, `ROCHA` Rocha, `FERRO` Ferro, `CARVAO` Carvão, `SALINAS` Salinas, `ENXOFRE` Enxofre, `MILITAR` Militar, `INDUSTRIA` Indústria, `COMERCIO` Comércio, `DESENVOLVIMENTO` Desenvolvimento. Nome `PLANTACOES` mantido (não renomear para Fazendas).
- `FaixaBonusRegiao` (novo; já existe `jogo.item.FaixaBonus`, não reutilizar o nome): `POSICAO_1 (35,50)`, `POSICAO_2 (16,34)`, `POSICAO_3 (5,15)`, com `min`, `max`, `de(int posicao)`.

Tabela "Bônus por tipo":

| Tipo | Bônus 1 | Bônus 2 | Bônus 3 |
|---|---|---|---|
| FLORESTA | FLORESTA | BARREIRO | PLANTACOES |
| PLANICIE | PLANTACOES | CRIACOES | FLORESTA |
| URBANA | INDUSTRIA | COMERCIO | DESENVOLVIMENTO |
| LITORAL | SALINAS | ENXOFRE | MILITAR |
| MONTANHA | ROCHA | FERRO | CARVAO |

Alternativa considerada: manter RURAL/COLETA mapeados para os tipos novos — rejeitada (banco limpo, sem legado).

### D2. Geração do mapa por semente

- `GeradorMapaService` (novo, `jogo/servico`, `@Service` puro, sem banco): `List<RegiaoGerada> gerar(long semente)`; `record RegiaoGerada(int indice, TipoRegiao tipo, List<BonusGerado> bonus)`; `record BonusGerado(BonusRegiao bonus, int posicao, int valor)` (lista ordenada por posição).
- Segue **o algoritmo JS** (`/docs/designe/handoff/referencia/geracao-mapa.js`), com `java.util.Random(semente)`: `inteiro(min,max) = min + rng.nextInt(max - min + 1)`; embaralhar = Fisher–Yates igual ao JS (`for i = n-1..1: j = inteiro(0,i); troca`); quantidades por **amostragem por rejeição** (5 inteiros 2..4 na ordem FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA; aceita se soma 16 e ≤ 2 valores iguais a 4); layout = lista dos tipos repetidos pela quantidade, embaralhada; para cada região, na ordem dos índices 1..16: embaralha a lista de bônus do tipo e sorteia o valor de cada posição pela faixa. Mesma ordem de consumo do RNG do JS.
- Os números do JS (mulberry32) não são reproduzidos em Java — não é necessário: o servidor é a única fonte (R19).
- `boolean conectadas(List<Integer> indices)` (BFS ortogonal usando `GradeRegioes.adjacente`) e `Map<BonusRegiao,Integer> somarBonus(List<RegiaoGerada> mapa, Collection<Integer> indices)`.
- Validação da seleção no backend usa **conectividade do conjunto** (decisão do usuário; difere do JS, que exige "vizinho de algum anterior"): `[1,3,2]` é aceito. Ver D4.
- Testes: ~1000 sementes (R8 2..4 por tipo; R9 soma 16; R10 ≤ 2 tipos com 4; R13 3 bônus distintos do tipo; R14/R15 faixas inclusivas por posição; R16: cada bônus do tipo aparece em cada posição ao longo das sementes), determinismo (mesma semente ⇒ mesmo mapa), ambas as famílias de distribuição (4,4,3,3,2 e 4,3,3,3,3) aparecem, existe sempre ao menos uma seleção válida (2–4 Urbanas), `conectadas` e `somarBonus` (Exemplo 2 do handoff).

### D3. Prévia persistida em vila_previa

- Tabela `vila_previa` (1 linha por usuário) guarda `previa_id` (UUID gerado no Java), `semente` (bigint, gerada com `ThreadLocalRandom.current().nextLong()`), `rodada` e `criado_em`. O mapa é **regenerado** da semente em GET e na criação (determinístico). Sessão HTTP rejeitada (expira em 30 min e não sobrevive a restart).
- `POST /previa`: se não houver linha, cria com `rodada = 1`; se houver, troca semente e `previa_id` e faz `rodada + 1`. "Gerar novo mapa" é ilimitado e gratuito.
- Entidade `VilaPrevia` (`jogo/modelo`) + `VilaPreviaRepository` (`jogo/repositorio`); `VilaPreviaService` (novo, `jogo/servico`) com `gerar(usuarioId)`, `obter(usuarioId)`, `exigirVigente(usuarioId, UUID previaId)` e `remover(usuarioId)`.
- Na criação da vila, o mapa regenerado é **persistido** (16 regiões com tipo + `regiao_bonus`) e a linha de `vila_previa` é apagada. `vila.semente` = semente da prévia (continua alimentando `GeradorJazidaService` e nomes).

### D4. Criação da vila e validação da seleção

Contratos finais (ids numéricos, `previaId` string UUID):

`POST /api/jogo/vila/previa` → 200 (e `GET /api/jogo/vila/previa` → 200 mesmo formato):
```json
{
  "previaId": "5b1f8e0a-3c2d-4f7e-9a61-2f0c9d7e4b13",
  "rodada": 3,
  "regioes": [
    { "indice": 1, "tipo": "FLORESTA",
      "bonus": [ { "bonus": "FLORESTA", "posicao": 1, "valor": 44 },
                 { "bonus": "PLANTACOES", "posicao": 2, "valor": 21 },
                 { "bonus": "BARREIRO", "posicao": 3, "valor": 9 } ] }
  ]
}
```
(`regioes` sempre com 16 itens, índices 1..16; `bonus` ordenado por `posicao`.)
Erros: POST → `409 VILA_JA_EXISTE`; GET → `409 VILA_JA_EXISTE` (usuário com vila) ou `404 PREVIA_NAO_ENCONTRADA`.

`POST /api/jogo/vila` com `{ "previaId": "5b1f...", "indices": [6, 7, 10] }` → `201`
`{ "vilaId": 42, "proximaEtapa": "DISTRIBUIR_POPULACAO" }`.
Ordem de validação e erros:
1. usuário já tem vila → `409 VILA_JA_EXISTE` "Usuário já possui uma vila";
2. sem prévia, `previaId` nulo ou diferente da vigente → `409 PREVIA_EXPIRADA` "O mapa mudou. Escolha as regiões novamente";
3. `indices` nulo, ≠ 3 itens, repetidos ou fora de 1..16 → `400 SELECAO_INVALIDA` "Escolha 3 regiões diferentes de 01 a 16";
4. as 3 regiões não formam conjunto conexo (BFS ortogonal) → `400 REGIAO_NAO_ADJACENTE` "As regiões escolhidas precisam ser vizinhas entre si";
5. nenhuma URBANA → `400 SEM_REGIAO_URBANA` "Ao menos uma região deve ser Urbana".
Efeitos: cria a vila com a semente da prévia; grava as 16 regiões com tipo e 3 linhas de `regiao_bonus` cada; marca as 3 como possuídas; gera ladrilhos (`GeradorJazidaService`) das 3 possuídas (como hoje); estoque inicial; 4 casas N1 em x = 0,2,4,6 / y = 0 da **1ª região Urbana na ordem de `indices`**; 16 cidadãos com 0 pontos e pendentes 20/10 (`FamiliaService.gerarFamiliasIniciais`, inalterado); apaga a prévia. Concorrência: `uk_vila_usuario`.
`VilaService` expõe `criarVila(Long usuarioId, UUID previaId, List<Integer> indices)` (API) e `criarVilaComSemente(Long usuarioId, long semente, List<Integer> indices)` (usada pela anterior e pelos testes).
`PreviaVilaDTO` e `GET /api/jogo/vila/preview` são removidos. Alternativa rejeitada: resposta superset com `VilaResumoDTO` (o frontend só precisa de `proximaEtapa`; `GET /api/jogo/vila` continua devolvendo o resumo).

### D5. Formato de erro com código

- Resposta de erro do jogo: `{ "erro": "<mensagem>", "codigo": "<CODIGO>" }`; `codigo` só aparece quando definido (mantém compatibilidade com todo o frontend, que lê `erro`). O contrato do handoff falava em `mensagem`; adotamos `erro`.
- `JogoException` ganha `String codigo` (getter) e o construtor `JogoException(HttpStatus, String codigo, String mensagem)`; o construtor atual continua (codigo nulo). `ApiExceptionHandler` monta o corpo com `LinkedHashMap` (erro, codigo).
- Exceções existentes recebem código: `VilaJaExisteException` → `VILA_JA_EXISTE` (409), `RegiaoNaoAdjacenteException` → `REGIAO_NAO_ADJACENTE` (400), `UrbanaObrigatoriaException` → `SEM_REGIAO_URBANA` (400, mensagem "Ao menos uma região deve ser Urbana"), `VilaNaoEncontradaException` → `VILA_NAO_ENCONTRADA` (404).
- Tabela de códigos desta change:

| Código | HTTP | Mensagem | Onde |
|---|---|---|---|
| VILA_JA_EXISTE | 409 | Usuário já possui uma vila | §1, §2, §3 |
| PREVIA_NAO_ENCONTRADA | 404 | Nenhuma prévia de mapa gerada | GET /previa |
| PREVIA_EXPIRADA | 409 | O mapa mudou. Escolha as regiões novamente | POST /vila |
| SELECAO_INVALIDA | 400 | Escolha 3 regiões diferentes de 01 a 16 | POST /vila |
| REGIAO_NAO_ADJACENTE | 400 | As regiões escolhidas precisam ser vizinhas entre si | POST /vila |
| SEM_REGIAO_URBANA | 400 | Ao menos uma região deve ser Urbana | POST /vila |
| VILA_NAO_ENCONTRADA | 404 | (mensagem atual) | geral |
| POPULACAO_JA_CONFIRMADA | 409 | A população já foi confirmada | POST /populacao |
| POPULACAO_INCOMPLETA | 400 | Informe os 16 cidadãos da vila | POST /populacao |
| PONTOS_INVALIDOS | 400 | Pontos inválidos para <nome> | POST /populacao |
| LIMITE_CARACTERISTICAS | 400 | Máximo 20 pontos de característica (<nome>) | POST /populacao |
| LIMITE_PROFISSOES | 400 | Máximo 10 pontos de profissão (<nome>) | POST /populacao |
| FAMILIA_LIDER_OBRIGATORIA | 400 | Escolha uma família líder | POST /populacao |
| MINIMO_CONSTRUTORES | 400 | São necessários ao menos 2 Construtores principais | POST /populacao |
| MINIMO_CARREGADORES | 400 | São necessários ao menos 2 Carregadores principais | POST /populacao |

### D6. Schema V17 sem retrocompatibilidade

**Sem retrocompatibilidade de dados:** o usuário recria o banco; a V17 não converte RURAL/COLETA nem preenche bônus de vilas antigas. Em banco com dados antigos a V17 falha (esperado). Bancos de teste dos subagentes também são recriados.
Arquivo `/src/main/resources/db/migration/V17__regioes_v2_bonus_e_previa.sql`:
```sql
alter table regiao drop constraint ck_regiao_tipo;
alter table regiao add constraint ck_regiao_tipo
    check (tipo is null or tipo in ('FLORESTA', 'PLANICIE', 'URBANA', 'LITORAL', 'MONTANHA'));

create table regiao_bonus (
    id        bigint generated always as identity,
    regiao_id bigint      not null,
    bonus     varchar(20) not null,
    posicao   smallint    not null,
    valor     int         not null,
    constraint pk_regiao_bonus primary key (id),
    constraint fk_regiao_bonus_regiao foreign key (regiao_id) references regiao (id),
    constraint uk_regiao_bonus_bonus unique (regiao_id, bonus),
    constraint uk_regiao_bonus_posicao unique (regiao_id, posicao),
    constraint ck_regiao_bonus_posicao check (posicao between 1 and 3),
    constraint ck_regiao_bonus_valor check ((posicao = 1 and valor between 35 and 50)
        or (posicao = 2 and valor between 16 and 34) or (posicao = 3 and valor between 5 and 15)),
    constraint ck_regiao_bonus_nome check (bonus in ('FLORESTA', 'BARREIRO', 'PLANTACOES', 'CRIACOES', 'ROCHA',
        'FERRO', 'CARVAO', 'SALINAS', 'ENXOFRE', 'MILITAR', 'INDUSTRIA', 'COMERCIO', 'DESENVOLVIMENTO'))
);
create index ix_regiao_bonus_regiao on regiao_bonus (regiao_id);

create table vila_previa (
    usuario_id bigint      not null,
    previa_id  uuid        not null,
    semente    bigint      not null,
    rodada     int         not null,
    criado_em  timestamptz not null default current_timestamp,
    constraint pk_vila_previa primary key (usuario_id),
    constraint uk_vila_previa_previa unique (previa_id),
    constraint fk_vila_previa_usuario foreign key (usuario_id) references usuarios (id),
    constraint ck_vila_previa_rodada check (rodada >= 1)
);
```
(Mesmo padrão de identity da V3: `bigint generated always as identity`.)
- `regiao.tipo` continua nullable no banco (testes criam regiões sem tipo); vilas novas sempre gravam o tipo das 16.
- Entidade `RegiaoBonus` (`jogo/modelo`, campos `id`, `regiaoId`, `bonus` (`@Enumerated STRING`), `posicao`, `valor`) + `RegiaoBonusRepository` (`findByRegiaoIdOrderByPosicao`, `findByRegiaoIdIn`, `deleteByRegiaoIdIn` e uma consulta de soma por vila — ver D10). Entidade própria escolhida em vez de `@ElementCollection` (padrão do projeto: id bigint + repositório; consulta agregada simples).
- Não há coluna para "bônus da vila": é derivado (D10).

### D7. Distribuição da população e paridade

- Java: `/src/main/java/com/example/loginbase/jogo/cidadao/DistribuicaoPopulacao.java` (classe final, métodos estáticos, porte linha a linha do JS) + `PapelFamiliar { PAI, MAE, FILHO, FILHA }` (novo).
  Constantes: `PLANO_PADRAO` (COMERCIANTE 1, CONSTRUTOR 2, CARREGADOR 2, MADEIREIRO 2, MINEIRO 2, AGRICULTOR 2, FAZENDEIRO 1, COZINHEIRO 1, GUERREIRO 2, FERREIRO 1, COSTUREIRO 0, CACADOR 0), `MINIMOS` (CONSTRUTOR 2, CARREGADOR 2), `LIMITE_CARACTERISTICAS = 20`, `LIMITE_PROFISSOES = 10`, `SECUNDARIA` (Construtor→Carregador, Carregador→Construtor, Agricultor→Fazendeiro, Fazendeiro→Agricultor, Mineiro→Madeireiro, Madeireiro→Mineiro, Ferreiro→Mineiro, Cozinheiro→Comerciante, Costureiro→Cozinheiro, Caçador→Guerreiro, Guerreiro→Caçador, Comerciante→Cozinheiro), `APOIO_CANDIDATAS = [CARREGADOR, CONSTRUTOR, MADEIREIRO]`, `ORDEM_PLANO = [COMERCIANTE, CONSTRUTOR, CARREGADOR, MADEIREIRO, MINEIRO, AGRICULTOR, FAZENDEIRO, COZINHEIRO, GUERREIRO, FERREIRO, COSTUREIRO, CACADOR]` (≠ ordem do enum), pontos de profissão 5/3/2, pesos (base VIT 0,5; principal +3; secundária +1,5; apoio +0,5 por característica ligada), greedy de 20 passos com `double`, `peso/(valor+1)`, `>` estrito, ordem VIT, FOR, VEL, INT, CAR.
  Características ligadas = as de `Profissao` (mesma tabela do JS). Desempate da principal = `Profissao.ordinal()`.
  Funções: `distribuirPessoa(Profissao)`, `distribuirPopulacao(List<FamiliaEntrada>, Map<Profissao,Integer> plano)` (exige soma 16; rodízio membro PAI→MAE→FILHO→FILHA × família na ordem dada; papel faltante ⇒ CARREGADOR como no JS), `principal(...)` (null se tudo 0), `liderDaFamilia` (≥ 18 anos, mais velho, empate pela ordem do papel), `bonusLider(car) = min(10, car/2)`, `familiaLiderSugerida` (índice do líder com maior CAR; empate → primeira).
- TS: `/frontend/src/domain/populacao.ts` (porte do mesmo JS, chaves de característica em MAIÚSCULAS) — ver D14.
- **Fixture de paridade** em `/frontend/src/domain/__fixtures__/distribuicao-populacao.json` (dentro de `frontend/` porque o container do frontend só monta essa pasta). Gerada uma vez com Node a partir do JS de referência e congelada. Conteúdo: `pessoas` (as 12 saídas de `distribuirPessoa`), `familias` de entrada (4×4 com nome, sexo, idadeAnos, papel — nomes do handoff), `casos` (3 casos: `plano-padrao`, `plano-cacador-costureiro` e `plano-construtores-carregadores`, cada um com `nome`, `plano`, `resultado` e `familiaLiderSugerida`), e para cada caso a saída esperada por cidadão (caracteristicas em MAIÚSCULAS, profissões com as 12 chaves).
  O teste Java lê o arquivo por caminho relativo à raiz do projeto (`Path.of("frontend/src/domain/__fixtures__/...")`, diretório de trabalho do Maven) e o teste TS importa o JSON (`resolveJsonModule` já vem do `@vue/tsconfig`). Valores esperados do plano padrão (pai, mãe, filho, filha): F1 COMERCIANTE (VIT1 FOR1 VEL5 INT0 CAR13), CARREGADOR, MINEIRO, COZINHEIRO; F2 CONSTRUTOR, MADEIREIRO, AGRICULTOR, GUERREIRO; F3 igual a F2; F4 CARREGADOR, MINEIRO, FAZENDEIRO, FERREIRO; família sugerida índice 0, +6%.
- Vetores por profissão (saída do JS):

| Principal | VIT | FOR | VEL | INT | CAR |
|---|---|---|---|---|---|
| CONSTRUTOR | 3 | 5 | 4 | 8 | 0 |
| CARREGADOR | 2 | 8 | 7 | 3 | 0 |
| AGRICULTOR | 1 | 1 | 1 | 17 | 0 |
| FAZENDEIRO | 1 | 1 | 1 | 17 | 0 |
| MINEIRO | 5 | 14 | 1 | 0 | 0 |
| MADEIREIRO | 8 | 11 | 1 | 0 | 0 |
| FERREIRO | 1 | 7 | 1 | 11 | 0 |
| COZINHEIRO | 1 | 1 | 8 | 0 | 10 |
| COSTUREIRO | 1 | 1 | 10 | 0 | 8 |
| CACADOR | 7 | 2 | 7 | 0 | 4 |
| GUERREIRO | 7 | 4 | 7 | 0 | 2 |
| COMERCIANTE | 1 | 1 | 5 | 0 | 13 |

- O exemplo do §4 do contrato (Marcos `car 15`) diverge do algoritmo; **vale o algoritmo** (CAR 13).
- Sem `POST /populacao/sugestao`: o frontend redistribui localmente com o porte TS; o backend usa o porte Java para a sugestão do GET e para calcular a principal na validação. A fixture garante que TS e Java dão o mesmo resultado.

### D8. Contratos da população

- **Família líder por id** (escolha única front/back): o POST recebe `familiaLiderId` (bigint) e o GET devolve `familiaLiderSugeridaId`. Não há `familiaLiderIndex`.
- Chaves de característica em **MAIÚSCULAS** (`VIT, FOR, VEL, INT, CAR`) na entrada e na saída (iguais ao enum e à `/api/jogo/cidadao`); a entrada aceita qualquer caixa. `profissoes` sempre com as 12 chaves (zeros incluídos).
- Famílias ordenadas por `familia.id`; membros na ordem PAI, MAE, FILHO, FILHA. Papel derivado sem coluna: `paiId == null && maeId == null` ⇒ PAI (sexo M) / MAE (F); senão FILHO (M) / FILHA (F).

`GET /api/jogo/vila/populacao` → 200:
```json
{
  "populacaoConfirmada": false,
  "plano": { "COMERCIANTE": 1, "CONSTRUTOR": 2, "CARREGADOR": 2, "MADEIREIRO": 2, "MINEIRO": 2, "AGRICULTOR": 2,
             "FAZENDEIRO": 1, "COZINHEIRO": 1, "GUERREIRO": 2, "FERREIRO": 1, "COSTUREIRO": 0, "CACADOR": 0 },
  "minimos": { "CONSTRUTOR": 2, "CARREGADOR": 2 },
  "limites": { "caracteristicasTotal": 20, "profissoesTotal": 10 },
  "familiaLiderSugeridaId": 11,
  "familiaLiderId": null,
  "familias": [
    { "familiaId": 11, "sobrenome": "Oliveira",
      "cidadaos": [
        { "cidadaoId": 101, "nome": "Marcos", "sexo": "M", "idadeAnos": 40, "papel": "PAI",
          "caracteristicas": { "VIT": 1, "FOR": 1, "VEL": 5, "INT": 0, "CAR": 13 },
          "profissoes": { "CONSTRUTOR": 0, "CARREGADOR": 2, "AGRICULTOR": 0, "FAZENDEIRO": 0, "MINEIRO": 0,
                          "MADEIREIRO": 0, "FERREIRO": 0, "COZINHEIRO": 3, "COSTUREIRO": 0, "CACADOR": 0,
                          "GUERREIRO": 0, "COMERCIANTE": 5 } } ] } ]
}
```
Enquanto `populacaoConfirmada = false`, `caracteristicas`/`profissoes` são a **sugestão** do `PLANO_PADRAO` (calculada na hora, não persistida; recarregar dá o mesmo resultado). Depois de confirmada: valores reais, `familiaLiderId` preenchido. Sem vila → `404 VILA_NAO_ENCONTRADA`.

`POST /api/jogo/vila/populacao`:
```json
{ "familiaLiderId": 11,
  "familias": [ { "familiaId": 11, "cidadaos": [ { "cidadaoId": 101,
      "caracteristicas": { "VIT": 1, "FOR": 1, "VEL": 5, "INT": 0, "CAR": 13 },
      "profissoes": { "COMERCIANTE": 5, "COZINHEIRO": 3, "CARREGADOR": 2 } } ] } ] }
```
→ 200 `{ "bonusLider": 6, "familiaLiderId": 11, "proximaEtapa": "MAPA" }` (sem `redirect`; o frontend navega pelo router). Ordem de validação: `409 POPULACAO_JA_CONFIRMADA` → `400 POPULACAO_INCOMPLETA` (cada cidadão vivo da vila exatamente uma vez, na família informada) → `400 PONTOS_INVALIDOS` (valor nulo/negativo ou chave desconhecida) → `400 LIMITE_CARACTERISTICAS` (Σ > 20) → `400 LIMITE_PROFISSOES` (Σ > 10) → `400 FAMILIA_LIDER_OBRIGATORIA` (`familiaLiderId` nulo ou de outra vila) → `400 MINIMO_CONSTRUTORES` → `400 MINIMO_CARREGADORES`.
Efeitos: grava valores **absolutos** (`cidadao` características; `cidadao_profissao` só para valor > 0, removendo as demais), `pontos_car_pendentes = 20 − Σcar`, `pontos_prof_pendentes = 10 − Σprof`, `vila.familiaLiderId`, `populacaoConfirmada = true`. Sem máximo por atributo (VIT 20 e Construtor 10 são aceitos). Pendências não bloqueiam.
`bonusLider` = `min(10, floor(CAR ÷ 2))` do líder (adulto mais velho, empate PAI→MAE→FILHO→FILHA).
Remover `MAX_POR_CARACTERISTICA` e `MAX_POR_PROFISSAO` do `PopulacaoService`.

### D9. Construções permitidas por tipo

Regra: prédio com bônus associado só pode ser construído em região cujo **tipo tenha esse bônus** na tabela de D1; prédio sem bônus associado é urbano (só URBANA). `ConstrucaoCatalogo.Entrada` troca `TipoRegiao regiaoPermitida` por `BonusRegiao bonusRegiao` (nullable) e ganha `regioesPermitidas()` derivado (`bonus == null ? {URBANA} : tipos cujo bonus() contém o bônus`); `permiteRegiao(tipo, regiao)` usa o conjunto; `ehPredioDeColeta` passa a ser `jazidaAssociada != null`. `CatalogoConstrucaoDTO`: `regiao` → `List<TipoRegiao> regioes` (ordem do enum) + `BonusRegiao bonusRegiao`. Mensagem do `ConstrucaoService`: "<Prédio> só pode ser construído em região <Tipo1> ou <Tipo2>" (nomes de exibição).

| Prédio | Bônus associado | Tipos permitidos |
|---|---|---|
| Casa, Armazém, Ferraria, Alfaiataria, Carpintaria, Mercado, Estalagem, Quartel | — | URBANA |
| Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha (fábricas) | INDUSTRIA | URBANA |
| Fazenda de plantio | PLANTACOES | FLORESTA, PLANICIE |
| Fazenda de criação | CRIACOES | PLANICIE |
| Acampamento de lenhadores | FLORESTA | FLORESTA, PLANICIE |
| Cabana de caça | FLORESTA | FLORESTA, PLANICIE |
| Barreiro | BARREIRO | FLORESTA |
| Pedreira | ROCHA | MONTANHA |
| Mina de ferro | FERRO | MONTANHA |
| Mina de carvão | CARVAO | MONTANHA |
| Salina | SALINAS | LITORAL |
| Mina de enxofre | ENXOFRE | LITORAL |

Ladrilhos/jazidas inalterados (a jazida continua sendo exigida na marcação). Em Urbana a jazida segue ignorada.

### D10. Bônus de região na produção

- **Bônus da vila** = soma, bônus a bônus, dos bônus de **todas as regiões possuídas** (na criação = soma das 3; cresce com anexações). `BonusRegiaoService` (novo, `jogo/servico`): `Map<BonusRegiao,Integer> bonusDaVila(Long vilaId)` (13 chaves, 0 quando ausente; consulta agregada em `RegiaoBonusRepository`: `select b.bonus, sum(b.valor) from RegiaoBonus b, Regiao r where b.regiaoId = r.id and r.vilaId = :vilaId and r.possuida = true group by b.bonus`) e `BigDecimal fator(Long vilaId, BonusRegiao b)` = `1 + soma/100` (ex.: 42 ⇒ 1,42).
- **Por que somar a vila e não por região do prédio:** (1) R17 do handoff define "bônus da vila = soma" e a tela de criação mostra exatamente essa soma — o número que o jogador vê é o % que ele recebe; (2) bônus de Indústria, Comércio, Militar e Desenvolvimento são de escopo da vila (ouro, obras, treino) e não teriam "região do prédio" clara; (3) uma consulta por etapa do turno, sem lookup por prédio. Alternativa considerada: aplicar só o bônus da região onde o prédio está (limitado a 50%, mais "geográfico") — rejeitada por divergir do número exibido e por não cobrir os bônus de vila. Risco: acúmulo alto com muitas anexações (ver Risks).
- Mapeamento (cada ponto = +1%; aplicado a todos os níveis):

| Bônus | Efeito | Código |
|---|---|---|
| FLORESTA | produção de Acampamento de lenhadores (Madeira) e Cabana de caça (Carne, Couro) | `ProducaoService.processarProducaoColataRural` |
| BARREIRO | produção do Barreiro (Argila) | idem |
| PLANTACOES | produção da Fazenda de plantio (Grãos ou Fibra) | idem |
| CRIACOES | produção da Fazenda de criação (Carne + Couro ou Lã + Carne) | idem |
| ROCHA | produção da Pedreira (Pedra) | idem |
| FERRO | produção da Mina de ferro (Minério de ferro) | idem |
| CARVAO | produção da Mina de carvão (Carvão) | idem |
| SALINAS | produção da Salina (Sal) | idem |
| ENXOFRE | produção da Mina de enxofre (Enxofre) | idem |
| INDUSTRIA | ciclos disponíveis das fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha); insumos continuam limitando | `ProducaoService.processarProducaoFabricas` |
| COMERCIO | Ouro do passo 2: imposto e receita da Estalagem | `OuroService` |
| DESENVOLVIMENTO | PO gerada por turno em obras e upgrades | `ObraService` |
| MILITAR | XP de treino por turno no Quartel | `TreinamentoQuartelService` |

- Fórmulas e arredondamento: coleta/rural `q = (Σef × base × fator).setScale(2, HALF_UP)` (o bônus do prédio vem de `ConstrucaoCatalogo.bonusRegiao(tipo)`); fábricas `disponivel = (Σef × ciclosBase × fator).setScale(2, HALF_UP)`; imposto `0,5 × adultos × fator` e Estalagem `4 × servidas × fator`, ambos `.setScale(2, HALF_UP)` (refeições servidas não mudam); obras `ganho = Σ(ef × peso) × (1 + B/100)` em double; treino `xp × fator` `.setScale(2, HALF_UP)` (coluna `numeric(6,2)`). Com bônus 0 o resultado é idêntico ao atual (testes existentes sem `regiao_bonus` não mudam).
  Eventos de produção/ouro incluem `"bonusRegiao": B` nos dados quando B > 0.
- Exemplos: Acampamento N1 com 2 trabalhadores de eficiência 1,0 e Floresta 42 ⇒ 2 × 1,0 × 5 × 1,42 = **14,20 Madeira** (sem bônus: 10). Serraria com 1 trabalhador ef. 1,0 e Indústria 30 ⇒ 3 × 1,30 = 3,90 ciclos disponíveis. Vila inicial com 16 adultos e Comércio 47 ⇒ imposto 8 × 1,47 = **11,76 Ouro**. Construtor ef. 1,0 com Desenvolvimento 12 ⇒ 1,12 PO. Quartel N1 (0,5 XP) com Militar 20 ⇒ 0,60 XP.

### D11. Mapa, resumo e anexação

- `RegiaoBonusDTO(BonusRegiao bonus, int posicao, int valor)` (novo, `jogo/dto`), reutilizado por prévia, mapa e resumo.
- `GET /api/jogo/vila` (`VilaResumoDTO`): regiões ganham `bonus: [...]`; novo campo `bonusRegiao: { "FLORESTA": 25, ... }` (13 chaves, ordem do enum).
- `GET /api/jogo/vila/mapa` (`MapaDTO`): `tipo` e `bonus` das **16 regiões, inclusive não possuídas**; `vila` ganha `bonusRegiao`.
- `GET /api/jogo/regioes/{indice}`: `regiao` ganha `bonus`; ladrilhos: persistidos ou, se ausentes e tipo ≠ URBANA, gerados para exibição (antes: só COLETA).
- `POST /api/jogo/regioes/{indice}/anexar`: corpo opcional e ignorado (`AnexarRegiaoRequest` removido; `@RequestBody(required = false)` não é necessário — remover o parâmetro). A região mantém tipo e bônus gravados na criação; ladrilhos gerados se ausentes (qualquer tipo). Custos, adjacência e masmorra inalterados.

### D12. Turno ignora vila com população pendente

- `VilaRepository.findIdsComPopulacaoConfirmada()` (`select v.id from Vila v where v.populacaoConfirmada = true order by v.id`) usado por `TurnoProcessorPorVila` no lugar de `findAllIds()`. `ProcessadorTurnoVila.processarVila` não muda (testes que o chamam direto continuam). Evita envelhecer/mudar a população antes da confirmação.
- Testes `TurnoProcessorPorVilaIntegrationTest` e `AgendadorTurnoIntegrationTest` passam a marcar `vila.setPopulacaoConfirmada(true)` nas vilas que devem ser processadas e ganham um caso de vila pendente ignorada.

### D13. Frontend: rotas, guarda e ApiError

- `ApiError extends Error { status: number; codigo?: string }` em `/frontend/src/api/http.ts`; mensagem = `dados.erro ?? dados.mensagem ?? 'Erro <status>'`. Continua `instanceof Error` (composables atuais não mudam).
- Rotas (`/frontend/src/router/index.ts`): `criar-vila` (name `criar-vila`, `meta: { etapaInicial: true, abaAtiva: 'mapa' }`), `distribuir-populacao` (name `distribuir-populacao`, componente `DistribuicaoPopulacao.vue`, `meta: { etapaInicial: true, abaAtiva: 'familias' }`), `{ path: 'populacao', redirect: '/jogo/distribuir-populacao' }`; demais rotas com `meta.abaAtiva` (mapa, estoque, familias, mercado, inventario, batalhas).
- Guarda (`guardaVila.ts`): pendente → `/jogo/distribuir-populacao`; aceita `/jogo/distribuir-populacao`; erro 404 = sem vila; outros erros (rede/5xx) não ficam em cache (deixa navegar e reconsulta depois).
- Nomes de arquivos mantidos (`CriacaoVila.vue`, `DistribuicaoPopulacao.vue`), sem sufixo `View`.
- URLs: `/app/jogo/criar-vila`, `/app/jogo/distribuir-populacao`, `/app/jogo/mapa` (base `/app/`); no código, caminhos do router sem `/app`.

### D14. Frontend: domínio TS e composables

- `/frontend/src/domain/regioes.ts` (novo): tipos `TipoRegiao`, `BonusRegiao`, `BonusDaRegiao`, `RegiaoPrevia`, `PreviaMapa`; `TIPOS`, `BONUS` (ordem do enum Java), `ROTULO_TIPO`, `ROTULO_BONUS`, `COR_TIPO`/`COR_BONUS` (`var(--vl-tipo-*)`/`var(--vl-bonus-*)`), `adjacentes(a,b)`, `conectado(sel)` (BFS), `podeSelecionar(sel, i)` (vizinha de **qualquer** selecionada, máx. 3), `temUrbana`, `selecaoValida`, `totaisBonus` (13 chaves), `bonusOrdenados` (valor desc), `dicaSelecao` (5 mensagens da tela 1: nenhuma, parcial, válida, sem Urbana, desconectadas).
- `/frontend/src/domain/populacao.ts` (novo): porte TS do JS (D7) com chaves MAIÚSCULAS: `CARACTERISTICAS`, `PROFISSOES` (ordem da tabela), `CARACTERISTICAS_LIGADAS`, `SECUNDARIA`, `APOIO_CANDIDATAS`, `ORDEM_PLANO`, `PLANO_PADRAO`, `MINIMOS`, `LIMITES`, `ROTULOS_PROFISSAO`, `distribuirPessoa`, `distribuirPopulacao`, `principal`, `trocarPrincipal`, `bonusR3`, `explicacaoBonusR3` ("Bônus R3: FOR 8÷5 + VEL 7÷5"), `liderDaFamilia`, `bonusLider`, `familiaLiderSugerida`, `validar`, `soma`.
- `usePopulacao.ts` reexporta `CARACTERISTICAS`, `PROFISSOES`, `ROTULOS_PROFISSAO`, `soma`, `bonusLider` (usados por `CaracteristicasTab.vue`, `ProfissoesTab.vue`, `PainelPredio.vue`, `PainelCidadao.vue`).
- `useMapa.ts` passa a reexportar `TipoRegiao` e `ROTULOS_TIPO` a partir do domínio (5 tipos).
- Valores de `plano`, `minimos`, `limites` usados pela tela vêm do GET; constantes do domínio são padrão/testes.
- Toast próprio (`AvisoToast.vue`, sem `ToastService`), com `role="status"`/`aria-live="polite"` (erro: `role="alert"`).
- Comportamentos do protótipo: `hover` limpo em `mouseleave` da grade (volta à última selecionada) e foco de teclado = hover; "Redistribuir pelo plano" desabilitado com `title="O plano precisa somar 16"` quando o plano ≠ 16; "01 Regiões ✓" é texto (não link); abas do cabeçalho inativas (`aria-disabled`) nas rotas `meta.etapaInicial`.
- Após confirmar a população: `marcarPopulacaoConfirmada()` + `router.push('/jogo/mapa')`; 409 `POPULACAO_JA_CONFIRMADA` faz o mesmo. Na criação: `PREVIA_EXPIRADA` → toast, recarrega a prévia (GET) e limpa a seleção; `VILA_JA_EXISTE` → `resetarGuardaVila()` e `router.push('/jogo/mapa')`.

### D15. Tema Vilarejo: tokens, fontes e preset

- `/frontend/src/styles/tokens.css` (novo, global em `:root`, valores oklch do protótipo; hex só como comentário): `--vl-bg: oklch(0.16 0.01 60)`, `--vl-surface-1: oklch(0.19 0.012 60)`, `--vl-surface-2: oklch(0.20 0.012 60)`, `--vl-surface-3: oklch(0.22 0.012 60)`, `--vl-surface-4: oklch(0.25 0.012 60)`, `--vl-border: oklch(0.28 0.012 60)`, `--vl-text: oklch(0.94 0.01 80)`, `--vl-text-2: oklch(0.72 0.015 70)`, `--vl-text-3: oklch(0.60 0.012 70)`, `--vl-accent: oklch(0.80 0.13 75)`, `--vl-accent-bg: oklch(0.80 0.13 75 / 0.12)`, `--vl-accent-ink: oklch(0.18 0.01 60)`, `--vl-warn: oklch(0.78 0.10 60)`, `--vl-error: oklch(0.72 0.15 30)`; tipos `--vl-tipo-floresta` (#6cbf72), `--vl-tipo-planicie: oklch(0.80 0.13 120)`, `--vl-tipo-urbana` (#e8b55a), `--vl-tipo-litoral: oklch(0.80 0.08 215)`, `--vl-tipo-montanha` (#a9afba); bônus `--vl-bonus-floresta|barreiro|plantacoes|criacoes|rocha|ferro|carvao|salinas|enxofre|militar|industria|comercio|desenvolvimento` com os oklch da tabela do `design-tokens.md`; jazidas como aliases (`--vl-jazida-floresta: var(--vl-bonus-floresta)`, `rocha→rocha`, `barreiro→barreiro`, `veio-de-ferro→ferro`, `veio-de-carvao→carvao`, `salina→salinas`, `enxofre→enxofre`, `campo→plantacoes`); fontes `--vl-font-display: 'Bricolage Grotesque', sans-serif`, `--vl-font-sans: 'IBM Plex Sans', sans-serif`, `--vl-font-mono: 'IBM Plex Mono', monospace`; raios `--vl-radius-chip: 6px`, `-tab: 8px`, `-slot: 10px`, `-tile: 12px`, `-card: 14px`, `-panel: 16px`, `-pill: 999px`. `body { background: var(--vl-bg); color: var(--vl-text); font-family: var(--vl-font-sans); margin: 0 }`. Prefixo `--vl-` evita colisão com `--p-*` do PrimeVue.
- Fontes: `<link>` do Google Fonts (mesma URL do protótipo `Criar Vila.dc.html`: Bricolage Grotesque 500/700, IBM Plex Sans 400/500/600, IBM Plex Mono 400/500) em `/frontend/index.html` **e** em `/src/main/resources/templates/sistema/seguro/app/index.html` (o template do backend é cópia manual do build; manter as linhas de assets com hash). `<html lang="pt-BR">`, `<title>Vilarejo</title>`.
- Preset PrimeVue: `/frontend/src/theme/vilarejo.ts` (novo) com `definePreset(Aura, {...})` de `@primeuix/themes`: `semantic.primary` = escala âmbar ancorada em `#e8b55a` (500) e `semantic.colorScheme.dark` com `surface` 0..950 quentes (950 `#1a1714`, 900 `#211d19`, 800 `#27231f`, 700 `#332e29`, 600 `#3d3731`, …, 0 `#ffffff`), `primary.color` `#e8b55a`, `primary.contrastColor` `#1d1a17`, `highlight` com fundo âmbar 12–16%. Em `/frontend/src/main.ts`: `theme: { preset: Vilarejo, options: { darkModeSelector: '.vl-escuro' } }`, importa `./styles/tokens.css` e adiciona `document.documentElement.classList.add('vl-escuro')` antes do `mount` (sempre escuro). Licença `VITE_PRIMEUI_LICENSE` continua. Validar a API com a skill `primevue:primevue-theming-customization`.
- Alternativa considerada: só CSS vars sem preset (recomendação inicial da análise) — rejeitada porque o tema vale para o jogo inteiro e há dezenas de componentes PrimeVue (DataTable, Dialog, Drawer, Tabs, InputNumber, Button, Select) que ficariam claros sobre fundo escuro.

### D16. Migração visual do jogo inteiro

- Layout `Jogo.vue` usa `CabecalhoJogo` (logo "Vilarejo", abas Mapa · Estoque · Famílias · Mercado · Inventário · Batalhas com a ativa em `--vl-surface-4` via `route.meta.abaAtiva`, pílulas "TURNO n" e "PRÓXIMO TURNO mm:ss" em `--vl-accent`, botão-pílula "Relatório" que abre o `Drawer`) em **todas** as rotas; `BarraTurno.vue` e o menu de `RouterLink` são removidos. Nas rotas `meta.etapaInicial` as abas ficam inativas e o botão Relatório some.
- Regras de migração de cada tela: trocar cores fixas (hex/rgb/`var(--p-*, #fallback)` claros) por tokens `--vl-*`; títulos em `--vl-font-display`, números em `--vl-font-mono`; raios pelos tokens; componentes PrimeVue sem `style` de cor (o preset cuida); manter estrutura, textos, `data-testid` e comportamento; specs existentes continuam passando (ajustar só seletores/classes que mudarem); sem mudanças em lógica, composables ou contratos.
- Grupos (uma task cada): região e construção; estoque e mercado; famílias e cidadão; inventário, oficina e itens; quartel e batalhas. Telas novas (criação/distribuição) e Mapa já nascem com tokens.

### D17. Documentação docs/jogo

- Seguir o formato dos docs existentes (Resumo, Regras R#, Números e tabelas, Exemplos, Interações, Modelo de dados, Questões em aberto). Regras do handoff marcadas `[proposta]` passam a valer (remover a marca quando decididas aqui).
- Renomear `h-001-tarefa-002-geracao-de-jazidas-por-semente.md` → `h-001-tarefa-002-geracao-do-mapa-por-semente.md` (com `git mv`) e corrigir links (`h-001-criar-vila-...md`, `/docs/jogo/plano-de-construcao.md`).
- Corrigir modelos de dados dos docs para ids BIGINT (não UUID) e citar `V17__regioes_v2_bonus_e_previa.sql`.

## Risks / Trade-offs

- Bônus acumulados sem teto (vila com muitas regiões pode passar de +150%) → aceito na v1; anotar em Open Questions para balanceamento; o cálculo está isolado em `BonusRegiaoService.fator`, fácil de limitar depois.
- Mapa Java ≠ mapa JS para a mesma semente (PRNG diferente) → aceitável: o servidor é a única fonte; frontend nunca gera mapa.
- Divergência TS×Java na distribuição → mitigada pela fixture compartilhada testada nos dois lados; backend revalida tudo.
- Tema escuro em todas as telas pode quebrar contraste/legibilidade de telas menos revistas → migração por grupos com verificação visual na task final; preset cobre componentes PrimeVue.
- Template do backend é cópia manual do build → a task de tokens adiciona o link das fontes nos dois arquivos; a verificação final confere.
- Remover `RURAL/COLETA` toca ~15 testes → feito por último (task 5.6), com `@Deprecated` no meio do caminho para não travar tasks paralelas.
- Vila parada na tela 2 não envelhece (turno pula) → desejado.
- Sem máximo por atributo pode desequilibrar (VIT 20) → decisão do handoff; eficiência continua limitada a 3,0.

## Migration Plan

1. Usuário para a aplicação e **recria o banco** (`login_base`) vazio; Flyway aplica V1..V17.
2. Deploy do backend e do frontend juntos (contratos quebram; não há compatibilidade com o frontend antigo).
3. Após `npm run build`, copiar o `index.html` gerado para o template do backend (processo atual) mantendo o link das fontes.
Rollback: voltar a versão anterior do código e recriar o banco (V17 não tem down).

## Open Questions

- Teto para bônus acumulados (ex.: limitar o fator a +150%)? Hoje sem teto.
- Plano inicial variável pelas regiões escolhidas (ex.: +Mineiro com Montanha)? Fora desta change.
- Nomes fixos das famílias do handoff (Oliveira/Lima/Almeida/Pereira)? Mantidos gerados.
- Limite/custo para "Gerar novo mapa" no futuro (a `rodada` já é guardada).
