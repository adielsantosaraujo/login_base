---
titulo: Documentação em docs por público e Diátaxis
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-10-04
atualizado_em: 2026-10-04
fontes:
  - .claude/skills/documentacao/SKILL.md
  - .claude/skills/dev-subagentes/SKILL.md
---

# 0013 — Documentação em docs por público e Diátaxis

**Status:** Aceita · **Data:** 2026-10-04

## Contexto

O projeto cresce. Documentação esparsa, desatualizada ou em múltiplos lugares (README, wikis internas, comentários de código) leva a inconsistências e gasto de tempo buscando informações. É necessário centralizar, organizar e manter a documentação com a mesma disciplina do código.

## Decisão

Centralizar **toda** documentação em `docs/` no repositório, organizada por **público-alvo** (desenvolvimento, operação, usuário, negócio) e **tipo** (segundo Diátaxis: tutorial, guia, referência, explicação, ADR). Estrutura proposta:

```
docs/
├── README.md (índice geral)
├── desenvolvimento/
│   ├── tutoriais/, guias/, referencia/, explicacoes/ (+ decisoes/)
├── operacao/
│   ├── tutoriais/, guias/, referencia/, explicacoes/
├── usuario/
│   ├── tutoriais/, guias/, referencia/, explicacoes/
└── negocio/
    ├── tutoriais/, guias/, referencia/, explicacoes/
```

Implementação atual cobre principalmente `desenvolvimento/` e `operacao/`; demais públicos e tipos foram adicionados conforme surgiram documentos.

- **Front matter YAML obrigatório:** `titulo`, `publico`, `tipo`, `atualizado_em`, `fontes` (arquivos do repo).
- **Convenção de nomes:** kebab-case ASCII; ADR: `NNNN-slug.md` (4 dígitos sequenciais).
- **Links:** sempre relativos; URLs da aplicação em código inline.
- **Diagramas Mermaid:** fluxo, sequência, entidade-relacionamento e arquitetura; obrigatório em 5 documentos chave.
- **Skill `documentacao`:** redator usa `dev-subagentes` (orquestração) + `documentacao` (redação) para:
  - **Modo sob demanda:** usuário pede documentação; redator copia template, consulta código, escreve.
  - **Modo manutenção:** ao final de qualquer mudança de código, `dev-subagentes` chama `documentacao` para atualizar docs afetados (via mapa: qual arquivo de código → quais documentos revisar).
- **Verificação:** script Python `verificar_docs.py` valida: front matter, nomes, links, coerência, kebab-case, pasta vazia, números de ADR, blocos Mermaid.
- **README da raiz:** enxuto (2–3 linhas de stack, início rápido em ~6 comandos, link para `docs/README.md`, linha de Claude Code). Seções antigas migram para `docs/`.

## Alternativas descartadas

- **Só README na raiz** — Rejeitada porque escala mal; 50+ documentos num único arquivo fica impossível de navegar.
- **Wiki externa (GitHub Pages, Notion, etc.)** — Rejeitada porque fica desacoplada do código; mudanças de código sem aviso não disparam atualização da wiki.
- **Diátaxis sem divisão por público** — Rejeitada porque desenvolvedor, operador e usuário final querem estruturas de conteúdo diferentes.

## Consequências

### Positivas

- **Única fonte da verdade:** todos sabem procurar em `docs/`; evita wiki duplicada e desatualizada.
- **Versionada com código:** mudanças de código + documentação no mesmo commit; história coerente.
- **Automatizada:** `verificar_docs.py` pega inconsistências cedo (links quebrados, pasta vazia, ADR duplicada).
- **Escalável:** estrutura por público suporta crescimento (novos documentos vão para a pasta certa).
- **Manutenção sistêmica:** mapa código → docs guia redator; `dev-subagentes` orquestra para não deixar passar.

### Negativas

- **Aprendizado:** redator precisa entender Diátaxis e a estrutura; novos precisam ler a skill.
- **Rigor:** obrigatoriedade de front matter, kebab-case e verificação pode parecer burocrática.
- **Sincronia manual:** redator precisa lembrar de atualizar `docs/README.md` quando cria/apaga documento; script só valida, não repara.
- **Sem acesso externo default:** documentação está no repo; precisa ser publicada (GitHub Pages, site, etc.) para ser acessível fora do git.

