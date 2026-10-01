# H-003 · Tarefa 002 — Interface de marcação

**História:** [H-003 — Marcar ladrilhos de coleta](h-003-marcar-ladrilhos-de-coleta.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-003-tarefa-001-api-de-marcacao-de-ladrilhos.md](h-003-tarefa-001-api-de-marcacao-de-ladrilhos.md) · **Camada:** Frontend

## Objetivo

Implementar modo de marcação interativa na grade 10x10, exibindo contagem de marcados e cálculo de trabalhadores produtivos em tempo real.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Fórmula de produtivos (seção 4.6)
  > Produtivos = min(alocados, piso(marcados ÷ 2)).

## Backend

Não se aplica.

## Frontend

- **Modo de marcação em `GradeRegiao.vue`**:
  - Button "Modo marcação" ativa overlay.
  - Células clicáveis: click = marcar/desmarcar.
  - Célula marcada: visual destaque (cor, ícone).
  - Células com jazida errada: desabilitadas ou aviso ao clicar.
  - Contador: "X/N marcados" (ex: "4/4" para N1).
  - Preview de produtivos: "Produtivos: min(alocados, floor(X÷2))".

- **Validação em tempo real**:
  - Limite atingido: desabilita novos cliques.
  - Jazida incompatível: aviso ao clicar ou visual.

- **Chamadas de API**:
  - `POST /api/jogo/construcoes/{id}/marcacoes` (marcar).
  - `DELETE /api/jogo/construcoes/{id}/marcacoes/{x}/{y}` (desmarcar).
  - Feedback: notificações de sucesso/erro.

- **Integração com store**:
  - Estado: marcacoes[construcaoId] = [(x, y), ...].
  - Actions: addMarcacao, removeMarcacao.

## Arquivos prováveis

- [/frontend/src/components/GradeRegiao.vue](/frontend/src/components/GradeRegiao.vue) (modificar)
- [/frontend/src/services/api/marcacaoApi.ts](/frontend/src/services/api/marcacaoApi.ts) (novo)

## Testes

- Renderizar grade com botão "Modo marcação".
- Click em célula marca/desmarca.
- Contador atualiza.
- Preview de produtivos correto.
- Limite desabilita novos cliques.
- POST/DELETE chamados corretamente.

## Definição de pronto

- Critérios CA1, CA2, CA3, CA4, CA5 cobertos.
- Build sem erros.
- Testes passando.

## Fora de escopo

- Pintura em batch (vários cliques).
