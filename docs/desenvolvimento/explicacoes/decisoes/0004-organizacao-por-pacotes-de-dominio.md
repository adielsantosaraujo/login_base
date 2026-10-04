---
titulo: Organização por pacotes de domínio
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/acesso
  - src/main/java/com/example/loginbase/auditoria
  - src/main/java/com/example/loginbase/seguranca
  - src/main/java/com/example/loginbase/web
---

# 0004 — Organização por pacotes de domínio

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

O código está em `com.example.loginbase`. É necessário escolher como organizar entidades, repositórios, serviços e controladores: por camada técnica (entity, repository, service) ou por domínio/funcionalidade.

## Decisão

Estrutura de pacotes por domínio/responsabilidade:

- **`acesso`** — Entidades `Usuario`, `Perfil`, `Permissao`, `UsuarioPerfil`, `PerfilPermissao`, `Sessao` e seus repositórios Spring Data. Lógica de normalização de e-mail e celular.
- **`auditoria`** — Superclasse `EntidadeAuditavel` com campos de auditoria (`criado_em`, `criado_por`, `alterado_em`, `alterado_por`) e implementação de `AuditorAware`.
- **`seguranca`** — `SecurityConfig` (filters, autorização), `UsuarioDetailsService`, handlers/listeners de sessão (`RegistroSessaoSuccessHandler`, `SessaoEncerradaListener`), `AdminInicialRunner`, `SessoesAbertasRunner`.
- **`web`** — Controladores (ex.: `PaginaController` para `/login` e `/`) e configurações de documentação OpenAPI (`OpenApiConfig`).

## Alternativas descartadas

- **Organização por camada técnica** (pacotes `entity`, `repository`, `service`, `controller`) — Rejeitada porque espalha um domínio único (controle de acesso) por vários pacotes, dificultando navegação e manutenção futura. Cada domínio fica isolado e fácil de estender.

## Consequências

### Positivas

- **Coesão:** cada pacote agrupa o domínio por inteiro (entidades, repositórios, lógica de negócio). Adicionar um novo campo a `Usuario` requer mudança em poucos lugares.
- **Escalabilidade:** novos domínios (ex.: relatórios, auditoria detalhada) vêm como novos pacotes, sem mexer nos existentes.
- **Fácil de estender:** quem quer adicionar telas de cadastro sabe onde procurar e adicionar código.
- **Testes:** é natural testar um domínio por inteiro (unitário + integração) em um pacote só.

### Negativas

- **Menos óbvio para iniciantes:** desenvolvedores acostumados com MVC por camada podem ter que aprender a navegação.
- **Risco de inchação:** se um domínio crescer muito, pode ser necessário reorganizar internamente (ex.: `acesso/entidades`, `acesso/servicos`). Avaliado periodicamente.
- **Importações entre pacotes:** haverá dependências entre `acesso` e `seguranca`, entre `seguranca` e `web`. Necessário manter a direção clara (ex.: web → seguranca → acesso → auditoria).

