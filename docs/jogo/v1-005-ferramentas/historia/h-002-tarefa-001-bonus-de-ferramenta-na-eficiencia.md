# H-002 · Tarefa 001 — Bônus de ferramenta na eficiência

**História:** [H-002 — Aplicar bônus da ferramenta no trabalho](h-002-aplicar-bonus-da-ferramenta-no-trabalho.md) · **Domínio:** [cidadao.md](../../v1-002-cidadaos/cidadao.md) · **Depende de:** [H-004 de cidadaos (painel, equipamento)](../../v1-002-cidadaos/historia/h-004-tarefa-001-api-do-cidadao-e-distribuicao-de-pontos.md), [H-002 de recursos-e-producao (cálculo de eficiência base)](../../v1-010-recursos-e-producao/historia/h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md) · **Camada:** Backend

## Objetivo

Implementar a soma do bônus de ferramenta (+L PE) no cálculo de PE efetivo quando um cidadão trabalha com a ferramenta da sua profissão, respeitando o teto de eficiência 3,0.

## Contexto necessário

- [cidadao.md](../../v1-002-cidadaos/cidadao.md) — PE efetivo e eficiência (5.2, 5.3).
  > PE efetivo = PE base + Σ floor(característica ÷ 5) + **bônus de ferramenta** + bônus PROF de itens. Eficiência = (0,5 + 0,1 × PE efetivo) limitada a 3,0.

- [ferramentas.md](../ferramentas.md) — bônus de ferramenta = +L PE (só na profissão correta).

- Seção 5.3 da bíblia: ordem de multiplicadores após o limite 3,0 (idade, fome, bem alimentada, líder, PROD%).

## Backend

**Serviço:** `EficienciaServico` (método existente em tarefas anteriores)

Adicionar parâmetro de ferramenta ao cálculo:

```java
public BigDecimal calcularEficiencia(
    Cidadao cidadao,
    Profissao profissaoTrabalho,
    Construcao construcao,
    Item ferramentaEquipada,  // NOVO
    int turnoAtual
) {
    // PE efetivo atual (sem ferramenta)
    int peEfetivo = calcularPEEfetivo(cidadao, profissaoTrabalho);
    
    // Adicionar bônus de ferramenta se profissão da ferramenta = profissão do prédio
    if (ferramentaEquipada != null && ferramentaEquipada.isToolFor(profissaoTrabalho)) {
        peEfetivo += ferramentaEquipada.getNivel(); // +L PE
    }
    
    // Eficiência base com limite 3,0
    BigDecimal eficienciaBase = BigDecimal.valueOf(0.5)
        .add(BigDecimal.valueOf(0.1).multiply(BigDecimal.valueOf(peEfetivo)))
        .min(BigDecimal.valueOf(3.0));
    
    // Multiplicadores (idade, fome, bem alimentada, líder, PROD%)
    BigDecimal eficienciaFinal = aplicarMultiplicadores(
        cidadao, construcao, eficienciaBase, turnoAtual
    );
    
    return eficienciaFinal;
}

// Verificar se ferramenta é da profissão
public boolean isToolFor(Profissao profissao) {
    return this.ferramenta != null 
        && this.ferramenta.getProfissao().equals(profissao);
}
```

**Validação:**

- Ao alocar cidadão em prédio, carregar `cidadao.itemEquipado(ItemSlot.FERRAMENTA)`.
- Se ferramenta está equipada e profissão diferente do prédio, ignorar bônus (CA2).
- Após adicionar bônus, aplicar limite 3,0 antes dos multiplicadores (CA3).

**Modificar cálculos de produção e consumo:**

Qualquer lugar que chama `calcularEficiencia()` para:
- Produção de recursos (H-002 de recursos-e-producao).
- Progresso de obras (H-001 de construcoes).
- Receitas de fábrica (H-002 de recursos-e-producao).

Passar a ferramenta equipada como parâmetro novo.

## Frontend

Não se aplica (visualização em cidadao, H-004).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/EficienciaServico.java](/src/main/java/com/example/loginbase/jogo/cidadao/EficienciaServico.java) (modificar)
- [/src/main/java/com/example/loginbase/jogo/item/Item.java](/src/main/java/com/example/loginbase/jogo/item/Item.java) (adicionar método `isToolFor()`)
- [/src/test/java/com/example/loginbase/jogo/cidadao/EficienciaServicoTest.java](/src/test/java/com/example/loginbase/jogo/cidadao/EficienciaServicoTest.java) (testes novos)

## Testes

- Teste CA1: Agricultor com Enxada L3, PE base 2 → PE efetivo 5 → eficiência 1,0.
- Teste CA2: Agricultor com Enxada em Armazém (profissão Carregador) → bônus não soma.
- Teste CA3: Agricultor com PE base 10, INT +3, Enxada L10 → PE 23 → eficiência capped 3,0.
- Teste CA4: Mineiro com Picareta L10, PE base 2 → eficiência 1,7.
- Teste CA5: Agricultor sem ferramenta → PE base 2 → eficiência 0,7.
- Teste de multiplicadores: eficiência com idade 14–17, fome, bem alimentada, etc.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5.
- Build do backend (`./mvnw verify`) sem erros.
- Testes de bônus de ferramenta passando.
- Produção de recursos, progresso de obras e receitas de fábrica usando novo parâmetro.

## Fora de escopo

- Interface de equipamento de ferramenta (cidadao, H-004).
- Limite de carga de ferramentas no inventário (ilimitado na v1).
