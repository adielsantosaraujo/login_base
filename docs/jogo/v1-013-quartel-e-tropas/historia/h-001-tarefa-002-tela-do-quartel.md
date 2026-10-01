# H-001 · Tarefa 002 — Tela do quartel

**História:** [h-001-formar-tropa-no-quartel.md](h-001-formar-tropa-no-quartel.md) · **Domínio:** [../tropas.md](../tropas.md) · **Depende de:** [h-001-tarefa-001-modelo-e-regras-de-tropa.md](h-001-tarefa-001-modelo-e-regras-de-tropa.md) · **Camada:** Frontend

## Objetivo

Criar a tela do quartel no frontend onde o jogador visualiza guerreiros disponíveis, forma tropas definindo membros e posições (Frente/Retaguarda), e gerencia tropas aquarteladas.

## Contexto necessário

- [../tropas.md](../tropas.md) — Estados de tropa (AQUARTELADA, EM_VIAGEM_IDA, EM_VIAGEM_VOLTA)
  > Apenas tropas em estado AQUARTELADA podem ser editadas ou desfeitas.

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) — PE Guerreiro, requisitos de arma
  > Tela mostra lista de cidadãos elegíveis (arma, PE ≥ 1, 16–54 anos, não em tropa).

- [../../v1-003-construcoes/construcoes.md](../../v1-003-construcoes/construcoes.md) — Quartel como prédio
  > Acesso à tela pelo painel de construção (região 10x10).

## Frontend

### Componentes PrimeVue

- **QuartelPanel.vue** — layout da tela
  - Header: nome do quartel, nível (N1/N2/N3), capacidade (ex.: "3/5 membros").
  - Abas: "Guerreiros" (disponíveis), "Tropas" (existentes), "Formar tropa" (modal).

- **GuerreirosDisponivelTable.vue** — lista de cidadãos elegíveis
  - Colunas: Nome, Idade, PE Guerreiro, Arma (tipo+nível), Status (livre/em tropa).
  - Filtro: por PE, por arma, apenas jovens.
  - Botão "Adicionar à seleção" (na modal de formação).

- **TropasListTable.vue** — lista de tropas do quartel
  - Colunas: Nome, Estado, Membros (número), Posição detalhada (Frente/Retaguarda).
  - Ações: "Editar membros" (se AQUARTELADA), "Enviar expedição", "Desfazer" (se AQUARTELADA).
  - Botão "Nova tropa".

- **FormarTropaModal.vue** — dialog para criar ou editar tropa
  - Campo de nome da tropa.
  - Lista de cidadãos "Selecionados" com campos de posição (Frente/Retaguarda).
  - Botão "Adicionar" (busca guerreiro disponível), "Remover", "Salvar tropa".
  - Validação em tempo real: se PE insuficiente ou sem arma, desabilita adição.
  - Exibe limite de capacidade (ex.: "3/5 membros; máx. 2 Retaguarda").

### Rotas

- `/jogo/regiao/{indice}/construcao/{construcaoId}` — abre painel da construção (inclui quartel).

### Chamadas de API

- **GET** `/api/jogo/quartel/{quartelId}/guerreiros-disponiveis`
  - Response: lista de cidadãos com PE, arma, idade.

- **GET** `/api/jogo/quartel/{quartelId}/tropas`
  - Response: lista de tropas com membros e posições.

- **POST** `/api/jogo/quartel/{quartelId}/tropa`
  - Body: `{ "nome": "...", "membros": [ { "cidadaoId": 123, "posicao": "FRENTE" }, ... ] }`
  - Response: tropa criada.

- **PATCH** `/api/jogo/tropa/{tropaId}` (opcional: editar nome/membros)
  - Body: `{ "nome": "...", "membros": [...] }`

- **DELETE** `/api/jogo/tropa/{tropaId}`
  - Response 204: tropa desfeita.

## Arquivos prováveis

- [/frontend/src/views/Jogo/QuartelPanel.vue](/frontend/src/views/Jogo/QuartelPanel.vue) (novo)
- [/frontend/src/components/Quartel/GuerreirosDisponivelTable.vue](/frontend/src/components/Quartel/GuerreirosDisponivelTable.vue) (novo)
- [/frontend/src/components/Quartel/TropasListTable.vue](/frontend/src/components/Quartel/TropasListTable.vue) (novo)
- [/frontend/src/components/Quartel/FormarTropaModal.vue](/frontend/src/components/Quartel/FormarTropaModal.vue) (novo)
- [/frontend/src/services/quartelService.js](/frontend/src/services/quartelService.js) (novo)

## Testes

- Teste visual:
  - Abrir tela de quartel N1; verificar limite exibido (5 membros).
  - Listar guerreiros; adicionar 1 à seleção; verificar que aparece na aba "Selecionados".
  - Submeter; verificar que nova tropa aparece na lista com estado AQUARTELADA.

- Teste de validação:
  - Tentar adicionar cidadão sem arma → desabilitado ou erro em tempo real.
  - Tentar adicionar cidadão com PE 0 → desabilitado.
  - Seleção: 5 membros em N1 → botão Salvar habilitado; 6 → desabilitado.

- Teste de estado:
  - Tropa EM_VIAGEM_IDA → botão "Editar membros" desabilitado; apenas "Detalhes".
  - Tropa AQUARTELADA → botão "Editar", "Desfazer" habilitados.

## Definição de pronto

- CA1–CA6 da história cobertos visualmente.
- Build Frontend (`npm run build`) sem erros.
- Componentes carregam e exibem dados.
- Validações impedem ações inválidas (sem arma, PE insuficiente, capacidade excedida).
- Modal de formação funciona para criar e editar tropas.

## Fora de escopo

- Tema visual (será aplicado depois).
- Internacionalização (i18n).
- Exportar lista de tropas.
