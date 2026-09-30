# 0025 — Curva de Progressão Configurável para Níveis de Prédio

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-28 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [raise-building-max-level-100](../../openspec/changes/raise-building-max-level-100/) |

## Contexto e Problema

Ao estender o máximo de níveis de prédio de 5 para 100, as fórmulas de custo e capacidade (que crescem exponencialmente) precisam de um expoente `p` configurável para permitir ajuste fino do game balance sem recompile. Atualmente, o custo segue `base × 1,5^(N−1)` e a capacidade é `500 × 2^(N−1)`, ambas hardcoded. A change `raise-building-max-level-100` precisa de um mecanismo que:

1. Permita variar `p` em um conjunto discreto para garantir determinismo em todos os ambientes.
2. Use aritmética exata (BigDecimal) para evitar erros de ponto flutuante.
3. Valide `p` na inicialização, com falha rápida se inválido.
4. Mantenha os níveis 1–5 bit a bit iguais aos atuais (compatibilidade para dados existentes).
5. Propague `p` para todas as fórmulas que dependem da curva (custo, capacidade, canteiros, nível forjável).

**Requisitos associados**: Specs vigentes de `game-buildings` (PRD) e `game-army` (EXE) da change `add-city-builder-game` (arquivada); design `raise-building-max-level-100` §D1–D3.

## Direcionadores da Decisão

- **Determinismo transversal**: qualquer ambiente (dev, testes, produção) com o mesmo `p` deve gerar o mesmo custo/capacidade.
- **Falha rápida**: erro de configuração deve impedir inicialização, não aparecer em runtime.
- **Flexibilidade**: permitir até 5 curvas distintas (múltiplos de 0,25 em [1,0; 2,0]) sem recompile.
- **Precisão**: evitar acúmulo de erro de ponto flutuante (ex.: `double.pow` arredonda diferentemente em JVMs).
- **Simplicidade**: sem adicionar beans Spring (já existem `Clock` e `Aleatorio`); sem estado global mutável.

## Opções Consideradas

### Opção (a): `double` com `Math.pow` — **DESCARTADA**

- Usar `Math.pow(N/5, p)` em `double` (ou `StrictMath.pow` para garantir reprodutibilidade entre plataformas).
- Arredondar resultado com `BigDecimal.valueOf` antes de multiplicar.

**Prós**: Simples; `StrictMath.pow` é reprodutível na mesma plataforma.  
**Contras**: `double` ainda introduz erros de arredondamento em `N/5` e no `pow`; empates (`.5`) podem arredondar para o lado errado; difícil depuração se resultados divergirem entre versões de JVM.

### Opção (b): Série exponencial/logarítmica em BigDecimal — **DESCARTADA**

- Implementar `exp(p × ln(x))` via série de Taylor ou Maclaurin em BigDecimal.
- Aceita qualquer `BigDecimal` como expoente.

**Prós**: Máxima flexibilidade (qualquer `p`, não só múltiplos).  
**Contras**: Código numérico próprio, difícil de testar e validar; lento; risco de divergência se série converge diferentemente entre configurações.

### Opção (c): Apenas múltiplos de 0,5 — **DESCARTADA**

- Restringir `p` a {1,0; 1,5; 2,0} (3 valores).

**Prós**: Menos permutações (2 raízes quadradas em vez de 2).  
**Contras**: Pouco para ajuste fino; falta o meio-termo (1,25 e 1,75) que pode ser necessário para balance.

### Opção (d): Bean Spring `CurvaNiveis` em `JogoConfig` — **DESCARTADA**

- Definir `CurvaNiveis` como `@Bean` em `JogoConfig`; injetar em `TipoPredio`.
- Facilita testes com mocks.

**Prós**: Integração Spring padrão.  
**Contras**: Exige registrar o bean em todos os `@Import` de `@WebMvcTest`/`@DataJpaTest` de testes; `JogoProperties` já é importada em todas as fatias, sem `JogoConfig`; mudança desnecessária.

### Opção (e): Holder estático preenchido na inicialização — **DESCARTADA**

- `public static CurvaNiveis INSTANCIA = null;` em `CurvaNiveis`.
- `@PostConstruct` preencheria antes de qualquer uso.

**Prós**: Acesso global sem injeção.  
**Contras**: Estado global mutável; testes frágeis (precisam resetar ou sincronizar); difícil de trocar em testes; anti-padrão.

### Opção (f): `BigDecimal` com `DECIMAL128`, múltiplos de 0,25, raízes quadradas — **ESCOLHIDA**

