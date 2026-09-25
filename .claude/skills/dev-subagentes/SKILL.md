---
name: dev-subagentes
description: Fluxo obrigatório para QUALQUER desenvolvimento de código — pedido avulso (implementar, corrigir bug, refatorar, criar teste, alterar arquivo de código/config) ou trabalho com uma change do OpenSpec (propose / apply / opsx apply / implementar tasks / specs). A sessão principal só orquestra; pensamento, planejamento e raciocínio ficam com subagentes Opus, código e aplicação de changes/specs com subagentes Sonnet (sempre em sessão nova e limpa), e textos/documentos .md com subagentes Haiku. OpenSpec com tasks/: cada task em um subagente novo recebe só seu arquivo tasks/X.Y-slug.md. Tarefas independentes rodam em paralelo; ao final, relatório em resumo_utilizacao_agentes.md na pasta da change. Use sempre que for escrever ou alterar código.
metadata:
  author: adiel
  version: "2.1"
---

# Desenvolvimento orquestrado por subagentes (Opus / Sonnet / Haiku)

Você (sessão principal) é o **orquestrador**. Você recebe o pedido, delega, dispara subagentes, acompanha os resultados e reporta. **Você não escreve código nem documentos diretamente** e não faz o raciocínio pesado sozinho: cada tipo de trabalho vai para um subagente do modelo certo.

Vale para:
- **Código avulso**: qualquer pedido de implementar, corrigir, refatorar, testar ou alterar código/configuração.
- **Change do OpenSpec**: propor, implementar (apply), atualizar ou sincronizar uma change. Use as skills `openspec-*` para selecionar a change e ler o contexto, mas execute cada etapa com este fluxo.

## Regras inegociáveis

1. **Modelo por tipo de trabalho** — todo subagente é criado com o tool `Agent`, `subagent_type: "general-purpose"` (ou outro tipo específico adequado, nunca `fork`) e o `model` abaixo:

   | Tipo de trabalho | Modelo | Exemplos |
   |---|---|---|
   | Pensamento, planejamento, raciocínio | `opus` | analisar o pedido, desenhar a solução, quebrar em tarefas/ondas, investigar causa de bug, revisar resultados e decidir correções |
   | Desenvolvimento de código, aplicação de changes/specs | `sonnet` | implementar uma task, corrigir código, criar testes, rodar build/testes de verificação |
   | Escrita de textos e documentos `.md` | `haiku` | proposal.md, design.md, specs, README, documentação, relatórios |

   Use **um ou mais** subagentes de cada tipo conforme o tamanho do trabalho (ex.: dois Opus analisando partes independentes do problema em paralelo).
2. **Sessão limpa sempre**: cada tarefa = um subagente **novo**, sem contexto anterior.
   - Nunca use `subagent_type: "fork"` (herda o contexto da sessão principal e ignora o modelo).
   - Nunca envie uma tarefa nova para um subagente já existente via `SendMessage`.
   - Correções/ajustes também são feitos por um **novo** subagente, recebendo no prompt o contexto necessário.
3. **OpenSpec: cada task começa em uma sessão nova e limpa** — ao iniciar qualquer task, dispare um subagente novo. Seu prompt receberá APENAS o caminho do arquivo `openspec/changes/<nome>/tasks/X.Y-slug.md` (se a pasta `tasks/` existir). Se a change for legada (sem pasta `tasks/`, formato antigo), vale o comportamento anterior. Nunca continue uma task nova em um agente que já trabalhou em outra.
4. **Paralelismo**: tarefas independentes são disparadas **ao mesmo tempo** — várias chamadas `Agent` na **mesma mensagem**, uma por tarefa.
5. **Relatório final obrigatório**, gravado em `resumo_utilizacao_agentes.md` na pasta da change (ver seção "Relatório final").

## Passo 1 — Planejar (subagente Opus)

- O orquestrador faz só leituras pontuais para localizar o contexto (estrutura do projeto, qual change, quais arquivos).
- Dispare um ou mais subagentes **Opus** de planejamento com o pedido e os caminhos relevantes. Para OpenSpec com pasta `tasks/`, eles leem `proposal.md`, `design.md`, `specs/**` e o **índice** `tasks.md` da change (`openspec/changes/<nome>/`) para montar ondas e dependências; os arquivos de task individuais (`tasks/X.Y-slug.md`) são lidos pelos subagentes de execução. Para changes legadas (sem `tasks/`), eles leem `proposal.md`, `design.md`, `specs/**` e `tasks.md` normalmente.
- O subagente de planejamento devolve: tarefas pequenas e verificáveis, dependências entre elas, arquivos principais de cada uma, o modelo indicado para cada tarefa (sonnet para código, haiku para `.md`) e dúvidas em aberto. Em uma change do OpenSpec com pasta `tasks/`, a unidade padrão é uma task do índice (uma linha de `tasks.md`).
- Se o plano trouxer algo genuinamente ambíguo, pergunte ao usuário **antes** de disparar a execução.

