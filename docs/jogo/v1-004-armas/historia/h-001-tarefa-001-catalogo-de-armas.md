# H-001 · Tarefa 001 — Catálogo de Armas

**História:** [h-001-fabricar-armas.md](h-001-fabricar-armas.md) · **Domínio:** [../armas.md](../armas.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo
Implementar o catálogo centralizado de armas (receitas, custos, atributos, alcance) como enums/constantes no pacote `jogo.catalogo`, de modo que a tabela 7.8 fique refletida no código e reutilizável por fabricação, equipamento e combate.

## Contexto necessário
- [../armas.md](../armas.md) — Visão geral e tabela 7.8.
- [../../v1-011-itens-e-fabricacao/itens.md](../../v1-011-itens-e-fabricacao/itens.md) — Sistema comum de qualidades e níveis (7.2–7.3).
- [../../v1-011-itens-e-fabricacao/fabricacao.md](../../v1-011-itens-e-fabricacao/fabricacao.md) — Requisitos de PE e custo multiplicado por nível (7.4).

> Tabela 7.8 — Cada arma tem:
> - Oficina (Ferraria ou Carpintaria)
> - Receita base (Ferro, Tábua, Tecido etc.) multiplicada por nível
> - Ataque base (10 espada, 9 lança, 8 arco, 13 besta)
> - Atributo-chave (FOR, média FOR/VEL, VEL, INT)
> - Modificador de iniciativa (0, +1, +2, −4)
> - Alcance e posicionamento (detalhes em 10.3)
> - Especial (Espada +2 DEF)
> - Bônus intrínsecos permitidos (FOR, VEL, INT, ATK, CRIT, INI)

## Backend

**Entidade/Enum:**
- `jogo.catalogo.ArmaEnum` (ou classe `Arma` imutável): cada tipo (ESPADA, LANCA, ARCO, BESTA).
- Campos: oficina requerida (`TipoConstrucao`), receita base (map de recursos → quantidade), ataque base, atributo-chave, mod. iniciativa, alcance (enum: CORPO_A_CORPO_FRENTE, CORPO_A_CORPO_FLEX, DISTANCIA), especial (descrição + regra em código).
- Bônus intrínsecos permitidos: list de `TipoBonus`.

**Receita dinâmica:**
- Método `public Map<Recurso, Integer> receita(int nivel)` que multiplica a receita base por nível, com ajuste Ferro → Aço para L ≥ 6 em Espada, Lança, Besta.

**Validação:**
- Método `validarFabricacao(int nivel, int peEfetivo, int nivelOficina)` que verifica:
  - PE efetivo ≥ 2L − 2
  - Nível da oficina permite L (N1 até L3, N2 até L6, N3 até L10)

**Exemplo de código (pseudocódigo Java):**
```java
public enum ArmaEnum {
  ESPADA(TipoConstrucao.FERRARIA, 10, AtributoChave.FOR, 0,
    Map.of(Recurso.FERRO, 3, Recurso.TABUA, 1),
    Alcance.CORPO_A_CORPO_FRENTE, "+2 Defesa ao portador",
    List.of(TipoBonus.FOR, TipoBonus.VEL, TipoBonus.INT, TipoBonus.ATK, TipoBonus.CRIT, TipoBonus.INI)),
  
  LANCA(TipoConstrucao.FERRARIA, 9, AtributoChave.MEDIA_FOR_VEL, +1,
    Map.of(Recurso.FERRO, 2, Recurso.TABUA, 2),
    Alcance.CORPO_A_CORPO_FLEX, "—",
    List.of(...)),
  
  ARCO(TipoConstrucao.CARPINTARIA, 8, AtributoChave.VEL, +2,
    Map.of(Recurso.TABUA, 3, Recurso.TECIDO, 1),
    Alcance.DISTANCIA, "—",
    List.of(...)),
  
  BESTA(TipoConstrucao.CARPINTARIA, 13, AtributoChave.INT, −4,
    Map.of(Recurso.FERRO, 2, Recurso.TABUA, 3, Recurso.TECIDO, 1),
    Alcance.DISTANCIA, "Ignora 25% da Defesa (Defesa_efetiva = DEF × 0,75)",
    List.of(...));
  
  public Map<Recurso, Integer> receita(int nivel) {
    // Multiplica receita base por nível; troca Ferro → Aço para L ≥ 6
    Map<Recurso, Integer> resultado = new HashMap<>(this.receitaBase);
    resultado.replaceAll((k, v) -> v * nivel);
    if (nivel >= 6 && this != ARCO) {
      Integer ferro = resultado.remove(Recurso.FERRO);
      if (ferro != null) {
        resultado.put(Recurso.ACO, ferro);
      }
    }
    return resultado;
  }
}
```

## Frontend
Não se aplica (catálogo é consumido internamente).

## Arquivos prováveis
- [/src/main/java/com/example/loginbase/jogo/catalogo/ArmaEnum.java](/src/main/java/com/example/loginbase/jogo/catalogo/ArmaEnum.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/catalogo/AtributoChave.java](/src/main/java/com/example/loginbase/jogo/catalogo/AtributoChave.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/catalogo/Alcance.java](/src/main/java/com/example/loginbase/jogo/catalogo/Alcance.java) (novo)

## Testes
- Teste unitário `ArmaEnumTest`:
  - `receita_multiplica_base_por_nivel()` → Espada L5 = 15 Ferro + 5 Tábua.
  - `receita_troca_ferro_por_aco_em_l6()` → Espada L6 = 18 Aço + 6 Tábua.
  - `validarFabricacao_rejeita_pe_insuficiente()` → PE 3, L5 (requer 8) → erro.
  - `validarFabricacao_rejeita_nivel_oficina_insuficiente()` → Ferraria N1, L5 → erro.
  - Exemplo da tabela 7.8 (espada, lança, arco, besta): custos e atributos conferem.

## Definição de pronto
- Critérios de aceite da história cobertos por esta tarefa: CA1, CA3, CA4.
- Build do backend (`./mvnw verify`) sem erros.
- Testes unitários passando (cobertura ≥ 80%).
- Catálogo centralizado em `jogo.catalogo`; reutilizável por outras tarefas (fabricação, combate).

## Fora de escopo
- Cálculo de ataque (que usa a tabela 7.8, mas é da tarefa de batalha).
- Integração com a fila de fabricação; fica para a tarefa de produção.
