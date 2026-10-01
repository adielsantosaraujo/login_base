# H-003 · Tarefa 001 — Regra e API de anexação

**História:** [h-003-anexar-nova-regiao.md](h-003-anexar-nova-regiao.md) · **Domínio:** [../regioes.md](../regioes.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) · **Camada:** Backend

## Objetivo

Implementar endpoint `POST /api/jogo/regioes/{indice}/anexar` que valida adjacência, masmorra, recursos, e conclui a anexação com atualização de tipo.

## Contexto necessário

- [../regioes.md](../regioes.md) — adjacência, custo, masmorras (1.2, 1.6)
  > Custo: Ouro = round(150 × 1,35^(k−3)); Madeira = Pedra = 50 × (k−2).
  > Regiões com masmorra ativa não podem ser anexadas.

## Backend

**Controlador (novo/existente):**
- [/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java](/src/main/java/com/example/loginbase/jogo/controlador/RegiaoControlador.java) (novo)
  - Endpoint: `POST /api/jogo/regioes/{indice}/anexar`
  - Corpo: `{ "tipo": "COLETA" }`
  - Resposta sucesso (200): `{ "regiao": { indice, tipo, possuida }, "estoque": {...}, "custo": { ouro: 203, madeira: 100, pedra: 100 } }`
  - Resposta erro (400/409): `{ "erro": "..." }`

**Serviço (novo):**
- [/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java](/src/main/java/com/example/loginbase/jogo/servico/AnexacaoService.java) (novo)
  - Método: `anexarRegiao(Long usuarioId, int indiceRegiao, TipoRegiao tipo)` throws ...
  - Validações:
    1. Usuário tem vila
    2. Região não é uma das 3 iniciais (já possuída)
    3. Região existe em 1-16
    4. Região não está possuída (possuida == false)
    5. Região é adjacente a uma já possuída (buscar k primeiro)
    6. Região não tem masmorra ativa (regiao.masmorra == null ou !ativa)
    7. Estoque tem Ouro, Madeira, Pedra suficientes (fórmula de custo)
  - Execução (transação):
    - Debitar recursos do estoque
    - Atualizar regiao: possuida = true, tipo = escolhido
    - Gravar evento no relatório de turno (ou log)
  - Retornar Regiao atualizada

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

- **Teste de integração**: POST /api/jogo/regioes/3/anexar com tipo COLETA, k=3, recursos suficientes → 200, região 3 possuída, estoque debitado.
- **Teste de validação**: POST região não adjacente → 400, "não adjacente".
- **Teste de validação**: POST região com masmorra ativa → 400, "masmorra ativa".
- **Teste de validação**: POST com recursos insuficientes → 400, "recursos insuficientes".
- **Teste de validação**: POST região já possuída → 400, "já possuída".
- **Teste de custo**: k=3 → Ouro 150; k=4 → Ouro 203; k=10 → Ouro 1.226 (verificar fórmula).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA4
- Build do backend (`./mvnw verify`) sem erros
- Testes listados passando
- Fórmula de custo verificada com números de game-design.md

## Fora de escopo

- Animação de anexação (visual).
- Histórico de anexações (para relatório do turno).
