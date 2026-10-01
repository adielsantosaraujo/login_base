# H-001 · Tarefa 004 — Tela da oficina

**História:** [H-001 — Fabricar item na oficina](h-001-fabricar-item-na-oficina.md) · **Domínio:** [../itens.md](../itens.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-itens.md](h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Frontend

## Objetivo

Criar interface (Vue + PrimeVue) para que o jogador visualize a oficina, veja a fila de fabricação, e submeta novos itens para fabricar com validação de requisitos (PE, nível máximo, recursos).

## Contexto necessário

- [CA1–CA6 de H-001](h-001-fabricar-item-na-oficina.md#critérios-de-aceite)
  > Rejeições validadas: nível acima do máximo, PE insuficiente, recursos insuficientes
  > Conversão Ferro→Aço em L≥6
  > Qualidade sorteada conforme margem

- [Requisitos de fabricação (seção 7.4)](../fabricacao.md#requisitos-de-fabricação)
  > PE efetivo ≥ 2L − 2
  > Nível máximo: oficina N1 → L3; N2 → L6; N3 → L10

## Frontend

- Componente `OficinaTela.vue`:
  - Dados da oficina: tipo (Ferraria/Alfaiataria/Carpintaria), nível (N1/N2/N3), artesãos alocados
  - Fila de fabricação: lista com item (nome, ícone), nível, progresso (barra com PF atual / PF total), tempo estimado (turnos)
  - Botão "Nova fabricação": diálogo modal com:
    - Seletor de item (receitas disponíveis para a oficina)
    - Seletor de nível (L1–máximo permitido)
    - Seletor de artesão (lista de cidadãos com PE base ≥ 2L−2 na profissão)
    - Estimativa de custo (recursos) e tempo (turnos, baseado em eficiência média)
    - Botão "Fabricar" → API POST

- Validações no frontend:
  - Nível ≤ máximo da oficina
  - PE do artesão ≥ 2L − 2
  - Custo mostrado com aviso se recursos insuficientes
  - Desabilitar botão se validação falha

- Componentes PrimeVue:
  - `pButton` (Nova fabricação, Fabricar)
  - `pDataTable` (fila de fabricação, com progressBar)
  - `pDialog` (modal de nova fabricação)
  - `pDropdown` (seletores de item, nível, artesão)
  - `pToast` (feedback de sucesso/erro)

- Rotas:
  - `/jogo/regioes/:indice/oficina/:id` → rota para acessar oficina de uma região

- Chamadas de API:
  - `GET /api/jogo/oficinas/:id` → dados da oficina e artesãos
  - `GET /api/jogo/oficinas/:id/fila` → fila de fabricação
  - `POST /api/jogo/oficinas/:id/fabricacoes` (body: itemSubtipo, nivel, artesaoId) → criar novo item
    - Response: { success, item, erro?: { codigo, mensagem } }
  - `GET /api/jogo/items/receitas?oficina=FERRARIA` → receitas disponíveis

## Arquivos prováveis

- [/frontend/src/views/OficinaTela.vue](/frontend/src/views/OficinaTela.vue) (novo)
- [/frontend/src/components/FabricacaoModal.vue](/frontend/src/components/FabricacaoModal.vue) (novo)
- [/frontend/src/services/jogoApiService.js](/frontend/src/services/jogoApiService.js) (atualizar)

## Testes

- Teste de renderização: componente exibe fila vazia se nenhum item em fabricação
- Teste de seletor de nível: máximo L3 em N1, L6 em N2, L10 em N3
- Teste de validação: PE insuficiente desabilita botão "Fabricar"
- Teste de erro de recursos: toast mostra mensagem de erro da API
- Teste de sucesso: item adicionado à fila após POST bem-sucedido

## Definição de pronto

- Critérios de aceite da história cobertos: CA1–CA6 (validações e feedback)
- Build do frontend (`npm run build`) sem erros
- Testes de componente (Jest) passando
- Interface responsiva (mobile 320px+)
- Acessibilidade: labels em inputs, títulos em tabelas
- Integração com API do backend (mock em testes)

## Fora de escopo

- Aprimoramento de itens (tarefa h-003-tarefa-001)
- Equipamento de itens (tarefa h-002)
- Cancelamento de fabricação (fora de escopo)
