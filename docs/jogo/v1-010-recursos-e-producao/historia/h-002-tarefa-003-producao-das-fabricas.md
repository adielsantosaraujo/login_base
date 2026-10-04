# H-002 · Tarefa 003 — Produção das fábricas

**História:** [h-002-produzir-recursos-nos-predios.md](h-002-produzir-recursos-nos-predios.md) · **Domínio:** [../producao.md](../producao.md) ·
**Depende de:** [h-002-tarefa-002-producao-de-coleta-e-rural.md](h-002-tarefa-002-producao-de-coleta-e-rural.md) · **Camada:** Backend

## Objetivo

Implementar a produção de fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha) no passo 1 do turno, em ordem exata, usando receitas e ciclos. Fábricas produzem continuamente se houver insumo, respeitando limite de ciclos por trabalhador.

## Contexto necessário

- [../producao.md#números-e-tabelas](../producao.md#números-e-tabelas) — tabela de receitas e ciclos
  > Ex.: Serraria (2 Madeira → 1 Tábua, 3 ciclos/turno); Cozinha (2 Grãos + 1 Carne → 5 Refeição, 2 ciclos/turno).
  > **Ciclos executados por fábrica:** Σ(eficiência × ciclos_base × mult. nível) × fator de indústria.
  > **Fator de indústria:** 1 + (bonus_total da âncora ÷ 100) se a âncora estiver em ladrilho Indústria; senão 1,0.

- Seção 2.2 da bíblia — passo 1, ordem: Serraria → Olaria → Fundição → Tecelagem → Curtume → Cozinha.

- Seção 4.5 da bíblia — se faltar insumo, executa ciclos possíveis (fração).

## Backend

- **Serviço**
  - `ProducaoService.processarProducaoFabricas(vila, turno)`: etapa 1, após coleta e rural
    - Iterar sobre fábricas em ordem fixa (SERRARIA, OLARIA, FUNDIÇÃO, TECELAGEM, CURTUME, COZINHA)
    - Para cada fábrica ativa (estado == ATIVA):
      - Contar alocados (profissão específica da fábrica)
      - Calcular ciclos máximos: `Σ eficiência × ciclos_base × mult. nível`
      - Executar ciclos em sequência:
        - Verificar se há insumo suficiente
        - Se sim: debitar insumo, adicionar produto; ciclo--
        - Se não: parar
      - Caso especial Fundição N2+: jogador escolhe Ferro ou Aço por trabalhador (configuração de construcao)
      - Gravar evento com ciclos executados

- **Catálogo**
  - `CatalogoFabricas` com: tipo → (profissão, insumo[], produto[], ciclos_base)

- **Testes**
  - `testSerrariaN1()`: 1 Madeireiro eficiência 1,0, 6 Madeira → 3 ciclos × 1,0 (fator) = 3 Tábuas, 6 Madeira consumidas
  - `testSerrariaN1ComFatorIndustria()`: Serraria âncora em Indústria, bonus_total 50, 1 Madeireiro eficiência 1,0 → 3 × (1 + 50÷100) = 3 × 1,5 = **4,5 ciclos** (arredondados para 4 ciclos completos, ou mantém 4,5 se aceitar ciclos fracionários), 9 Madeira consumidas para 4,5 Tábuas
  - `testOlariaSemInsumo()`: Olaria com 1 Argila (precisa 2 por ciclo) → 0 ciclos, 1 Argila permanece
  - `testFundaoN2Aco()`: Fundição N2 com 4 Ferro + Carvão/Enxofre suficientes → 2 ciclos de Aço se configurado
  - `testCozinhaMultiplosCiclos()`: Cozinha com 10 Grãos + 5 Carne → 2 × 5 Refeição = 10 Refeição
  - `testOrdemFabricas()`: Serraria primeiro, depois Olaria (precedência de consumo comprovada)
  - `testEventoProducaoFabrica()`: cada fábrica com ciclos gera evento_turno

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/construcao/CatalogoFabricas.java](/src/main/java/com/example/loginbase/jogo/construcao/CatalogoFabricas.java) (novo)
- Extensão de [/src/main/java/com/example/loginbase/jogo/recurso/ProducaoService.java](/src/main/java/com/example/loginbase/jogo/recurso/ProducaoService.java) com método processarProducaoFabricas()

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA2, CA3
- Testes passando
- Ordem de fábricas rigorosamente respeitada
- Build sem erros
- Etapa integrada ao pipeline (EtapaTurno), sequência correta após coleta/rural

## Fora de escopo

- UI para escolher Ferro vs Aço (fica para tarefa de alocação/UI)
- Mudança de configuração entre turnos (fica para UI)
