# H-002 · Tarefa 002 — Telas do mapa e da região

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-002-tarefa-001-api-do-mapa-da-vila.md](h-002-tarefa-001-api-do-mapa-da-vila.md) · **Camada:** Frontend

## Objetivo

Implementar componentes Vue + PrimeVue para grade 4×4 (mapa da vila) e grade 10×10 (detalhes de uma região), com navegação entre eles.

## Contexto necessário

- [../regioes.md](../regioes.md) — grade 4×4 e adjacência (1.1, 1.2)
  > Regiões 1-16, índice = linha × 4 + coluna + 1.

- Telas 1, 3, 4 de 11.4: Criação, Mapa, Região

## Frontend

**Componentes (novos):**
- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
  - Título: "Mapa da vila"
  - Grid 4×4 com células iteradas de 1-16
  - Cada célula: número, cor/ícone por tipo (Rural verde, Urbana azul, Coleta marrom, Vazio cinza)
  - Click: abre drawer/modal com grade 10×10 de região
  - Display masmorra: ícone + nível se ativa

- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
  - Grid 10×10 de ladrilhos
  - Cada ladrilho: ícone de jazida (Floresta, Rocha, etc.) ou construção se houver
  - Tooltip ao hover: detalhes (construção, recurso, etc.)
  - Interação: clicar ladrilho abre detalhes (não nesta tarefa, fora de escopo)

- [/frontend/src/composables/useMapa.js](/frontend/src/composables/useMapa.js) (novo)
  - Funções: `useMapaVila()` — fetch GET /api/jogo/vila/mapa
  - Funções: `useRegiaoDetalhes(indice)` — fetch GET /api/jogo/regioes/{indice}
  - Reatividade: `regioes`, `regionSelecionada`

**Rota (nova):**
- `/jogo/mapa` — Mapa.vue

**Estilos/Assets:**
- Ícones de tipos de região (Rural, Urbana, Coleta)
- Ícones de jazidas (Floresta, Rocha, Barreiro, etc.)
- Ícones de prédios (Casa, Armazém, etc.) — será expandido em tarefas posteriores
- Cores: RGB para tipos ou design system (PrimeVue)

## Backend

Não se aplica (tarefa 002).

## Arquivos prováveis

- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
- [/frontend/src/composables/useMapa.js](/frontend/src/composables/useMapa.js) (novo)

## Testes

- **Teste funcional**: renderizar Mapa.vue, verificar 4×4 grid com 16 células.
- **Teste funcional**: clicar em região 6 (possuída) → drawer abre, mostra 10×10 ladrilhos, 4 casas em (0,0), (2,0), (4,0), (6,0).
- **Teste funcional**: clicar em região 1 (vazia) → drawer abre, mostra "Região não possuída, clicar para anexar" (mesma tela ou rota anexação).
- **Teste E2E**: Mapa → clicar região 6 → expandir → voltar → Mapa ainda visível.
- **Teste de dados**: GET /api/jogo/vila/mapa retorna 3 regiões possuídas → Mapa mostra 3 preenchidas, 13 vazias.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA5
- Testes E2E passando
- Componentes renderizam sem erro
- Navegação funciona

## Fora de escopo

- Click em ladrilho para detalhe (próxima tarefa).
- Ações em célula vazia (anexação — história h-003).
- Animações/transições.
