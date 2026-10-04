# H-001 · Tarefa 002 — Surgimento e evolução no turno

**História:** [h-001-surgimento-e-evolucao-de-masmorras.md](h-001-surgimento-e-evolucao-de-masmorras.md) · **Domínio:** [../masmorras.md](../masmorras.md) ·
**Depende de:** [h-001-tarefa-001-modelo-de-dados-de-masmorras.md](h-001-tarefa-001-modelo-de-dados-de-masmorras.md), [../../../v1-009-turnos/historia/h-001-tarefa-002-pipeline-de-resolucao-por-vila.md](../../v1-009-turnos/historia/h-001-tarefa-002-pipeline-de-resolucao-por-vila.md) · **Camada:** Backend

## Objetivo

Implementar surgimento e evolução de masmorras como etapa 12 do pipeline de turno (seção 2.2), verificando elegibilidade, aplicando probabilidade, incrementando contador e promovendo níveis.

## Contexto necessário

- [../masmorras.md](../masmorras.md) — regras R1–R5 de surgimento e evolução.
  > 1% chance por região elegível; máx. 3 ativas; carência 6 turnos pós-limpeza; +1 nível a cada 18 turnos.

- [../../../v1-008-vila-e-mapa/historia/h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md](../../v1-008-vila-e-mapa/historia/h-001-tarefa-001-modelo-de-dados-da-vila-e-regioes.md) — estrutura vila e regiões.
  > Vila tem 16 regiões (indice 1–16); região tem tipo (FLORESTA/PLANICIE/URBANA/LITORAL/MONTANHA), possuida (bool), limpa_ate_turno (int).

## Backend

- **Interface** `EtapaTurno` (ou anotação `@TurnoEtapa(ordem=12)`)
  - Implementação: `EtapaEvolutMasmorras` (novo) para executar no passo 12 do turno.

- **Serviço** `MasmorraService` (novo)
  - `procesarMasmorrasVila(vila, numeroTurno)`: executa surgimento e evolução.
    1. Listar todas as regiões da vila.
    2. Para cada região não possuída, sem masmorra ativa, fora do período de limpeza:
       - Se vila tem <12 turnos, pular.
       - Se vila tem <3 masmorras ativas: sorteio 1% → criar masmorra N1.
    3. Para cada masmorra ativa: `turnos_sem_ataque++`; se `turnos_sem_ataque ≥ 18`, somar 1 ao nível (máx. 10) e resetar contador.
  - `registrarAtaque(masmorra)`: `turnos_sem_ataque = 0`.
  - `removerMasmorra(masmorra)`: `ativa = false`; chamar `regiao.limpa_ate_turno = numeroTurno + 6`.

- **Gerador determinístico** (usar semente da vila)
  - Embaralhar regiões elegíveis com semente `vila.semente + numeroTurno` para reprodutibilidade.

## Frontend

Não se aplica.

## Arquivos prováveis

- [/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraService.java](/src/main/java/com/example/loginbase/jogo/masmorra/MasmorraService.java) (novo)
- [/src/main/java/com/example/loginbase/jogo/turno/EtapaEvolutMasmorras.java](/src/main/java/com/example/loginbase/jogo/turno/EtapaEvolutMasmorras.java) (novo)

## Testes

- Teste de elegibilidade: regiões não possuídas, sem masmorra, fora do período de limpeza.
- Teste de probabilidade: 1% chance reproduzível com semente.
- Teste de limite: não cria 4ª masmorra quando há 3.
- Teste de evolução: contador incrementa; a cada 18 turnos, nível sobe (máx. 10).
- Teste de ataque: registrar ataque reseta `turnos_sem_ataque`.
- Teste de carência: vila com <12 turnos não recebe masmorras.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA7.
- Build do backend (`./mvnw verify`) sem erros.
- Testes passando com cenários numéricos da bíblia (seção 8.1, 8.2).
- Etapa registrada no pipeline de turno (seção 2.2, passo 12).

## Fora de escopo

- Interface de exibição de masmorras (tarefa 003).
- Geração de inimigos (tarefa h-002-tarefa-001).
- Batalha (épico v1-014).
