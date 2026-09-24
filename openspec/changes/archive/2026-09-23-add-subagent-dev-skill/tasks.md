# Tasks

## 1. Skill

- [x] 1.1 Escrever `SKILL.md` da skill `dev-subagentes` com frontmatter (`name`, `description` com gatilhos para código avulso e apply de OpenSpec) e as seções: papel do orquestrador, regras de sessão limpa, modelo Sonnet, planejamento em ondas/paralelismo, template de prompt do subagente, integração com OpenSpec (marcação de `tasks.md` pelo orquestrador), correções e relatório final de agentes/tokens
- [x] 1.2 Instalar a skill globalmente em `~/.claude/skills/dev-subagentes/SKILL.md`
- [x] 1.3 Versionar a mesma skill no projeto em `.claude/skills/dev-subagentes/SKILL.md` e verificar com `diff` que as duas cópias são idênticas

## 2. Reforço de acionamento

- [x] 2.1 Adicionar ao `~/.claude/CLAUDE.md` uma instrução para usar a skill `dev-subagentes` sempre que for desenvolver código
- [x] 2.2 Criar `CLAUDE.md` na raiz do projeto com a mesma instrução, para outros desenvolvedores

## 3. Verificação

- [x] 3.1 Validar que o frontmatter do `SKILL.md` é YAML válido e que `name` coincide com o nome da pasta
