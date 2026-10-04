---
name: documentacao
description: Documentação de software do login_base em docs/ (Diátaxis por público: desenvolvimento, operacao, usuario, negocio). Use quando o usuário pedir para documentar, escrever/atualizar documentação, docs, manual, guia, tutorial, referência, explicação de arquitetura, README, resumo da API/Swagger, ou registrar uma decisão de arquitetura (ADR). Use também no modo manutenção, ao final de qualquer mudança de código ou configuração (passo final da skill dev-subagentes), para revisar e atualizar só os documentos de docs/ afetados pelos arquivos alterados. Diagramas em Mermaid; templates por tipo nesta pasta.
metadata:
  author: adiel
  version: "1.0"
---

## Objetivo

Toda a documentação do projeto fica em `docs/`. O `README.md` da raiz é só a porta de entrada: descrição curta, início rápido e links. A documentação descreve o que o código faz hoje. Nunca invente.

## Quando usar

**Modo sob demanda:** o usuário pede para documentar algo.

**Modo manutenção:** ao fim de uma mudança de código, chamado pela `dev-subagentes`.

## Stack do projeto

Contexto do redator:

- **Backend:** Java 25, Spring Boot 4.1.1, Maven wrapper `./mvnw`, pacote `com.example.loginbase` (`acesso`, `auditoria`, `seguranca`, `web`).
- **Banco:** PostgreSQL 17 com Flyway (`src/main/resources/db/migration`).
- **Segurança e telas:** Spring Security com form login e sessões registradas em banco; Thymeleaf.
- **Frontend:** Vue 3, Vite, TypeScript, PrimeVue 5 e vue-router em `frontend/`, com build copiado para `/app` por `scripts/build_front.py`.
- **Ambiente:** Docker Compose com profiles, Makefile, rodando no WSL.
- **API:** springdoc 3.1.1, Swagger UI só para `/api/**`, exigindo login, desligável com `SPRINGDOC_ENABLED=false`.

## Públicos e estrutura

A documentação divide-se em 4 públicos, com subdivisão por tipo segundo Diátaxis. Públicos:

- **desenvolvimento:** quem altera o código.
- **operacao:** quem sobe, configura e mantém o ambiente.
- **usuario:** quem usa as telas.
- **negocio:** gestão, sem jargão técnico.

Árvore de pastas:

```
docs/
├── README.md
├── desenvolvimento/
│   ├── README.md
│   ├── tutoriais/
│   │   └── *.md
│   ├── guias/
│   │   └── *.md
│   ├── referencia/
│   │   └── *.md
│   └── explicacoes/
│       ├── *.md
│       └── decisoes/
│           ├── README.md
│           └── NNNN-*.md
├── operacao/
│   ├── README.md
│   ├── tutoriais/
│   │   └── *.md
│   ├── guias/
│   │   └── *.md
│   ├── referencia/
│   │   └── *.md
│   └── explicacoes/
│       └── *.md
├── usuario/
│   ├── README.md
│   ├── tutoriais/
│   │   └── *.md
│   ├── guias/
│   │   └── *.md
│   ├── referencia/
│   │   └── *.md
│   └── explicacoes/
│       └── *.md
└── negocio/
    ├── README.md
    ├── tutoriais/
    │   └── *.md
    ├── guias/
    │   └── *.md
    ├── referencia/
    │   └── *.md
    └── explicacoes/
        └── *.md
```

**Regra:** uma subpasta só é criada junto com o primeiro documento dela. Nunca deixe pasta vazia; ao apagar o último documento, apague a pasta.

## Onde colocar cada documento

Use a tabela a seguir para identificar público e tipo, e copie o template correspondente:

| Intenção | Tipo | Template |
|---|---|---|
| "Quero aprender fazendo, do zero até um resultado" | tutorial | `.claude/skills/documentacao/templates/tutorial.md` |
| "Quero realizar uma tarefa específica" | guia | `.claude/skills/documentacao/templates/guia.md` |
| "Quero consultar um fato exato (tabela, rota, variável, comando)" | referência | `.claude/skills/documentacao/templates/referencia.md` |
| "Quero entender como funciona e por quê" | explicação | `.claude/skills/documentacao/templates/explicacao.md` |
| "Foi tomada uma decisão de arquitetura ou tecnologia" | ADR | `.claude/skills/documentacao/templates/adr.md` |

**Uma única fonte da verdade:** antes de criar, procure em `docs/` um documento que já cubra o assunto e atualize-o. Se outro público precisar do mesmo fato, faça um link e não duplique o conteúdo. Exemplo: variáveis de ambiente ficam só em `operacao/referencia/variaveis-de-ambiente.md`.

## Cabeçalho padrão (front matter YAML)

Obrigatório em todo `.md` de `docs/`, exceto nos `README.md` de índice:

```yaml
---
titulo: Executar os testes
publico: desenvolvimento        # desenvolvimento | operacao | usuario | negocio
tipo: guia                      # tutorial | guia | referencia | explicacao | adr
atualizado_em: 2026-10-04       # AAAA-MM-DD da última alteração de conteúdo
fontes:                         # arquivos do repositório (caminho a partir da raiz, sem barra inicial) dos quais o texto deriva
  - pom.xml
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
---
```