- Expoente `p` restrito a múltiplos de 0,25 em [1,0; 2,0] (5 valores: 1,0 · 1,25 · 1,5 · 1,75 · 2,0).
- `CurvaNiveis.fator(int nivel)` calcula `(N/5)^p` usando `BigDecimal`, `MathContext.DECIMAL128`, `pow` (para potências inteiras) e `sqrt` (para raízes de ordem 2 e 4).
- Validação com `@DecimalMin/@DecimalMax/@AssertTrue` em `JogoProperties`; verificação de limites de overflow em `verificarLimites()`.

**Prós**:
- Precisão garantida (BigDecimal exato onde possível, sqrt `DECIMAL128` quando necessário).
- Raízes quadradas são funções bem testadas da biblioteca padrão.
- Determinismo completo (mesma entrada, sempre mesmo resultado).
- Falha rápida: validação na inicialização via Spring Validation.
- Compatibilidade: níveis 1–5 invariantes (`p = 1,5` continua idêntico).
- Sem mudança de testes: `JogoProperties` já importada.

**Contras**:
- Algoritmo de raízes um pouco menos intuitivo (precisa de documentação).
- 5 curvas é discreto; impossível usar `p = 1,3` ou qualquer outro.

## Resultado da Decisão

**Decidimos pela Opção (f)**: `BigDecimal` com `DECIMAL128`, múltiplos de 0,25, raízes quadradas em `CurvaNiveis`.

**Racional**:
1. **Determinismo**: BigDecimal garante a mesma saída em qualquer plataforma / versão de JVM; sem surpresas de `double.pow`.
2. **Flexibilidade razoável**: 5 curvas é suficiente para o ajuste fino recomendado; não é excessivo.
3. **Falha rápida**: Validação Bean Spring (`@Validated`) bloqueia aplicação na inicialização.
4. **Simplicidade operacional**: Uma propriedade (`app.jogo.expoente-curva`), uma variável de ambiente (`JOGO_EXPOENTE_CURVA`), sem mudança de configuração de teste.
5. **Precisão**: Raízes quadradas do BigDecimal (`sqrt`) retornam valores exatos quando cabem na precisão (ex.: `sqrt(4)` = 2 exatamente); arredondamentos `HALF_UP` são confiáveis.

## Decisões Complementares (do Design)

### 1. Propriedade e Validação

Em `JogoProperties` (pacote `jogo/config`):

```java
@ConfigurationProperties("app.jogo")
@Validated
public class JogoProperties {
    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("2.0")
    private BigDecimal expoenteCurva = new BigDecimal("1.5");

    @AssertTrue(message = "Expoente deve ser múltiplo de 0.25 e estar entre 1.0 e 2.0")
    public boolean isCurvaNiveisValida() {
        // Constrói CurvaNiveis e chama verificarLimites()
        try {
            CurvaNiveis curva = new CurvaNiveis(expoenteCurva);
            curva.verificarLimites();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CurvaNiveis curvaNiveis() {
        return new CurvaNiveis(expoenteCurva);
    }
}
```

Variável de ambiente: `app.jogo.expoente-curva=${JOGO_EXPOENTE_CURVA:1.5}`.

### 2. Record `CurvaNiveis`

```java
public record CurvaNiveis(BigDecimal expoente) {
    public CurvaNiveis {
        if (expoente == null || expoente.compareTo(BD_1_0) < 0 || expoente.compareTo(BD_2_0) > 0) {
            throw new IllegalArgumentException("Expoente fora de faixa");
        }
        if (!ehMultiploDe025(expoente)) {
            throw new IllegalArgumentException("Expoente deve ser múltiplo de 0.25");
        }
    }

    public BigDecimal fator(int nivel) {
        // Calcula (nivel / 5)^expoente com BigDecimal + sqrt
        // Algoritmo descrito em design.md § D2
    }

    public void verificarLimites() throws ArithmeticException {
        // Itera TipoPredio.values() × níveis 1..100
        // Calcula custo(n, this) e capacidade(n, this)
        // Usa Math.multiplyExact para detectar overflow
    }

    private static boolean ehMultiploDe025(BigDecimal valor) {
        // Valida que valor × 4 é inteiro
    }
}
```

### 3. Propagação às Fórmulas

