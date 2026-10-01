# H-001 · Tarefa 003 — Tela de distribuição de pontos e família líder

**História:** [H-001 — Gerar famílias...](h-001-gerar-familias-e-distribuir-pontos-iniciais.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** [h-001-tarefa-002-geracao-das-familias-iniciais.md](h-001-tarefa-002-geracao-das-familias-iniciais.md) · **Camada:** Frontend

## Objetivo

Criar tela Vue + PrimeVue para distribuir os 20 pontos de característica e 10 pontos de profissão de cada cidadão (4 famílias × 4 membros = 16 cidadãos). Selecionar família líder. Enviar dados ao backend e criar vila.

## Contexto necessário

- [cidadao.md](../cidadao.md) — características, profissões, família líder
  > Características: VIT, FOR, VEL, INT, CAR. Profissões: 12 tipos.

- [familias.md](../familias.md) — bônus líder
  > Líder = adulto mais velho da família escolhida. Bônus +1% eficiência a cada 2 CAR, máx. +10%.

## Backend

**Endpoint** (novo ou existente):
- `POST /api/jogo/vilafamilia` (ou integrado em criação de vila):
  - Recebe JSON: `{ familias: [ { familiaId, cidadaos: [ { cidadaoId, vit, for, vel, int, car, profissoes: { profissao: pontos, ... } }, ... ], ... ], familiaLiderIndex: N }`
  - Valida: Σ características ≤ 20 por cidadão; cada ≤ 10; Σ profissões ≤ 10 por cidadão; cada ≤ 5.
  - Atualiza cidadãos com valores distribuídos.
  - Define vila.familiaLiderIdx com a família escolhida.
  - Calcula bônus líder = +1% × (CAR líder ÷ 2), máx. 10%.
  - Retorna vila criada com bônus no resumo.

**Validações backend**:
- Σ características > 20 → erro "Máximo 20 pontos de característica".
- Característica > 10 → erro "Máximo 10 por característica".
- Σ profissões > 10 → erro "Máximo 10 pontos de profissão".
- Profissão > 5 → erro "Máximo 5 por profissão".
- Família líder não selecionada → erro "Escolha uma família líder".

## Frontend

**Componentes PrimeVue**:
- `FamiliaPanel` (TabView ou Accordion):
  - Abas/painéis: Família 1, Família 2, Família 3, Família 4.
  - Cada aba exibe 4 cidadãos da família.

- `CidadaoForm` (para cada cidadão):
  - Nome (read-only).
  - Idade (read-only).
  - Características: VIT, FOR, VEL, INT, CAR (InputNumber, min 0, max 10).
  - Contador de pontos usados / 20.
  - Profissões: dropdowns/inputs para 12 profissões (min 0, max 5 cada).
  - Contador de pontos usados / 10.
  - Validação inline: avisar se Σ > limite.

- `FamiliaLiderSelector`:
  - RadioButton para escolher 1 das 4 famílias.
  - Exibe nome do líder (adulto mais velho) e CAR.
  - Calcula bônus eficiência em tempo real: +1% × (CAR ÷ 2).
  - Aviso se bônus > 10%.

- `ConfirmarButton`:
  - Validar todas as distribuições (backend recusa anyway, mas feedback local).
  - POST para `/api/jogo/vila/familia` com JSON.
  - Spinner enquanto aguarda.
  - Redireciona para tela do jogo após sucesso.
  - Exibe erro se backend rejeitar.

**Fluxo**:
1. Tela abre com 4 abas (famílias) com 4 cidadãos cada, tudo zerado.
2. Jogador distribui pontos em cada cidadão.
3. Jogador seleciona família líder.
4. Clica "Confirmar" → valida → POST → cria vila → redireciona.

## Arquivos prováveis

- [/frontend/src/components/jogo/FamiliaDistribuicao.vue](/frontend/src/components/jogo/FamiliaDistribuicao.vue) (novo)
- [/frontend/src/components/jogo/CidadaoForm.vue](/frontend/src/components/jogo/CidadaoForm.vue) (novo)
- [/frontend/src/components/jogo/FamiliaLiderSelector.vue](/frontend/src/components/jogo/FamiliaLiderSelector.vue) (novo)

## Testes

- Teste E2E: distribuir 20 pontos em características (validar contador).
- Teste E2E: tentar distribuir > 10 em uma característica (validar erro local).
- Teste E2E: distribuir 10 pontos em profissões (validar contador).
- Teste E2E: tentar distribuir > 5 em uma profissão (validar erro local).
- Teste E2E: selecionar família líder e ver bônus atualizado.
- Teste E2E: clicar Confirmar → POST bem-sucedido → redirecionar para mapa.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2, CA3, CA4, CA5.
- Build do frontend (`npm run build`) sem erros.
- Testes E2E listados passando.
- Limites validados no frontend (feedback) e no backend (enforce).

## Fora de escopo

- Editar distribuição após confirmação.
- Múltiplas famílias líderes (apenas 1).
