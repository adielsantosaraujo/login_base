# login_base

## Desenvolvimento de código

- Sempre que for desenvolver código neste projeto (pedido avulso ou trabalho com uma change do
  OpenSpec), use a skill `dev-subagentes` (`.claude/skills/dev-subagentes/SKILL.md`): a sessão
  principal só orquestra e delega por tipo de trabalho:
  - pensamento, planejamento e raciocínio → um ou mais subagentes **Opus**;
  - desenvolvimento de código e aplicação de changes/specs → um ou mais subagentes **Sonnet**;
  - escrita de textos e documentos `.md` → um ou mais subagentes **Haiku**.
- Todo subagente começa em sessão nova e limpa, sem contexto anterior. No OpenSpec, cada task
  começa em um subagente novo. Tarefas independentes rodam em paralelo.
- Ao final, grave o relatório `resumo_utilizacao_agentes.md` na pasta da change
  (`openspec/changes/<change>/`), com a lista de agentes usados e os tokens de cada um (o agente
  que grava o relatório fica fora da lista). Em pedido avulso, sem change, apresente o relatório
  no chat.
