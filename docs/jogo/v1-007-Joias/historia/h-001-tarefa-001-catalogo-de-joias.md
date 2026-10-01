# H-001 · Tarefa 1 — Catálogo de joias

**História:** [h-001-fabricar-joias.md](h-001-fabricar-joias.md) · **Domínio:** [../joias.md](../joias.md) ·
**Depende de:** [../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md](../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md) | [../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md](../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md) · **Camada:** Backend

## Objetivo

Centralizar as receitas e configurações de colares e anéis no catálogo `jogo.catalogo`, permitindo que o motor de fabricação consulte receitas, requisitos e efeitos sem duplicação. Suportar a escolha de característica para anéis.

## Contexto necessário

- [../../v1-011-itens-e-fabricacao/fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md)
  > Receita: oficina certa, nível permitido, PE efetivo artesão ≥ 2L − 2, custo ×L, para L≥6 Ferro→Aço. PF = 1 + L.

- [../colar.md](../colar.md) e [../anel.md](../anel.md)
  > Colar: 1 Ferro (→Aço em L6), 20 Ouro. Efeito: +5×L Vida.
  > Anel: 1 Ferro (→Aço em L6), 15 Ouro. Efeito: +1/+2/+3/+4 (L1–3/4–6/7–9/L10) característica (escolhida ao fabricar).

## Backend

**Enum/Constante em `com.example.loginbase.jogo.catalogo` (novo):**

```java
public enum TipoJoia {
    COLAR, ANEL
}

public class ReceitaJoia {
    private TipoJoia tipo;
    private Integer nivelMinimo = 1;
    private Integer nivelMaximo = 10;
    private Receita receitaBase;  // 1 Ferro, 20 Ouro (Colar) ou 1 Ferro, 15 Ouro (Anel)
    private Integer pfBase = 1;   // PF = pfBase + nivel
    
    // Colar
    private int vidaPorNivel = 5; // +5 × L
    
    // Anel (efeito por faixa de nível)
    private Map<Integer, Integer> bonisPorFaixa; // L1–3: +1, L4–6: +2, L7–9: +3, L10: +4
    
    // Características permitidas para Anel (cópia de 6.2)
    private Set<Caracteristica> caracteristicasPermitidas = {VIT, FOR, VEL, INT, CAR};
}

// Instâncias
public static final ReceitaJoia COLAR = new ReceitaJoia(
    tipo = COLAR,
    receitaBase = {Ferro: 1, Ouro: 20},
    vidaPorNivel = 5
);

public static final ReceitaJoia ANEL = new ReceitaJoia(
    tipo = ANEL,
    receitaBase = {Ferro: 1, Ouro: 15},
    bonisPorFaixa = {1->1, 4->2, 7->3, 10->4}
);
```

**Validações e consultas (serviço `JoiaService`, novo):**

- `validarNivelOficina(TipoJoia, nivel, nivelOficina)` → verifica se nível é permitido; máx. L3 em N1, L6 em N2, L10 em N3 (herda de 4.11).
- `validarPEArtesao(nivel) → requisito PE ≥ 2×nivel − 2` (vinda de 7.4).
- `calcularCusto(TipoJoia, nivel)` → aplica Receita ×L, converte Ferro→Aço se L≥6.
- `calcularPF(nivel)` → retorna 1 + nivel.
- `obterEfeitoPrincipal(TipoJoia, nivel, [caracteristicaSelecionada])` → para Colar retorna "+5×L Vida"; para Anel retorna "+N característica" conforme faixa.
- `validarCaracteristica(TipoJoia, Caracteristica)` → se ANEL, verifica se está em {VIT, FOR, VEL, INT, CAR}.

**Bônus intrínsecos permitidos (consultar tabela 6.2/7.2):**

```java
private Set<CodigoBonus> bonusPermitidosJoia = {VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD};
```

Faixa por nível (tabela 7.2): L1–4 → baixa, L5–7 → média, L8–10 → alta.

## Frontend

Não se aplica (sistema de fabricação é compartilhado com armas/ferramentas).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/catalogo/ReceitaJoia.java](/src/main/java/com/example/loginbase/jogo/catalogo/ReceitaJoia.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoJoia.java](/src/main/java/com/example/loginbase/jogo/catalogo/CatalogoJoia.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/JoiaService.java](/src/main/java/com/example/loginbase/jogo/item/JoiaService.java) (novo)

## Testes

- `ReceitaJoiaTest` — calcular custo Colar L1 (1 Ferro, 20 Ouro), L6 (6 Aço, 120 Ouro).
- `ReceitaJoiaTest` — calcular efeito Anel L1 (faixa bônus +1), L4 (+2), L10 (+4).
- `JoiaServiceTest` — validar PE artesão L5 (requisito ≥8, falhar com PE 5).
- `JoiaServiceTest` — validar nível N1 (máx. L3, falhar L4).
- `JoiaServiceTest` — validar característica Anel (aceitar VIT/FOR/VEL/INT/CAR, rejeitar VIDA).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA2 (catálogo consultável, custo e PF corretos); CA3–CA5 (validações); CA6 (conversão Ferro→Aço).
- Build do backend (`./mvnw verify`) sem erros.
- Testes listados passando.
- Constantes de receita centralizadas em `CatalogoJoia`.

## Fora de escopo

- Motor de fabricação em si (tarefa do sistema de itens).
- Geração de bônus intrínsecos (tarefa da geração de qualidade).
- Engaste de pedras (tarefa de pedras).