As ADRs acrescentam:
- `status:` (Proposta | Aceita | Substituída pela NNNN | Descontinuada)
- `data:` (AAAA-MM-DD da decisão)

Regras:
- `publico` e `tipo` precisam bater com a pasta. Em ADR, o tipo é `adr` e a pasta é `decisoes/`.
- O H1 do documento é igual a `titulo`. Em ADR, o H1 é `# NNNN — Título`.
- `fontes` é a chave do modo manutenção: liste todos os arquivos lidos para escrever o documento. Use `fontes: []` só em documento sem origem no código.

## Convenção de nomes

- **Formato:** kebab-case ASCII, sem acento: `^[a-z0-9]+(-[a-z0-9]+)*\.md$`. Os textos usam acentuação normal em pt-BR.
- **Guias e tutoriais:** começam com verbo no infinitivo. Exemplo: `executar-os-testes.md`, `subir-e-derrubar-o-ambiente-docker.md`.
- **Referências e explicações:** usam substantivo. Exemplo: `modelo-de-dados.md`, `autenticacao-e-sessoes.md`.
- **ADR:** `NNNN-slug.md`, com 4 dígitos sequenciais. Nunca reutilize nem renumere.

## Links

Todos os links são relativos ao arquivo, tanto para outros docs quanto para o código. Nunca use URL absoluta do repositório nem caminho começando por `/`.

**Prefixo até a raiz por profundidade:**

- `docs/README.md` → `../`
- `docs/<publico>/<tipo>/x.md` → `../../../`
- `docs/desenvolvimento/explicacoes/decisoes/NNNN-x.md` → `../../../../`

**Exemplos:**
- De `docs/desenvolvimento/guias/x.md` para o pom: `[pom.xml](../../../pom.xml)`
- Link para seção: `arquivo.md#secao`
- URLs da aplicação (`http://localhost:8080/...`) vão em código inline, não como link.

## Diagramas Mermaid

- Use bloco ` ```mermaid ` para fluxo (`flowchart`), interação no tempo (`sequenceDiagram`), dados (`erDiagram`) e arquitetura (`flowchart` com `subgraph`).
- Rótulos em pt-BR entre aspas; ids sem acento.
- No máximo cerca de 15 nós. Sempre um parágrafo antes dizendo o que o diagrama mostra.
- Nunca use imagens binárias.
- **Obrigatório em:** `arquitetura.md`, `autenticacao-e-sessoes.md`, `modelo-de-dados.md`, `frontend-integrado-ao-backend.md` e `servicos-docker-compose.md`.

## Estilo de escrita

- pt-BR, frases curtas e voz ativa.
- Tutoriais e guias falam com "você" e usam passos numerados no imperativo.
- Comandos em blocos ` ```bash `, executados num shell **WSL** na raiz do projeto.
- Mostre a saída ou o resultado esperado quando ajudar.
- Nunca copie valores do `.env` real: use os do `.env.example`.
- O público de negócio não recebe nomes de classe nem comandos.
- **Só afirme o que está no código ou na configuração.** O que não puder ser confirmado vai num aviso do tipo:

  ```
  > **A confirmar com o responsável:** <pergunta>
  ```

- Problema conhecido do código vai em:

  ```
  > **Problema conhecido:** <descrição> (fonte: <arquivo>)
  ```

## Quando criar ADR

**Crie** para: escolha ou troca de tecnologia ou biblioteca; padrão arquitetural (pacotes, camadas, integração frontend/backend); decisão de segurança com trade-off; estratégia de banco ou esquema; mudança de infraestrutura ou ambiente; convenção de projeto que afeta todos (ex.: documentação).

**Não crie** para: correção de bug, refatoração local, ajuste de texto.

Formato curto, pelo template.

**ADR aceita é imutável.** Para mudar uma decisão, crie uma nova ADR ("Substitui a NNNN"), troque só o `status` da antiga para `Substituída pela NNNN` e atualize `decisoes/README.md`.

## Modo sob demanda

Siga os passos:

1. Identifique público e tipo (seção "Onde colocar cada documento").
2. Procure documento existente com `grep -ril <assunto> docs/` e atualize, em vez de duplicar.
3. Copie o template para o local correto em `docs/`.
4. Leia as fontes do código e preencha o documento.
5. Inclua `fontes` e `atualizado_em` no front matter.
6. Atualize `docs/README.md` (e `decisoes/README.md` se for ADR).
7. Rode o checklist abaixo e execute `python3 .claude/skills/documentacao/scripts/verificar_docs.py`.

## Modo manutenção

**Entrada:** lista de arquivos alterados (de `git status --porcelain` e `git diff --name-only HEAD`) mais um resumo da mudança e das decisões tomadas.

**Passos:**

