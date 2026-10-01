# H-002 · Tarefa 002 — Interface de equipamento

**História:** [H-002 — Equipar itens no painel da pessoa](h-002-equipar-itens-no-painel-da-pessoa.md) · **Domínio:** [../itens.md](../itens.md), [../equipamento.md](../equipamento.md) · **Depende de:** [h-002-tarefa-001-regras-e-api-de-equipar.md](h-002-tarefa-001-regras-e-api-de-equipar.md) · **Camada:** Frontend

## Objetivo

Criar interface no painel da pessoa (Vue + PrimeVue) para visualizar slots de equipamento, equipar itens do inventário e validar requisitos no frontend.

## Contexto necessário

- [CA1–CA6 de H-002](h-002-equipar-itens-no-painel-da-pessoa.md#critérios-de-aceite)
  > Validações de slot, limite de anéis, PE, idade, estado da tropa

- [Requisitos e limites (seção 7.5, 7.7)](../equipamento.md)
  > Idade ≥14/≥16, PE mínimos, 1 Colar, 2 Anéis

- [Tabela de requisitos de PE](../equipamento.md#requisitos-de-pe-por-nível)

## Frontend

- Componente `PainelEquipamento.vue`:
  - Grid/flexbox com slots de equipamento:
    - Arma (1 slot)
    - Ferramenta (1 slot)
    - Armaduras (6 slots: Peitoral, Capacete, Ombreiras, Luvas, Calças, Sapato)
    - Joias (1 Colar, 2 Anéis)
  - Cada slot mostra:
    - Ícone e nome do item equipado (ou "Vazio")
    - Nível (L1–L10)
    - Bônus listados (principais)
    - Botão "Trocar" (abre seletor de inventário)
    - Botão "Remover" (opcional; pode deixar vazio)

- Modal `SeletorInventario.vue`:
  - Campo de busca (filtrar por nome/categoria)
  - Lista de itens do inventário, filtrados por categoria do slot
  - Para cada item: ícone, nome, nível, qualidade (cor), bônus (preview)
  - Validação visual:
    - Desabilitar itens que não atendem requisitos (PE, idade)
    - Ícone/tooltip explicando o motivo (pe-insuficiente, idade-minima, etc.)
  - Botão "Equipar" → PUT API

- Validações no frontend (antes da API):
  - Idade < mínima: desabilita item, tooltip "Mínimo X anos"
  - PE insuficiente: desabilita item, tooltip "PE mínimo Y necessário"
  - Limite de anéis: ao abrir seletor de anel, desabilita 3º anel se 2 já equipados
  - Tropa em expedição: desabilita todos os slots, mensagem "Equipamento bloqueado em expedição"

- Componentes PrimeVue:
  - `pCard` (painel)
  - `pButton` (Trocar, Remover)
  - `pDialog` (modal seletor)
  - `pDataTable` (lista de itens no inventário)
  - `pTooltip` (validações)
  - `pTag` (qualidade)

- Chamadas de API:
  - `GET /api/jogo/pessoas/{pessoaId}` → dados, idade, PE, estado da tropa
  - `GET /api/jogo/inventario?vila={vilaId}` → todos os itens não equipados
  - `PUT /api/jogo/pessoas/{pessoaId}/equipamento/{slot}` (body: itemId) → equipar
    - Erro: { codigo, mensagem }
    - Sucesso: { pessoa, item }

- Feedback UX:
  - Toast de sucesso: "Espada L3 equipada com sucesso"
  - Toast de erro: "PE insuficiente: mínimo 4, você tem 2"

## Arquivos prováveis

- [/frontend/src/components/PainelEquipamento.vue](/frontend/src/components/PainelEquipamento.vue) (novo)
- [/frontend/src/components/SeletorInventario.vue](/frontend/src/components/SeletorInventario.vue) (novo)
- [/frontend/src/views/PainelPessoa.vue](/frontend/src/views/PainelPessoa.vue) (atualizar com PainelEquipamento)

## Testes

- Teste de renderização: 6 slots de armadura + 1 arma + 1 ferramenta + 1 colar + 2 anéis = 11 slots
- Teste de filtro: seletor de Espada mostra apenas armas
- Teste de desabilitar: Espada L5 com requisito PE 4; pessoa com PE 2 → desabilita com tooltip
- Teste de limite: 2 anéis equipados → 3º anel desabilido no seletor
- Teste de expedição: GET pessoa retorna tropa EM_VIAGEM_IDA → todos slots desabilitados
- Teste de sucesso: PUT bem-sucedido → toast e atualização de painel

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6
- Build do frontend (`npm run build`) sem erros
- Testes de componente (Jest) passando
- Responsivo (mobile 320px+)
- Acessibilidade (labels, títulos, tooltips)
- Toast feedback para todas as ações

## Fora de escopo

- Troca de posição entre Anel 1 e Anel 2
- Remoção permanente de item (apenas desequipar)
- Venda de itens
