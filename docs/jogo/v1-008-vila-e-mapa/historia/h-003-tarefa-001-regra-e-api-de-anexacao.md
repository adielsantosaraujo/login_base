# H-003 · Tarefa 001 — Regra e API de anexação

**História:** [h-003-anexar-nova-regiao.md](h-003-anexar-nova-regiao.md) · **Domínio:** [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar endpoint `POST /api/jogo/regioes/{indice}/anexar` que valida adjacência, masmorra, recursos, e conclui a anexação mantendo tipo e percentuais de terreno sorteados.

## Contexto necessário

- [design.md — D11. Mapa, resumo e anexação](/openspec/changes/redesenho-criacao-vila-populacao/design.md#d11-mapa-resumo-e-anexação)
  > Anexação usa tipo/percentuais de terreno gravados na criação; POST sem corpo; ladrilhos gerados se ausentes (todos os tipos).

- [../regioes.md](../regioes.md) — adjacência, custo, masmorras (1.2, 1.6)
  > Custo: Ouro = round(150 × 1,35^(k−3)); Madeira = Pedra = 50 × (k−2).
  > Regiões com masmorra ativa não podem ser anexadas.

## Backend

**Controlador (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java)
  - Endpoint: `POST /api/jogo/regioes/{indice}/anexar`
  - Corpo: vazio ou ignorado (sem `AnexarRegiaoRequest`)
  - Resposta sucesso (200): `{ "regiao": { indice, tipo, terrenos: [{terreno, posicao, percentual}, ...], possuida }, "estoque": {...}, "custo": { ouro: 203, madeira: 100, pedra: 100 } }`
  - Resposta erro (400/409): `{ "erro": "...", "codigo": "..." }`

**Serviço (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java](/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java)
  - Método: `anexarRegiao(Long usuarioId, int indiceRegiao)` throws ... (sem `tipo` como parâmetro)
  - Validações:
    1. Usuário tem vila
    2. Região existe em 1-16
    3. Região não está possuída (possuida == false)
    4. Região é adjacente a uma já possuída (buscar k primeiro)
    5. Região não tem masmorra ativa (regiao.masmorra == null ou !ativa)
    6. Estoque tem Ouro, Madeira, Pedra suficientes (fórmula de custo)
  - Execução (transação):
    - Debitar recursos do estoque
    - Atualizar regiao: possuida = true (tipo e 3 percentuais de terreno já gravados na criação)
    - Gerar ladrilhos se ausentes (todos os tipos, inclusive Urbana)
    - Gravar evento no relatório de turno (ou log)
  - Retornar Regiao atualizada com percentuais de terreno

**Exceções:**
- `RegiaoJaPossuídaException`
- `RegiaoComMasmorraException`
- `RecursosInsuficientesException`
- `RegiaoNaoAdjacenteException`

**Serviço auxiliar:**
- `validarAdjacencia(vila, indiceRegiao)` → boolean
- `calcularCustoAnexacao(vila)` → Map<Recurso, BigDecimal>

## Frontend

Não se aplica (tarefa 002).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java](/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java) (novo)

## Testes

- **Teste de integração**: POST /api/jogo/regioes/3/anexar sem corpo, k=3, região Montanha com percentuais de terreno sorteados, recursos suficientes → 200, região 3 possuída com tipo Montanha, percentuais retornados, estoque debitado.
- **Teste de validação**: POST região não adjacente → 400, "As regiões escolhidas precisam ser vizinhas entre si".
- **Teste de validação**: POST região com masmorra ativa → 400, erro apropriado.
- **Teste de validação**: POST com recursos insuficientes → 400, erro apropriado.
- **Teste de validação**: POST região já possuída → 400, erro apropriado.
- **Teste de geração**: POST região Floresta sem ladrilhos → 200, ladrilhos gerados conforme semente.
- **Teste de custo**: k=3 → Ouro 150; k=4 → Ouro 203; k=10 → Ouro 1.226 (verificar fórmula).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA4
- Build do backend (`./mvnw verify`) sem erros
- Testes listados passando
- Fórmula de custo verificada com números de game-design.md

## Fora de escopo

- Animação de anexação (visual).
- Histórico de anexações (para relatório do turno).
