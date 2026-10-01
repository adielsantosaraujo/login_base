# H-003 · Tarefa 001 — Tabela de recompensas e drop

**História:** [h-003-receber-recompensas-da-masmorra.md](h-003-receber-recompensas-da-masmorra.md) · **Domínio:** [../recompensas.md](../recompensas.md) ·
**Depende de:** [h-002-tarefa-002-integracao-expedicao-e-batalha.md](h-002-tarefa-002-integracao-expedicao-e-batalha.md), [../../v1-010-recursos-e-producao/historia/h-001-tarefa-001-modelo-de-estoque-e-capacidade.md](../../v1-010-recursos-e-producao/historia/h-001-tarefa-001-modelo-de-estoque-e-capacidade.md), [../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md](../../v1-011-itens-e-fabricacao/historia/h-001-tarefa-002-gerador-de-itens-qualidade-e-bonus.md), [../../v1-012-pedras-de-bonus/historia/h-001-tarefa-001-modelo-e-gerador-de-pedras.md](../../v1-012-pedras-de-bonus/historia/h-001-tarefa-001-modelo-e-gerador-de-pedras.md) · **Camada:** Backend

## Objetivo

Implementar cálculo e distribuição de recompensas ao derrotar masmorra, incluindo ouro, recursos, item aleatório, XP e pedras de bônus.

## Contexto necessário

- [../recompensas.md](../recompensas.md) — fórmulas por nível, tabelas de drop.
  > Ouro: 40N + aleatório(0..20N); Recursos: N sorteios × 10N cada; Item: chance min(80%; 10%N); XP: N por sobrevivente; Pedras: 1 + floor(N÷3) sorteios.

## Backend

- **Serviço** `GeradorRecompensas` (novo)
  - `gerarRecompensas(masmorra: Masmorra, tropaSobreviventes: List<Cidadao>, sementeBatalha: long): Recompensas`
    1. Calcular ouro: `40 × N + randomDouble(0, 20 × N)` (arredondar).
    2. Gerar N sorteios de recurso (com semente): sortear tipo de lista [Madeira, Pedra, Ferro, Couro curtido, Tecido] + [Aço se N≥6]; adicionar `10 × N` ao estoque.
    3. Chance de item: `randomDouble(0, 1) < min(0,80; 0,10 × N)` → chamar `GeradorItens.gerarItem(nivel=N, qualidade_margem=m)` onde m = 0–4 (N1–7) ou 15+ (N8–10).
    4. XP por guerreiro: `N × sobreviventes.count()`.
    5. Pedras: `1 + floor(N ÷ 3)` sorteios, cada um com probabilidade conforme tabela 8.4 (níveis de masmorra) → chamar `GeradorPedras.gerarPedra(tipo, nivel_masmorra, semente)`.

  - Retornar objeto `Recompensas` com: ouro, recursos, item (nullable), xp_por_guerreiro, pedras.

- **Classe** `Recompensas` (novo)
  ```
  ouro: double
  recursos: Map<String, Double> // recurso → quantidade
  item: Item | null
  xp_por_guerreiro: int
  pedras: List<Pedra>
  ```

- **Registro em batalha** (tabela `batalha`)
  - Campo `recompensas (jsonb)` armazena resumo de recompensas para o replay.

- **Aplicação de recompensas**
  - Ao receber vitória em `EtapaMovimentacaoTropas`:
    1. Chamar `GeradorRecompensas.gerarRecompensas(...)`.
    2. Adicionar ouro a `vila.estoque[Ouro]`.
    3. Adicionar cada recurso a `vila.estoque[recurso]`.
    4. Criar item no `inventario_vila` com `dono = null`.
    5. Para cada guerreiro sobrevivente: adicionar `xp_por_guerreiro` a `cidadao.xp_guerreiro`.
    6. Para cada pedra: criar em `inventario_vila` com `dono = null`.
    7. Registrar em `evento_turno` o resumo de recompensas.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/masmorra/GeradorRecompensas.java](/src/main/java/com/example/loginbase/jogo/masmorra/GeradorRecompensas.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/masmorra/Recompensas.java](/src/main/java/com/example/loginbase/jogo/masmorra/Recompensas.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaMovimentacaoTropas.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaMovimentacaoTropas.java) (modificação)

## Testes

- Teste de ouro: N6 → 240–360 Ouro conforme fórmula.
- Teste de recursos: N5 → 5 sorteios × 50 unidades cada.
- Teste de item: N6 → 60% chance de item L6; qualidade Simples/Boa/Excelente.
- Teste de XP: 5 guerreiros vivos em N3 → cada um ganha 3 XP.
- Teste de pedras: N6 → 3 sorteios; distribuição conforme tabela N4–6.
- Teste de reprodutibilidade: mesma semente → mesmas recompensas.
- Teste numérico completo com exemplo 8.4 (N6).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA5 (todas as recompensas).
- Build do backend (`./mvnw verify`) sem erros.
- Testes passando com tabelas 8.4 da bíblia.
- Recompensas adicionadas ao estoque, inventário e XP de guerreiros.

## Fora de escopo

- Renderização de recompensas (tela de relatório de batalha).
- Engaste de pedras (épico v1-012).
