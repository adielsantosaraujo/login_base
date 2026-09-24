# Design

## Context

Ver proposal.md. Pontos relevantes para o "como":

- Skills do Claude Code são descobertas em `~/.claude/skills/<nome>/SKILL.md` (usuário, todos os projetos) e em `.claude/skills/<nome>/SKILL.md` (projeto, versionado). O acionamento automático depende do campo `description` do frontmatter.
- O tool `Agent` aceita `model: "sonnet"` e `subagent_type`. Um agente novo (qualquer tipo diferente de `fork`) começa com contexto vazio — é isso que garante "sessão limpa". `fork` herda o contexto da sessão principal e ignora `model`, portanto é proibido neste fluxo.
- Várias chamadas ao `Agent` na mesma mensagem rodam em paralelo; o resultado/notificação de cada subagente traz um bloco de uso (`total_tokens`, `tool_uses`, `duration_ms`).
- Este projeto não é (ainda) um repositório git, então isolamento via `worktree` nem sempre está disponível.

## Goals / Non-Goals

**Goals:**
- Sessão principal = orquestradora: planeja, divide, dispara, revisa, marca `tasks.md` e reporta. Não escreve código de produção.
- Toda implementação feita por subagentes Sonnet em sessões limpas, uma sessão nova por tarefa.
- Máximo paralelismo seguro: tarefas independentes ao mesmo tempo, tarefas que tocam os mesmos arquivos ou dependem uma da outra em sequência.
- Relatório final padronizado com todos os agentes e o total de tokens.
- Mesma skill global e no projeto.

**Non-Goals:**
- Não substituir as skills do OpenSpec (propose/apply/archive); apenas definir a forma de execução.
- Não medir com precisão o custo em dólares nem o consumo da sessão principal (indicar `/cost`).
- Não automatizar via hooks do `settings.json`.

## Decisions

1. **Nome `dev-subagentes`** — em português, como o restante do projeto, e descritivo.
2. **Subagente `general-purpose` + `model: "sonnet"`** — tem todos os tools necessários para editar e testar; nunca `fork`.
3. **Prompt autocontido** — como a sessão do subagente é limpa, o orquestrador envia um prompt com: caminho do projeto, objetivo, arquivos relevantes, critérios de aceite, restrições (não editar `tasks.md`, não fazer commit, responder em pt-BR) e formato de retorno (arquivos alterados, verificação executada, pendências).
4. **`tasks.md` é marcado só pelo orquestrador** — evita conflito de escrita entre subagentes paralelos e permite revisar antes de marcar `[x]`.
5. **Ondas de paralelismo** — o orquestrador monta um grafo simples de dependências (por ordem declarada e por arquivos afetados) e dispara uma "onda" de tarefas independentes por vez; quando a onda termina, dispara a próxima. Com repositório git, pode usar `isolation: "worktree"` para tarefas paralelas que tocam arquivos próximos.
6. **Correções também em sessão limpa** — se o resultado de um subagente precisar de ajuste, o orquestrador dispara um novo subagente com o contexto necessário, em vez de continuar o anterior.
7. **Reforço em `CLAUDE.md`** — uma linha no global e no do projeto referencia a skill, para que o fluxo seja aplicado sempre que houver desenvolvimento de código.

## Risks / Trade-offs

- **Mais tokens no total** (cada subagente relê contexto) → aceito em troca de isolamento e paralelismo; o relatório torna isso visível.
- **Conflitos em edições paralelas** → mitigado pela divisão por arquivos e pela ordem de ondas.
- **Skill global e do projeto podem divergir** → a cópia do projeto é a fonte versionada; ao alterar uma, copiar para a outra.
