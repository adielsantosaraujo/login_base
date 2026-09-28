# Design

## Context

Conforme descrito em [proposal.md](../proposal.md), o quartel hoje oferece treino anônimo, uma unidade por vez, sem visão do equipamento. Implementação atual:
- `TreinarRequest` = `{tipo, armaId, armaduraId}` (ids específicos).
- `QuartelService.treinar()` cria 1 unidade, 1 ordem TREINO com `quantidade=1`, ocupa `arma_item_id` e `armadura_item_id` diretamente em `jogo_ordens`.
- `jogo_unidades` sem campo de nome; `UnidadeDto` com `armaId`/`armaduraId`.
- Sem tela de detalhe; lista mostra apenas tipo e status.
- `Aleatorio` bean único; novo sorteio em testes deslocaria sequências determinísticas de loot/IA.
- Arquivos de nomes (`nome_pessoas.json`, `sobrenome_pessoas.json`) em `docs/`.

A change deve implementar nome + sobrenome, treino em lote, tela de detalhe com 9 slots e persistência dos nomes em V4, com JSONs movidos para classpath.

## Goals / Non-Goals

**Goals:**

- Toda unidade nasce com nome e sobrenome, sorteados uniformes e independentes de listas carregadas do classpath na inicialização.
- Nomes repetidos exibidos com sufixo ordinal ("Ana Silva (2)"), contagem por vila e histórica (inclui mortas), com ordinal persistido na unidade e contador persistido por vila/nome.
- Treino em lote: jogador escolhe tipo, nível da arma, modelo e nível da armadura, quantidade (1–15); servidor valida com rejeição 422 se insuficiente; botão "Máx." preenche o máximo treinável = min(armas, armaduras, ⌊comida ÷ comida por treino⌋, capacidade livre).
- Tela de detalhe em rota `/quartel/unidades/:id` exibindo nome, tipo, status, atributos finais e 9 slots de equipamento (2 preenchidos na criação, 7 vazios).
- Migração V4 persistindo nome/sobrenome/ordinal em `jogo_unidades` (NOT NULL).
- JSONs movidos para `src/main/resources/jogo/nomes/` via `git mv`.
- Troca de Arma/Armadura por item compatível `DISPONIVEL`, fora da masmorra; item retirado volta ao inventário.
- API **BREAKING**: novo corpo POST e novo `UnidadeDto` com 9 slots.

**Non-Goals:**

- Permitir renomear unidades ou editar nomes (nomes permanentes).
- Desequipar (deixar Arma/Armadura vazios) ou equipar slots futuros (CABECA…ANEL_3).
- Trocar equipamento com a unidade `EM_MASMORRA` ou mover item diretamente entre unidades (só itens `DISPONIVEL` entram).
- Reaproveitar ordinais de unidades mortas ou contar unidades mortas antes da V4.
- Exibir nomes no combate (fora do escopo).
- Implementar categorias CABECA, BOTA, LUVA, COLAR, ANEL (itens futuros).

## Decisions

### D1 – Listas de nomes como recurso de classpath

**Decisão:** Mover `nome_pessoas.json` e `sobrenome_pessoas.json` de `docs/` para `src/main/resources/jogo/nomes/` via `git mv`. Carregamento via `ClassPathResource` no Spring.

**Rationale:** Classpath padrão em aplicações Spring; Dockerfile já copia `src/` completo. Sem necessidade de configuração extra em pom.xml ou volumes Docker. Falha rápida se lista vazia/ausente: construtor de `GeradorNomes` tira a aplicação do ar na inicialização.

**Alternativa descartada:** Manter em `docs/` e copiar via `<resources>` em pom.xml + volume Docker; adiciona duas fontes de verdade no pipeline e complexidade de build.

### D2 – `GeradorNomes` e bean separado `AleatorioNomes`

**Decisão:** Classe `GeradorNomes` (`jogo/quartel/GeradorNomes.java`, `@Component`) carrega as 2 listas no construtor via Jackson + `ClassPathResource`. Sorteia via bean separado `AleatorioNomes` (ou `@Qualifier("aleatorioNomes")` no `Aleatorio`).

**Rationale:** Bean `Aleatorio` único é usado em loot/IA com sequências determinísticas nos testes. Novo sorteio no mesmo bean deslocaria aquelas sequências. `AleatorioNomes` isolado permite testes determinísticos sem lado.

