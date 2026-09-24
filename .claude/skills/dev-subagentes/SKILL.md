---
name: dev-subagentes
description: Fluxo obrigatório para QUALQUER desenvolvimento de código — pedido avulso (implementar, corrigir bug, refatorar, criar teste, alterar arquivo de código/config) ou implementação de uma change do OpenSpec (apply / opsx apply / implementar tasks). A sessão principal só orquestra; cada tarefa é executada por um subagente Sonnet em sessão limpa, tarefas independentes rodam em paralelo, e ao final é apresentado o relatório de todos os agentes usados e do total de tokens. Use sempre que for escrever ou alterar código.
metadata:
  author: adiel
  version: "1.0"
---

# Desenvolvimento orquestrado por subagentes Sonnet

Você (sessão principal) é o **orquestrador**. Você entende o pedido, planeja, divide em tarefas, dispara subagentes, revisa os resultados e reporta. **Você não escreve código de produção diretamente** — toda alteração de código é feita por subagentes.

Vale para:
- **Código avulso**: qualquer pedido de implementar, corrigir, refatorar, testar ou alterar código/configuração.
- **Change do OpenSpec**: ao implementar (apply) uma change, use a skill `openspec-apply-change` para selecionar a change e ler o contexto, mas execute cada task com este fluxo.

## Regras inegociáveis

1. **Modelo Sonnet**: todo subagente é criado com o tool `Agent` usando `model: "sonnet"` e `subagent_type: "general-purpose"` (ou outro tipo específico adequado, nunca `fork`).
2. **Sessão limpa sempre**: cada tarefa = um subagente **novo**.
   - Nunca use `subagent_type: "fork"` (herda o contexto da sessão principal e ignora o modelo).
   - Nunca envie uma tarefa nova para um subagente já existente via `SendMessage`.
   - Correções/ajustes também são feitos por um **novo** subagente, recebendo no prompt o contexto necessário.
3. **Paralelismo**: tarefas independentes são disparadas **ao mesmo tempo** — várias chamadas `Agent` na **mesma mensagem**, uma por tarefa.
4. **Relatório final obrigatório** com todos os agentes e o total de tokens (ver seção "Relatório final").

## Passo 1 — Entender e planejar

- Leia o suficiente para planejar (estrutura do projeto, arquivos envolvidos). Leituras pontuais pelo orquestrador são permitidas; implementação não.
- Para OpenSpec: leia `proposal.md`, `design.md`, `specs/**` e `tasks.md` da change (via `openspec-apply-change` / `openspec instructions apply` quando o CLI existir; senão, direto dos arquivos em `openspec/changes/<nome>/`).
- Quebre o trabalho em tarefas pequenas e verificáveis. Em uma change do OpenSpec, a unidade padrão é uma task de `tasks.md` (tasks muito pequenas e fortemente acopladas podem ir juntas em um mesmo subagente).
- Se algo for genuinamente ambíguo, pergunte ao usuário **antes** de disparar os subagentes.

## Passo 2 — Montar as ondas de execução

Monte um plano em **ondas**:
- Duas tarefas podem estar na mesma onda se **não dependem uma da outra** e **não alteram os mesmos arquivos**.
- Tarefas dependentes (ex.: criar entidade → criar repositório que a usa) ou que tocam o mesmo arquivo vão em ondas sucessivas.
- Se o projeto for um repositório git e houver risco de sobreposição, tarefas paralelas podem usar `isolation: "worktree"`.

Mostre ao usuário o plano em uma tabela curta (onda, tarefa, arquivos principais) e prossiga.

## Passo 3 — Disparar subagentes

Para cada onda, dispare todos os subagentes dela **em uma única mensagem**. Como a sessão do subagente é limpa, o prompt precisa ser **autocontido**. Use este template:

```
Você é um subagente de implementação. Responda em português do Brasil.

## Projeto
- Diretório: <caminho absoluto>
- Stack/convenções relevantes: <ex.: Java 25, Spring Boot 4, Maven wrapper ./mvnw, pacote com.example...>

## Tarefa
<descrição objetiva da tarefa; para OpenSpec: "Task <id> da change <nome>: <texto da task>">

## Contexto a ler antes de começar
- <arquivos da change: openspec/changes/<nome>/proposal.md, design.md, specs/...>
- <arquivos de código relevantes>

## Critérios de aceite
- <o que precisa estar verdadeiro ao final>
- <comando de verificação: build/teste/lint que deve passar>

## Restrições
- Altere apenas o necessário para esta tarefa; não mexa em: <arquivos de outras tarefas paralelas>.
- NÃO edite openspec/changes/**/tasks.md (o orquestrador marca as tasks).
- NÃO faça commit, push nem ações destrutivas.
- Siga o estilo do código existente.

## Retorno esperado
1. Resumo do que foi feito
2. Arquivos criados/alterados
3. Verificações executadas e resultado (com saída relevante se falhou)
4. Pendências, dúvidas ou riscos
```

Na chamada `Agent`, use `description` curta (3–5 palavras) identificando a tarefa (ex.: "Task 2.3 criar entidade User").

## Passo 4 — Revisar e iterar

Quando cada subagente terminar:
- Registre, para o relatório: descrição, tipo, modelo, status e o uso reportado (`total_tokens`, `tool_uses`, `duration_ms`) que vem no resultado/notificação do agente.
- Revise o resultado (leia o diff/arquivos principais, confira a verificação reportada).
- Se estiver OK e for OpenSpec, **o orquestrador** marca a task como `- [x]` em `tasks.md`.
- Se precisar de ajuste, dispare um **novo** subagente Sonnet com: o que foi feito, o problema encontrado e o que corrigir.
- Siga para a próxima onda. Nunca invente resultados de um subagente que ainda está rodando — aguarde a notificação.

Ao final, se couber, dispare um subagente Sonnet de verificação (build + testes do projeto) em sessão limpa.

## Relatório final

Ao concluir (ou ao pausar por bloqueio), apresente:

```
## Agentes utilizados

| # | Agente (description) | Tipo | Modelo | Status | Tool uses | Duração | Tokens |
|---|----------------------|------|--------|--------|-----------|---------|--------|
| 1 | Task 1.1 ...         | general-purpose | sonnet | ✅ concluído | 12 | 1m 20s | 34.512 |
| 2 | ...                  | ... | ... | ... | ... | ... | ... |

**Total de subagentes:** N
**Total de tokens (subagentes):** X
**Sessão principal (orquestrador):** consumo não incluso acima — consulte com `/cost`.
```

- Inclua **todos** os agentes iniciados, inclusive os que falharam, foram interrompidos ou fizeram correções/verificação.
- Se algum agente não reportou uso, marque "n/d" e diga isso explicitamente — não estime como se fosse medido.
- Em change do OpenSpec, inclua também o progresso (`N/M tasks concluídas`).
