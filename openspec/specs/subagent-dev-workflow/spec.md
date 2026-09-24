# subagent-dev-workflow Specification

## Purpose
Padronizar o desenvolvimento de código (pedidos avulsos e implementação de changes do OpenSpec) para que a sessão principal apenas orquestre e cada tarefa seja executada por um subagente Sonnet em sessão limpa, com paralelismo entre tarefas independentes e relatório final de agentes e tokens.

## Requirements

### Requirement: Execução de código por subagentes Sonnet
Toda tarefa de desenvolvimento de código, seja um pedido avulso ou uma task de uma change do OpenSpec, SHALL ser executada por um subagente iniciado com o modelo `sonnet`. A sessão principal MUST NOT implementar o código diretamente.

#### Scenario: Pedido avulso de código
- **WHEN** o usuário pede uma alteração de código fora de uma change do OpenSpec
- **THEN** a sessão principal dispara um subagente com `model: "sonnet"` para executar a alteração e depois revisa o resultado

#### Scenario: Implementação de uma change do OpenSpec
- **WHEN** o usuário pede para implementar (apply) uma change
- **THEN** cada task pendente de `tasks.md` é executada por um subagente Sonnet

### Requirement: Sessão limpa por tarefa
Cada agente ou subagente iniciado SHALL começar em uma sessão limpa, sem herdar o contexto da sessão principal nem de outro subagente. O modo `fork` MUST NOT ser usado, e uma nova tarefa MUST NOT ser enviada a um subagente já existente.

#### Scenario: Nova tarefa
- **WHEN** o orquestrador inicia uma tarefa
- **THEN** ele cria um subagente novo com um prompt autocontido

#### Scenario: Correção de um resultado
- **WHEN** o resultado de um subagente precisa de ajuste
- **THEN** o ajuste é feito por um novo subagente em sessão limpa, recebendo no prompt o contexto necessário

### Requirement: Paralelismo de tarefas independentes
Quando houver tarefas sem dependência entre si e que não alterem os mesmos arquivos, o orquestrador SHALL executá-las ao mesmo tempo, cada uma em seu próprio subagente Sonnet.

#### Scenario: Tarefas independentes
- **WHEN** existem duas ou mais tarefas independentes pendentes
- **THEN** o orquestrador dispara um subagente para cada uma na mesma mensagem

#### Scenario: Tarefas dependentes ou no mesmo arquivo
- **WHEN** uma tarefa depende de outra ou ambas alteram o mesmo arquivo
- **THEN** elas são executadas em sequência

### Requirement: Relatório final de agentes e tokens
Ao concluir o trabalho, a sessão principal SHALL apresentar uma lista de todos os agentes e subagentes utilizados (descrição, modelo, status, tokens) e o total de tokens consumido pelos subagentes, indicando que o consumo da sessão principal pode ser consultado com `/cost`.

#### Scenario: Fim do desenvolvimento
- **WHEN** todas as tarefas foram concluídas ou interrompidas
- **THEN** o orquestrador exibe a tabela de agentes e a soma de tokens

### Requirement: Disponibilidade global e no projeto
A skill SHALL estar instalada em `~/.claude/skills/dev-subagentes/` e versionada no projeto em `.claude/skills/dev-subagentes/`, com conteúdo idêntico.

#### Scenario: Outro desenvolvedor clona o projeto
- **WHEN** um desenvolvedor abre o projeto clonado no Claude Code
- **THEN** a skill `dev-subagentes` fica disponível a partir de `.claude/skills/`
