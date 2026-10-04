# H-002 · Tarefa 001 — API do mapa da vila

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para obter dados do mapa (grade 4×4 com regiões, tipo, bônus, masmorra) e detalhes de uma região (10×10 ladrilhos com construções e jazidas).

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Mapa mostra tipo e bônus das 16 regiões; ladrilhos gerados se ausentes (tipo ≠ Urbana).

- [../regioes.md](../regioes.md) — regiões e jazidas
  > Regiões têm tipo (Floresta, Planície, Urbana, Litoral, Montanha) e jazidas em tipos não-Urbanos.

## Backend

**Controlador (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java)
  - Endpoint: `GET /api/jogo/vila/mapa`
  - Resposta: `{ "vila": { ..., "bonusRegiao": {"FLORESTA": 25, ...} }, "regioes": [ { "indice": 1, "tipo": "MONTANHA", "bonus": [...], "possuida": false, "masmorra": null }, ... ] }`
  - Endpoint: `GET /api/jogo/regioes/{indice}`
  - Resposta: `{ "regiao": { id, indice, tipo, bonus: [...], possuida }, "ladrilhos": [ { x: 0, y: 0, jazida: "Floresta", construcao: { tipo: "Casa", nivel: 1 } }, ... ] }`

**Serviço (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/servico/MapaService.java](/src/main/java/com/example/loginbase/jogo/servico/MapaService.java)
  - Método: `MapaDTO obterMapaVila(Long usuarioId)` — lista 16 regiões com tipo, bônus, possuida, masmorra
  - Método: `RegiaoDetalheDTO obterRegiaoDetalhada(Long usuarioId, int indiceRegiao)` — 10×10 ladrilhos com jazidas e construções; gera ladrilhos se ausentes e tipo ≠ URBANA

**DTOs (novos/existentes):**
- `MapaDTO` com lista de `RegiaoResumoDTO` e `bonusRegiao` da vila
- `RegiaoResumoDTO`: indice, tipo, bonus (array de RegiaoBonusDTO), possuida, masmorraAtiva, nivelMasmorra
- `RegiaoDetalheDTO`: regiao (indice, tipo, bonus), ladrilhos (array 10×10 com Jazz e Construção)
- `RegiaoBonusDTO`: bonus (enum), posicao (1-3), valor (int)
- `LadrilhoDTO`: x, y, jazida, construcao (null se não houver)

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/MapaService.java](/src/main/java/com/example/loginbase/jogo/servico/MapaService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/MapaDTO.java](/src/main/java/com/example/loginbase/jogo/dto/MapaDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoResumoDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoResumoDTO.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/dto/RegiaoDetalheDTO.java](/src/main/java/com/example/loginbase/jogo/dto/RegiaoDetalheDTO.java) (novo)

## Testes

- **Teste de integração**: GET /api/jogo/vila/mapa com usuário autenticado → 200, retorna 16 regiões, 3 possuídas, tipos, bônus e `bonusRegiao` da vila corretos.
- **Teste de integração**: GET /api/jogo/regioes/6 para região possuída → 200, 100 ladrilhos (10×10), 4 casas nos ladrilhos (0,0), (2,0), (4,0), (6,0), tipo e bônus.
- **Teste de geração**: GET /api/jogo/regioes/2 para região não possuída Montanha sem ladrilhos → 200, 100 ladrilhos gerados pela semente, tipo e bônus.
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