1. Para cada arquivo alterado, rode `grep -rl "<caminho>" docs/` para pegar fontes e links.
2. Aplique o mapa abaixo (seção "Mapa: mudança no código → documentos a revisar").
3. Edite só as seções afetadas e atualize `atualizado_em`.
4. Crie ADR se a mudança trouxer decisão (seção "Quando criar ADR").
5. Se criou, renomeou ou removeu documento, atualize o índice.
6. Rode `python3 .claude/skills/documentacao/scripts/verificar_docs.py`.
7. Devolva a lista de documentos alterados ou a frase "nenhum documento afetado", com justificativa.

**Não reescreva** documentos que não foram afetados.

### Mapa: mudança no código → documentos a revisar

| Mudança | Documentos |
|---|---|
| `pom.xml`, `frontend/package.json` (dependência/versão) | `desenvolvimento/referencia/stack-e-versoes.md`; ADR se for nova tecnologia |
| `src/main/resources/db/migration/**` | `desenvolvimento/referencia/modelo-de-dados.md`; `negocio/referencia/regras-de-acesso.md` se mudar regra; `desenvolvimento/guias/criar-uma-migracao-flyway.md` se mudar convenção |
| `acesso/**` (entidades/repositórios) | `modelo-de-dados.md`, `desenvolvimento/explicacoes/autenticacao-e-sessoes.md` |
| `seguranca/SecurityConfig.java` | `desenvolvimento/referencia/rotas-e-seguranca.md`, `autenticacao-e-sessoes.md`, `usuario/**` se o comportamento do login mudar |
| `seguranca/Sessao*`, `RegistroSessao*` | `autenticacao-e-sessoes.md`, `operacao/guias/consultar-sessoes-registradas.md` |
| `seguranca/AdminInicialRunner.java` | `operacao/guias/configurar-o-administrador-inicial.md`, `desenvolvimento/tutoriais/primeiros-passos.md` |
| `seguranca/UsuarioDetailsService.java`, `IdentificadorLogin.java`, `NormalizacaoContato.java` | `autenticacao-e-sessoes.md`, `usuario/referencia/mensagens-e-formatos-de-login.md`, `negocio/referencia/regras-de-acesso.md` |
| `auditoria/**` | `desenvolvimento/explicacoes/auditoria.md` |
| `web/**`, novo `@RestController` ou rota `/api/**`, `OpenApiConfig` | `desenvolvimento/referencia/api-rest.md`, `rotas-e-seguranca.md` |
| `templates/**` (login.html) | `usuario/**` |
| `application.properties` | `operacao/referencia/variaveis-de-ambiente.md` |
| `docker-compose.yml`, `Dockerfile`, `.env.example`, `frontend/Dockerfile` | `operacao/referencia/servicos-docker-compose.md`, `variaveis-de-ambiente.md`, `operacao/guias/subir-e-derrubar-o-ambiente-docker.md`, `executar-a-aplicacao-em-container.md` |
| `Makefile`, `scripts/**` | `operacao/referencia/comandos-make-e-scripts.md`, `desenvolvimento/guias/gerar-o-build-de-producao.md` |
| `frontend/src/**` (views, rotas) | `usuario/**` (telas), `desenvolvimento/guias/desenvolver-o-frontend.md`, `frontend-integrado-ao-backend.md` |
| `frontend/vite.config.ts` | `desenvolver-o-frontend.md`, `frontend-integrado-ao-backend.md` |
| `src/test/**`, `frontend/src/**/*.spec.ts` | `desenvolvimento/guias/executar-os-testes.md` (só se mudar como rodar) |
| `.claude/**`, `CLAUDE.md` | `desenvolvimento/guias/configurar-o-claude-code.md` |
| qualquer outro | o resultado do grep em `fontes` |

## Manutenção do índice `docs/README.md`

Formato:

- parágrafo de abertura;
- tabela "Por onde começar" (Se você é… | Comece por…), com uma linha por público;
- uma seção `##` por público, com subseções `###` Tutoriais / Guias / Referência / Explicações, só as que existirem;
- itens `- [Título](caminho) — uma frase`, na ordem de leitura sugerida;
- na seção de desenvolvimento, link para `decisoes/README.md`.

**Regras:**

- Todo documento de `docs/` aparece exatamente uma vez: no índice geral, ou no índice de ADRs no caso das ADRs. Nada que não exista é listado.
- O índice de ADRs (`decisoes/README.md`) é uma tabela com Nº | Título (link) | Status | Data.

## Checklist de qualidade

Todos os itens abaixo são obrigatórios:

- [ ] Front matter completo e coerente com a pasta
- [ ] H1 igual ao título
- [ ] Um assunto por documento
- [ ] Fatos conferidos nas fontes, e o que não está no código marcado com "A confirmar"
- [ ] Comandos copiáveis e testados ou conferidos no Makefile e no compose
- [ ] Links relativos que resolvem
- [ ] Mermaid onde o documento exige
- [ ] Sem segredos
- [ ] kebab-case
- [ ] Índice atualizado
- [ ] Nenhuma pasta vazia
- [ ] `python3 .claude/skills/documentacao/scripts/verificar_docs.py` com saída 0
