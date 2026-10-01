# H-002 · Tarefa 001 — Geração dos inimigos por nível

**História:** [h-002-atacar-masmorra.md](h-002-atacar-masmorra.md) · **Domínio:** [../inimigos.md](../inimigos.md) ·
**Depende de:** [h-001-tarefa-002-surgimento-e-evolucao-no-turno.md](h-001-tarefa-002-surgimento-e-evolucao-no-turno.md), [../../v1-014-batalha/historia/h-001-tarefa-001-calculo-de-atributos-de-combate.md](../../v1-014-batalha/historia/h-001-tarefa-001-calculo-de-atributos-de-combate.md) · **Camada:** Backend

## Objetivo

Gerar composição de inimigos para uma masmorra de nível N, calculando atributos com multiplicador M(N) e sorteando grupo de comuns + chefe apropriado.

## Contexto necessário

- [../inimigos.md](../inimigos.md) — tabela de atributos base, grupos por nível, multiplicador M(N).
  > M(N) = 1 + 0,15 × (N − 1); quantidade comuns = min(8; 2 + N); chefe a partir de N3.

## Backend

- **Serviço** `GeradorInimigos` (novo)
  - `gerarInimigos(nivelMasmorra: int, sementeBatalha: long): List<Inimigo>`
    1. Calcular `M(N) = 1 + 0,15 × (N − 1)`.
    2. Determinar quantidade de comuns: `min(8; 2 + N)`.
    3. Sorteio com semente: selecionar inimigos comuns do grupo correspondente (N1–2, N3–4, ..., N9–10).
    4. Se N ≥ 3, adicionar chefe correspondente (Chefe goblin N3, Senhor orc N4–6, Troll ancião N7–9, Dragão jovem N10).
    5. Para cada inimigo, multiplicar atributos por M(N) e arredondar.
    6. Retornar lista de inimigos com vida inicial = PV calculado.

- **Enum/Constantes** `GrupoInimigos` (novo)
  ```
  N1_2: [Rato gigante, Goblin]
  N3_4: [Goblin, Goblin arqueiro, Lobo]
  ...
  N9_10: [Orc, Xamã orc, Troll]
  
  CHEFE_N3: Chefe goblin
  CHEFE_N4_6: Senhor orc
  CHEFE_N7_9: Troll ancião
  CHEFE_N10: Dragão jovem
  ```

- **Classe** `Inimigo` (novo, para uso interno em batalha)
  - `tipo: String` (ex.: "Goblin")
  - `pv_atual: int`
  - `pv_max: int` (= PV × M(N))
  - `atq: double`
  - `def: double`
  - `ini: double`
  - `linha: "Frente" | "Retaguarda"`
  - `tipo_ataque: String` (ex.: "Corpo a corpo", "À distância")

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/masmorra/GeradorInimigos.java](/src/main/java/com/example/loginbase/jogo/masmorra/GeradorInimigos.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/masmorra/Inimigo.java](/src/main/java/com/example/loginbase/jogo/masmorra/Inimigo.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/catalogo/GrupoInimigos.java](/src/main/java/com/example/loginbase/jogo/catalogo/GrupoInimigos.java) (novo)

## Testes

- Teste de multiplicador: M(5) = 1,60; atributos Senhor orc (220 PV) × 1,60 = 352 PV (arredondado).
- Teste de quantidade: N5 → min(8; 2+5) = 7 comuns + 1 chefe.
- Teste de grupo: N5 deve sortear do grupo "Lobo, Esqueleto, Esqueleto arqueiro, Orc".
- Teste de chefe: N3 = Chefe goblin; N4–6 = Senhor orc; N7–9 = Troll ancião; N10 = Dragão jovem.
- Teste de reprodutibilidade: mesma semente → mesma composição e atributos.
- Teste com exemplo 8.3: N5 com semente reproduz composição documentada (Lobos, Esqueletos, Orc + Senhor orc).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1 (inimigos gerados).
- Build do backend (`./mvnw verify`) sem erros.
- Testes passando com tabelas de atributos e fórmula de multiplicador (seção 8.3).
- Gerador retorna lista não vazia de inimigos com atributos válidos.

## Fora de escopo

- Renderização visual dos inimigos (frontend).
- Lógica de batalha (épico v1-014).
