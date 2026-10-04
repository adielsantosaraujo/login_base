# H-002 · Tarefa 002 — Telas do mapa e da região

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-002-tarefa-001-api-do-mapa-da-vila.md](h-002-tarefa-001-api-do-mapa-da-vila.md) · **Camada:** Frontend

## Objetivo

Implementar componentes Vue + PrimeVue para grade 4×4 (mapa da vila com tipo e composição de terrenos) e grade 10×10 (detalhes de uma região), com navegação entre eles.

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Mapa mostra tipo e composição dos 3 terrenos de todas as 16 regiões; ladrilhos com terreno/bônus e construções (todos os tipos); cores do tema v2.

- [../regioes.md](../regioes.md) — grade 4×4 e adjacência (1.1, 1.2)
  > Regiões 1-16, índice = linha × 4 + coluna + 1.

## Frontend

**Componentes (novos):**
- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
  - Título: "Mapa da vila"
  - Grid 4×4 com células iteradas de 1-16
  - Cada célula: número, tipo, composição dos 3 terrenos (%), cor/ícone por tipo (Floresta verde, Planície bege, Urbana amarela, Litoral azul, Montanha cinza)
  - Click: abre drawer/modal com grade 10×10 de região
  - Display masmorra: ícone + nível se ativa, sobreposto

- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
  - Grid 10×10 de ladrilhos
  - Cada ladrilho: exibe endereço "(A,1)" acima da sigla de 2 letras do terreno (ex.: "(A,1)" acima de "Fl" para Floresta; x=coluna A–J, y=linha 1–10)
  - Cor do terreno (token `--vl-terreno-xx`) ou construção se houver
  - Tooltip ao hover: endereço, terreno (sigla + nome), bonus_base, bonus_adjacente, bonus_total, construção (se houver)
  - Exibe tipo e 3 percentuais de terreno da região no cabeçalho
  - Interação: clicar ladrilho abre detalhes (não nesta tarefa, fora de escopo)

- [/frontend/src/composables/useMapa.ts](/frontend/src/composables/useMapa.ts) (novo)
  - Funções: `useMapaVila()` — fetch GET /api/jogo/vila/mapa com tipo, terrenos e percentuais
  - Funções: `useRegiaoDetalhes(indice)` — fetch GET /api/jogo/regioes/{indice}
  - Reatividade: `regioes`, `regionSelecionada`

**Rota (nova):**
- `/jogo/mapa` — Mapa.vue (da nova change, aplicando tema v2)

**Estilos/Assets:**
- Ícones de tipos de região (Floresta, Planície, Urbana, Litoral, Montanha)
- Cores e siglas dos 13 terrenos (tabela de regioes.md): tokens `--vl-terreno-*` (fl, ba, pl, cr, ro, fe, ca, sa, en, mi, in, co, de)
- Ícones de prédios (Casa, Armazém, etc.) — será expandido em tarefas posteriores
- Cores: tokens `--vl-tipo-*` do tema v2

## Backend

Não se aplica (tarefa 002).

## Arquivos prováveis

- [/frontend/src/views/Mapa.vue](/frontend/src/views/Mapa.vue) (novo)
- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (novo)
- [/frontend/src/composables/useMapa.ts](/frontend/src/composables/useMapa.ts) (novo)

## Testes

- **Teste funcional**: renderizar Mapa.vue, verificar 4×4 grid com 16 células, todas com tipo, composição dos 3 terrenos (%) e cores do tema.
- **Teste funcional**: clicar em região 6 (possuída, Urbana) → drawer abre, mostra 10×10 ladrilhos com endereços "(A,1)" acima da sigla do terreno (ex.: "(A,1)" acima de "De" para Desenvolvimento), 4 casas nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura e todas em terreno Desenvolvimento, tipo e percentuais visíveis; tooltip ao hover mostra endereço, terreno, bonus_base, bonus_adjacente, bonus_total.
- **Teste funcional**: clicar em região 2 (não possuída, Montanha) → drawer abre, mostra ladrilhos gerados com endereços e siglas de terreno (ex.: "(B,2)" acima de "Fe" para Ferro), tipo e percentuais sorteados, opção para anexar.
- **Teste E2E**: Mapa → clicar região 6 → expandir (verificar endereços dos ladrilhos) → voltar → Mapa ainda visível.
- **Teste de dados**: GET /api/jogo/vila/mapa retorna 3 regiões possuídas com terrenos → Mapa mostra 16 células com tipos, percentuais de todas.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA5
- Testes E2E passando
- Componentes renderizam sem erro
- Navegação funciona

## Fora de escopo

- Click em ladrilho para detalhe (próxima tarefa).
- Interface de anexação (história h-003).
- Animações/transições.
