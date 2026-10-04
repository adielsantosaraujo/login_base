# H-001 · Tarefa 004 — Tela de criação da vila

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-003-api-de-criacao-da-vila.md](h-001-tarefa-003-api-de-criacao-da-vila.md) · **Camada:** Frontend · **Tela:** [/docs/designe/handoff/telas/tela-01-criar-vila.md](/docs/designe/handoff/telas/tela-01-criar-vila.md)

## Objetivo

Implementar a tela de criação de vila (Vue 3 + PrimeVue, alta fidelidade) conforme handoff. Exibir prévia do mapa (tipos e composição de terrenos de cada região), permitir seleção de 3 regiões conexas (≥1 Urbana), validar em tempo real e submeter POST `/vila`.

## Contexto necessário

- [/docs/designe/handoff/telas/tela-01-criar-vila.md](/docs/designe/handoff/telas/tela-01-criar-vila.md) — layout completo (seções Mapa, Painel, Card, Estado)
  > Mapa 4×4, grade com gap 10px, tiles 150px mín, composição de terrenos com barras (largura = percentual ÷ 60, máximo 60%), checklist, botão "Gerar novo mapa", painel "Sua seleção", terrenos de região (barras 6px), CTA "Criar vila".

- [/docs/designe/handoff/api/contratos-api.md](/docs/designe/handoff/api/contratos-api.md) — §1–§3
  > `POST /previa`, `GET /previa`, `POST /vila`; respostas e erros.

- [/docs/designe/handoff/telas/design-tokens.md](/docs/designe/handoff/telas/design-tokens.md) — cores, tamanhos, fonte (Mono para números).

## Frontend

**Página/View (nova):**
- [/frontend/src/views/CriacaoVila.vue](/frontend/src/views/CriacaoVila.vue) — layout conforme handoff (header, main, grid, painel).
  - **Estado:**
    - `previa`: resposta de GET/POST `/previa` (16 regiões, tipos, bônus).
    - `selecionadas: number[]`: índices selecionados (ordem).
    - `hover: number | null`: região em foco.
    - `gerando: boolean`: spinner durante POST `/previa` (gerar novo mapa).
    - `enviando: boolean`: spinner durante POST `/vila` (criar vila).
    - `erro: string | null`: mensagem de erro.
  - **Derivados (via `useCriacaoVila`):**
    - `conectado`: estado autenticado.
    - `totaisLadrilhos`: total de ladrilhos por terreno nas regiões selecionadas.
    - `dicaSelecao`: dica conforme estado (nenhuma, parcial, válida, sem urbana, desconectadas).
    - `valida`: 3 regiões, conectadas, ≥1 Urbana.
    - `emFoco`: região destacada em RegiaoFoco.
  - **Métodos:**
    - `carregarPrevia()` → GET `/previa`; se 404, POST `/previa` e GET novamente.
    - `gerar()` → POST `/previa`, limpa seleção, atualiza previa.
    - `alternar(i)`: alterna i na seleção; valida vizinhança.
    - `criar()` → POST `/vila` com `{ previaId, indices: selecionadas }`; se 201 → navega `/jogo/distribuir-populacao`; se erro → toast.
    - `podeSelecionar(i)`: < 3 ou (seleção vazia or i vizinha de alguma).
    - `setHover(i)`, `limparHover()`: gerencia foco.

**Componentes (novos):**
- [/frontend/src/components/criacao/MapaPrevia.vue](/frontend/src/components/criacao/MapaPrevia.vue) — grade 4×4 com tiles de regiões.
  - Props: `regioes`, `selecionadas`, `podeSelecionar`.
  - Emits: `alternar` (toggle seleção), `foco` (mouseover), `desfoco` (mouseleave).
  - Exibe cada região com número 01–16, tipo, 3 terrenos com percentuais (barras).
  - Estados: normal, hover, selecionado (borda accent + selo), indisponível (opacidade .4).

- [/frontend/src/components/criacao/RegiaoTile.vue](/frontend/src/components/criacao/RegiaoTile.vue) — tile individual.
  - Props: `regiao`, `ordem` (0-15), `disponivel`.
  - Emits: `click`, `mouseenter`, `mouseleave`.
  - Conteúdo: número formatado (01–16), ícone de tipo, 3 terrenos com percentuais (barras, largura = percentual ÷ 60).

