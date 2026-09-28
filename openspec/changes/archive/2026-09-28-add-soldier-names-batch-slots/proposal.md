# Proposal

## Why

Atualmente, as unidades do quartel nascem anônimas, sem nome próprio. O jogador treina uma unidade por vez, sem visão do equipamento que ela vai receber. Na interface, falta uma tela de detalhe para explorar os atributos e slots de equipamento de cada unidade. Estas limitações reduzem a conexão emocional com as tropas e o engajamento no sistema de equipamento.

## What Changes

- **Nome e sobrenome da unidade**: Toda unidade nasce com nome (p. ex. "Ana Silva") sorteado de duas listas (nome e sobrenome) carregadas do classpath. Sorteio independente e uniforme; nome permanente.
- **Sufixo para nomes repetidos**: quando o "nome sobrenome" já existiu na vila, a exibição ganha sufixo ordinal — "Ana Silva (2)", "Ana Silva (3)"; a primeira ocorrência fica sem sufixo. Contagem por vila (jogador), histórica (inclui unidades mortas), via contador persistido (`jogo_contadores_nome`) e ordinal gravado na unidade (`jogo_unidades.ordinal_nome`).
- **Troca de equipamento**: nos slots Arma e Armadura, o jogador troca o item por outro compatível `DISPONIVEL` (arma do modelo exigido pelo tipo; qualquer armadura); o item retirado volta ao inventário. Proibido com a unidade `EM_MASMORRA` (422 `UNIDADE_EM_MASMORRA`); sem desequipar. Novo endpoint `POST /api/jogo/unidades/{id}/equipamento`.
- **Treino em lote**: Em vez de treinar uma unidade por vez, o jogador configura tipo de tropa, nível da arma, modelo e nível da armadura, e **quantidade** (1–15, limitada pelo máximo treinável). Servidor valida limites (armas, armaduras, comida, capacidade) e rejeita com 422 se quantidade for insuficiente. Botão "Máx." preenche o máximo treinável = min(armas, armaduras, ⌊comida ÷ comida por treino⌋, capacidade livre).
- **Tela de detalhe da unidade**: Nova rota `/quartel/unidades/:id` exibe nome, tipo, status, atributos finais e 9 slots de equipamento em grid (2 preenchidos na criação: Arma e Armadura; 7 vazios para extensão futura). Os slots Arma e Armadura têm ação "Trocar".
- **BREAKING: Mudança de API**: `POST /api/jogo/quartel/ordens` muda de `{tipo, armaId, armaduraId}` para `{tipo, armaNivel, armaduraModelo, armaduraNivel, quantidade}` (seleção por configuração em vez de ids).
- **BREAKING: Mudança de `UnidadeDto`**: Perde `armaId`/`armaduraId`, ganha `nome`, `sobrenome`, `ordinalNome`, `nomeExibicao` (ex.: "Ana Silva (2)") e `equipamento` (LinkedHashMap com 9 slots).
- **Migração V4**: `jogo_unidades` recebe colunas `nome`/`sobrenome` (NOT NULL) e `ordinal_nome` (NOT NULL, ≥ 1); nova tabela `jogo_contadores_nome` (contador histórico por vila e nome); `jogo_itens` recebe coluna `ordem_id` (vinculação aos itens reservados); `jogo_ordens` perde `arma_item_id`/`armadura_item_id`.
- **JSONs movidos**: Arquivos `nome_pessoas.json` e `sobrenome_pessoas.json` movem-se de `docs/` para `src/main/resources/jogo/nomes/` via `git mv`.

## Capabilities

### New Capabilities

<!-- Nenhuma nova capability introduzida. -->

### Modified Capabilities

- `game-army`: Nome e sorteio na conclusão do treino; sufixo ordinal para nomes repetidos (contagem por vila, histórica); treino em lote com validação estrita; slots de equipamento (9 slots, 2 preenchidos na criação); troca de Arma/Armadura fora da masmorra; morte destrói os itens equipados no momento.
- `game-data`: Colunas `nome`/`sobrenome`/`ordinal_nome` em `jogo_unidades` (migração V4); tabela `jogo_contadores_nome`; coluna `ordem_id` em `jogo_itens`; remoção de `arma_item_id`/`armadura_item_id` de `jogo_ordens`.
- `game-frontend`: Seleção de lote por configuração; botão "Máx."; coluna "Nome" (com sufixo) clicável que navega ao detalhe; tela de detalhe com 9 slots e ação "Trocar" em Arma/Armadura.
- `frontend-app`: Nova rota SPA `/quartel/unidades/{id}` servida pelo backend.

## Impact

