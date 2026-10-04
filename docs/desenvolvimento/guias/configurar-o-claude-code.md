---
titulo: Configurar o Claude Code
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - README.md
  - CLAUDE.md
  - .claude/skills/dev-subagentes/SKILL.md
  - .claude/skills/documentacao/SKILL.md
---

# Configurar o Claude Code

## Quando usar

Use este guia quando estiver configurando a máquina para desenvolver no login-base com o Claude Code, ou quando precisar diagnosticar problemas de plugins e skills.

## Pré-requisitos

- Claude Code instalado e rodando.
- Node.js 26 e npm 12 (instalados no WSL para `npx`).
- Acesso ao repositório do projeto.

## Passos

1. **Instale o plugin PrimeVue.**

   O projeto usa PrimeVue para componentes de interface. O plugin traz skills e um servidor MCP (`@primevue/mcp`) necessários para trabalhar com esses componentes no Claude Code.

   Execute no terminal WSL (qualquer diretório):

   ```bash
   npx -y @primeui/cli plugin install --tool claude --library primevue
   ```

   Ou, se usar `pnpm`:

   ```bash
   pnpm dlx @primeui/cli plugin install --tool claude --library primevue
   ```

   A saída será algo como:

   ```
   ✔ Installing the PrimeVue plugin...
   Installation completed successfully.
   ```

   > **Nota:** o plugin é instalado no escopo do usuário em `~/.claude/plugins`. Reinicie o Claude Code depois de instalar.

2. **Diagnostique a instalação.**

   Após reiniciar, rode o doctor do PrimeUI:

   ```bash
   npx -y @primeui/cli doctor --tool claude --library primevue
   ```

   Você verá uma tabela de verificações. Esperado:

   - `[PASS]` para a maioria dos itens.
   - `[UNSUPPORTED]` para `direct-mcp` e `duplicate-mcp` (limitação do Claude Code, não é erro).

   O resumo final pode mostrar:

   ```
   Summary: blocked
   ```

   Isso é **normal e esperado** — significa que os itens `UNSUPPORTED` não bloqueiam o funcionamento. Se vir `Summary: ok` ou `Summary: passed`, tudo está perfeito.

3. **Conheça as skills do projeto.**

   O projeto define duas skills principais para trabalhar com Claude Code:

   - **`dev-subagentes`** (arquivo `.claude/skills/dev-subagentes/SKILL.md`)  
     Orquestra subagentes especializados para diferentes tipos de trabalho:
     - Agentes **Opus** para planejamento e raciocínio complexo.
     - Agentes **Sonnet** para desenvolvimento de código.
     - Agentes **Haiku** para redação de textos e documentação.

     **Use essa skill toda vez que for desenvolver código neste projeto**, conforme estabelecido em `CLAUDE.md`. A delegação por especialização melhora a qualidade e organiza melhor o trabalho.

   - **`documentacao`** (arquivo `.claude/skills/documentacao/SKILL.md`)  
     Gerencia a documentação em `docs/`, organizada por públicos (desenvolvimento, operação, usuário, negócio) e tipos de conteúdo (tutorial, guia, referência, explicação, ADR).

     Use essa skill para criar, revisar ou atualizar qualquer documento em `docs/`.

## Como verificar

Abra o Claude Code na pasta do projeto. Você deve ver:

- Acesso aos arquivos da raiz, `src/`, `frontend/`, `docs/` e `.claude/`.
- Sugestão automática de skills quando apropriado (ex.: digitar "documentação" ou "criar um guia" pode disparar a skill `documentacao`).
- Sugestão de skill `dev-subagentes` quando começar a descrever uma tarefa de desenvolvimento complexa.

Teste digitando em uma sessão:

```
/dev-subagentes
```

ou

```
/documentacao
```

Você deve ver a descrição da skill e instruções de como usá-la.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| Plugin PrimeVue não aparece listado | `~/.claude/plugins` vazio ou o comando falhou | Rode `npx -y @primeui/cli plugin install --tool claude --library primevue` novamente. Verifique permissões em `~/.claude`. |
| `[UNSUPPORTED]` para muitos itens no doctor | Instalação incompleta ou versão incompatível do Claude Code | Atualize o Claude Code para a versão mais recente. Reinstale o plugin. |
| Skills não aparecem ao digitar | Skills não sincronizadas ou a sessão não foi carregada com o projeto | Reinicie o Claude Code. Abra a pasta do projeto novamente. |

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — como clonar e subir o ambiente.
- [Desenvolver o frontend](./desenvolver-o-frontend.md) — rodar o dev server Vue com hot reload.
- [Executar e depurar no IntelliJ](./executar-e-depurar-no-intellij.md) — alternativa com IDE tradicional.
