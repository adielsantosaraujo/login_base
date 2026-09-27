# 0020 — Motor Determinístico: `Clock` e `Aleatorio` Injetáveis

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Combate tático envolve movimento, ataques, IA. Testes precisam de **determinismo**: mesmo input = mesma batalha em todas as rodadas. Há dois componentes: (1) **tempo**, (2) **aleatoriedade**. Código usa `System.currentTimeMillis()` e `Random.nextInt()` que não são mockáveis. Solução: injetar ambos.

## Direcionadores da decisão

- Testabilidade: testes determinísticos precisam controlar tempo e random
- Injeção: beans `Clock` e `Aleatorio` controláveis
- Motor puro: `MotorCombate` recebe delegates, sem chamar `System` direto
- IA determinística: empates resolvem por ordem (menor y, menor x) — sem randomness

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Interfaces `Clock` e `Aleatorio` com beans** | Motor recebe delegates, sem `System.currentTimeMillis()` direto. Teste injeta implementações controladas. |
| `System` direto + Mockito | Testes mockam `System`. Rejeitada: mock de classe estática é frágil. |
| Sem randomness na IA | IA determinística (sem `Aleatorio`). Rejeitada: loot precisa de rolagens. |

## Resultado da decisão

Adotou-se **beans injetáveis de tempo e aleatoriedade**:

1. **`java.time.Clock`** como bean (`JogoConfig.clock()`):
   - Produção: `Clock.systemUTC()`
   - Testes: `RelogioAjustavel extends Clock` com `avancar(Duration)` para avançar o tempo

2. **`Aleatorio`** (interface com implementações):
   - Padrão: `AleatorioPadrao` (RandomGenerator)
   - Teste: `AleatorioSequencia(Integer...)` ou `AleatorioSequencia(List<Integer>)` com sequência pré-definida; falha ao esgotar

3. **Motor puro**: `MotorCombate` é estático e sem dependências:
   - `processar(estado, acao)` aplica a ação
   - `executarIA(estado)` executa os inimigos
   - `Clock` é usado pelos serviços (tempos de ordem, `iniciadaEm`)
   - `Aleatorio` apenas por `GeradorLoot.gerar(nivel, aleatorio)`

4. **IA dos inimigos**:
   - Determinística: sem randomness no pathfinding
   - Alvo: menor distância; empate → menor HP → menor ID
   - Movimento: BFS; empate → menor y → menor x

### Consequências positivas
- **Determinístico**: testes controlam tempo e random
- **Testável**: motor é puro (não chamaSystem)
- **Flexível**: trocar gerador random sem refatoração
- **Rastreável**: `AleatorioSequencia` registra todas as chamadas

### Consequências negativas
- **Boilerplate**: interfaces `Clock`/`Aleatorio` = mais código
- **Sem verdadeiro random**: testes com `AleatorioSequencia` não testam variância (mitigado: testes estatísticos separados)
- **IA simplificada**: determinística em vez de adaptativa (intencional, simplificação)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Interfaces injetáveis | Determinístico; testável; puro; flexível. | Boilerplate; IA simplificada. |
| System direto + Mockito | Simples. | Frágil; mock de classe estática problemático. |
| Sem randomness | Mais simples. | Loot sem variância; testes não cobrem IA adaptativa. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §20](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**:
  - [`jogo/config/JogoConfig.java`](../../src/main/java/com/example/loginbase/jogo/config/JogoConfig.java) — bean `java.time.Clock` (`Clock.systemUTC()`) e bean `Aleatorio` (`AleatorioPadrao`)
  - [`jogo/config/Aleatorio.java`](../../src/main/java/com/example/loginbase/jogo/config/Aleatorio.java)
  - [`jogo/masmorra/combate/MotorCombate.java`](../../src/main/java/com/example/loginbase/jogo/masmorra/combate/MotorCombate.java) — processar(estado, acao)
- **Teste**:
  - [`suporte/RelogioAjustavel.java`](../../src/test/java/com/example/loginbase/jogo/suporte/RelogioAjustavel.java)
  - [`suporte/AleatorioSequencia.java`](../../src/test/java/com/example/loginbase/jogo/suporte/AleatorioSequencia.java)
  - [`jogo/masmorra/combate/MotorCombateTest.java`](../../src/test/java/com/example/loginbase/jogo/masmorra/combate/MotorCombateTest.java)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