## Passo 2 — Montar as ondas de execução

A partir do plano, monte **ondas**:
- Duas tarefas podem estar na mesma onda se **não dependem uma da outra** e **não alteram os mesmos arquivos**.
- Tarefas dependentes (ex.: criar entidade → criar repositório que a usa) ou que tocam o mesmo arquivo vão em ondas sucessivas.
- Se o projeto for um repositório git e houver risco de sobreposição, tarefas paralelas podem usar `isolation: "worktree"`.

Mostre ao usuário o plano em uma tabela curta (onda, tarefa, modelo, arquivos principais) e prossiga.

## Passo 3 — Disparar subagentes de execução

Para cada onda, dispare todos os subagentes dela **em uma única mensagem**, com o modelo da tarefa (`sonnet` para código/changes/specs, `haiku` para textos e `.md`). Como a sessão do subagente é limpa, o prompt precisa ser **autocontido**. Use este template:

```
Você é um subagente de <implementação | redação>. Responda em português do Brasil.

## Projeto
- Diretório: <caminho absoluto>
- Stack/convenções relevantes: <ex.: Java 25, Spring Boot 4, Maven wrapper ./mvnw, pacote com.example...>

## Tarefa
<Para pedido avulso: descrição objetiva da tarefa.
Para OpenSpec com tasks/: "Task <id> da change <nome>: siga openspec/changes/<nome>/tasks/<id>-<slug>.md".
Para OpenSpec legado (sem tasks/): "Task <id> da change <nome>: <texto da task>">

## Contexto a ler antes de começar
<Para pedido avulso: arquivos de código relevantes.
Para OpenSpec com tasks/: "Leia APENAS o arquivo da task (openspec/changes/<nome>/tasks/<id>-<slug>.md) e os arquivos linkados nele. NÃO leia tasks.md nem outros arquivos de task."
Para OpenSpec legado: arquivos da change (proposal.md, design.md, specs/) e de código relevantes.>

## Critérios de aceite
- <o que precisa estar verdadeiro ao final>
- <comando de verificação: build/teste/lint que deve passar>

## Restrições
- Altere apenas o necessário para esta tarefa; não mexa em: <arquivos de outras tarefas paralelas>.
- NÃO edite openspec/changes/**/tasks.md (o orquestrador marca as tasks).
- NÃO faça commit, push nem ações destrutivas.
- Siga o estilo do código/texto existente.

## Retorno esperado
1. Resumo do que foi feito
2. Arquivos criados/alterados
3. Arquivos lidos (todos), separados em dois tópicos: **Harness** (skills, commands, agents, CLAUDE.md) e **Negócio** (changes, specs, tasks, código-fonte, etc.). Arquivos apenas criados/alterados, sem leitura prévia, não entram.
4. Verificações executadas e resultado (com saída relevante se falhou)
5. Pendências, dúvidas ou riscos
```

Na chamada `Agent`, use `description` curta (3–5 palavras) identificando a tarefa (ex.: "Task 2.3 criar entidade User").

## Passo 4 — Revisar e iterar

Quando cada subagente terminar:
- Registre, para o relatório: description, tipo, modelo, status, o uso reportado (`total_tokens`, `tool_uses`, `duration_ms`) que vem no resultado/notificação do agente, e os arquivos lidos (separados em Harness e Negócio, conforme informado no retorno).
- Confira o resultado (diff/arquivos principais e a verificação reportada). Se a análise exigir raciocínio (falha difícil, decisão de design, revisão de uma onda grande), dispare um subagente **Opus** de revisão em sessão limpa.
- Se estiver OK e for OpenSpec, **o orquestrador** marca a task como `- [x]` em `tasks.md`.
- Se precisar de ajuste, dispare um **novo** subagente do modelo da tarefa (Sonnet para código, Haiku para `.md`) com: o que foi feito, o problema encontrado e o que corrigir.
- Siga para a próxima onda. Nunca invente resultados de um subagente que ainda está rodando — aguarde a notificação.

Ao final, se couber, dispare um subagente **Sonnet** de verificação (build + testes do projeto) em sessão limpa.

## Relatório final

Ao concluir (ou ao pausar por bloqueio):

