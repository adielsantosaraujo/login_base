---
titulo: Administrador inicial por variáveis de ambiente
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/seguranca/AdminInicialRunner.java
  - src/main/resources/application.properties
  - .env.example
---

# 0010 — Administrador inicial por variáveis de ambiente

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

A aplicação precisa de um usuário administrador para funcionar (atribuído ao perfil `ADMIN` criado pela migração V2). É necessário permitir criação automática desse usuário sem exigir tela de cadastro (fora do escopo atual) e sem hard-codar credenciais no código ou SQL.

## Decisão

Criar um `ApplicationRunner` (`AdminInicialRunner`) que lê `app.admin.email` e `app.admin.password` do `application.properties` (cujos valores vêm de variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_PASSWORD`). Na inicialização:

1. Se `ADMIN_PASSWORD` estiver vazio ou nulo, log de aviso e retorna (não cria).
2. Se usuário com o e-mail normalizado já existe, log de info ("já existe") e retorna.
3. Caso contrário, cria `Usuario` (nome `Administrador`, sem celular) com senha codificada via `PasswordEncoder`, vincula ao perfil `ADMIN` com vigência desde hoje, e log de info ("criado").

Não altera usuário existente nem sua senha.

## Alternativas descartadas

- **Migração SQL com hash fixo** — Rejeitada porque a senha não viria de variável de ambiente; expõe a credencial no repositório.
- **Tela de bootstrap/cadastro inicial** — Rejeitada porque está fora do escopo (Non-Goals do design).
- **Default com hash pré-computado** — Rejeitada porque força o desenvolvedor a manter/trocar a senha no banco manualmente.

## Consequências

### Positivas

- **Seguro:** senha vem de variável de ambiente, não está no código; cada ambiente pode ter credencial diferente.
- **Automatizado:** não requer intervenção manual no banco; infraestrutura consegue setupar a app via `ADMIN_EMAIL` e `ADMIN_PASSWORD`.
- **Idempotente:** rodar a inicialização múltiplas vezes não cria duplicatas nem altera senha existente.

### Negativas

- **Sem interface de troca:** usuário final não consegue trocar a senha do admin pela UI; hoje só é possível via banco de dados.
- **Sem validação da senha:** a força da senha não é verificada em tempo de setup. > **A confirmar com o responsável:** validar força de senha ou impor comprimento mínimo?
- **Sem admin se variável vazia:** se `ADMIN_PASSWORD` estiver vazio, nenhum admin é criado; há log de aviso (`log.warn`) informando a situação.