**Alternativa descartada:** Usar `Aleatorio` direto; riscos de regression em loot/IA existentes.

### D3 – Migração V4 com backfill determinístico

**Decisão:** SQL `V4__unidade_nome_e_lote_treino.sql` executa:
1. Add `nome` varchar(60), `sobrenome` varchar(60) nullable em `jogo_unidades`.
2. Backfill determinístico: `nome = (array[10 primeiros nomes])[1 + id % 10]`, `sobrenome = (array[10 primeiros sobrenomes])[1 + (id / 10) % 10]`; SET NOT NULL.
3. Add `ordinal_nome` int; backfill `row_number() over (partition by vila_id, nome, sobrenome order by id)`; SET NOT NULL, CHECK `>= 1`, UNIQUE `(vila_id, nome, sobrenome, ordinal_nome)`.
4. Create `jogo_contadores_nome` (ver D11) e semear com `count(*)` agrupado por `(vila_id, nome, sobrenome)`, auditoria `sistema`/`now()`.
5. Add `ordem_id` bigint nullable em `jogo_itens` + FK para `jogo_ordens(id)` + índice `ix_jogo_itens_ordem`.
6. Migrar ordens TREINO em andamento: `UPDATE jogo_itens i SET ordem_id = o.id FROM jogo_ordens o WHERE o.categoria = 'TREINO' AND i.id IN (o.arma_item_id, o.armadura_item_id)`.
7. Remove FKs `arma_item_id`, `armadura_item_id` de `jogo_ordens`; remove as colunas.

**Rationale:** Unidades pré-existentes herdam nomes determinísticos sem duplicação; `ordem_id` vincula itens à ordem sem tabela de junção (auditoria). Usar uma coluna em item reduz schema complexity e satisfaz restrição de auditoria (tabela de junção exigiria colunas de auditoria).

**Alternativa descartada:** Não migrar nomes de unidades existentes; deixaria hiato visual entre novas e antigas.

### D4 – Validação em `QuartelService.treinar()` com rejeição 422

**Decisão:** `treinar(usuarioId, tipo, armaNivel, armaduraModelo, armaduraNivel, quantidade)` valida em série:
1. Nível do quartel, fila (máx. 1 ordem TREINO).
2. Capacidade = `3 × n_quartel − unidades − Σ quantidade ordens TREINO`. Se `quantidade > capacidade`, rejeita `CAPACIDADE_EXERCITO`.
3. Busca N armas `DISPONIVEL`, modelo exigido pelo tipo, nível exato. Se < N, rejeita `ITEM_INDISPONIVEL`.
4. Busca N armaduras `DISPONIVEL`, modelo exato, nível exato. Se < N, rejeita `ITEM_INDISPONIVEL`.
5. `armaduraModelo` deve ser categoria ARMADURA, senão `ITEM_INDISPONIVEL`.
6. Valida comida (quantidade × comida_tipo). Se insuficiente, rejeita `RECURSOS_INSUFICIENTES`.
7. Reserva N+N itens com `ordem_id = nova_ordem.id` (status `RESERVADO`), débito comida, gera ordem TREINO com `alvo=tipo, nivel=armaNivel, quantidade=quantidade`.

**Rationale:** Validação estrita antes de persistência. Nenhuma ordem é criada se houver erro; cliente recebe 422 claro com código de motivo.

**Alternativa descartada:** Validação soft com fallback automático; confunde jogador ("por que pedi 5 e treinei 3?").

### D5 – Conclusão em `AplicadorOrdens.aplicarTreino()` com sorteio de nomes

**Decisão:** Ao aplicar ordem TREINO concluída:
1. Lista itens com `ordem_id = ordem.id`.
2. Separa por categoria (ARMA, ARMADURA), ordena por id.
3. Para cada par arma+armadura, cria Unidade com `nome` e `sobrenome` sorteados via `GeradorNomes` e `ordinalNome = NumeradorNomes.proximoOrdinal(vilaId, nome, sobrenome)` (ver D11), status DISPONIVEL, `armaItemId`/`armaduraItemId` apontando aos itens.
4. Marca itens `EQUIPADO`, seta `ordem_id = null` (desvincula da ordem).
5. Delete ordem.

