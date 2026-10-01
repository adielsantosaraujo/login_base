# H-001 · Tarefa 001 — Catálogo de ferramentas

**História:** [H-001 — Fabricar ferramentas](h-001-fabricar-ferramentas.md) · **Domínio:** [fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) · **Depende de:** [H-001 de itens-e-fabricacao (modelo de dados de itens)](../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Backend

## Objetivo

Centralizar a definição de todas as ferramentas (11 tipos) com suas profissões, oficinas, receitas e limites de nível no catálogo `jogo.catalogo`, garantindo que fabricação, equipamento e bônus funcionem corretamente.

## Contexto necessário

- [ferramentas.md](../ferramentas.md) — tabela 7.9 da bíblia com receita base de cada ferramenta e profissão associada.
  > | Ferramenta | Profissão | Oficina | Receita base (×L) |
  > |---|---|---|---|
  > | Martelo | Construtor | Ferraria | 1 Ferro, 1 Tábua |
  > | Carrinho de mão | Carregador | Carpintaria | 3 Tábua, 1 Ferro |
  > | Enxada | Agricultor | Ferraria | 1 Ferro, 1 Tábua |
  > | ... (11 no total) |
  
- [fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) — sistema comum de fabricação (custo ×L, PF, qualidade, oficina).
- Seção 7.4 da bíblia: requisitos (oficina, PE efetivo ≥ 2L−2, nível máximo por oficina).

## Backend

**Entidade:** `FerramentaCatalogo` (enum ou table; sugerido enum em `jogo.catalogo`)

```java
public enum FerramentaCatalogo {
    MARTELO(
        Profissao.CONSTRUTOR, 
        OficinaTipo.FERRARIA, 
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1),
            new ReceitaComponente(RecursoTipo.TABUA, 1)
        )
    ),
    CARRINHO_DE_MAO(
        Profissao.CARREGADOR,
        OficinaTipo.CARPINTARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.TABUA, 3),
            new ReceitaComponente(RecursoTipo.FERRO, 1)
        )
    ),
    ENXADA(
        Profissao.AGRICULTOR,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1),
            new ReceitaComponente(RecursoTipo.TABUA, 1)
        )
    ),
    FORCADO(
        Profissao.FAZENDEIRO,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1),
            new ReceitaComponente(RecursoTipo.TABUA, 1)
        )
    ),
    PICARETA(
        Profissao.MINEIRO,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 2),
            new ReceitaComponente(RecursoTipo.TABUA, 1)
        )
    ),
    MACHADO(
        Profissao.MADEIREIRO,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 2),
            new ReceitaComponente(RecursoTipo.TABUA, 1)
        )
    ),
    MALHO(
        Profissao.FERREIRO,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 2)
        )
    ),
    CUTELO(
        Profissao.COZINHEIRO,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1)
        )
    ),
    KIT_DE_COSTURA(
        Profissao.COSTUREIRO,
        OficinaTipo.ALFAIATARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1),
            new ReceitaComponente(RecursoTipo.TECIDO, 1)
        )
    ),
    FACA_DE_CACA(
        Profissao.CACADOR,
        OficinaTipo.FERRARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.FERRO, 1),
            new ReceitaComponente(RecursoTipo.COURO_CURTIDO, 1)
        )
    ),
    BALANCA(
        Profissao.COMERCIANTE,
        OficinaTipo.CARPINTARIA,
        List.of(
            new ReceitaComponente(RecursoTipo.TABUA, 2),
            new ReceitaComponente(RecursoTipo.FERRO, 1)
        )
    );

    private Profissao profissao;
    private OficinaTipo oficinaTipo;
    private List<ReceitaComponente> receitaBase;

    // getters
}
```

**Validação:**

- Ao iniciar fabricação de ferramenta, validar que a oficina é a correta (enum).
- Ao calcular PE efetivo necessário, usar 2L−2 (ex.: L5 → PE mínimo 8).
- Ao concluir fabricação, aplicar bônus intrínseco de ferramenta conforme subconjunto permitido (PROF, PROD, INT, FOR, VEL).

## Frontend

Não se aplica (tarefas de frontend em H-001 de itens-e-fabricacao).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/catalogo/FerramentaCatalogo.java](/src/main/java/com/example/loginbase/jogo/catalogo/FerramentaCatalogo.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/item/ItemServico.java](/src/main/java/com/example/loginbase/jogo/item/ItemServico.java) (modificar para usar catálogo)

## Testes

- Teste que cada ferramenta tem profissão e oficina mapeadas corretamente.
- Teste de cálculo de receita (10 Ferro para Machado L5: 2 Ferro × 5).
- Teste de validação de PE mínimo por nível (L3 Enxada → PE mínimo 4).
- Teste que Ferro vira Aço a partir de L6 (seção 7.4).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5.
- Build do backend (`./mvnw verify`) sem erros.
- Testes de catálogo passando (receitas, profissões, validações).
- Catálogo centralizado em `jogo.catalogo.FerramentaCatalogo`.

## Fora de escopo

- Tela de seleção de ferramentas (H-001 de itens-e-fabricacao).
- Cálculo de qualidade por margem (H-001 de itens-e-fabricacao).
