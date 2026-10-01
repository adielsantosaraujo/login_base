# H-004 · Tarefa 002 — Tela do mercado

**História:** [h-004-negociar-recursos-no-mercado.md](h-004-negociar-recursos-no-mercado.md) · **Domínio:** [../comercio.md](../comercio.md) ·
**Depende de:** [h-004-tarefa-001-api-de-compra-e-venda.md](h-004-tarefa-001-api-de-compra-e-venda.md) · **Camada:** Frontend

## Objetivo

Criar a tela do Mercado que exibe preços de venda/compra em tempo real, permite selecionar recurso e quantidade, e executa a transação.

## Contexto necessário

- [../comercio.md#números-e-tabelas](../comercio.md#números-e-tabelas) — 19 recursos com preços base
  > Exibir preço base e preço atual (dinâmico conforme PE Comerciante).

## Backend

- Endpoint disponível: `POST /api/jogo/mercado/ordens`
- GET `/api/jogo/mercado/precos` (novo): retorna `{ recurso: { precoVenda, precoCompra, precoBase }, ... }` calculado com PE melhor Comerciante

## Frontend

- **Componentes PrimeVue**
  - `TabView` com abas "Vender" e "Comprar"
  - Aba Vender:
    - `Dropdown` de recursos (apenas com estoque > 0)
    - `InputNumber` para quantidade
    - Exibir: quantidade disponível, preço unitário, total Ouro a receber
    - Botão "Vender"
  - Aba Comprar:
    - `Dropdown` de recursos
    - `InputNumber` para quantidade
    - Exibir: Ouro disponível, preço unitário, total Ouro a gastar
    - Botão "Comprar"
  - `DataTable` com histórico das 5 últimas transações (recurso, tipo, quantidade, preço)

- **Tela**
  - `/frontend/src/views/JogoMercado.vue` (novo)
  - Chamada: `GET /api/jogo/mercado/precos` ao carregar
  - Atualizar preços a cada 5 segundos (PE muda quando Comerciante é alocado/desalocado)
  - Feedback visual: sucesso verde / erro vermelho

## Arquivos prováveis

- [/frontend/src/views/JogoMercado.vue](/frontend/src/views/JogoMercado.vue) (novo)
- [/frontend/src/components/MercadoPanel.vue](/frontend/src/components/MercadoPanel.vue) (novo)
- [/src/main/java/com/example/loginbase/jogo/comercio/MercadoController.java](/src/main/java/com/example/loginbase/jogo/comercio/MercadoController.java) — adicionar método GET precos()

## Testes

- Teste integração: `GET /api/jogo/mercado/precos` retorna 19 recursos com precoVenda < precoBase < precoCompra
- Teste frontend: `POST /api/jogo/mercado/ordens` com quantidade válida atualiza estoque e histórico

## Definição de pronto

- Critério de aceite da história: não direto (fica para H-004 tarefa 3 + essa)
- Frontend compila sem erros
- Tela integrada ao navegação principal
- Preços atualizam em tempo real
- Feedback visual claro

## Fora de escopo

- Favoritos de recursos
- Alertas de preço limite
