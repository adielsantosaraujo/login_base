# H-003 · Tarefa 002 — Tela de expedição

**História:** [h-003-enviar-tropa-em-expedicao.md](h-003-enviar-tropa-em-expedicao.md) · **Domínio:** [../tropas.md](../tropas.md), [../expedicoes.md](../expedicoes.md) · **Depende de:** [h-003-tarefa-001-viagem-de-tropas-no-turno.md](h-003-tarefa-001-viagem-de-tropas-no-turno.md) · **Camada:** Frontend

## Objetivo

Criar a interface para enviar tropas em expedição: seleção de masmorra, visualização de distância, validação de comida, confirmação e acompanhamento de viagem.

## Contexto necessário

- [../expedicoes.md](../expedicoes.md) — Cálculo de distância, consumo de comida
  > Turnos viagem = max(1; distância Manhattan). Comida = membros × (ida+volta).

- [../../v1-001-masmorras/masmorras.md](../../v1-001-masmorras/masmorras.md) — Masmorras ativas, níveis, localizações
  > Destino deve ser região com masmorra ativa. Vitória remove masmorra.

- [../../v1-010-recursos-e-producao/recursos.md](../../v1-010-recursos-e-producao/recursos.md) — Estoque de recursos
  > Comida é debitada do estoque ao partir (seção 3.4).

## Frontend

### Componentes PrimeVue

- **ExpedicaoModal.vue** — dialog para selecionar e enviar expedição
  - Lista de masmorras ativas (nome, nível, região, distância calculada).
  - Preview de custo em comida (membros × turnos calcula automaticamente).
  - Botão "Confirmar": envia POST `/api/jogo/tropa/{tropaId}/expedicao`.
  - Feedback: "Comida insuficiente", "Expedição iniciada".

- **ExpedicaoStatusPanel.vue** — barra de progresso de expedição
  - Exibido na tela de quartel, em lista de tropas.
  - Mostra: masmorra destino, turnos restantes (ida/volta), estado (IDA/VOLTA).
  - Se turnosRestantes = 0 na ida: "Em batalha...".

- **MasmorrasDisponivelTable.vue** — lista filtrável de masmorras para expedição
  - Colunas: Nível, Região, Distância (turnos), Custo de comida (para tropa selecionada).
  - Linha clicável abre preview/confirmação.
  - Desabilita masmorra inativa ou se comida insuficiente.

### Integração com QuartelPanel.vue

- Aba "Expedições" (ou expandida em "Tropas"):
  - Lista de tropas com estado.
  - Para cada tropa AQUARTELADA: botão "Enviar expedição" → abre modal.
  - Para cada tropa EM_VIAGEM_IDA/EM_VIAGEM_VOLTA: mostra barra de progresso.

### Rotas

- `/jogo/regiao/{indice}/construcao/{construcaoId}` — quartel (inclui aba expedições).

### Chamadas de API

- **GET** `/api/jogo/vila/{vilaId}/masmorras`
  - Response: lista de masmorras ativas com nível, região, localizacao.
  - Exemplo:
    ```json
    [
      { "id": 1, "nivel": 5, "regiaoIndice": 16, "ativa": true },
      { "id": 2, "nivel": 2, "regiaoIndice": 10, "ativa": true }
    ]
    ```

- **POST** `/api/jogo/tropa/{tropaId}/expedicao`
  - Body: `{ "masmorraId": 1 }`
  - Response 201: `{ "id": 1, "estado": "EM_VIAGEM_IDA", "turnosRestantes": 4, "masmorraId": 1 }`
  - Response 400: erro (comida insuficiente, masmorra inativa)

- **GET** `/api/jogo/tropa/{tropaId}`
  - Response: detalhes da tropa (estado, membros, expedição se houver).

### Cálculo dinâmico

```javascript
// No componente, ao selecionar tropa e masmorra:
function calcularCustosExpedicao(tropa, masmorra) {
  const distancia = calcularManhattan(tropaRegiao, masmorraRegiao);
  const turnosViagem = Math.max(1, distancia);
  const alimentosNecessarios = tropa.membros.length * (turnosViagem * 2);
  
  return {
    turnosIda: turnosViagem,
    turnosVolta: turnosViagem,
    alimentosNecessarios: alimentosNecessarios,
    temComida: estoque.alimentos >= alimentosNecessarios
  };
}
```

## Arquivos prováveis

- [/frontend/src/components/Quartel/ExpedicaoModal.vue](/frontend/src/components/Quartel/ExpedicaoModal.vue) (novo)
- [/frontend/src/components/Quartel/ExpedicaoStatusPanel.vue](/frontend/src/components/Quartel/ExpedicaoStatusPanel.vue) (novo)
- [/frontend/src/components/Quartel/MasmorrasDisponivelTable.vue](/frontend/src/components/Quartel/MasmorrasDisponivelTable.vue) (novo)
- [/frontend/src/views/Jogo/QuartelPanel.vue](/frontend/src/views/Jogo/QuartelPanel.vue) (atualizar: adicionar aba/seção de expedições)
- [/frontend/src/services/quartelService.js](/frontend/src/services/quartelService.js) (atualizar: funções de expedição)
- [/frontend/src/utils/distancia.js](/frontend/src/utils/distancia.js) (novo: cálculo de Manhattan)

## Testes

- Teste visual:
  - Abrir QuartelPanel, aba "Expedições".
  - Clicar "Enviar expedição" em tropa AQUARTELADA.
  - Modal abre lista de masmorras; selecionar uma N5 em região 16.
  - Exibir custo: "Turnos: 4 ida + 4 volta = 8; comida: 5 membros × 8 = 40".
  - Estoque com 40 alimentos → botão "Confirmar" habilitado.
  - Clicar → tropa muda para EM_VIAGEM_IDA, barra mostra "Turnos restantes: 4".

- Teste de validação:
  - Estoque com 39 alimentos → botão "Confirmar" desabilitado ou erro ao clicar.
  - Masmorra inativa → linha desabilitada na lista.

- Teste de progresso:
  - Tropa EM_VIAGEM_IDA com turnosRestantes = 4.
  - Após 1 turno (tela recarrega) → exibe "3 turnos restantes (ida)".
  - Após 4 turnos → "Em batalha..." (se ainda em viagem) ou "1 turno (volta)".
  - Após 8 turnos totais → tropa volta a AQUARTELADA, expedição desaparece, painel mostra saque.

## Definição de pronto

- CA1–CA8 de H-003 cobertos visualmente.
- Modal de expedição funciona: seleciona masmorra, calcula distância/comida, envia.
- Barra de progresso exibe corretamente estado (IDA/VOLTA) e turnos.
- Build Frontend (`npm run build`) sem erros.
- Cálculo de Manhattan correto (testes unitários).

## Fora de escopo

- Tema visual.
- Histórico de expedições anteriores.
- Mapa de regiões com masmorras marcadas.
- Cancelamento de expedição.
