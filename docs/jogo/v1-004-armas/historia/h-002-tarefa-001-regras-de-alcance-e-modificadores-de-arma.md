# H-002 · Tarefa 001 — Regras de Alcance e Modificadores de Arma

**História:** [h-002-usar-arma-em-combate.md](h-002-usar-arma-em-combate.md) · **Domínio:** [../armas.md](../armas.md), [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) ·
**Depende de:** [h-001-tarefa-001-catalogo-de-armas.md](h-001-tarefa-001-catalogo-de-armas.md) · **Camada:** Backend

## Objetivo
Implementar no motor de batalha as regras de alcance (posicionamento permitido por arma) e modificadores de iniciativa, garantindo que espada/lança/arco/besta respeitem suas limitações e o modificador de iniciativa seja aplicado no cálculo.

## Contexto necessário
- [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) — Fluxo de batalha, atributos (10.1), dano (10.2), ordem de ação por iniciativa (10.3).
- [../armas.md](../armas.md) — Resumo de alcance, modificadores e especiais por arma.

> **Alcance e posicionamento (10.3):**
> - Corpo a corpo só atinge linha de frente inimiga enquanto houver alguém vivo nela.
> - Espada só ataca se o portador estiver na frente.
> - Lança ataca de qualquer linha (frente ou retaguarda).
> - Ataques à distância (arco, besta) atingem qualquer alvo.
>
> **Modificadores de iniciativa (7.8):**
> - Espada: 0
> - Lança: +1
> - Arco: +2
> - Besta: −4
>
> **Iniciativa (10.1):**
> - Iniciativa = 2 × VEL + G + mod. arma + Σ INI; a cada rodada soma 1d6 (aleatório).

## Backend

**Serviço de Batalha (`jogo.batalha.MotorBatalha` ou similar):**

1. **Validação de alvo:**
   - Método `List<Combatente> alvosPermitidos(Combatente atacante, List<Combatente> inimigos)`:
     - Lê `atacante.arma` (enum ArmaEnum).
     - Conforme alcance da arma:
       - CORPO_A_CORPO_FRENTE (Espada): verifica se atacante está na frente; retorna inimigos apenas da linha de frente.
       - CORPO_A_CORPO_FLEX (Lança): verifica se há inimigos vivos na frente; se sim, filtra frente; senão, permite retaguarda.
       - DISTANCIA (Arco, Besta): retorna todos os inimigos vivos (frente e retaguarda).
     - Se nenhum alvo permitido, lança erro com mensagem clara.

2. **Cálculo de iniciativa:**
   - Método `int calcularIniciativa(Combatente c)`:
     - base = 2 × c.velocidade + c.peGuerreiro + c.arma.modificadorIniciativa + Σ bônus INI.
     - Resultado base usado como tiebreaker; a cada rodada, adiciona 1d6 ao base.
   - Exemplo: Arqueiro (VEL 8, G 5, Arco +2) = 16 + 5 + 2 = 23; +1d6 para ordenar nesta rodada.

3. **Integração no fluxo:**
   - No passo "calcular ordem de ação", usar `calcularIniciativa()` para cada combatente.
   - Desempate: maior VEL total; se empate, menor ID.
   - No passo "validar alvo", usar `alvosPermitidos()` antes de resolver o ataque.

**Estratégia da tropa do jogador (10.3):**
- "Mira o inimigo permitido com menor PV atual."
- Implementar seleção automática: `Combatente selecionarAlvo(List<Combatente> alvos)` retorna aquele com PV mínimo.

**Exemplo de aplicação (teste):**
- Espada na retaguarda tenta atacar: `alvosPermitidos()` retorna lista vazia; erro "Espada ataca apenas corpo a corpo da frente".
- Besta com INT 7, G 5, modificador −4: iniciativa = 2×VEL + 5 − 4 = variável (depende de VEL).

## Frontend
Não se aplica (validação é backend; frontend exibe mensagens de erro).

## Arquivos prováveis
- [/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalha.java) (alterado)
- [/src/main/java/com/example/loginbase/jogo/batalha/ValidadorAlvo.java](/src/main/java/com/example/loginbase/jogo/batalha/ValidadorAlvo.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/CalculadorIniciativa.java](/src/main/java/com/example/loginbase/jogo/batalha/CalculadorIniciativa.java) (novo)

## Testes
- Teste unitário `ValidadorAlvoTest`:
  - `espada_frente_retaguarda_rejeitada()` — Combatente com Espada na retaguarda; `alvosPermitidos()` retorna vazio.
  - `espada_frente_frente_aceita()` — Combatente na frente; alvos da frente retornados.
  - `lanca_frente_ou_retaguarda_aceita()` — Lança ataca inimigos da frente independente de posição do portador.
  - `arco_qualquer_alvo_aceita()` — Arco retorna todos os inimigos vivos.
  - `besta_qualquer_alvo_aceita()` — Besta retorna todos os inimigos vivos.

- Teste unitário `CalculadorIniciativaTest`:
  - `iniciativa_aplica_modificador_arma()` — Arco +2, base 10 → 12 + 1d6.
  - `iniciativa_aplica_modificador_arma_negativo()` — Besta −4, base 10 → 6 + 1d6.
  - `desempate_por_velocidade()` — Dois com iniciativa 10; maior VEL age primeiro.

- Teste integrado no fluxo de batalha:
  - Cenário 10.4 (Guerreiro vs. Goblin): calcular iniciativa com Espada (mod 0), verificar alvos permitidos, executar ataque.

## Definição de pronto
- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5, CA6.
- Build do backend (`./mvnw verify`) sem erros.
- Testes unitários passando (cobertura ≥ 80%).
- Modificadores de iniciativa aplicados no cálculo; regras de alcance validadas antes de cada ataque.
- Exemplo 10.4 da bíblia reproduzido com cálculos conferindo.

## Fora de escopo
- Knockback ou deslocamento por ataque; fora desta versão.
- Interface gráfica de posicionamento; fica para frontend (visualização do replay).
- Modificadores ambientais (terreno, clima); fora da v1.
