# H-001 · Tarefa 003 — API de criação da vila

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-002-geracao-do-mapa-por-semente.md](h-001-tarefa-002-geracao-do-mapa-por-semente.md) · **Camada:** Backend · **Spec:** [/openspec/changes/redesenho-criacao-vila-populacao/specs/jogo-criacao-vila/spec.md#requirement-criação-da-vila-a-partir-da-prévia](/openspec/changes/redesenho-criacao-vila-populacao/specs/jogo-criacao-vila/spec.md#requirement-criação-da-vila-a-partir-da-prévia)

## Objetivo

Implementar endpoints de prévia (`POST /api/jogo/vila/previa`, `GET /api/jogo/vila/previa`) e criação (`POST /api/jogo/vila`) conforme §1–§3 do handoff. Validar seleção (3 regiões conexas, ≥1 Urbana, `previaId` vigente), criar vila com 16 regiões (tipo + percentuais de terreno) e 100 ladrilhos por região, estoque e 4 casas iniciais nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura, apagar prévia.

## Contexto necessário

- [Design D3, D4, D5, D6](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d3-prévia-persistida-em-vila_previa)
  > `vila_previa` (usuario_id, previa_id UUID, semente, rodada); V17 cria tabelas.
  > POST/GET `/previa`: resposta com 16 regiões, tipos e 3 percentuais de terreno cada.
  > POST `/vila`: `{ "previaId", "indices": [6, 7, 10] }`; validações em ordem; grava 16 regiões + percentuais de terreno e 100 ladrilhos de cada região possuída; efeitos: 3 possuídas, estoque, 4 casas nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura, 16 cidadãos, prévia apagada.

- [/docs/designe/handoff/api/contratos-api.md](/docs/designe/handoff/api/contratos-api.md) — §1–§3 (Endpoints da Prévia e Criação da Vila)
  > `POST /previa`: 200 ou 409 VILA_JA_EXISTE.
  > `GET /previa`: 200 (mesmo formato) ou 404 ou 409 VILA_JA_EXISTE.
  > `POST /vila`: 201 ou 400 (SELECAO_INVALIDA, REGIAO_NAO_ADJACENTE, SEM_REGIAO_URBANA) ou 409 (VILA_JA_EXISTE, PREVIA_EXPIRADA).

## Backend

**Entidades (novas):**
- [/src/main/java/com/example/loginbase/jogo/modelo/VilaPrevia.java](/src/main/java/com/example/loginbase/jogo/modelo/VilaPrevia.java) — `@Entity`, `usuario_id` (PK), `previa_id` UUID, `semente` bigint, `rodada` int, `criado_em` timestamp.
- [/src/main/java/com/example/loginbase/jogo/modelo/RegiaoTerreno.java](/src/main/java/com/example/loginbase/jogo/modelo/RegiaoTerreno.java) — `@Entity`, PK `id` bigint identity; UKs `(regiao_id, terreno)` e `(regiao_id, posicao)`; checks `posicao` 1–3 e `percentual` 10–60.

**Repositórios (novos):**
- [/src/main/java/com/example/loginbase/jogo/repositorio/VilaPreviaRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/VilaPreviaRepository.java) — finder: `findByUsuarioId`, `deleteByUsuarioId`.
- [/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoRepository.java) (já existe ou novo) — finder: `findByVilaIdAndIndice`, `findAllByVilaId`.
- [/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoTerrenoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoTerrenoRepository.java) (novo) — finder: `findByRegiaoId`.

**Serviços (novos):**
- [/src/main/java/com/example/loginbase/jogo/servico/VilaPreviaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaPreviaService.java)
  - `PreviaMapaDTO gerar(Long usuarioId)` — cria ou renova `vila_previa`; usa `GeradorMapaService.gerar(semente)`.
  - `PreviaMapaDTO obter(Long usuarioId)` throws `PreviaNaoEncontradaException`.
  - `VilaPrevia exigirVigente(Long usuarioId, UUID previaId)` throws `PreviaExpiradaException`.
  - `void remover(Long usuarioId)`.

- [/src/main/java/com/example/loginbase/jogo/servico/VilaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaService.java)
  - `Vila criarVila(Long usuarioId, UUID previaId, List<Integer> indices)` — valida conforme D4 (ordem); retorna `Vila`.
  - `Vila criarVilaComSemente(Long usuarioId, long semente, List<Integer> indices)` — cria a vila com o mapa da semente informada, sem passar pela prévia (usado também pelos testes).
  - Validações em ordem (D4): (1) sem vila; (2) prévia vigente; (3) 3 índices distintos 1..16; (4) conexas (BFS); (5) ≥1 Urbana.
  - Efeitos: grava 16 regiões + percentuais de terreno; marca 3 como possuídas; grava os 100 ladrilhos de cada região possuída (terreno, bonus_base); estoque; 4 casas N1 nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura da 1ª Urbana; 16 cidadãos; apaga prévia.
  - Resposta `{ vilaId, proximaEtapa: DISTRIBUIR_POPULACAO }` é montada no `VilaControlador` como `CriarVilaRespostaDTO`.

**Controlador (novo):**
- [/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java)
  - `POST /api/jogo/vila/previa` → `200 PreviaMapaDTO` ou `409 VILA_JA_EXISTE`.
  - `GET /api/jogo/vila/previa` → `200 PreviaMapaDTO` ou `404 PREVIA_NAO_ENCONTRADA` ou `409 VILA_JA_EXISTE`.
  - `POST /api/jogo/vila` com `{ previaId, indices }` → `201 { vilaId, proximaEtapa }` ou erros (D5).

**DTOs (novos):**
- `PreviaMapaDTO` — `previaId` (UUID), `rodada` (int), `regioes` (List<RegiaoPreviaDTO>).
- `RegiaoPreviaDTO` — `indice` (int), `tipo` (TipoRegiao), `terrenos` (List<RegiaoTerrenoDTO>) — lista dos 3 terrenos com posição (1–3) e percentual.
- `RegiaoTerrenoDTO` — `terreno` (TipoTerreno enum: FLORESTA, BARREIRO, PLANTACOES, CRIACOES, ROCHA, FERRO, CARVAO, SALINAS, ENXOFRE, MILITAR, INDUSTRIA, COMERCIO, DESENVOLVIMENTO), `posicao` (int 1–3), `percentual` (int 10–60).
- `CriarVilaRespostaDTO` — `vilaId` (Long), `proximaEtapa` (String).
- `CriarVilaRequest` — `previaId` (UUID), `indices` (List<Integer>).

**Exceções (novas ou atualizar):**
- `JogoException` ganha getter `String codigo` e construtor com código.
- `VilaJaExisteException` → `VILA_JA_EXISTE` (409).
- `PreviaNaoEncontradaException` → `PREVIA_NAO_ENCONTRADA` (404).
- `PreviaExpiradaException` → `PREVIA_EXPIRADA` (409); mensagem: "O mapa mudou. Escolha as regiões novamente".
- `SelecaoInvalidaException` → `SELECAO_INVALIDA` (400).
- `RegiaoNaoAdjacenteException` → `REGIAO_NAO_ADJACENTE` (400).
- `UrbanaObrigatoriaException` → `SEM_REGIAO_URBANA` (400).

## Frontend

Não se aplica (será chamado pela tarefa 004).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/modelo/VilaPrevia.java](/src/main/java/com/example/loginbase/jogo/modelo/VilaPrevia.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/modelo/RegiaoTerreno.java](/src/main/java/com/example/loginbase/jogo/modelo/RegiaoTerreno.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/repositorio/VilaPreviaRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/VilaPreviaRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoTerrenoRepository.java](/src/main/java/com/example/loginbase/jogo/repositorio/RegiaoTerrenoRepository.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/VilaPreviaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaPreviaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/VilaService.java](/src/main/java/com/example/loginbase/jogo/servico/VilaService.java) (reescrever)
- [/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/VilaControlador.java) (novo ou atualizar)
- [/src/main/java/com/example/loginbase/jogo/dto/PreviaMapaDTO.java](/src/main/java/com/example/loginbase/jogo/dto/PreviaMapaDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoPreviaDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoPreviaDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoTerrenoDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoTerrenoDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/CriarVilaRequest.java](/src/main/java/com/example/loginbase/jogo/dto/CriarVilaRequest.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/CriarVilaRespostaDTO.java](/src/main/java/com/example/loginbase/jogo/dto/CriarVilaRespostaDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/excecao/PreviaNaoEncontradaException.java](/src/main/java/com/example/loginbase/jogo/excecao/PreviaNaoEncontradaException.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/excecao/PreviaExpiradaException.java](/src/main/java/com/example/loginbase/jogo/excecao/PreviaExpiradaException.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/excecao/SelecaoInvalidaException.java](/src/main/java/com/example/loginbase/jogo/excecao/SelecaoInvalidaException.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/comum/CodigoErro.java](/src/main/java/com/example/loginbase/jogo/comum/CodigoErro.java) (novo)
- [/src/main/resources/db/migration/V17__regioes_v2_terreno_e_previa.sql](/src/main/resources/db/migration/V17__regioes_v2_terreno_e_previa.sql) (novo)
- Atualizar: `JogoException`, `VilaJaExisteException`, `RegiaoNaoAdjacenteException`, `UrbanaObrigatoriaException`.

