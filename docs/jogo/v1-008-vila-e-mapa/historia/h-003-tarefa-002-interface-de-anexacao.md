# H-003 · Tarefa 002 — Interface de anexação

**História:** [h-003-anexar-nova-regiao.md](h-003-anexar-nova-regiao.md) · **Domínio:** [../regioes.md](../regioes.md) ·
**Depende de:** [h-003-tarefa-001-regra-e-api-de-anexacao.md](h-003-tarefa-001-regra-e-api-de-anexacao.md) · **Camada:** Frontend

## Objetivo

Implementar diálogo/modal Vue + PrimeVue para anexação de região, mostrando custo, permitindo seleção de tipo, validação de adjacência visual.

## Contexto necessário

- [../regioes.md](../regioes.md) — adjacência, custo (1.2, 1.6)
  > Região clicada deve estar adjacente; custo exibido antes de confirmar.

## Frontend

**Componentes (novos):**
- [/frontend/src/components/DialogoAnexacao.vue](/frontend/src/components/DialogoAnexacao.vue) (novo)
  - Mostra quando célula vazia clicada na grade 4×4
  - Exibe:
    - Número da região, posição na grade
    - Teste de adjacência: "Região adjacente a [lista de vizinhas possuídas]" (✓ verde) ou "Não adjacente" (✗ vermelho)
    - Custo: Ouro X, Madeira Y, Pedra Z (sombreado se insuficiente)
    - Dropdown para tipo (Rural, Urbana, Coleta)
    - Botões: "Anexar" (desabilitado se não adjacente ou recursos insuficientes), "Cancelar"
  - Chamada: POST /api/jogo/regioes/{indice}/anexar ao clicar "Anexar"
  - Feedback: spinner durante requisição, mensagem de sucesso/erro, atualiza mapa

**Integração com Mapa.vue:**
- Ao clicar em célula vazia (não possuída), abre DialogoAnexacao passando indiceRegiao
- Ao fechar diálogo após sucesso, Mapa atualiza lista de regiões (refetch ou atualização local)

**Composable (novo/existente):**
- [/frontend/src/composables/useAnexacao.js](/frontend/src/composables/useAnexacao.js) (novo)
  - Função: `anexarRegiao(indice, tipo)` — POST /api/jogo/regioes/{indice}/anexar

**Rota/Estado:**
- Mapa.vue reage a click em célula vazia e abre diálogo

## Backend

Não se aplica (tarefa 001).

## Arquivos prováveis

- [/frontend/src/components/DialogoAnexacao.vue](/frontend/src/components/DialogoAnexacao.vue) (novo)
- [/frontend/src/composables/useAnexacao.js](/frontend/src/composables/useAnexacao.js) (novo)

## Testes

- **Teste funcional**: clicar em região vazia adjacente → diálogo abre, mostra custo correto, tipo pode ser selecionado, botão "Anexar" habilitado.
- **Teste funcional**: diálogo com recursos insuficientes → botão "Anexar" desabilitado, mensagem "Recursos insuficientes".
- **Teste funcional**: clicar "Anexar" → POST enviado, diálogo fecha, mapa atualiza, região agora possuída com cor/tipo escolhido.
- **Teste E2E**: Mapa → clicar vazia → diálogo anexação → "Anexar" → sucesso → mapa atualiza.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA5 (e validações visuais)
- Testes E2E passando
- Componente renderiza sem erro
- Integração com Mapa funciona

## Fora de escopo

- Confirmação dupla (redundante com botão).
- Histórico de anexações.
