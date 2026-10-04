# H-003 · Tarefa 002 — Interface de anexação

**História:** [h-003-anexar-nova-regiao.md](h-003-anexar-nova-regiao.md) · **Domínio:** [../regioes.md](../regioes.md) ·
**Depende de:** [h-003-tarefa-001-regra-e-api-de-anexacao.md](h-003-tarefa-001-regra-e-api-de-anexacao.md) · **Camada:** Frontend

## Objetivo

Implementar diálogo/modal Vue + PrimeVue para anexação de região, mostrando tipo, composição de terrenos, custo e validação de adjacência.

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Anexação mostra tipo e percentuais de terreno da região; POST sem corpo; sem escolha de tipo.

- [../regioes.md](../regioes.md) — adjacência, custo (1.2, 1.6)
  > Região clicada deve estar adjacente; custo exibido antes de confirmar.

## Frontend

**Componentes (novos):**
- [/frontend/src/components/DialogoAnexacao.vue](/frontend/src/components/DialogoAnexacao.vue) (novo)
  - Mostra quando célula vazia clicada na grade 4×4
  - Exibe:
    - Número da região, posição na grade
    - Tipo sorteado (Floresta, Planície, Urbana, Litoral ou Montanha) com ícone e cor
    - 3 percentuais de terreno da região (posição, sigla, valor; exemplo: "1º Rocha 44%, 2º Ferro 28%, 3º Carvão 28%")
    - Teste de adjacência: "Região adjacente a [lista de vizinhas possuídas]" (✓ verde) ou "Não adjacente" (✗ vermelho)
    - Custo: Ouro X, Madeira Y, Pedra Z (sombreado se insuficiente)
    - Botões: "Anexar" (desabilitado se não adjacente ou recursos insuficientes), "Cancelar"
  - Chamada: POST /api/jogo/regioes/{indice}/anexar sem corpo ao clicar "Anexar"
  - Feedback: spinner durante requisição, mensagem de sucesso/erro, atualiza mapa (com os ladrilhos da região)

**Integração com Mapa.vue:**
- Ao clicar em célula vazia (não possuída), abre DialogoAnexacao passando indiceRegiao
- Ao fechar diálogo após sucesso, Mapa atualiza lista de regiões (refetch ou atualização local)

**Composable (novo/existente):**
- [/frontend/src/composables/useAnexacao.ts](/frontend/src/composables/useAnexacao.ts) (novo)
  - Função: `anexarRegiao(indice)` — POST /api/jogo/regioes/{indice}/anexar sem corpo

**Rota/Estado:**
- Mapa.vue reage a click em célula vazia e abre diálogo

## Backend

Não se aplica (tarefa 001).

## Arquivos prováveis

- [/frontend/src/components/DialogoAnexacao.vue](/frontend/src/components/DialogoAnexacao.vue) (novo)
- [/frontend/src/composables/useAnexacao.ts](/frontend/src/composables/useAnexacao.ts) (novo)

## Testes

- **Teste funcional**: clicar em região vazia adjacente → diálogo abre, mostra tipo sorteado e 3 percentuais de terreno (ex.: "1º Rocha 44%, 2º Ferro 28%, 3º Carvão 28%"), custo correto, botão "Anexar" habilitado.
- **Teste funcional**: diálogo com recursos insuficientes → botão "Anexar" desabilitado, mensagem apropriada.
- **Teste funcional**: clicar "Anexar" → POST enviado sem corpo, diálogo fecha, mapa atualiza, região agora possuída com tipo sorteado, ladrilhos exibidos.
- **Teste E2E**: Mapa → clicar vazia (Montanha) → diálogo anexação mostra tipo/percentuais → "Anexar" → sucesso → mapa atualiza, grade 10×10 mostra endereços dos ladrilhos.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA5 (e validações visuais)
- Testes E2E passando
- Componente renderiza sem erro
- Integração com Mapa funciona

## Fora de escopo

- Confirmação dupla (redundante com botão).
- Histórico de anexações.