- [/frontend/src/components/criacao/RegiaoFoco.vue](/frontend/src/components/criacao/RegiaoFoco.vue) — destaque de região em foco.
  - Props: `regiao`, `hover`.
  - Exibe info expandida de região selecionada ou em hover.

- [/frontend/src/components/criacao/SelecaoPainel.vue](/frontend/src/components/criacao/SelecaoPainel.vue) — painel lateral.
  - Props: `selecionadas`, `regioes`, `totaisLadrilhos`, `dicaSelecao`, `valida`.
  - 3 slots de seleção, dica dinâmica, terrenos com percentuais (3 valores de percentuais por região selecionada; ex.: "Floresta 40% · Plantações 35% · Barreiro 25%").
  - Exibe composição % de tipos de terreno por região (distribuição em ladrilhos) e total de ladrilhos por tipo de terreno.
  - Botão "Limpar" (reseta seleção).
  - Botão CTA "Criar vila" (habilitado se `valida`) ou mensagem de validação.
  - Emits: `criar`, `limpar`.

- [/frontend/src/components/criacao/ComposicaoTerrenos.vue](/frontend/src/components/criacao/ComposicaoTerrenos.vue) — lista de terrenos com percentuais.
  - Props: `itens` (List<{terreno, percentual}>), `maximo` (largura da barra = 60), `espessura`.
  - Renderiza barra proporcional (largura = percentual ÷ 60) e rótulo (ex.: "Floresta 40%").

- [/frontend/src/components/vilarejo/AvisoToast.vue](/frontend/src/components/vilarejo/AvisoToast.vue) — toast de notificações.
  - Exibe sucesso, erro, aviso durante operações assíncronas.

**Serviço/Composable (novo):**
- [/frontend/src/composables/useVila.ts](/frontend/src/composables/useVila.ts) — gerencia estado e API da vila.
  - `useCriacaoVila()`: retorna `{ previa, selecionadas, hover, gerando, enviando, erro, conectado, totaisLadrilhos, dicaSelecao, valida, emFoco, podeSelecionar, carregarPrevia, alternar, gerar, criar, setHover, limparHover }`.
  - `async carregarPrevia()`: GET `/previa`; se 404, POST `/previa` e repete GET.
  - `async gerar()`: POST `/previa`, limpa seleção.
  - `async criar(previaId, indices)`: POST `/vila`.
  - Estado reativo: `previa`, `selecionadas`, `hover`, `gerando`, `enviando`, `erro`.

- [/frontend/src/domain/regioes.ts](/frontend/src/domain/regioes.ts) — lógica de regiões.
  - `conectado(vila)`: booleano, usuário autenticado.
  - `podeSelecionar(indice, selecionadas, regiao)`: valida vizinhança e limite.
  - `temUrbana(indices, previa)`: ≥1 Urbana na seleção.
  - `selecaoValida(indices, previa)`: 3 regiões, ≥1 Urbana.
  - `totalLadrilhosPorTerreno(indices, previa)`: total de ladrilhos por tipo de terreno (para o painel).
  - `terrenosOrdenados(mapa)`: lista dos 13 terrenos em ordem.
  - `dicaSelecao(selecionadas, previa, valida)`: mensagem de estado.

**Rota (nova/atualizar):**
- `/jogo/criar-vila` → `CriacaoVila` (name: 'criar-vila', meta: { etapaInicial: true, abaAtiva: 'mapa' }).
- Guarda global: sem vila → permite; com vila → `/jogo/mapa`.

**Guarda de rota:**
- [/frontend/src/router/guardaVila.ts](/frontend/src/router/guardaVila.ts) — guarda global única.
  - Sem vila → redireciona `/jogo/criar-vila` (etapa inicial).
  - Prévia pendente → redireciona `/jogo/distribuir-populacao`.
  - Vila confirmada indo para `/jogo/criar-vila` → redireciona `/jogo/mapa`.
  - Exporta: `marcarVilaCriada()`, `resetarGuardaVila()`.

## Backend

Não se aplica (tudo em tarefa 003).

## Arquivos prováveis

