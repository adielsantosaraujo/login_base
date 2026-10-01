# H-003 · Tarefa 001 — Consumo de comida e fome

**História:** [h-003-alimentar-a-populacao.md](h-003-alimentar-a-populacao.md) · **Domínio:** [../alimentacao.md](../alimentacao.md) ·
**Depende de:** — · **Camada:** Backend

## Objetivo

Implementar a etapa 3 do turno (consumo de alimentos) que calcula demanda da população, subtrai da ordem Refeição → Grãos → Carne, marca pessoas famintas, aplica penalidades de fome, e testa morte.

## Contexto necessário

- [../alimentacao.md#números-e-tabelas](../alimentacao.md#números-e-tabelas) — consumo 1,0 adulto / 0,5 menor, ordem fixa
  > Refeição → Grãos → Carne. Bem alimentada ≥50% Refeição. Fome ×0,5 eficiência, não concebem. Morte a partir de turno 3 de fome.

- [../alimentacao.md#exemplos](../alimentacao.md#exemplos) — caso 3 com morte em VIT 0.

## Backend

- **Serviço**
  - `AlimentacaoService.processarConsumoAlimentacao(vila, turno)`: etapa 3
    - Contar população e calcular consumo total:
      - Para cada cidadão vivo:
        - Se idade_meses ≥ 14 × 12: +1,0 alimento
        - Senão: +0,5 alimento
    - Calcular bem_alimentada:
      - Consumir da ordem: Refeição (100%) → Grãos → Carne
      - Se Refeição ≥ 50% do consumo: vila.bem_alimentada = true; +10% eficiência próximo turno
      - Senão: bem_alimentada = false
    - Processar fome:
      - Se alimento insuficiente, pessoas sem comida recebem cidadao.faminto_turnos++
      - Se faminto_turnos ≥ 3: perder 1 VIT por turno
      - Se VIT chegar a 0 com fome: morte (cidadao.vivo = false)
    - Gravar eventos: ALIMENTOS_CONSUMIDOS, BEM_ALIMENTADA, FOME_DETECTADA, MORTE_FOME

- **Testes**
  - `testConsumoNormal()`: 10 adultos + 4 menores, 12 Refeições → todas consumidas
  - `testBemAlimentada()`: 100% Refeição → bem_alimentada = true
  - `testNaoBemAlimentada()`: 40% Refeição → bem_alimentada = false
  - `testFomeParcia1()`: falta alimento para alguns → faminto_turnos incrementado
  - `testFomePrimeiroTurno()`: família 3 turnos consecutivos → +3 contador
  - `testMorteFome()`: VIT 1, fome turno 3+ → morte ao processar
  - `testBonusEficiencia()`: bem_alimentada flag ativa para próximo turno

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/recurso/AlimentacaoService.java](/src/main/java/com/example/loginbase/jogo/recurso/AlimentacaoService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoAlimentacao.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoAlimentacao.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1, CA2, CA3, CA4
- Testes passando
- Build sem erros
- Etapa integrada ao pipeline de turno (passo 3, após passo 2)
- Eventos registrados no relatório

## Fora de escopo

- Preferência por tipo de alimento (ordem fixa)
- UI de alertas de fome (fica para tela de pop)
