# login_base

## Desenvolvimento de código

- Sempre que for desenvolver código neste projeto (pedido avulso ou implementação de uma change do
  OpenSpec), use a skill `dev-subagentes` (`.claude/skills/dev-subagentes/SKILL.md`): a sessão
  principal só orquestra, cada tarefa roda em um subagente Sonnet em sessão limpa, tarefas
  independentes em paralelo, e ao final relate todos os agentes usados e o total de tokens.