- [/frontend/src/views/CriacaoVila.vue](/frontend/src/views/CriacaoVila.vue) (novo)
- [/frontend/src/components/criacao/MapaPrevia.vue](/frontend/src/components/criacao/MapaPrevia.vue) (novo)
- [/frontend/src/components/criacao/MapaPrevia.spec.ts](/frontend/src/components/criacao/MapaPrevia.spec.ts) (teste)
- [/frontend/src/components/criacao/RegiaoTile.vue](/frontend/src/components/criacao/RegiaoTile.vue) (novo)
- [/frontend/src/components/criacao/RegiaoTile.spec.ts](/frontend/src/components/criacao/RegiaoTile.spec.ts) (teste)
- [/frontend/src/components/criacao/RegiaoFoco.vue](/frontend/src/components/criacao/RegiaoFoco.vue) (novo)
- [/frontend/src/components/criacao/RegiaoFoco.spec.ts](/frontend/src/components/criacao/RegiaoFoco.spec.ts) (teste)
- [/frontend/src/components/criacao/SelecaoPainel.vue](/frontend/src/components/criacao/SelecaoPainel.vue) (novo)
- [/frontend/src/components/criacao/SelecaoPainel.spec.ts](/frontend/src/components/criacao/SelecaoPainel.spec.ts) (teste)
- [/frontend/src/components/criacao/ComposicaoTerrenos.vue](/frontend/src/components/criacao/ComposicaoTerrenos.vue) (novo)
- [/frontend/src/components/criacao/ComposicaoTerrenos.spec.ts](/frontend/src/components/criacao/ComposicaoTerrenos.spec.ts) (teste)
- [/frontend/src/components/vilarejo/AvisoToast.vue](/frontend/src/components/vilarejo/AvisoToast.vue) (novo)
- [/frontend/src/components/vilarejo/AvisoToast.spec.ts](/frontend/src/components/vilarejo/AvisoToast.spec.ts) (teste)
- [/frontend/src/composables/useVila.ts](/frontend/src/composables/useVila.ts) (novo)
- [/frontend/src/composables/useVila.spec.ts](/frontend/src/composables/useVila.spec.ts) (teste)
- [/frontend/src/domain/regioes.ts](/frontend/src/domain/regioes.ts) (novo)
- [/frontend/src/domain/regioes.spec.ts](/frontend/src/domain/regioes.spec.ts) (teste)
- [/frontend/src/router/guardaVila.ts](/frontend/src/router/guardaVila.ts) (novo)
- Atualizar: [/frontend/src/router/index.ts](/frontend/src/router/index.ts) (rota `/jogo/criar-vila`, name `criar-vila`, meta, guarda global).

## Testes

- **Carregamento:** montar view → GET `/previa`; se 404, POST `/previa` e repete GET; prévia carregada com 16 regiões.
- **Seleção:** clicar região 6 → entra em `selecionadas`; dica muda para "Regiões destacadas...".
- **Vizinhança:** região 6 selecionada, clicar 1 (não vizinha) → não entra; clicar 7 (vizinha) → entra.
- **Conexidade:** selecionar 1, 5, 9 (linha diagonal, desconectadas) → checklist "Vizinhas entre si" = false.
- **Urbana:** regiões sem Urbana → checklist "Ao menos 1 Urbana" = false; botão desabilitado.
- **Percentuais:** selecionar 6, 7, 10 → `totaisLadrilhos` soma os ladrilhos de cada terreno das regiões; barras renderizam; composição % de terrenos e total de ladrilhos por tipo exibidos.
- **Gerar novo mapa:** POST `/previa` → `rodada` incrementa; `selecionadas` limpa.
- **Criar vila válida:** POST `/vila` → 201 → navega `/jogo/distribuir-populacao`.
- **Erro prévia expirada:** POST com `previaId` antigo → 409 → toast com mensagem; GET `/previa` recarrega.
- **Responsividade:** grid 1180px max, gap 28px; em tela < 768px, painel desce.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA9.
- Componentes renderizam sem erros; layout conforme handoff.
- Testes unitários (Vitest/Jest) dos composables e componentes.
- Testes E2E (Cypress/Playwright) de fluxo completo (gerar prévia → selecionar → criar).
- Navegação funciona; guardas de rota impedem acesso sem vila / com vila.
- Mensagens de erro (toast) exibem resposta do servidor.

## Fora de escopo

- Edição/cancelamento após criação.
- Temas ou temas escuros (mantém Aura claro).
