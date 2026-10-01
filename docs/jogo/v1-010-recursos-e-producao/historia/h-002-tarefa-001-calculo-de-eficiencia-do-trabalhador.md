# H-002 · Tarefa 001 — Cálculo de eficiência do trabalhador

**História:** [h-002-produzir-recursos-nos-predios.md](h-002-produzir-recursos-nos-predios.md) · **Domínio:** [../producao.md](../producao.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar a fórmula de eficiência de trabalhador como serviço puro, testado com exemplos numéricos da bíblia. A eficiência combina PE efetivo da profissão, idade, fome, bônus do líder e PROD% de itens.

## Contexto necessário

- [../producao.md#regras](../producao.md#regras) — fórmula de eficiência seção 5.3 da bíblia
  > `eficiência = (0,5 + 0,1 × PE efetivo) × modificadores × multiplicadores`, capped em 3,0.
  > Modificadores: ×0,5 se 14–17 anos; ×0,5 se faminto; ×1,10 se bem alimentada.
  > Multiplicadores: × (1 + bônus do líder); × (1 + PROD% de itens).
  > Quem tem 0 PE na profissão rende 0,5.

- Seção 5.3 da bíblia — PE efetivo = PE base + floor(característica ÷ 5) das ligadas + bônus ferramenta + bônus PROF de itens.

## Backend

- **Serviço**
  - `EficienciaService.calcularEficiencia(cidadao, profissao, vila, construcao)`: retorna Double [0, 3,0]
    - PE efetivo da profissão (consultar cidadao_profissao + características + ferramenta + itens)
    - Base: 0,5 + 0,1 × PE efetivo
    - Limite temporário: min(3,0, base)
    - Aplicar modificadores:
      - ×0,5 se idade entre 14 e 17 anos (inclusive)
      - ×0,5 se cidadao.faminto_turnos > 0
      - ×1,10 se vila.bem_alimentada = true
    - Aplicar multiplicadores:
      - × (1 + bônus do líder) — consultar vila.familia_lider_id → CAR → floor(CAR ÷ 2) × 1%, máx. 10%
      - × (1 + PROD% de itens equipados) — soma PROD% de arma/armadura/joia/ferramenta do cidadão
    - Aplicar cap final: min(3,0, resultado)
    - Caso especial: se PE base profissão = 0 e PE de itens = 0, retorna 0,5 (regra seção 5.3)

- **Testes**
  - `testEficienciaBasica()`: PE 5 → 1,0
  - `testEficienciaMenor14a17Anos()`: PE 5, idade 15 → 0,5 × 0,5 = 0,25
  - `testEficienciaFaminto()`: PE 5 com fome → 1,0 × 0,5 = 0,5
  - `testEficienciaBemAlimentada()`: PE 5 com bem_alimentada → 1,0 × 1,10 = 1,1
  - `testEficienciaComBonusLider()`: PE 5, líder com CAR 10 → 1,0 × 1,05 = 1,05
  - `testEficienciaComPROD()`: PE 5, item com +5% PROD → 1,0 × 1,05 = 1,05
  - `testEficienciaCapada()`: PE 30 → 1,0 × 1,10 × 1,10 = 1,331 → cap em 3,0
  - `testEficienciaZeroPE()`: cidadão sem PE na profissão → 0,5 (regra especial)

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/cidadao/EficienciaService.java](/src/main/java/com/example/loginbase/jogo/cidadao/EficienciaService.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/cidadao/EficienciaServiceTest.java](/src/test/java/com/example/loginbase/jogo/cidadao/EficienciaServiceTest.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história: não direto (CA1, CA2, CA3 dependem das tarefas 2 e 3)
- Todos os 8 testes passando
- Build sem erros
- Eficiência máxima = 3,0
- Regra especial (PE = 0 → 0,5) validada

## Fora de escopo

- Bônus de ferramenta específicos por profissão (já contemplado em PE efetivo)