**Código backend:**
- `src/main/java/com/example/loginbase/jogo/dominio/Unidade.java`: novos campos `nome`, `sobrenome`, `ordinalNome`; método `nomeExibicao()`.
- `src/main/java/com/example/loginbase/jogo/dominio/Item.java`: novo campo `ordem_id` (FK nullable).
- `src/main/java/com/example/loginbase/jogo/dominio/Ordem.java`: remoção de `arma_item_id`, `armadura_item_id`.
- `src/main/java/com/example/loginbase/jogo/dominio/ItemRepository.java`: novos queries por `ordem_id`.
- `src/main/java/com/example/loginbase/jogo/quartel/GeradorNomes.java` (novo): carrega listas e sorteia.
- `src/main/java/com/example/loginbase/jogo/dominio/ContadorNome.java` e `ContadorNomeRepository.java` (novos): contador histórico de nomes por vila.
- `src/main/java/com/example/loginbase/jogo/quartel/NumeradorNomes.java` (novo): próximo ordinal por vila/nome.
- `src/main/java/com/example/loginbase/jogo/quartel/EquipamentoService.java` (novo): troca de Arma/Armadura.
- `src/main/java/com/example/loginbase/jogo/api/TrocarEquipamentoRequest.java` (novo).
- `src/main/java/com/example/loginbase/jogo/CodigoErro.java`: novo `UNIDADE_EM_MASMORRA`.
- `src/main/java/com/example/loginbase/jogo/quartel/QuartelService.java`: nova assinatura `treinar(usuarioId, tipo, armaNivel, armaduraModelo, armaduraNivel, quantidade)`.
- `src/main/java/com/example/loginbase/jogo/economia/AplicadorOrdens.java`: novo `aplicarTreino` com sorteio de nomes e formação de pares.
- `src/main/java/com/example/loginbase/jogo/masmorra/MasmorraService.java`: morte itera slots e libera itens.
- `src/main/java/com/example/loginbase/jogo/catalogo/SlotEquipamento.java` (novo): enum dos 9 slots.
- `src/main/java/com/example/loginbase/jogo/api/TreinarRequest.java`: nova estrutura.
- `src/main/java/com/example/loginbase/jogo/api/VilaDto.java`, `UnidadeDto`: novo nome/sobrenome, equipamento (9 slots).
- `src/main/java/com/example/loginbase/jogo/api/JogoMapper.java`: mapear 9 slots.
- `src/main/java/com/example/loginbase/jogo/api/AcoesVilaController.java`: novo corpo de treino; endpoint `POST /unidades/{id}/equipamento`.
- `src/main/java/com/example/loginbase/web/PaginaController.java`: incluir rota `/quartel/unidades/{id}`.
- `src/main/java/com/example/loginbase/jogo/config/JogoConfig.java`: bean `AleatorioNomes`.

**Dados:**
- `src/main/resources/jogo/nomes/nome_pessoas.json` (movido de `docs/`).
- `src/main/resources/jogo/nomes/sobrenome_pessoas.json` (movido de `docs/`).
- `src/main/resources/db/migration/V4__unidade_nome_e_lote_treino.sql`: migração.

**Frontend:**
- `frontend/src/views/QuartelView.vue`: selects de nível/modelo/nível, quantidade com "Máx.", coluna Nome clicável.
- `frontend/src/views/UnidadeDetalheView.vue` (novo): detalhe com 9 slots e diálogo de troca.
- `frontend/src/router/index.ts`: rota `/quartel/unidades/:id`.
- `frontend/src/api/tipos.ts`: novo `UnidadeDto`, `TreinarRequest`.
- `frontend/src/api/jogo.ts`: novo corpo em `treinar()`; `trocarEquipamento()`.

**Testes:**
- `src/test/java/com/example/loginbase/jogo/dominio/RepositoriosJogoTest.java`: V4.
- `src/test/java/com/example/loginbase/jogo/quartel/GeradorNomesTest.java` (novo).
- `src/test/java/com/example/loginbase/jogo/quartel/NumeradorNomesTest.java` (novo).
- `src/test/java/com/example/loginbase/jogo/quartel/EquipamentoServiceTest.java` (novo).
- `src/test/java/com/example/loginbase/jogo/quartel/QuartelServiceTest.java`: treino em lote.
- `src/test/java/com/example/loginbase/jogo/economia/VilaServiceTest.java`: pares e nomes.
- `src/test/java/com/example/loginbase/jogo/masmorra/MasmorraServiceTest.java`: morte com slots.
- `src/test/java/com/example/loginbase/jogo/api/AcoesVilaControllerWebMvcTest.java`: novo corpo; troca de equipamento.
- `src/test/java/com/example/loginbase/jogo/api/VilaControllerWebMvcTest.java`: UnidadeDto com 9 slots.
- `src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java`: rota `/quartel/unidades/{id}`.

**Documentação:**
- `docs/05-modelo-dados.md`: V4, colunas de Unidade, Item e Ordem.
- `docs/06-api-rest.md`: novo corpo de treino, UnidadeDto com 9 slots, rota de detalhe.
- `docs/02-requisitos.md`: RF-EXE-007, RF-EXE-008, RF-EXE-009 (novos), ajustes em RF-EXE-003 e RF-EXE-005.
- `docs/13-manual-jogador.md`: §3.4 Quartel, "Treinar tropas", passo 6 do tutorial.
- `docs/15-rastreabilidade.md`: linhas dos novos RFs.
- `docs/16-historico-changelog.md`: entrada.

## Documentos relacionados

- [GDD — Tropas e quartel (12.7)](/docs/12-gdd/12.7-gdd-tropas-e-quartel.md)
- [GDD — Itens e forja (12.6)](/docs/12-gdd/12.6-gdd-itens-e-forja.md)
- [GDD — Interface (12.11)](/docs/12-gdd/12.11-gdd-interface-6-telas.md)
- [GDD — Simplificações (12.12)](/docs/12-gdd/12.12-gdd-simplificacoes-intencionais.md)
- [GDD — Extensões futuras (12.13)](/docs/12-gdd/12.13-gdd-extensoes-futuras.md)
- [GDD — Exemplos numéricos (12.14)](/docs/12-gdd/12.14-gdd-exemplos-numericos.md)
- [Game Design Document (índice 12)](/docs/12-gdd.md)
- [Arquivo de nomes (novo local)](/src/main/resources/jogo/nomes/nome_pessoas.json)
- [Arquivo de sobrenomes (novo local)](/src/main/resources/jogo/nomes/sobrenome_pessoas.json)
- [Spec: game-army](/openspec/specs/game-army/spec.md)
- [Spec: game-data](/openspec/specs/game-data/spec.md)
- [Spec: game-frontend](/openspec/specs/game-frontend/spec.md)
- [Spec: frontend-app](/openspec/specs/frontend-app/spec.md)
