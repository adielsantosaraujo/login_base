# H-004 · Tarefa 002 — Painel do prédio

**História:** [H-004 — Alocar trabalhadores nos prédios](h-004-alocar-trabalhadores-nos-predios.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-004-tarefa-001-api-de-alocacao.md](h-004-tarefa-001-api-de-alocacao.md) · **Camada:** Frontend

## Objetivo

Implementar painel de prédio exibindo alocações, eficiência, vagas disponíveis e lista interativa para alocar/desalocar cidadãos.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Vagas por nível (seção 4.3)

## Backend

Não se aplica.

## Frontend

- **Componente `PainelPredio.vue`**:
  - Abre ao clicar em prédio ativo na grade.
  - Exibe:
    - Nome, tipo, nível, posição.
    - Progresso de obra (se EM_OBRA/EM_UPGRADE).
    - Vagas: "X/N ocupadas" (ex: "2/2 para N1").
    - Lista de alocados com eficiência de cada um.
    - Botão "Desalocar" por cidadão.
  - Abas:
    - "Alocados": lista de cidadãos já alocados.
    - "Disponíveis": lista de cidadãos elegíveis (idade, não em tropa, não alocados).
  - Click em cidadão de "Disponíveis" → aloca (POST).
  - Click em "Desalocar" → confirma e desaloca (DELETE).
  - Filtros (opcional): por profissão, por idade, por eficiência.

- **Chamadas de API**:
  - `GET /api/jogo/construcoes/{id}` (detalhes).
  - `GET /api/jogo/construcoes/{id}/alocacoes` (lista alocações).
  - `POST /api/jogo/construcoes/{id}/alocacoes` (alocar).
  - `DELETE /api/jogo/construcoes/{id}/alocacoes/{cidadaoId}` (desalocar).

- **Integração em `RegiaoConstrucao.vue`**:
  - Click em prédio → abre `PainelPredio`.

## Arquivos prováveis

- [/frontend/src/components/PainelPredio.vue](/frontend/src/components/PainelPredio.vue) (novo)
- [/frontend/src/services/api/alocacaoApi.ts](/frontend/src/services/api/alocacaoApi.ts) (novo)

## Testes

- Renderizar painel com dados mock.
- Listar alocados e disponíveis.
- Botão "Desalocar" chama DELETE.
- Click em cidadão disponível chama POST.
- Vagas atualizam em tempo real.
- Notificações de sucesso/erro.

## Definição de pronto

- Critérios CA1, CA2, CA3, CA4, CA5, CA6 cobertos.
- Build sem erros.
- Testes passando.
- UX clara: usuário entende vagas, eficiência, restrições.

## Fora de escopo

- Drag-and-drop.
- Histórico de alocações.
