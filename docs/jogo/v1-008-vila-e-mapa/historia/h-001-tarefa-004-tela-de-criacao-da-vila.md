# H-001 · Tarefa 004 — Tela de criação da vila

**História:** [h-001-criar-vila-escolhendo-regioes-iniciais.md](h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-003-api-de-criacao-da-vila.md](h-001-tarefa-003-api-de-criacao-da-vila.md) · **Camada:** Frontend

## Objetivo

Implementar tela de criação de vila (Vue + PrimeVue) mostrando grade 4×4 com pré-visualização de jazidas, permitindo seleção de 3 regiões, tipos e submit.

## Contexto necessário

- [../regioes.md](../regioes.md) — grade 4×4, adjacência, tipos de região
  > Região 6 (linha 1, coluna 1) é adjacente a 2, 5, 7, 10.

## Frontend

**Componentes (novos):**
- [/frontend/src/views/CriacaoVila.vue](/frontend/src/views/CriacaoVila.vue) (novo)
  - Estado: seleçãoRegiao[] (1-3), tipos{}, semente (gerada no mount)
  - UI:
    - Título: "Criar minha vila"
    - Grade 4×4 com células clicáveis
    - Cada célula mostra: número (1-16), tipo de vizinhos em tons de cor (Floresta/Rocha/...)
    - Validação ao vivo: seleção 2ª/3ª deve estar adjacente a anterior
    - Dropdown para tipo (Rural/Urbana/Coleta) por região
    - Botão "Criar vila" desabilitado até 3 válidas + ≥1 Urbana
  - Método: obter pré-visualização de jazidas via `GET /api/jogo/vila/preview?semente=X`
  - Chamada: `POST /api/jogo/vila` ao clicar "Criar vila", redirecionar para rota `/jogo/mapa`

**API preparada pelo backend:**
- GET `/api/jogo/vila/preview?semente=...` — retorna mapa de jazidas (opcional, pode ser calculado no frontend)
- POST `/api/jogo/vila` — criação (tarefa 003)

**Rota (nova):**
- Quando autenticado e sem vila → rota `CriacaoVila` serve
- Quando autenticado e com vila → redireciona para `/jogo/mapa` (história h-002)

## Backend

Não se aplica (tudo em tarefa 003).

## Arquivos prováveis

- [/frontend/src/views/CriacaoVila.vue](/frontend/src/views/CriacaoVila.vue) (novo)
- [/frontend/src/composables/useVila.js](/frontend/src/composables/useVila.js) (novo) — helper para chamar API de vil

## Testes

- **Teste funcional**: renderizar CriacaoVila, clicar em região 1 → seleção muda.
- **Teste de validação**: selecionar região 1, depois clicar em 3 (não adjacente) → erro visual, não muda seleção.
- **Teste de usabilidade**: selecionar 3 regiões válidas (1, 2, 6) com tipos URBANA, RURAL, COLETA → botão "Criar vila" fica habilitado.
- **Teste de submissão**: clicar "Criar vila" → POST enviado, resposta 201 → redireciona `/jogo/mapa`.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA6
- Testes E2E (Cypress/Playwright) passando
- Tela renderiza sem erros no navegador
- Links e navegação funcionando

## Fora de escopo

- Cancelamento/voltar (usuário pode recarregar página).
- Geração de nome de vila automático (usar padrão do backend).
