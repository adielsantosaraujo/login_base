# H-002 · Tarefa 002 — Interface de engaste

**História:** [H-002 — Engastar pedra em item](h-002-engastar-pedra-em-item.md) · **Domínio:** [../pedras-de-bonus.md](../pedras-de-bonus.md), [../bonus.md](../bonus.md) ·
**Depende de:** H-002-tarefa-001 (API de engaste), H-006-tarefa-004 (tela de inventário) | — · **Camada:** Frontend

## Objetivo

Implementar interface Vue + PrimeVue para engaste de pedras em itens, exibindo disponibilidade de slots, custos e bônus de forma clara.

## Contexto necessário

- [Pedras de bônus — tipos e custos](../pedras-de-bonus.md)
  > Simples 10 Ouro, Boa 25, Excelente 60, Divina 150. Slots de pedra: Boa 1, Excelente 3, Divina 5.

- [Catálogo de bônus](../bonus.md)
  > Códigos e magnitudes para exibição dos efeitos de forma legível.

- [API de engaste](h-002-tarefa-001-api-de-engaste.md)
  > Endpoints: `POST /api/jogo/ferraria/engaste`, `POST /api/jogo/ferraria/remover-pedra`.

## Frontend

### Componentes Vue + PrimeVue

#### **Diálogo de engaste (ModalEngaste.vue)**

Exibido ao selecionar "Engastar" em uma pedra do inventário.

**Props:**
- `pedra`: objeto com `id`, `qualidade`, `bonus[]`.
- `itensCompativeis`: array de itens que podem receber a pedra (qualidade ≠ Simples, com slot livre).
- `ouroDisponivel`: número.

**Estrutura:**
1. **Cabeçalho:** "Engastar [nome-da-pedra-tipo]"
2. **Detalhes da pedra:**
   - Tipo (Simples/Boa/Excelente/Divina)
   - Lista de bônus com ícones/cores (ex.: VIT em vermelho, ATK em laranja)
   - Custo em Ouro

3. **Seleção de item alvo:**
   - Dropdown/Grid com itens compatíveis
   - Para cada item: nome, qualidade, slots livres/totais

4. **Validações inline:**
   - Se nenhum item compatível: "Sem itens com slots disponíveis"
   - Se ouro < custo: "Ouro insuficiente (faltam X Ouro)"
   - Caso contrário: botão "Engastar" habilitado

5. **Botões:**
   - "Engastar" (desabilitado se validação falhar)
   - "Cancelar"

**Comportamento:**
- Ao clicar "Engastar", chama `POST /api/jogo/ferraria/engaste`.
- Em caso de sucesso (200): exibe toast "Pedra engastada com sucesso"; atualiza lista de itens/inventário; fecha diálogo.
- Em caso de erro (400/403): exibe mensagem de erro específica (ex.: "Ouro insuficiente").

#### **Painel de slots de pedra (SlotsPedras.vue)**

Componente reutilizável exibindo slots de pedra de um item.

**Props:**
- `item`: objeto com `qualidade`, `pedras[]`.
- `editavel`: boolean (true se permitir remover).

**Estrutura:**
1. Grid visual de slots (quadrados):
   - Slot vazio: quadrado cinza
   - Slot com pedra: quadrado com cor/tipo, hover mostra bônus

2. Interação:
   - Hover em pedra: tooltip com lista de bônus
   - Clique em pedra (se editável): botão "Remover"

3. Remover pedra:
   - Diálogo de confirmação: "Remover pedra destruirá permanentemente. Continuar?"
   - Ao confirmar: chama `POST /api/jogo/ferraria/remover-pedra`.
   - Sucesso: atualiza slots; exibe toast.

#### **Integração no inventário (tela 8 do roadmap)**

Adicionar aba ou seção "Gerenciar pedras" que:
1. Lista pedras do inventário (não engastadas)
2. Para cada pedra: tipo, bônus, custo, botão "Engastar"
3. Ao clicar "Engastar": abre ModalEngaste

Além disso:
- Na visualização de itens, exibir SlotsPedras (editável se item no inventário, não-editável se equipado)

### Rotas

- `/jogo/inventario/pedras`: página de gerenciamento de pedras.
- ModalEngaste integrada na rota de inventário.

## Arquivos prováveis

- [/frontend/src/components/ModalEngaste.vue](/frontend/src/components/ModalEngaste.vue) (novo)
- [/frontend/src/components/SlotsPedras.vue](/frontend/src/components/SlotsPedras.vue) (novo)
- [/frontend/src/views/InventarioPedras.vue](/frontend/src/views/InventarioPedras.vue) (novo, ou integrado em InventarioItems.vue)
- [/frontend/src/services/EngasteService.ts](/frontend/src/services/EngasteService.ts) (novo)

## Testes

- **Teste de renderização: `testeRenderizacaoModalEngaste()`**
  - Renderiza ModalEngaste com pedra Boa, 2 itens compatíveis, ouro suficiente.
  - Valida: campo de seleção de item visível, botão "Engastar" habilitado.

- **Teste de interação: `testeDesabilitacaoBotaoOuroInsuficiente()`**
  - Props com ouroDisponivel = 15, custo = 25.
  - Valida: botão "Engastar" desabilitado, mensagem de erro exibida.

- **Teste de chamada de API: `testeEnvioChamadaEngaste()`**
  - Usuário seleciona item e clica "Engastar".
  - Valida: `POST /api/jogo/ferraria/engaste` chamado com `pedra_id` e `item_id` corretos.
  - Resposta 200: toast exibido, diálogo fechado, inventário atualizado.

- **Teste de remocao: `testeRemocaoPermanente()`**
  - SlotsPedras com pedra engastada.
  - Clica em pedra, confirma remoção.
  - Valida: `POST /api/jogo/ferraria/remover-pedra` chamado; slot fica vazio após sucesso.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2, CA3, CA4, CA6.
- Build do frontend (`npm run build`) sem erros ou warnings.
- Testes listados acima passando (cobertura ≥70% dos componentes).
- Componentes acessíveis (labels, ARIA, navegação por teclado).
- Interface responsiva (telas ≥320px de largura).

## Fora de escopo

- Integração de cálculo de atributo em tempo real (será feita em outro módulo).
- Histórico de engastes.
- Busca/filtro de pedras por tipo.
