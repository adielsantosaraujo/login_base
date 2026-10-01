# H-001 · Tarefa 003 — Masmorras no mapa

**História:** [h-001-surgimento-e-evolucao-de-masmorras.md](h-001-surgimento-e-evolucao-de-masmorras.md) · **Domínio:** [../masmorras.md](../masmorras.md) ·
**Depende de:** [h-001-tarefa-002-surgimento-e-evolucao-no-turno.md](h-001-tarefa-002-surgimento-e-evolucao-no-turno.md), [../../../v1-008-vila-e-mapa/historia/h-002-tarefa-002-telas-do-mapa-e-da-regiao.md](../../v1-008-vila-e-mapa/historia/h-002-tarefa-002-telas-do-mapa-e-da-regiao.md) · **Camada:** Frontend

## Objetivo

Exibir masmorras no mapa de grade 4x4 da vila como ícones com indicação de nível, permitindo que o jogador veja o estado das masmorras.

## Contexto necessário

- [../masmorras.md](../masmorras.md) — estrutura de masmorras (nível 1–10).
  > Ativa até masmorra ser derrotada; exibe ícone e nível no mapa.

- [../../../v1-008-vila-e-mapa/historia/h-002-tarefa-002-telas-do-mapa-e-da-regiao.md](../../v1-008-vila-e-mapa/historia/h-002-tarefa-002-telas-do-mapa-e-da-regiao.md) — componente de grid 4x4.
  > Célula por região exibe tipo, posse e agora masmorra.

## Frontend

- **Componente** `MapaVila.vue` (modificação)
  - Chamar `GET /api/jogo/vila/mapa` para trazer lista de regiões com masmorras incluídas.
  - Exibir ícone (ex.: caverna/escavação) + nível (ex.: "N5") para região com masmorra ativa.
  - Cor/estilo diferente para masmorra vs. região normal.

- **Componente novo** `MasmorraIndicador.vue`
  - Props: `nivel: number`, `ativa: boolean`.
  - Renderiza: ícone + nível em badge/label.

- **Rota** (ex.: `/jogo/vila`) já exibe o mapa; apenas adicionar camada de masmorras.

## Backend

- **Endpoint** `GET /api/jogo/vila/mapa` (modificação existente)
  - Retornar também lista de masmorras ativas da vila.
  - Exemplo: `{ regioes: [...], masmorras: [{ regiao_indice: 5, nivel: 3, ativa: true }, ...] }`.

## Arquivos prováveis

- [/frontend/src/components/jogo/MapaVila.vue](/frontend/src/components/jogo/MapaVila.vue) (modificação)
- [/frontend/src/components/jogo/MasmorraIndicador.vue](/frontend/src/components/jogo/MasmorraIndicador.vue) (novo)

## Testes

- Teste visual: masmorra N1 aparece com ícone e label "N1" na grade 4x4.
- Teste: masmorra desaparece do mapa quando removed (ativa = false).
- Teste: múltiplas masmorras exibidas simultaneamente (até 3).

## Definição de pronto

- Build do frontend (`npm run build`) sem erros.
- Mapa 4x4 exibe masmorras com nível visível.
- Ícone claramente distinguido de regiões normais.

## Fora de escopo

- Clique em masmorra para atacar (tarefa h-002).
- Detalhe de inimigos específicos.
