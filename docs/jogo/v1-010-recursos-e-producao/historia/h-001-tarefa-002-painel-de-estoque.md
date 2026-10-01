# H-001 · Tarefa 002 — Painel de estoque

**História:** [h-001-consultar-estoque-de-recursos.md](h-001-consultar-estoque-de-recursos.md) · **Domínio:** [../recursos.md](../recursos.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-estoque-e-capacidade.md](h-001-tarefa-001-modelo-de-estoque-e-capacidade.md) · **Camada:** Frontend

## Objetivo

Criar a tela de estoque que exibe a quantidade e capacidade de cada recurso, com alertas de excedente próximo ao limite.

## Contexto necessário

- [../recursos.md#números-e-tabelas](../recursos.md#números-e-tabelas) — tabela 3.1, 19 recursos
  > Recursos ordenados: Madeira, Pedra, Argila, Minério de ferro, Carvão, Sal, Enxofre, Grãos, Fibra, Carne, Couro, Lã, Tábua, Tijolo, Ferro, Aço, Tecido, Couro curtido, Refeição, Ouro.

- [../recursos.md#regras](../recursos.md#regras) — quantidade exibida em inteiro (floor); capacidade variável por Armazém

## Backend

- **Endpoint**
  - `GET /api/jogo/estoque` → `{ recursos: [{ nome, quantidade, capacidade, percentual_usado }], bem_alimentada: boolean, proxima_perda_estimada: {...} }`
  - Quantidade formatada com 2 casas decimais internamente, exibição inteira
  - Capacidade total calculada via EstoqueService.calcularCapacidadeTotal(vila)

## Frontend

- **Componentes PrimeVue**
  - `DataTable` com lista de 19 recursos
  - Colunas: Nome, Quantidade (inteiro), Capacidade, Percentual (gráfico de barras), Status
  - Linha vermelha se quantidade > 0,9 × capacidade
  - Linha verde se bem_alimentada = true (bônus ativo)

- **Tela**
  - `/frontend/src/views/JogoEstoque.vue` (novo)
  - Chamada: `GET /api/jogo/estoque` ao carregar
  - Atualizar a cada 5 segundos (ou ao clicar "Atualizar")
  - Botão "Consultar capacidade por Armazém" mostra detalhamento

## Arquivos prováveis

- [/frontend/src/views/JogoEstoque.vue](/frontend/src/views/JogoEstoque.vue) (novo)
- [/frontend/src/components/EstoqueTable.vue](/frontend/src/components/EstoqueTable.vue) (novo)
- [/src/main/java/com/example/loginbase/jogo/recurso/EstoqueController.java](/src/main/java/com/example/loginbase/jogo/recurso/EstoqueController.java) (novo)

## Testes

- Teste de integração: `GET /api/jogo/estoque` retorna 19 recursos com quantidades e capacidade
- Teste frontend: DataTable renderiza todos os recursos; clique em linha mostra detalhes

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3
- Build do frontend (`npm run build`) sem erros
- Testes de integração passando
- Tela acessível via navegação principal

## Fora de escopo

- Histórico de variações
- Alertas de depleção de recursos