1. **Medir consumo da sessão principal** — execute, via Bash:
   ```bash
   python3 ~/.claude/skills/dev-subagentes/scripts/consumo_sessao.py --sessao <session-id>
   ```
   O script retorna duas partes: (a) uma linha pronta de tabela Markdown com o consumo do orquestrador até este momento, e (b) um bloco `### — Sessão principal (orquestrador)` com os arquivos lidos (separados em Harness e Negócio). Passe ambas as partes sem alteração ao Haiku do relatório — a linha na tabela de agentes, o bloco no fim da seção de arquivos. Se o script falhar, use "n/d" nos campos numéricos (nunca estime).

2. Dispare um subagente **Haiku** em sessão limpa para gravar o relatório em:

```
openspec/changes/<nome-da-change>/resumo_utilizacao_agentes.md
```

Passe no prompt todos os dados coletados. Formato do arquivo:

```
# Resumo de utilização de agentes — <nome-da-change>

Data: <AAAA-MM-DD>

| # | Agente (description) | Função | Modelo | Status | Tool uses | Duração | Tokens |
|---|----------------------|--------|--------|--------|-----------|---------|--------|
| 1 | Planejar change add-autenticacao | planejamento | opus | ✅ concluído | 8 | 2m 05s | 41.230 |
| 2 | Task 1.1 pom Flyway | código | sonnet | ✅ concluído | 12 | 1m 20s | 34.512 |
| 3 | Escrever design.md | documento | haiku | ✅ concluído | 3 | 0m 40s | 9.870 |
| — | Sessão principal (orquestrador) | orquestração | opus | ✅ até o início do relatório | 57 | 24m 10s | 112.480 |

## Arquivos lidos por agente

### 1 — Planejar change add-autenticacao

**Harness**
- .claude/skills/dev-subagentes/SKILL.md
- CLAUDE.md

**Negócio**
- openspec/changes/add-autenticacao/proposal.md
- openspec/changes/add-autenticacao/design.md

### 2 — Task 1.1 pom Flyway

**Harness**
- .claude/skills/dev-subagentes/SKILL.md

**Negócio**
- openspec/changes/add-autenticacao/tasks/1.1-pom-flyway.md
- openspec/changes/add-autenticacao/design.md
- pom.xml

### 3 — Escrever design.md

**Harness**
- CLAUDE.md

**Negócio**
- openspec/changes/add-autenticacao/proposal.md

### — Sessão principal (orquestrador)

**Harness**
- .claude/skills/dev-subagentes/SKILL.md
- CLAUDE.md

**Negócio**
- openspec/changes/add-autenticacao/proposal.md
- openspec/changes/add-autenticacao/tasks.md

## Totais por modelo

| Modelo | Agentes | Tokens |
|--------|---------|--------|
| opus   | N | X |
| sonnet | N | X |
| haiku  | N | X |
| orquestrador (sessão principal) | 1 | X |
| **Total** | **N** | **X** |

Progresso da change: N/M tasks concluídas.

Sessão principal: medida até o início da criação do relatório; o consumo posterior (incluindo o agente do relatório) não é contabilizado.
```

**Regras de formatação da seção "Arquivos lidos por agente":**
- Um cabeçalho `### <#> — <description>` por agente, na mesma ordem da tabela.
- Dois tópicos: **Harness** (skills, commands, agents, CLAUDE.md) e **Negócio** (changes, specs, tasks, código-fonte, etc.).
- Um arquivo por item de lista; nunca usar `<br>`.
- Tópico sem arquivos: mostrar `- —` em vez de deixar em branco.
- Se o agente não informou os arquivos lidos: usar `- n/d`.

- Inclua **todos** os agentes iniciados, inclusive os que falharam, foram interrompidos ou fizeram correções/verificação.
- **Não inclua** o próprio agente Haiku que grava o relatório — ele é a última ação e fica fora da lista. **Inclua** a linha da sessão principal como última linha da tabela (medida até o início do relatório).
- Se o script de consumo (`consumo_sessao.py`) falhar, a linha da sessão principal usa "n/d" nos campos numéricos — nunca estime.
- Se algum agente não reportou uso, marque "n/d" e diga isso explicitamente — não estime como se fosse medido.
- Se o arquivo já existir (execuções anteriores da mesma change), acrescente uma nova seção com a data em vez de sobrescrever.
- **Pedido avulso, sem change do OpenSpec**: não há pasta da change; apresente o relatório no chat no mesmo formato, incluindo a linha da sessão principal.
- Depois de gravado, mostre ao usuário a tabela no chat e o caminho do arquivo.