## Testes

- **Prévia geração:** POST `/previa` sem vila → 200, `rodada` 1, 16 regiões, tipos e percentuais de terreno; POST novamente → `rodada` 2, novo `previaId`.
- **Prévia obtenção:** GET `/previa` com prévia → 200 (mesmo formato); sem prévia → 404; com vila → 409 VILA_JA_EXISTE.
- **Criação simples:** POST `/vila` com regiões 6, 7, 10 (conexas, ≥1 Urbana) → 201 `{ vilaId, proximaEtapa: DISTRIBUIR_POPULACAO }`.
- **Validação: seleção invalida:** 3 regiões não distintas ou índices > 16 → 400 SELECAO_INVALIDA.
- **Validação: não conexas:** regiões 1, 3, 16 (desconectadas) → 400 REGIAO_NAO_ADJACENTE.
- **Validação: sem Urbana:** 3 regiões sem nenhuma Urbana → 400 SEM_REGIAO_URBANA.
- **Validação: prévia expirada:** POST com `previaId` inválido ou desfasado → 409 PREVIA_EXPIRADA.
- **Unicidade:** usuário com vila tenta POST `/vila` → 409 VILA_JA_EXISTE.
- **Efeitos:** 16 regiões criadas com percentuais de terreno; 3 marcadas `possuida=true`; 100 ladrilhos gravados para cada região possuída; estoque com valores iniciais; 4 casas N1 nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura; 16 cidadãos; prévia apagada.
- **Concorrência:** `uk_vila_usuario` previne duplicatas.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA9.
- Build (`./mvnw verify`) sem erros.
- Endpoints passam em testes de integração.
- Contrato conforme D4, erros conforme D5.

## Fora de escopo

- UI/frontend (tarefa 004).
- Edição/remoção de vila após criação.