- `TipoPredio.custo(int nivel, CurvaNiveis curva)` — antes havia `custo(int nivel)`, removido.
- `TipoPredio.capacidadeRecurso(int, CurvaNiveis)` — novo segundo argumento.
- `TipoRecurso.capacidadeArmazem(int, CurvaNiveis)` — novo segundo argumento.
- `Custo.paraNivel(int, CurvaNiveis)` — novo segundo argumento.
- `CalculadoraProducao` passa a receber `(int velocidade, CurvaNiveis curva)`.
- `MasmorraService` injeta `JogoProperties` para calcular `capacidadeExercito(nivel, curva)`.

### 4. Teste de Verificação

Teste `JogoPropertiesTest` valida:
- `expoenteCurva` default = 1.5.
- `isCurvaNiveisValida()` = true.
- Tentar `expoenteCurva = 1.3` → validação falha.
- Tentar `expoenteCurva = 3.0` → validação falha.
- Níveis 1–5 com `p = 1.5` reproduzem custos/capacidades atuais (para cada prédio, verificar bit-a-bit).

## Consequências Positivas

- **Determinismo garantido**: mesma configuração, sempre mesmos resultados em qualquer máquina.
- **Configurável sem recompile**: `JOGO_EXPOENTE_CURVA=1.25` e restart; sem rebuild.
- **Falha rápida explícita**: `IllegalArgumentException` ou `ConstraintViolationException` bloqueia aplicação na inicialização com mensagem clara.
- **Sem mudança de testes**: `JogoProperties` já é importada em todas as fatias; nenhum novo `@Bean` ou `@Import`.
- **Nivelamento coerente**: níveis 1–5 inalterados, 6–100 seguem a nova curva de forma consistente.
- **Flexibilidade controlada**: 5 curvas (1,0 · 1,25 · 1,5 · 1,75 · 2,0) cobre cenários de game design sem excesso de permutações.

## Consequências Negativas

- **Restrição discreta**: não é possível usar `p = 1,3` ou qualquer outro decimal; mudança futura teria de estender para múltiplos de 0,1 ou 0,05 (com ajuste do algoritmo de raízes).
- **Complexidade do algoritmo**: cálculo de `fator(nivel)` com múltiplas raízes quadradas é menos intuitivo do que `pow(x, p)` simples; requer testes extensivos e documentação clara.
- **Validação na inicialização**: iteração sobre todos os níveis + prédios durante boot (milissegundos, aceitável); falha de validação aborta toda a aplicação (mitigado: erro deve ser claro na log).

## Mais Informações

- **Design relacionado**: [`raise-building-max-level-100/design.md` §D1–D3](../../openspec/changes/raise-building-max-level-100/design.md) — decisões, algoritmo, tabela de sensibilidade.
- **Código principal**:
  - [`jogo/catalogo/CurvaNiveis.java`](../../src/main/java/com/example/loginbase/jogo/catalogo/CurvaNiveis.java) — record com fator e validação.
  - [`jogo/config/JogoProperties.java`](../../src/main/java/com/example/loginbase/jogo/config/JogoProperties.java) — propriedade, validação Bean.
  - [`jogo/catalogo/TipoPredio.java`](../../src/main/java/com/example/loginbase/jogo/catalogo/TipoPredio.java) — fórmulas com novo argumento `CurvaNiveis`.
  - [`jogo/catalogo/TipoRecurso.java`](../../src/main/java/com/example/loginbase/jogo/catalogo/TipoRecurso.java) — capacidade armazém com `CurvaNiveis`.
  - [`jogo/catalogo/Custo.java`](../../src/main/java/com/example/loginbase/jogo/catalogo/Custo.java) — cálculo com curva.
- **Testes**:
  - [`jogo/JogoPropertiesTest.java`](../../src/test/java/com/example/loginbase/jogo/JogoPropertiesTest.java) — validação, default, níveis 1–5.
  - [`jogo/catalogo/CatalogoTest.java`](../../src/test/java/com/example/loginbase/jogo/catalogo/CatalogoTest.java) — cenários de limite (overflow).
- **Configuração**: `src/main/resources/application.properties` (padrão `1.5`); `.env.example` documenta faixa; `docker-compose.yml` passa `JOGO_EXPOENTE_CURVA`.
- **Predecessores**: [ADR 0016](0016-catalogo-em-codigo.md) (catálogo em código), [ADR 0021](0021-velocidade-configuravel.md) (configuração via `JogoProperties`).
- **Sucessor relacionado**: [ADR 0017](0017-calculo-preguicoso-milesimos.md) precisa atualização para citar a curva na capacidade (nova linha sobre fórmula em duas faixas).

---

**Histórico de Revisões:**

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-28 | Versão inicial, implementada pela change `raise-building-max-level-100` | Adiel, com apoio de agentes Claude |