**Rationale:** Sorteio acontece na conclusão, cada unidade do lote tem seu sorteio independente. Formação de pares pela ordem de ids garante distribuição uniforme e testável.

**Alternativa descartada:** Sortear antes de criar ordem; acoplaria nomes à criação da ordem (dados de saída misturados com entrada).

### D6 – Enum `SlotEquipamento` e LinkedHashMap no DTO

**Decisão:** Enum `SlotEquipamento` em `jogo/catalogo/SlotEquipamento.java`: `ARMA, ARMADURA, CABECA, BOTA, LUVA, COLAR, ANEL_1, ANEL_2, ANEL_3`, cada um com rótulo (label) e categoria aceita (ARMA, ARMADURA, null para futuros). `UnidadeDto.equipamento` = `LinkedHashMap<SlotEquipamento, ItemDto|null>` com todas as 9 chaves na ordem do enum; slots vazios = null.

**Rationale:** Ordem explícita dos slots; LinkedHashMap preserva ordem no JSON. Cliente renderiza na mesma ordem. Compatível com futura persistência `jogo_itens.unidade_id + slot` sem quebrar DTO.

**Alternativa descartada:** Array indexado ou Map simples; perde ordem e semântica dos slots.

### D7 – API com novo corpo POST e breaking changes

**Decisão:** `TreinarRequest` = `{@NotNull tipo, @Min(1) @Max(5) armaNivel, @NotNull armaduraModelo, @Min(1) @Max(5) armaduraNivel, @Min(1) @Max(15) quantidade}`. `POST /api/jogo/quartel/ordens` recebe esse body (Validação JSR-303). `UnidadeDto` perde `armaId`/`armaduraId`, ganha `nome`, `sobrenome`, `ordinalNome`, `nomeExibicao`, `equipamento: Map<SlotEquipamento, ItemDto|null>`.

**Rationale:** Seleção por configuração reduz bugs de ids inválidos. LinkedHashMap mantém ordem dos 9 slots. O detalhe usa `GET /api/jogo/vila` (sem endpoint de leitura novo); a troca de equipamento tem endpoint próprio (D12).

**Alternativa descartada:** Suportar ambas as formas (legacy + nova); acoplamento, confusão.

### D8 – Morte em `MasmorraService` libera itens de todos os slots

**Decisão:** `MasmorraService.atualizarEsquadraoAoFinal()` ao detectar unidade morta: itera `SlotEquipamento` (hoje ARMA e ARMADURA), lê o item **atualmente** equipado no slot (`armaItemId`/`armaduraItemId`, já refletindo trocas) e o apaga. Itens retirados antes por troca estão `DISPONIVEL` e não são afetados.

**Rationale:** Itera enum, agnóstico ao número de slots. Preparado para extensão 12.13.

**Alternativa descartada:** Deletar por id de item fixo; quebraria com novos slots.

### D9 – Frontend com "Máx." e grid de 9 slots

**Decisão:** `QuartelView.vue` com:
- Selects de `armaNivel` (1–5) e `armaduraModelo` (ARMADURA_COURO, ARMADURA_FERRO) + `armaduraNivel` (1–5), agrupados com contagem ("Couro N1 (4 disponível)").
- `InputNumber` quantidade min 1, max dica calculada (soft; backend valida).
- Botão "Máx." que preenche `quantidade` com a dica (Máximo treinável).
- Coluna "Nome" clicável que navega para `/quartel/unidades/:id`.
- Indicador "Treinando N" somando `quantidade` da ordem em andamento.

Nova `UnidadeDetalheView.vue` em rota `/quartel/unidades/:id`:
- Cabeçalho usa `nomeExibicao` (ex.: "Ana Silva (2)"); a coluna "Nome" da lista do quartel também.
- Atributos: HP, Ataque, Defesa, Alcance, Movimento.
- Grid de 9 Cards (um por slot): rótulo, categoria, item (nome, nível, atributos) ou "Vazio". Cards Arma e Armadura têm botão "Trocar" (desabilitado com a unidade `EM_MASMORRA`) que abre Dialog com os itens compatíveis `DISPONIVEL` de `vila.itens` (arma: `modelo === catalogo.tropas[tipo].armaExigida`; armadura: `categoria === 'ARMADURA'`); escolher um item chama `POST /api/jogo/unidades/{id}/equipamento` (ver D12).
- "Unidade não encontrada" + botão voltar se id inválido.

