# H-002 · Tarefa 001 — Cálculo de defesa com armaduras

**História:** [H-002 — Defesa das armaduras na batalha](h-002-defesa-das-armaduras-na-batalha.md) · **Domínio:** [../armaduras.md](../armaduras.md) · **Depende de:** [../../v1-014-batalha/historia/h-001-tarefa-001-calculo-de-atributos-de-combate.md](../../v1-014-batalha/historia/h-001-tarefa-001-calculo-de-atributos-de-combate.md) (cálculo de atributos) · **Camada:** Backend

## Objetivo

Implementar o cálculo de defesa total de um combatente como a soma das defesas de suas armaduras equipadas. Integrar este cálculo na fórmula de dano para reduzir dano recebido conforme a defesa.

## Contexto necessário

- [../armaduras.md](../armaduras.md) — soma de defesa, Sapato +1 INI
  > Defesa total = Σ DEFpeça(L) de cada peça equipada (seção 10.1).

- [../../v1-014-batalha/batalha.md](../../v1-014-batalha/batalha.md) — fórmula de dano
  > dano = max(1; round(Ataque × 100 ÷ (100 + 3 × Defesa_efetiva) × U(0,9; 1,1))); se Besta/Xamã orc: Defesa_efetiva = Defesa × 0,75 (seção 10.2).

## Backend

**Serviço de cálculo (puro, sem estado):**
- Classe `CalculoDefesa` no módulo `batalha`:
  - Método `calcularDefesaTotal(combatente)`: percorre itens equipados do combatente; para cada armadura (categoria = "Armadura"), soma sua defesa(L).
  - Método `calcularDefesaEfetiva(defesaTotal, atacante)`: retorna defesaTotal × 0,75 se atacante usa Besta ou é Xamã orc; senão retorna defesaTotal.
  - Método `calcularDano(ataque, defesaEfetiva, semente)`: aplica fórmula 10.2 com aleatoriedade U(0,9; 1,1) usando semente.

**Integração no motor de batalha:**
- Classe `MotorBatalha` ou `BatalhaService`:
  - Ao calcular dano recebido: chamar `CalculoDefesa.calcularDefesaEfetiva(defesa_alvo, atacante)` antes de aplicar a fórmula.
  - Registrar defesa total no log de batalha por rodada (para replay).

**Persistência:**
- Tabela `batalha.log` (JSONB ou similar) inclui por rodada: `{atacante_id, alvo_id, ataque, defesa_alvo, defesa_efetiva, dano, ...}`.

## Frontend

Não se aplica nesta tarefa. (Tela de replay usa dados do backend.)

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/batalha/CalculoDefesa.java](/src/main/java/com/example/loginbase/jogo/batalha/CalculoDefesa.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalha.java](/src/main/java/com/example/loginbase/jogo/batalha/MotorBatalha.java) (editar se já existir)
- [/src/test/java/com/example/loginbase/jogo/batalha/CalculoDefesaTest.java](/src/test/java/com/example/loginbase/jogo/batalha/CalculoDefesaTest.java) (novo)

## Testes

1. **Soma de defesa com peças L1**:
   - Entrada: combatente com Peitoral L1, Capacete L1, Ombreiras L1, Luvas L1, Calças L1, Sapato L1.
   - Esperado: defesa total = 24.

2. **Soma de defesa com peças L5**:
   - Entrada: combatente com todas peças L5.
   - Esperado: defesa total = 43,2.

3. **Defesa parcial (faltam peças)**:
   - Entrada: combatente com apenas Peitoral L5 (14,4 DEF) e Luvas L5 (3,6 DEF).
   - Esperado: defesa total = 18.

4. **Defesa efetiva com Besta**:
   - Entrada: defesa 40, atacante usa Besta.
   - Esperado: defesa_efetiva = 30.

5. **Defesa efetiva com Xamã orc**:
   - Entrada: defesa 40, atacante é Xamã orc.
   - Esperado: defesa_efetiva = 30.

6. **Dano reduzido com defesa**:
   - Entrada: ataque 25, defesa 24, semente fixa.
   - Esperado: dano ∈ [8, 11] aproximadamente (máximo ~23 sem armadura).

7. **Dano mínimo 1**:
   - Entrada: ataque muito baixo vs. defesa muito alta.
   - Esperado: dano = 1 (nunca 0).

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4, CA5, CA6.
- Build do backend (`./mvnw verify`) sem erros.
- Testes todos passando com cobertura ≥ 90%.
- Cálculo de defesa integrado no motor de batalha.
- Dano em combate verificável em testes do exemplo 10.4 (seção 10.4).

## Fora de escopo

- Bônus intrínsecos de armaduras (DEF%, VIT+) — ficam em v1-011 e sistema de bônus geral.
- Iniciativa (+1 do Sapato) — é calculada em tarefa de atributos de combate, H-001 tarefa 001 de Batalha.
- Crítico e dano crítico — ficam em tarefa separada de batalha.
