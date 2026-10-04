# login_base

## Desenvolvimento de código

- Sempre que for desenvolver código neste projeto, use a skill `dev-subagentes`
  (`.claude/skills/dev-subagentes/SKILL.md`): a sessão principal só orquestra e delega por tipo
  de trabalho:
  - pensamento, planejamento e raciocínio → um ou mais subagentes **Opus**;
  - desenvolvimento de código → um ou mais subagentes **Sonnet**;
  - escrita de textos e documentos `.md` → um ou mais subagentes **Haiku**.
- Todo subagente começa em sessão nova e limpa, sem contexto anterior. Tarefas independentes
  rodam em paralelo.
- Ao final, apresente no chat o relatório de utilização, com a lista de agentes usados e os
  tokens de cada um.

## Documentação

- Toda documentação fica em `docs/` e segue a skill `documentacao`
  (`.claude/skills/documentacao/SKILL.md`): use-a quando pedirem para documentar e, ao final de
  mudanças de código, para atualizar só os documentos afetados (passo final da
  `dev-subagentes`).