**Rationale:** Dica de máximo é UX, backend o cálculo exato (quebra em validação estrita). Grid de slots é expansível (12.13 adiciona itens sem quebrar layout).

**Alternativa descartada:** Mostrar máximo no backend e retornar via header; cliente faria cálculo; duplicação de regras.

### D10 – Rota SPA `/quartel/unidades/{id}` no `PaginaController`

**Decisão:** `PaginaController` mapeia `@GetMapping({"/", "/fazenda", ..., "/quartel/unidades/{id}"})` para a view da SPA, fallback de history mode. Teste WebMvc ajustado para testar mapeamento.

**Rationale:** Lista explícita evita conflitar com `/api/**`, `/login`, etc. Seguro contra catch-all `/**` acidental.

**Alternativa descartada:** Catch-all `/**`; captura erros 404 reais, difícil debugar.

### D11 – Sufixo ordinal para nomes duplicados

**Decisão:** Escopo **por vila** (= jogador; `jogo_vilas.usuario_id` é único), chave exata `(vila_id, nome, sobrenome)`. Nova tabela `jogo_contadores_nome (id, vila_id FK, nome varchar(60), sobrenome varchar(60), ultimo_ordinal int CHECK >= 1, auditoria, UNIQUE (vila_id, nome, sobrenome))` e coluna `jogo_unidades.ordinal_nome int NOT NULL CHECK >= 1` com UNIQUE `(vila_id, nome, sobrenome, ordinal_nome)`. Componente `NumeradorNomes` (`jogo/quartel`) com `int proximoOrdinal(long vilaId, String nome, String sobrenome)`: busca o contador; se ausente, grava com `ultimo_ordinal = 1` e retorna 1; senão incrementa e retorna o novo valor. O contador nunca é decrementado (a morte apaga a unidade, não o contador). Em `aplicarTreino`, as unidades do lote são criadas em sequência (arma de menor id primeiro) e cada uma chama `proximoOrdinal` logo após seu sorteio → ordinais determinísticos no lote. Exibição por `Unidade#nomeExibicao()`: `ordinal == 1 ? "Nome Sobrenome" : "Nome Sobrenome (N)"`, exposta em `UnidadeDto.nomeExibicao`.

**Rationale:** Unidades mortas são apagadas, então "maior ordinal entre as vivas" perderia a história; o contador persistido preserva a contagem com custo de 1 linha por nome distinto. A trava pessimista da vila (`obterParaAtualizacao`/sincronização) serializa conclusões concorrentes; os UNIQUE garantem integridade. Com `IDENTITY` o INSERT do contador é imediato e a próxima consulta no mesmo lote o encontra. Migração: ordinais por `row_number()` na ordem de id e contadores semeados com `count(*)`; mortas pré-V4 não são recuperáveis (aceito).

**Alternativas descartadas:** soft-delete de unidades (afeta todas as consultas de unidade); contagem global entre jogadores (sem sentido no contexto do exército do jogador); ordinal calculado só na leitura (instável com mortes).

### D12 – Troca de equipamento nos slots Arma e Armadura

**Decisão:** `EquipamentoService.trocar(usuarioId, unidadeId, slot, itemId)` (`jogo/quartel`, `@Transactional`), exposto em `AcoesVilaController` como `POST /api/jogo/unidades/{id}/equipamento` com `TrocarEquipamentoRequest(@NotNull SlotEquipamento slot, @NotNull @Positive Long itemId)`, retornando `VilaDto`. Validações em ordem, com a vila travada via `vilaService.obterParaAtualizacao`:
1. Unidade inexistente ou de outra vila → `RecursoNaoEncontradoException` (404 `NAO_ENCONTRADO`).
2. `status == EM_MASMORRA` → 422 `UNIDADE_EM_MASMORRA` (novo `CodigoErro`).
3. `slot.getCategoriaAceita() == null` (slots futuros) → 422 `ITEM_INDISPONIVEL`.
4. Item inexistente, de outra vila, não `DISPONIVEL` ou incompatível (ARMA: `modelo == tipo.armaExigida()`; ARMADURA: `modelo.categoria() == ARMADURA`) → 422 `ITEM_INDISPONIVEL`.
Efeito: item antigo → `DISPONIVEL`; novo → `EQUIPADO`; `unidade.armaItemId`/`armaduraItemId` = novo id. Sem desequipar (não há endpoint de remoção). Atributos continuam derivados na leitura pelo `JogoMapper`.

