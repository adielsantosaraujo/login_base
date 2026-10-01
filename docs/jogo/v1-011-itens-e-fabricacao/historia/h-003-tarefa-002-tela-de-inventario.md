# H-003 · Tarefa 002 — Tela de inventário

**História:** [H-003 — Gerenciar inventário e aprimorar itens](h-003-gerenciar-inventario-e-aprimorar-itens.md) · **Domínio:** [../itens.md](../itens.md), [../fabricacao.md](../fabricacao.md) · **Depende de:** [h-003-tarefa-001-api-de-inventario-e-aprimoramento.md](h-003-tarefa-001-api-de-inventario-e-aprimoramento.md) · **Camada:** Frontend

## Objetivo

Criar interface (Vue + PrimeVue) para gerenciar o inventário: listar itens com filtros, visualizar detalhes (bônus, pedras) e aprimorar itens (L→L+1) com validação de PE e recursos.

## Contexto necessário

- [CA1–CA6 de H-003](h-003-gerenciar-inventario-e-aprimorar-itens.md#critérios-de-aceite)
  > Listar/filtrar por categoria
  > Aprimorar com 50% do custo
  > Validação de PE, recursos, máximo L10

- [Custo de aprimoramento (seção 7.4)](../fabricacao.md#aprimoramento-de-item)
  > 50% × custo de fabricar L+1

## Frontend

- Componente `InventarioTela.vue`:
  - Header: título "Inventário", total de itens
  - Filtro: dropdown com categorias (ARMA, FERRAMENTA, ARMADURA, JOIA, Todos)
  - Tabela/DataTable de itens:
    - Colunas: Ícone, Nome, Nível, Qualidade (tag colorida), Bônus (resumo), Ação
    - Cada linha: item com seus dados
    - Botões de ação:
      - "Detalhas" → modal com bônus completos e pedras engastadas
      - "Aprimorar" → modal de aprimoramento (se L < 10)
      - "Equipar" → abre seletor de pessoa (fora de escopo desta tarefa?)

- Modal `AprimoramentoModal.vue`:
  - Mostra item atual (L, qualidade, bônus)
  - Mostra item após aprimoramento (L+1, mesma qualidade)
  - Custo de aprimoramento: 50% × custo L+1 (em recursos)
  - Estimativa de tempo: turnos (baseado em artesão mais eficiente da oficina)
  - Seletor de artesão (se múltiplos com PE ≥ 2(L+1)−2)
  - Validação visual:
    - Verde: recursos suficientes, PE ok
    - Amarelo: aviso (PE justo)
    - Vermelho: erro (PE insuficiente, recursos insuficientes, L10)
  - Botão "Aprimorar" → PUT API

- Validações no frontend:
  - Desabilitar "Aprimorar" se L == 10
  - Desabilitar "Aprimorar" se PE insuficiente (tooltip: "PE mínimo X necessário")
  - Desabilitar "Aprimorar" se recursos insuficientes (tooltip: "Recursos insuficientes")

- Componentes PrimeVue:
  - `pDataTable` (inventário, paginação, filtro)
  - `pButton` (Detalhes, Aprimorar, Equipar)
  - `pDialog` (modal de aprimoramento)
  - `pTag` (qualidade: cores Simples/Boa/Excelente/Divina)
  - `pToast` (feedback)
  - `pDropdown` (seletor de categoria, artesão)

- Chamadas de API:
  - `GET /api/jogo/inventario?categoria={cat}&page={p}&size={s}` → listar itens
  - `GET /api/jogo/inventario/{itemId}` → detalhes (bônus, pedras)
  - `POST /api/jogo/inventario/{itemId}/aprimorar` (body: { oficina_id? }) → aprimorar
    - Response: { success, fabricacao, erro?: { codigo, mensagem } }
  - `GET /api/jogo/oficinas?categoria=FERRARIA` → artesãos disponíveis (para estimativa de tempo)

- Feedback UX:
  - Toast sucesso: "Espada L1 enviada para aprimoramento (L→L2, ~3 turnos)"
  - Toast erro: "Ferro insuficiente (2 < 5)"
  - Desabilitar botão "Aprimorar" com tooltip explicativo

## Arquivos prováveis

- [/frontend/src/views/InventarioTela.vue](/frontend/src/views/InventarioTela.vue) (novo)
- [/frontend/src/components/AprimoramentoModal.vue](/frontend/src/components/AprimoramentoModal.vue) (novo)
- [/frontend/src/components/DetalhesItemModal.vue](/frontend/src/components/DetalhesItemModal.vue) (novo)

## Testes

- Teste de renderização: tabela exibe itens pagos
- Teste de filtro: categoria ARMA mostra apenas armas
- Teste de modal: "Detalhes" abre modal com bônus e pedras
- Teste de validação: L10 → "Aprimorar" desabilido
- Teste de validação: PE insuficiente → tooltip + botão desabilido
- Teste de validação: recursos insuficientes → aviso + botão vermelho
- Teste de sucesso: POST bem-sucedido → toast e atualização de tabela

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6
- Build do frontend (`npm run build`) sem erros
- Testes de componente (Jest) passando
- Paginação funcional (mostrar mais itens, navegação)
- Filtro funcional
- Responsivo (mobile 320px+)
- Acessibilidade: labels, títulos, tooltips
- Toast feedback para todas as ações

## Fora de escopo

- Venda/descarte de itens
- Reordenação manual do inventário
- Equipamento direto (pode ser fora de escopo; usar "Equipar" do painel da pessoa)
