# H-002 · Tarefa 001 — API do mapa da vila

**História:** [h-002-visualizar-mapa-da-vila.md](h-002-visualizar-mapa-da-vila.md) · **Domínio:** [../vila.md](../vila.md), [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar endpoints REST para obter dados do mapa (grade 4×4 com regiões possuídas, tipo, masmorra) e detalhes de uma região (10×10 ladrilhos com construções e jazidas).

## Contexto necessário

- [../vila.md](../vila.md) — estrutura de vila e regiões (11.1, 11.2)
  > Vila tem 16 regiões; cada região pode estar possuída (tipo definido) ou vazia (tipo nulo).

- [../regioes.md](../regioes.md) — regiões e jazidas
  > Regiões em Coleta têm jazidas; em Rural/Urbana, Jazz irrelevante.

## Backend

**Controlador (novo):**
- [/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/MapaControlador.java) (novo)
  - Endpoint: `GET /api/jogo/vila/mapa`
  - Resposta: `{ "vila": { id, nome, ... }, "regioes": [ { indice: 1, tipo: null }, { indice: 6, tipo: "URBANA", possuida: true, masmorra: null }, ... ] }`
  - Endpoint: `GET /api/jogo/regioes/{indice}`
  - Resposta: `{ "regiao": { id, indice, tipo, possuida }, "ladrilhos": [ { x: 0, y: 0, jazida: "Floresta", construcao: { tipo: "Casa", nivel: 1 } }, ... ] }`

**Serviço (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/servico/MapaService.java](/src/main/java/com/example/loginbase/jogo/servico/MapaService.java) (novo)
  - Método: `MapaDTO obterMapaVila(Long usuarioId)` — lista 16 regiões com tipo, possuida, masmorra
  - Método: `RegiaoDetalheDTO obterRegiaoDetalhada(Long usuarioId, int indiceRegiao)` — 10×10 ladrilhos com jazidas e construções

**DTOs (novos):**
- `MapaDTO` com lista de `RegiaoResumoDTO`
- `RegiaoResumoDTO`: indice, tipo, possuida, masmorraAtiva, nivelMasmorra
- `RegiaoDetalheDTO`: regiao (indice, tipo), ladrilhos (array 10×10 com Jazz e Construção)
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

- **Teste de integração**: GET /api/jogo/vila/mapa com usuário autenticado → 200, retorna 16 regiões, 3 possuídas, tipos corretos.
- **Teste de integração**: GET /api/jogo/regioes/6 para região possuída → 200, 100 ladrilhos (10×10), 4 casas nos ladrilhos (0,0), (2,0), (4,0), (6,0).
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