**Rationale:** Serviço separado evita conflito com o `QuartelService` (treino). Bloqueio na masmorra mantém coerente o estado tático gravado em `jogo_batalhas` no início da batalha. Arma/Armadura obrigatórias preservam o invariante `NOT NULL` de `jogo_unidades.arma_item_id/armadura_item_id`. POST segue a convenção de ações do projeto (CSRF).

**Alternativa descartada:** permitir desequipar (exigiria colunas nullable e unidade sem arma no combate); reutilizar `UNIDADE_INDISPONIVEL` (menos claro para o jogador).

## Risks / Trade-offs

**[Risco] Arquivo de nomes vazio ou ausente na inicialização**
- **Causa:** Falha no movimento via `git mv` ou arquivo corrompido.
- **Mitigação:** Construtor de `GeradorNomes` tira a aplicação do ar com erro claro na inicialização.

**[Risco] Unidades pré-existentes recebem nomes aleatórios na migração V4**
- **Causa:** Backfill determinístico usa `id % 10` (só 10 nomes únicos para muitos ids).
- **Mitigação:** Aceitável (jogo é sandbox, dados de testes); docs registram. Futuro: ID gerenciado ou sorteio de seed fixa se unicidade for crítica.

**[Risco] Mudança de API breaking; cliente outdated faz POST antigo e falha**
- **Causa:** Cliente não atualizado.
- **Mitigação:** Versão da API deve ser incrementada ou API deve rejeitar corpo antigo com erro claro (ex.: "Campos `armaId`/`armaduraId` não suportados; use `armaNivel`/`armaduraModelo`").

**[Risco] Dica de máximo no cliente fica defasada (backend valida mais rigorosamente)**
- **Causa:** Regras de cálculo mudam sem atualizar frontend.
- **Mitigação:** Documentar que dica é só UX; backend é source of truth. Testes comparam dica com backend.

**[Risco] Novas rotas SPA exigem atualizar `PaginaController` manualmente**
- **Causa:** Lista explícita não é dinâmica.
- **Mitigação:** Documentar padrão. No futuro, considerar catch-all com validação de safe paths se escopo crescer.

**[Risco] Contador e unidades divergirem (ex.: inserção manual no banco)**
- **Mitigação:** UNIQUE `(vila_id, nome, sobrenome, ordinal_nome)` rejeita ordinal repetido entre vivas; toda criação passa por `NumeradorNomes`.

**[Risco] Troca concorrente com início de batalha**
- **Mitigação:** ambos travam a vila (`PESSIMISTIC_WRITE`); a segunda operação vê o status atualizado (`EM_MASMORRA` → 422).

## Migration Plan

**Antes do deploy (desenvolvimento):**
1. Executar testes locais: `./mvnw test` (inclui V4 e GeradorNomes).
2. Validar que JSONs estão em `src/main/resources/jogo/nomes/` via `git mv`.
3. Testar frontend: `cd frontend && npm run build`.
4. Teste manual: treinar lote de 3, abrir detalhe, validar nome/slots, forçar nome repetido (ver 5.1), trocar arma de uma unidade e confirmar bloqueio em masmorra.

**Deploy:**
1. Pull da change para produção.
2. Executar migração V4 (Flyway automático no startup).
3. Restart da aplicação.
4. Validar: POST novo corpo funciona, UnidadeDto retorna 9 slots com nomes, rota `/quartel/unidades/:id` acessível.

**Rollback (se necessário):**
1. Reverter change (desfaz controller, DTO).
2. Executar nova migração corretiva para reverter V4 (Flyway não suporta undo).
3. Restart.
4. Clone anterior não carrega nomes (field missing de `UnidadeDto`), mas não quebra; detalhe 404 se alguém tentar.

## Open Questions

Nenhuma. Todas as decisões de design foram resolvidas com base no plano aprovado pelo usuário e na análise arquitetural existente.
