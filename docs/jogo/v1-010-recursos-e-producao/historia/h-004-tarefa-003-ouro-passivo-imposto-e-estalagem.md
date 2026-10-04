# H-004 · Tarefa 003 — Ouro passivo, imposto e estalagem

**História:** [h-004-negociar-recursos-no-mercado.md](h-004-negociar-recursos-no-mercado.md) · **Domínio:** [../comercio.md](../comercio.md) ·
**Depende de:** [../../v1-009-turnos/historia/h-001-tarefa-002-pipeline-de-resolucao-por-vila.md](../../v1-009-turnos/historia/h-001-tarefa-002-pipeline-de-resolucao-por-vila.md), [h-001-tarefa-001-modelo-de-estoque-e-capacidade.md](h-001-tarefa-001-modelo-de-estoque-e-capacidade.md), [../../v1-003-construcoes/historia/h-004-tarefa-001-api-de-alocacao.md](../../v1-003-construcoes/historia/h-004-tarefa-001-api-de-alocacao.md), [h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md](h-002-tarefa-001-calculo-de-eficiencia-do-trabalhador.md) · **Camada:** Backend

## Objetivo

Implementar a etapa 2 do turno (ouro passivo) que coleta imposto de cidadãos e gera ouro pela Estalagem (servindo Refeições). A imigração pela Estalagem **não** faz parte desta tarefa: é o passo 9 do turno, implementado em [../../v1-002-cidadaos/historia/h-003-tarefa-003-imigracao-pela-estalagem.md](../../v1-002-cidadaos/historia/h-003-tarefa-003-imigracao-pela-estalagem.md).

## Contexto necessário

- [../comercio.md#regras](../comercio.md#regras) — Imposto 0,5 Ouro por ≥18 anos; Estalagem 4 Ouro por Refeição servida (até 5/10/15 × eficiência × mult. nível).

- Seção 4.10 da bíblia — Estalagem vagas Cozinheiro/Comerciante (2/5/10).

- Seção 2.2 da bíblia — passo 2 do turno.

## Backend

- **Serviço**
  - `OuroService.processarOuroPassivo(vila, turno)`: etapa 2
    - **Imposto**
      - Contar cidadãos vivos com idade ≥ 18 × 12 meses
      - Ouro += 0,5 × quantidade
      - Gravar evento IMPOSTO_COBRADO
    - **Estalagem**
      - Se não houver Estalagem ativa: skip
      - Contar Cozinheiros/Comerciantes alocados
      - **Fator de Comércio:** calcular a média do bonus_total das âncoras dos Mercados e Estalagens que estão em ladrilho Comércio; fator = 1 + (média ÷ 100). Se nenhum prédio de comércio estiver em ladrilho Comércio, fator = 1,0.
      - Calcular capacidade: `floor(5 × Σ eficiência × mult. nível × fator de Comércio)` Refeições (arredondada para baixo — [../comercio.md#regras](../comercio.md#regras), R9)
      - Refeições servidas = `min(capacidade, floor(Refeições em estoque))`
      - Debitar do estoque, adicionar Ouro (4 por Refeição)
      - Gravar evento ESTALAGEM_RECEITA

- **Testes**
  - `testImpostoBasico()`: 10 adultos → +5 Ouro
  - `testImpostoSemAdultos()`: população só menores → +0 Ouro
  - `testEstalagemN1()`: 1 Cozinheiro eficiência 1,0, 5 Refeições → consume 5, gera 20 Ouro
  - `testEstalagemN1ComFatorComercio()`: Mercado em Comércio bonus_total 47, Estalagem em Comércio bonus_total 33 → média (47+33)÷2 = 40 → fator 1 + 40÷100 = 1,4; 1 Cozinheiro eficiência 1,0 → floor(5 × 1,0 × 1,0 × 1,4) = **7 Refeições** capacidade; com 7 em estoque → consume 7, gera **28 Ouro**
  - `testEstalagemSemRefeicao()`: 0 Refeição → gera 0 Ouro
  - `testEstalagemN2Multiplicador()`: N2, 2 Cozinheiros eficiência 1,0, 30 Refeições → consome 12, gera 48 Ouro
  - `testEstalagemRefeicoesInsuficientes()`: capacidade 10, 3 Refeições → consome 3, gera 12 Ouro
  - `testEstalagemArredondamento()`: N2, 2 Cozinheiros eficiência 1,2, 30 Refeições → capacidade 14,4 → serve 14, gera 56 Ouro
  - `testEstalagemSemTrabalhadores()`: Estalagem sem alocados → 0 Ouro da Estalagem, imposto cobrado
  - `testEstalagemFatorComercioZero()`: nenhum Mercado/Estalagem em Comércio → fator 1,0 → capacidade normal sem multiplicador
  - `testEventosRegistrados()`: cada passo gera evento_turno

## Frontend

Não se aplica (Backend only).

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/recurso/OuroService.java](/src/main/java/com/example/loginbase/jogo/recurso/OuroService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoOuroPassivo.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaTurnoOuroPassivo.java) (novo)

## Testes

Todos listados acima.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA5, CA6, CA7, CA8, CA9
- Testes passando
- Build sem erros
- Etapa integrada ao pipeline de turno (passo 2, após Produção)
- Eventos registrados no relatório

## Fora de escopo

- Gerenciamento manual de Refeições na Estalagem (automático)
- Imigração pela Estalagem (passo 9 do turno) — ver [../../v1-002-cidadaos/historia/h-003-tarefa-003-imigracao-pela-estalagem.md](../../v1-002-cidadaos/historia/h-003-tarefa-003-imigracao-pela-estalagem.md)
