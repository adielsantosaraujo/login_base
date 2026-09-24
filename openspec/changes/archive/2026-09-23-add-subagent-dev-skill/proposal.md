# Proposal

## Why

Hoje o desenvolvimento de código (tanto pedidos avulsos quanto a implementação de changes do OpenSpec) é feito diretamente pela sessão principal do Claude Code. Isso acumula contexto na sessão principal, não aproveita paralelismo entre tarefas independentes e não dá visibilidade de quantos agentes foram usados nem de quanto custou (em tokens) cada entrega. Queremos um fluxo padronizado em que a sessão principal atue apenas como orquestradora e toda implementação seja feita por subagentes Sonnet em sessões limpas, com relatório de consumo no final — e que esse fluxo valha em todos os projetos desta máquina e também para outros desenvolvedores que clonarem este projeto.

## What Changes

- Criar a skill `dev-subagentes` (arquivo `SKILL.md`) que define o fluxo de desenvolvimento orquestrado:
  - toda tarefa de código (avulsa ou task de uma change do OpenSpec) é executada por um subagente com modelo `sonnet`;
  - cada subagente é iniciado em sessão limpa (agente novo, nunca `fork` nem continuação de agente anterior via `SendMessage` para uma tarefa nova);
  - tarefas independentes são disparadas em paralelo (várias chamadas ao tool `Agent` na mesma mensagem), cada uma com seu próprio subagente;
  - ao concluir, a sessão principal relata todos os agentes/subagentes usados e o total de tokens consumido.
- Instalar a skill globalmente em `~/.claude/skills/dev-subagentes/SKILL.md` (vale para todos os projetos desta máquina).
- Versionar a mesma skill no projeto em `.claude/skills/dev-subagentes/SKILL.md`, para que outros desenvolvedores a recebam ao clonar o repositório.
- Adicionar uma instrução curta no `~/.claude/CLAUDE.md` (global) e em um `CLAUDE.md` na raiz do projeto apontando para a skill, garantindo que ela seja aplicada "sempre que for desenvolver código", e não só quando o modelo decidir acioná-la pela descrição.

## Capabilities

### New Capabilities
- `subagent-dev-workflow`: fluxo de desenvolvimento de código orquestrado por subagentes Sonnet em sessões limpas, com paralelismo e relatório de tokens.

### Modified Capabilities
(nenhuma)

## Impact

- **Código da aplicação**: nenhum — a change afeta apenas a configuração do Claude Code.
- **Arquivos novos no projeto**: `.claude/skills/dev-subagentes/SKILL.md`, `CLAUDE.md`.
- **Arquivos na máquina (fora do repositório)**: `~/.claude/skills/dev-subagentes/SKILL.md`; edição de `~/.claude/CLAUDE.md`.
- **Custo/uso**: cada tarefa passa a gerar um subagente próprio; o relatório final torna esse custo visível. O consumo da sessão principal (orquestradora) não aparece nos resultados dos subagentes e deve ser consultado com `/cost`.
- **Interação com OpenSpec**: a skill `openspec-apply-change` continua sendo o ponto de entrada para implementar uma change; a nova skill define *como* cada task é executada (delegada a subagentes) e mantém a marcação de `tasks.md` na sessão principal.
