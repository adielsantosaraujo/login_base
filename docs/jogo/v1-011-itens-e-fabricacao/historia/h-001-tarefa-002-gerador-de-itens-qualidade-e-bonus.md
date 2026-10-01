# H-001 · Tarefa 002 — Gerador de itens, qualidade e bônus

**História:** [H-001 — Fabricar item na oficina](h-001-fabricar-item-na-oficina.md) · **Domínio:** [../itens.md](../itens.md) · **Depende de:** [h-001-tarefa-001-modelo-de-dados-de-itens.md](h-001-tarefa-001-modelo-de-dados-de-itens.md) · **Camada:** Backend

## Objetivo

Implementar serviço puro (sem dependências de banco ou API) que:
1. Sorteie qualidade de item conforme margem de PE (`m = PE efetivo − (2L − 2)`)
2. Gere bônus intrínsecos distintos conforme a categoria e faixa de nível
3. Use semente para reprodutibilidade em testes

## Contexto necessário

- [Tabela de qualidade por margem (seção 7.4)](../fabricacao.md#qualidade-sorteada)
  > Margem 0–4: 70% Simples, 25% Boa, 5% Excelente, 0% Divina
  > Margem 5–9: 55% Simples, 33% Boa, 11% Excelente, 1% Divina
  > Margem 10–14: 40% Simples, 38% Boa, 19% Excelente, 3% Divina
  > Margem 15+: 30% Simples, 40% Boa, 25% Excelente, 5% Divina

- [Bônus intrínsecos permitidos por categoria (seção 7.2)](../itens.md#qualidades)
  > Armas: FOR, VEL, INT, ATK, CRIT, INI
  > Armaduras: VIT, DEF, VIDA, VEL
  > Joias: VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD
  > Ferramentas: PROF, PROD, INT, FOR, VEL

- [Faixa de magnitude por nível (seção 7.2)](../itens.md#qualidades)
  > L1–4: baixa (+1, +3%, +8, etc.)
  > L5–7: média (+2, +5%, +15, etc.)
  > L8–10: alta (+3, +8%, +25, etc.)

## Backend

- Serviço `ItemBonusGerador`:
  - Método `gerarQualidade(peEfetivo, nivel, eoficina, semente): Qualidade`
    - Calcula margem `m = peEfetivo − (2 × nivel − 2)`
    - Aplica tabela de qualidade conforme margem
    - Se Divina e oficina < N3: downgrade para Excelente
    - Usa `java.util.Random(semente)` para reprodutibilidade

  - Método `gerarBonusIntrinsecos(qualidade, categoria, nivel, semente): List<Bonus>`
    - Retorna lista de bônus distintos (tamanho = qualidade.numBonusIntrinsecos)
    - Seleciona bônus do subconjunto permitido (armas/armaduras/joias/ferramentas)
    - Sorteia magnitude conforme faixa de nível (baixa/média/alta)
    - Garante distinção (sem duplicatas)

  - Método `gerarAtributoAnel(semente): CaracteristicaEnum` (para Anéis)
    - Sorteia uma das 5 características (VIT, FOR, VEL, INT, CAR)

- Testes unitários:
  - `testQualidadeSimplesPorMargem()`: margem 0–4 → 70% Simples (com 1000 sorteios)
  - `testQualidadeBoaPorMargem()`: margem 5–9 → 33% Boa
  - `testDivinaSoN3()`: Divina sorteado em N3; downgrade em N1/N2
  - `testBonusDistintos()`: Excelente com 2 bônus → nenhuma duplicata
  - `testReproducibilidadeComSemente()`: mesma semente → mesmos bônus

## Frontend

- Não se aplica (serviço puro)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/item/ItemBonusGerador.java](/src/main/java/com/example/loginbase/jogo/item/ItemBonusGerador.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/item/ItemBonusGeradorTest.java](/src/test/java/com/example/loginbase/jogo/item/ItemBonusGeradorTest.java) (novo)

## Testes

- 100 sorteios de qualidade por margem; verificar distribuição dentro de ±5% da esperada
- 50 sorteios de bônus intrínsecos; verificar subconjunto permitido
- Testes de reprodutibilidade: mesma semente → mesmos resultados
- Teste de Divina downgrade em N1 vs. N3

## Definição de pronto

- Critérios de aceite da história cobertos: CA5 (qualidade sorteada conforme margem)
- Build (`./mvnw verify`) sem erros
- Testes passando e cobertura >80%
- Serviço sem dependências de contexto Spring (puro, testável)

## Fora de escopo

- Persistência (tarefa 001)
- API REST (tarefa 003)
- Frontend (tarefa 004)
