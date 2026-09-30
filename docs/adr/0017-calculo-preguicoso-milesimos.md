# 0017 — Cálculo Preguiçoso de Produção, Recursos em Milésimos

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Produção de recursos (madeira, pedra, comida, ferro) é contínua: a cada segundo, a taxa de produção gera novas quantidades. Há dois padrões: (1) **background job** (thread/scheduler que atualiza a cada segundo), (2) **cálculo lazy** (calcula sob demanda, quando a vila é consultada). Background jobs são complexos (controle de concorrência, múltiplas instâncias); lazy é mais simples. Recursos fracionários (ex.: 0,5 madeira/s) precisam ser armazenados sem perda: milésimos (`1000 × valor`).

## Direcionadores da decisão

- Simplicidade: sem threads de background, sem scheduler
- Precisão: produção fracionária sem perda (milésimos)
- Concorrência: lock pessimista por vila já existe (ADR 0018)
- Integração: sincronização acontece dentro da transação
- Escalabilidade: sem limite de simultâneas instâncias (cada ação recalcula)

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Cálculo lazy com milésimos** | `VilaService.sincronizar()` chamado em toda ação, calcula produção em trechos entre ordens vencidas, armazena em milésimos. |
| Background job (scheduler) | `@Scheduled` que atualiza vilas a cada segundo. Rejeitada: overhead; concorrência complexa; múltiplas instâncias. |
| WebSocket com push | Cliente recebe updates em tempo real. Rejeitada: frontend usa polling (simples). |

## Resultado da decisão

Adotou-se **cálculo lazy em trechos**:

1. **Armazenamento** (milésimos):
   - Colunas do banco: `comida`, `madeira`, `pedra`, `ferro` como `bigint` (1 = milésimo)
   - API mostra: `valor / 1000` (integer, perde fração)
   - Capacidade é `500 × 2^(N−1)` = quantidade, capacidade em banco = `500 × 2^(N−1) × 1000`

2. **Sincronização** (`VilaService.sincronizar(vila, agora)`):
   - Chamada no início de toda ação (read/write)
   - Dentro da transação, com vila travada (lock pessimista)
   - Algoritmo:
     ```
     ordensVencidas = ordens com concluiEm ≤ agora, ordenadas
     para cada ordem:
       produzirAte(vila, ordem.concluiEm)  // calcula até ordem vencer
       aplicador.aplicar(ordem)              // conclui ordem
       excluir ordem
     produzirAte(vila, agora)              // calcula até agora
     ```

3. **Produção em trecho** (`produzirAte`):
   - `dtMs = t − vila.recursosAtualizadosEm`
   - Se `dtMs ≤ 0`: skip
   - Para cada recurso:
     - `taxaHora` = taxa do recurso (milésimos/hora) derivada dos níveis atuais
     - `ganho = taxaHora × velocidade × dtMs / 3600000` (milésimos, floor)
     - `novo = min(capacidade × 1000, atual + ganho)` (respeita limite, com saturação em caso de overflow; ver D8 em [ADR 0025](0025-curva-progressao-configuravel.md))
   - `vila.recursosAtualizadosEm = t`

4. **Capacidade (fórmula em duas faixas)**:
   - Nível 1–5: `capacidade = 500 × 2^(N−1)` (fórmula binária original)
   - Nível 6–100: `capacidade = 8000 × (N/5)^p` (exponencial com curva configurável `p`)
   - Expoente `p` é configurável via [ADR 0025](0025-curva-progressao-configuravel.md); padrão `p = 1,5`
   - Armazenamento interno: capacidade × 1000 (milésimos)

### Consequências positivas
- **Sem threads**: simplicidade; sem race conditions de scheduler
- **Precisão fracionária**: milésimos preservam 0,1 madeira/s
- **Integrado**: sincronização dentro da transação existente (ACID)
- **Determinístico**: com `Clock` injetável, testes controlam tempo

### Consequências negativas
- **Não tempo real**: jogador vê atualização apenas quando faz ação (mitigado: polling 5s no frontend)
- **Cálculo sob demanda**: se não acessar vila, produção "passa despercebida" (intencional)
- **Timestamp do servidor**: usa `clock.instant()` (fuso: `user.timezone` na JVM)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Cálculo lazy (milésimos) | Simples; sem threads; determinístico; transacional. | Não real-time; exige polling frontend. |
| Background job | Tempo real. | Complexo; concorrência; não escalável horizontalmente. |
| WebSocket push | Mais responsivo. | Frontend não usa WebSocket. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §14–17](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**:
  - [`jogo/economia/VilaService.java#sincronizar`](../../src/main/java/com/example/loginbase/jogo/economia/VilaService.java)
  - [`jogo/economia/CalculadoraProducao.java`](../../src/main/java/com/example/loginbase/jogo/economia/CalculadoraProducao.java)
- **Teste**: [`jogo/economia/CalculadoraProducaoTest.java`](../../src/test/java/com/example/loginbase/jogo/economia/CalculadoraProducaoTest.java)
- **Configuração**: `app.jogo.velocidade` multiplica taxas ([ADR 0021](0021-velocidade-configuravel.md)); fórmula de capacidade acima do nível 5 usa expoente configurável ([ADR 0025](0025-curva-progressao-configuravel.md))
- **Relacionada**: [ADR 0025](0025-curva-progressao-configuravel.md) (curva de progressão configurável, que determina a capacidade para níveis 6–100)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.1.0 | 2026-09-28 | Adiciona informação sobre capacidade em duas faixas (nível 1–5 e 6–100) com referência a ADR 0025 | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
