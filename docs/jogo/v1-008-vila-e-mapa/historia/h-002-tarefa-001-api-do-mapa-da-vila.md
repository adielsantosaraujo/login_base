# H-002 · Tarefa 001 — API do mapa da vila

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para obter dados do mapa (grade 4×4 com regiões, tipo, composição de terrenos, masmorra) e detalhes de uma região (10×10 ladrilhos com terreno, bônus e construções).

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Mapa mostra tipo e composição de terrenos das 16 regiões; ladrilhos com terreno/bônus e construções (todos os tipos).

- [../regioes.md](../regioes.md) — regiões e terrenos
  > Regiões têm tipo (Floresta, Planície, Urbana, Litoral, Montanha) e 3 terrenos com percentuais; cada ladrilho tem terreno, bonus_base, bonus_adjacente.

## Backend

**Controlador (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java)
  - Endpoint: `GET /api/jogo/vila/mapa`
  - Resposta: `{ "regioes": [ { "indice": 1, "tipo": "MONTANHA", "terrenos": [{terreno: "ROCHA", posicao: 1, percentual: 42}, ...], "possuida": false, "masmorra": null }, ... ] }`
  - Endpoint: `GET /api/jogo/regioes/{indice}`
  - Resposta: `{ "regiao": { id, indice, tipo, terrenos: [{terreno, posicao, percentual}, ...], possuida }, "ladrilhos": [ { x: 0, y: 0, terreno: "ROCHA", bonusBase: 30, bonusAdjacente: 25, bonusTotal: 55, construcao: { tipo: "Casa", nivel: 1 } }, ... ] }`

**Serviço (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/servico/MapaService.java](/src/main/java/com/example/loginbase/jogo/servico/MapaService.java)
  - Método: `MapaDTO obterMapaVila(Long usuarioId)` — lista 16 regiões com tipo, terrenos e percentuais, possuida, masmorra
  - Método: `RegiaoDetalheDTO obterRegiaoDetalhada(Long usuarioId, int indiceRegiao)` — 10×10 ladrilhos com terreno, bonus_base/adjacente/total e construções; gera ladrilhos se ausentes (todos os tipos, inclusive Urbana)

**DTOs (novos/existentes):**
- `MapaDTO` com lista de `RegiaoResumoDTO`
- `RegiaoResumoDTO`: indice, tipo, terrenos (array de RegiaoTerrenoDTO), possuida, masmorraAtiva, nivelMasmorra
- `RegiaoDetalheDTO`: regiao (indice, tipo, terrenos), ladrilhos (array 10×10 com terreno, bônus e Construção)
- `RegiaoTerrenoDTO`: terreno (enum), posicao (1-3), percentual (int)
- `LadrilhoDTO`: x, y, terreno (enum), bonusBase (int), bonusAdjacente (int), bonusTotal (int), construcao (null se não houver)

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/MapaService.java](/src/main/java/com/example/loginbase/jogo/servico/MapaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/MapaDTO.java](/src/main/java/com/example/loginbase/jogo/dto/MapaDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoResumoDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoResumoDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoDetalheDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoDetalheDTO.java) (novo)

## Testes

- **Teste de integração**: GET /api/jogo/vila/mapa com usuário autenticado → 200, retorna 16 regiões, 3 possuídas, tipos, terrenos e percentuais corretos.
- **Teste de integração**: GET /api/jogo/regioes/6 para região possuída → 200, 100 ladrilhos (10×10), 4 casas nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura e todas em terreno Desenvolvimento, tipo e terrenos.
- **Teste de geração**: GET /api/jogo/regioes/2 para região não possuída Montanha → 200, 100 ladrilhos gerados pela semente com terreno e bônus, tipo e terrenos.
- **Teste de segurança**: GET sem autenticação → 401.
- **Teste de autorização**: GET /api/jogo/regioes/10 (região de outra vila) → 403 ou acesso negado.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA2
- Build do backend (`./mvnw verify`) sem erros
- Testes listados passando
- Endpoints documentados em Swagger

## Fora de escopo

- Dados de masmorra (virão da tarefa de masmorras).
- Dados de construções (virão da tarefa de construções).
