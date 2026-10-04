# H-002 · Tarefa 002 — Telas do mapa e da região

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-002-tarefa-001-api-do-mapa-da-vila.md](h-002-tarefa-001-api-do-mapa-da-vila.md) · **Camada:** Frontend

## Objetivo

Implementar componentes Vue + PrimeVue para grade 4×4 (mapa da vila com tipo e bônus) e grade 10×10 (detalhes de uma região), com navegação entre eles.

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Mapa mostra tipo e 3 bônus de todas as 16 regiões; ladrilhos gerados se ausentes (tipo ≠ Urbana); cores do tema v2.

- [../regioes.md](../regioes.md) — grade 4×4 e adjacência (1.1, 1.2)
  > Regiões 1-16, índice = linha × 4 + coluna + 1.

## Frontend

**Componentes (novos):**
- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
  - Título: "Mapa da vila"
  - Grid 4×4 com células iteradas de 1-16
  - Cada célula: número, tipo, 3 bônus, cor/ícone por tipo (Floresta verde, Planície bege, Urbana amarela, Litoral azul, Montanha cinza)
  - Click: abre drawer/modal com grade 10×10 de região
  - Display masmorra: ícone + nível se ativa, sobreposto
  - Display bônus da vila: totais de cada bônus em lugar visível

- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
  - Grid 10×10 de ladrilhos
  - Cada ladrilho: ícone de jazida (Floresta, Rocha, etc.) ou construção se houver
  - Tooltip ao hover: detalhes (construção, recurso, etc.)
  - Exibe tipo e bônus da região
  - Interação: clicar ladrilho abre detalhes (não nesta tarefa, fora de escopo)

- [/frontend/src/composables/useMapa.ts](/frontend/src/composables/useMapa.ts) (novo)
  - Funções: `useMapaVila()` — fetch GET /api/jogo/vila/mapa com tipo, bônus, bonusRegiao
  - Funções: `useRegiaoDetalhes(indice)` — fetch GET /api/jogo/regioes/{indice}
  - Reatividade: `regioes`, `regionSelecionada`, `bonusRegiao`

**Rota (nova):**
- `/jogo/mapa` — Mapa.vue (da nova change, aplicando tema v2)

**Estilos/Assets:**
- Ícones de tipos de região (Floresta, Planície, Urbana, Litoral, Montanha)
- Ícones de jazidas (Floresta, Rocha, Barreiro, etc.)
- Ícones de prédios (Casa, Armazém, etc.) — será expandido em tarefas posteriores
- Cores: tokens `--vl-tipo-*`, `--vl-bonus-*` do tema v2

## Backend

Não se aplica (tarefa 002).

## Arquivos prováveis

- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
- [/frontend/src/composables/useMapa.ts](/frontend/src/composables/useMapa.ts) (novo)

## Testes

- **Teste funcional**: renderizar Mapa.vue, verificar 4×4 grid com 16 células, todas com tipo, bônus e cores do tema.
- **Teste funcional**: clicar em região 6 (possuída, Urbana) → drawer abre, mostra 10×10 ladrilhos, 4 casas em (0,0), (2,0), (4,0), (6,0), tipo e bônus visíveis.
- **Teste funcional**: clicar em região 2 (não possuída, Montanha) → drawer abre, mostra ladrilhos gerados, tipo e bônus sorteados, opção para anexar.
- **Teste E2E**: Mapa → clicar região 6 → expandir → voltar → Mapa ainda visível.
- **Teste de dados**: GET /api/jogo/vila/mapa retorna 3 regiões possuídas + bonusRegiao → Mapa mostra 16 células com tipos, bônus de todas, totais na vila.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA5
- Testes E2E passando
- Componentes renderizam sem erro
- Navegação funciona

## Fora de escopo

- Click em ladrilho para detalhe (próxima tarefa).
- Interface de anexação (história h-003).
- Animações/transições.
