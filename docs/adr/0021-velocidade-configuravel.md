# 0021 — Velocidade do Jogo Configurável (`JOGO_VELOCIDADE`)

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Desenvolvimento e testes precisam de feedback rápido. Construir um prédio em 2 minutos é tedioso em dev; em testes, esperar 2 min por fase é impraticável. Multiplicador de velocidade permite `JOGO_VELOCIDADE=60` para rodar tudo 60× mais rápido: tempos ficam 1s, taxas de produção multiplicam. Implementação precisa ser transversal (afeta cálculos de produção, tempos de ordem, etc.).

## Direcionadores da decisão

- Desenvolvimento: testes rápidos, sem ajustes manuais de banco
- Testes: suite de testes executável em segundos
- Configuração: variável de ambiente, não hardcoded
- Transversal: aplica-se a taxas e tempos, não só UI

## Opções consideradas

| Opção | Descrição |
|---|---|
| **`JOGO_VELOCIDADE` × taxa e ÷ tempo** | `app.jogo.velocidade=${JOGO_VELOCIDADE:1}` (inteiro ≥ 1). Taxas × v, tempos = `ceil(tempo / v)`. |
| Apenas na UI (cliente) | Frontend multiplica display. Rejeitada: servidor não sabe, afeta validações. |
| Sem multiplicador | Rejeitada: dev é lento. |

## Resultado da decisão

Adotou-se **multiplicador `JOGO_VELOCIDADE` transversal**:

1. **Configuração** (`application.properties`):
   ```properties
   app.jogo.velocidade=${JOGO_VELOCIDADE:1}
   ```

2. **Bean** (`JogoProperties`):
   ```java
   @ConfigurationProperties("app.jogo")
   public class JogoProperties {
       private int velocidade = 1;  // validar ≥ 1
   }
   ```

3. **Aplicação**:
   - **Produção**: `ganho = taxaHora × velocidade × dtMs / 3600000`
   - **Tempos de ordem**: `tempoEfetivo = ceil(tempoBase × nivel × quantidade / velocidade)` segundos
   - **Catálogo**: `CatalogoDto` mostra tempos base sem multiplicar (velocidade é server-side)

4. **Uso em testes**:
   - Padrão: `JOGO_VELOCIDADE=1`
   - Teste manual: `JOGO_VELOCIDADE=60` → `make up`, login, ver upgrade em ~1s
   - Suite: pode use `JOGO_VELOCIDADE=1000` (abstrato, sem UI)

### Consequências positivas
- **Flexível**: dev rápido, testes mais rápidos
- **Transversal**: afeta todas as taxas/tempos
- **Sem mudança de código**: apenas env var
- **Testável**: cada teste pode variar velocidade (com @PropertySource)

### Consequências negativas
- **Velocidade ≠ realista**: 60× mais rápido muda game feel (aceito: dev only)
- **Catálogo não multiplica**: cliente vê tempos base (mitigado: frontend não calcula regras)
- **Ambiente**: `JOGO_VELOCIDADE` é repassado ao frontend container (divergência D-07: frontend não a usa)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Multiplicador `JOGO_VELOCIDADE` | Transversal; flexível; simples; testes rápidos. | Mudança de game feel; velocidade ≠ realista. |
| Apenas UI | Localizado. | Servidor não sabe; quebra validações. |
| Sem multiplicador | Realista. | Dev/testes lentos. |

## Mais informações

- **Design**: [`add-city-builder-game/design.md` §2, §21 item 3](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Código**:
  - [`jogo/config/JogoProperties.java`](../../src/main/java/com/example/loginbase/jogo/config/JogoProperties.java)
  - [`jogo/economia/CalculadoraProducao.java`](../../src/main/java/com/example/loginbase/jogo/economia/CalculadoraProducao.java) — aplicação de velocidade em produção
- **Configuração**: [`src/main/resources/application.properties`](../../src/main/resources/application.properties)
- **Docker Compose**: [`docker-compose.yml`](../../docker-compose.yml) — `JOGO_VELOCIDADE` no serviço `app`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
