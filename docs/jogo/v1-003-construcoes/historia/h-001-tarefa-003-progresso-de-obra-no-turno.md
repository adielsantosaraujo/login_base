# H-001 · Tarefa 003 — Progresso de obra no turno

**História:** [H-001 — Construir prédio nível 1](h-001-construir-predio-nivel-1.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-001-tarefa-002-api-de-construcao-e-posicionamento.md](h-001-tarefa-002-api-de-construcao-e-posicionamento.md) · **Camada:** Backend

## Objetivo

Implementar etapa 5 ("Obras") do pipeline de resolução de turno que processa todas as construções em obra de uma vila, acumula PO e conclui prédios quando atingem PO total.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Obras (seção 4.2)
  > Por turno, cada Construtor alocado adiciona `eficiência × 1,0` PO; Carregador adiciona `eficiência × 0,5` PO. Máximo 2/4/6 trabalhadores (N1/N2/N3).

- [../../v1-009-turnos/turnos.md](../../v1-009-turnos/turnos.md) — Ordem de resolução (passo 5 = Obras)

- [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md) — Eficiência de trabalho (seção 5.3)

## Backend

- **Etapa `EtapaObras`** (implementa `EtapaTurno`) em `/src/main/java/com/example/loginbase/jogo/turno/EtapaObras.java`:
  - Executa para cada obra EM_OBRA da vila.
  - Por obra, coleta Construtores alocados (até máximo do nível).
  - Calcula `Σ (eficiência × multiplicador de profissão)`: Construtor ×1,0, Carregador ×0,5.
  - Acumula PO_atual += PO_ganho_turno.
  - Se PO_atual ≥ PO_total: muda estado para ATIVA, reseta PO_atual = 0, registra evento.

- **Cálculo de eficiência**: delega para serviço `EficienciaService.calcularEficiencia(cidadao, profissao, vila)` que aplica todas as modificações (seção 5.3).

- **Testes**: 
  - 2 Construtores eficiência 1,0 em obra 4 PO → 2 turnos para conclusão.
  - Carregador ×0,5 aplicado corretamente.
  - Múltiplas obras processadas na mesma vila.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/turno/EtapaObras.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaObras.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/construcao/EficienciaService.java](/src/main/java/com/example/loginbase/jogo/construcao/EficienciaService.java) (novo)
- [/src/test/java/com/example/loginbase/jogo/turno/EtapaObrasTest.java](/src/test/java/com/example/loginbase/jogo/turno/EtapaObrasTest.java) (novo)

## Testes

- `testObrasProgresso_2ConstrutoresEficiencia1` → Casa 4 PO, 2 turnos até conclusão.
- `testObrasProgresso_ComCarregadores` → 1 Construtor + 1 Carregador → 1,5 PO/turno.
- `testObrasMultiplas` → 2 obras simultâneas são ambas processadas.
- `testObrasConclusa_MudaEstadoParaATIVA` → após conclusão, estado = ATIVA.

## Definição de pronto

- Critério CA4 coberto.
- Integração com `EtapaTurno` pipeline.
- Testes passando.

## Fora de escopo

- Cancelamento de obra.
- Pausa de obra.
