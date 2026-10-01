# H-001 · Tarefa 004 — Tela de construção na região

**História:** [H-001 — Construir prédio nível 1](h-001-construir-predio-nivel-1.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-002-api-de-construcao-e-posicionamento.md](h-001-tarefa-002-api-de-construcao-e-posicionamento.md) · **Camada:** Frontend

## Objetivo

Implementar tela de construção exibindo a grade 10x10 da região (ladrilhos com jazidas/prédios/marcações), painel de seleção de prédio com custos, validações visuais e botão de confirmação.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Tabela de custos (seção 4.4)
  > Cada prédio mostra: nome, tipo (Urbana/Rural/Coleta), profissão, Madeira, Pedra, ... PO.

- [../../v1-008-vila-e-mapa/regioes.md](../../v1-008-vila-e-mapa/regioes.md) — Grade 4x4, regiões 1–16, tipo de região.

## Backend

Não se aplica.

## Frontend

- **Rota**: `/jogo/regiao/{indice}` (compartilhada com h-002 visualização; aqui modo construção).

- **Componente `GradeRegiao.vue`**:
  - Grid 10x10 com células clicáveis.
  - Cada célula mostra: jazida (ícone), prédio (ícone + nível), ou vazio.
  - Célula N1 ocupa 1 quadrado; N2 ocupa 2×2; N3 ocupa 3×3 (visual).
  - Click em célula vazia → abre `SeletorConstrucao.vue`.

- **Componente `SeletorConstrucao.vue`**:
  - Modal/painel com lista de prédios possíveis para tipo de região.
  - Cada item: nome, custos (ícones Madeira/Pedra/...), PO, vagas.
  - Aba para filtrar: Todos, Urbana, Rural, Coleta.
  - Click em prédio → exibe preview e botão "Construir".

- **Validações visuais**:
  - Prédio não permitido nesta região → desabilitado (ícone X).
  - Recursos insuficientes → botão "Construir" desabilitado com tooltip.
  - Ladrilho ocupado → não clicável (visual).

- **Chamada de API**:
  - `POST /api/jogo/construcoes` com `{ tipo, regiaoIndice, x, y }`.
  - Sucesso: atualiza grade, exibe notificação "Construção iniciada".
  - Erro: exibe mensagem de erro.

- **Integração com store Vuex** (ou Pinia):
  - Estado: construcoes, estoque, regioes.
  - Mutations: addConstrucao, updateEstoque.
  - Actions: iniciarConstrucao.

## Arquivos prováveis

- [/frontend/src/views/RegiaoConstrucao.vue](/frontend/src/views/RegiaoConstrucao.vue) (novo)
- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
- [/frontend/src/components/SeletorConstrucao.vue](/frontend/src/components/SeletorConstrucao.vue) (novo)
- [/frontend/src/store/modules/construcao.ts](/frontend/src/store/modules/construcao.ts) (novo)
- [/frontend/src/services/api/construcaoApi.ts](/frontend/src/services/api/construcaoApi.ts) (novo)

## Testes

- Validar renderização de grade 10x10 com dados mock.
- Validar click em célula vazia abre seletor.
- Validar prédio desabilitado se região errada.
- Validar botão desabilitado se recursos insuficientes.
- Validar POST para API com dados corretos.
- Validar feedback após criação (notificação).

## Definição de pronto

- Critério CA1 coberto (UI indica erro).
- Build frontend (`npm run build`) sem erros.
- Testes unitários de componentes passando.
- UX clara: usuário sabe por que ação é rejeitada.

## Fora de escopo

- Modo 3D ou isométrico.
- Tooltip avançado com dicas de estratégia.
- Multiseleção (construir vários ao mesmo tempo).
