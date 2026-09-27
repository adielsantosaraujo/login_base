# 0005 — Subagentes Especializados (Opus, Sonnet, Haiku) com Sessões Limpas

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-23 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-subagent-dev-skill`](../../openspec/changes/archive/2026-09-23-add-subagent-dev-skill/) |

## Contexto e problema

Há necessidade de executar tarefas de desenvolvimento (OpenSpec changes) de forma escalável e controlada. Historicamente, uma única sessão faz tudo (planejamento, código, documentação), o que acumula contexto rapidamente. Paralelismo eficiente requer isolamento entre tarefas. Especializações por tipo de trabalho (raciocínio vs. código vs. escrita) podem otimizar uso de tokens e qualidade.

## Direcionadores da decisão

- **Isolamento**: cada tarefa em sessão nova = sem vazamento de contexto entre tarefas
- **Paralelismo**: tarefas independentes rodam em paralelo (ondas)
- **Especialização**: Opus para planejamento, Sonnet para código, Haiku para `.md`
- **Rastreabilidade**: relatório final consolida tokens de todos os agentes
- **Documentação**: skill global (`~/.claude/skills/dev-subagentes`) e no projeto (`.claude/skills`)

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Subagentes + sessões limpas + especialização** | Orquestrador dispara subagentes (Opus/Sonnet/Haiku) em ondas, cada um com tarefa autocontida, sessão nova. Relatório consolida tokens. |
| Uma sessão para tudo | Mais simples, sem overhead. Rejeitada: contexto explode, sem paralelismo. |
| Forks da sessão principal | Herda contexto, mais rápido. Rejeitada: perde isolamento, não compatível com paralelismo (conflitos de edição). |
| Subagentes sem especialização (todos iguais) | Rejeitada: Opus para raciocínio é mais eficiente que Sonnet; Haiku para `.md` reduz custo. |

## Resultado da decisão

Adotou-se **skill `dev-subagentes`** (global + projeto):

1. **Roles**:
   - **Orquestrador (sessão principal)**: Entende o OpenSpec, divide tasks, dispara ondas de subagentes, marca `[x]` em `tasks.md`, redige relatório

   - **Opus (planejamento)**: Explora, propõe, revisa design decisions, escreve proposal/design
   - **Sonnet (código)**: Implementa tasks, edita código, testa em local
   - **Haiku (docs)**: Escreve `.md` (visão, requisitos, adr, etc.)

2. **Sessão limpa**: cada subagente novo (`type != "fork"`) começa sem contexto anterior
   - Prompt autocontido: caminho projeto, objetivo, arquivos relevantes, critérios, restrições, formato retorno

3. **Paralelismo em ondas**:
   - Orquestrador monta grafo de dependências (por ordem em `tasks.md`, por arquivos afetados)
   - Dispara uma "onda" de tarefas paralelas independentes
   - Aguarda conclusão da onda, dispara a próxima

4. **Relatório**: `resumo_utilizacao_agentes.md` na pasta da change, tabela:
   ```
   | Agent ID | Modelo | Tipo | Task | Tokens usados | Status |
   ```
   Orquestrador não entra na tabela (não conta tokens próprios)

5. **Reforço em `CLAUDE.md`**: linha indica que toda mudança de código deve usar a skill

### Consequências positivas
- **Isolamento**: contexto não vaza entre tasks; paralelismo seguro
- **Eficiência**: Opus para raciocínio, Sonnet para código, Haiku para `.md` reduz tokens
- **Rastreabilidade**: relatório mostra custo de cada agent, otimizações futuras
- **Escalabilidade**: ondas permitem ~40+ tasks em paralelo (sem gargalos)
- **Qualidade**: especialização melhora consistência por tipo de trabalho

### Consequências negativas
- **Mais tokens totais**: cada subagente relê contexto do projeto
- **Conflitos potenciais**: edições simultâneas em mesmos arquivos (mitigado com divisão por arquivo + ondas)
- **Overhead de setup**: cada subagente precisa entender o prompt autocontido
- **Sem forks**: proibição de `fork` limita paralelismo em variantes do mesmo trabalho

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Subagentes + limpas + especialização | Isolamento seguro; paralelismo eficiente; especialização reduz tokens; rastreabilidade clara. | Mais tokens no total; overhead de setup; sem forks. |
| Uma sessão | Simples; contexto único. | Sem paralelismo; contexto explode; acoplamento entre tasks. |
| Forks da principal | Rápido; herda contexto. | Não isolado; conflitos em edições paralelas. |

## Mais informações

- **Skill**: [`.claude/skills/dev-subagentes/SKILL.md`](../../.claude/skills/dev-subagentes/SKILL.md)
- **CLAUDE.md**: [`CLAUDE.md`](../../CLAUDE.md) — referência à skill
- **Exemplos**: [`openspec/changes/archive/2026-09-27-add-city-builder-game/resumo_utilizacao_agentes.md`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/resumo_utilizacao_agentes.md) — relatório da change do jogo (25/25 tasks, 36 agentes)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
