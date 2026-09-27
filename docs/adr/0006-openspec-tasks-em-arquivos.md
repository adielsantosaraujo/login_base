# 0006 — OpenSpec com Tasks em Arquivos Autocontidos

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-25 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`6ce0641`](../../.git/logs/HEAD) commit, configuração em [`openspec/config.yaml`](../../openspec/config.yaml) |

## Contexto e problema

Especificações (OpenSpec) precisam de estrutura clara para capturar: propósito da mudança, design decisions, artefatos por capability, e principalmente **tarefas de implementação**. Inicialmente, tasks eram agregadas em um único `tasks.md`. Com tarefas complexas e paralelismo, centralizar tudo em um arquivo cria fricção: merges, referências opacas, difícil navegar à tarefa específica.

## Direcionadores da decisão

- Tarefas paralelas precisam de isolamento (um arquivo por tarefa, sem merge)
- Rastreabilidade: cada tarefa tem link bidirecional (specs ↔ tarefas ↔ código)
- Redação: subagente de uma tarefa lê apenas seu arquivo (contexto limpo)
- Versionamento: `tasks.md` é índice (checkout seguro), tarefas em subpasta

## Opções consideradas

| Opção | Descrição |
|---|---|
| **`tasks.md` como índice + task-N-slug.md autocontidos** | `tasks/` subpasta com um arquivo por tarefa. Índice lista `[ ] N — slug — Descrição` com link. Arquivo tem contexto completo: objetivo, aceitação, arquivos alterados, verificação. |
| Todas as tasks em um `tasks.md` | Simples, mas merges conflituosos com paralelismo; arquivo cresce indefinidamente. Rejeitada. |
| Wiki externa | Fora do repositório; difícil sincronizar. Rejeitada. |

## Resultado da decisão

Adotou-se **modelo de tasks distribuído**:

1. **Estrutura**:
   ```
   openspec/changes/<change>/
   ├── proposal.md
   ├── design.md
   ├── specs/
   │   ├── <capability>/spec.md
   │   └── ...
   ├── tasks.md               (índice)
   └── tasks/
       ├── 1.1-objetivo-algo.md
       ├── 1.2-objetivo-outro.md
       ├── 2.1-objetivo-terceiro.md
       └── ...
   ```

2. **`tasks.md` (índice)**:
   ```markdown
   - [ ] 1.1 — objetivo-algo
   - [ ] 1.2 — objetivo-outro
   - [ ] 2.1 — objetivo-terceiro
   ```
   Links apontam para `tasks/1.1-...md`, etc.

3. **Arquivo de task** (`tasks/1.1-objetivo-algo.md`):
   - Cabeçalho: número, slug, descrição, artefatos (specs/design/código)
   - Seção "Objetivo": o que fazer
   - Seção "Critérios de aceite": regra Gherkin opcional, verifica comportamento
   - Seção "Arquivos a criar/alterar": lista e links relativos
   - Seção "Verificação": passos manuais ou automatizados (testes)
   - Links markdown clicáveis: relativos ao repositório

4. **Convenção de links**:
   - Dentro de `docs/adr/`: relativos (`../04-arquitetura.md`)
   - Outros arquivos: desde raiz (`/src/...`, não barra inicial em GitHub)

5. **OpenSpec config**: `rules.tasks` define pattern `^(\d+\.\d+)-(.+)\.md$`

### Consequências positivas
- **Sem merges em tarefas**: cada arquivo = 1 task, merge conflict impossível
- **Contexto autocontido**: subagente lê 1 arquivo (não `tasks.md` inteiro)
- **Navegação clara**: link do índice leva direto à tarefa
- **Escalabilidade**: 25+ tasks sem fichário único crescer

### Consequências negativas
- **Índice pode ficar desincronizado**: se arquivo de task for deletado/renomeado manualmente
- **Mais arquivos**: pastas mais profundas (mitigado com nomenclatura clara)
- **Leitura manual**: developer precisa explorar 2 níveis (`tasks.md` → `tasks/`)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Index + tasks em arquivos | Sem merges; contexto isolado; escalável; claro. | Índice pode desincronizar; mais navegação. |
| Tudo em um `tasks.md` | Simples; 1 arquivo. | Merges com paralelismo; contexto explode; difícil navegar. |

## Mais informações

- **Config**: [`openspec/config.yaml`](../../openspec/config.yaml) — `rules.tasks`
- **Template**: [`openspec/templates/task.md`](../../openspec/templates/task.md)
- **Exemplo**: [`openspec/changes/archive/2026-09-27-add-city-builder-game/tasks/`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/tasks/) — 25 tasks, padrão aplicado

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
