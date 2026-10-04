---
titulo: Configurar o administrador inicial
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/seguranca/AdminInicialRunner.java
  - src/main/resources/application.properties
  - .env.example
  - docker-compose.yml
---

# Configurar o administrador inicial

## Quando usar

Na primeira inicialização da aplicação, o administrador inicial é criado automaticamente a partir de variáveis de ambiente. Use este guia para entender como funciona esse processo e como verificar se foi bem-sucedido.

## Pré-requisitos

- A aplicação está iniciando (no Docker, com `make up PROFILE_APP=local`, ou localmente com `./mvnw spring-boot:run`).
- O arquivo `.env` foi criado no diretório raiz (copie de `.env.example`).
- O banco de dados está funcional (migração V2 que cria o perfil ADMIN já rodou).

## Passos

1. Abra ou crie o arquivo `.env` na raiz:

   ```bash
   cp .env.example .env
   ```

2. Configure as variáveis do administrador inicial no `.env`:

   ```
   ADMIN_EMAIL=seu-email@dominio.com
   ADMIN_PASSWORD=sua-senha-forte
   ```

   - **`ADMIN_EMAIL`**: e-mail do administrador. Deve ser único; normalizado automaticamente (minúsculas, espaços removidos apenas nas pontas).
   - **`ADMIN_PASSWORD`**: senha do administrador. Obrigatória para criar o admin. Se deixada em branco, a aplicação exibe aviso no log e **não** cria nenhum usuário.

3. Se você estiver subindo via Docker:

   ```bash
   make up PROFILE_APP=local
   ```

   Se localmente com Maven:

   ```bash
   set -a && . ./.env && set +a
   ./mvnw spring-boot:run
   ```

4. Acompanhe os logs durante a inicialização. Procure por mensagens do `AdminInicialRunner`:

   - **Se o admin foi criado:**
     ```
     INFO ... — Administrador inicial criado com e-mail seu-email@dominio.com.
     ```

   - **Se já existia:**
     ```
     INFO ... — Administrador inicial já existe (seu-email@dominio.com); nada foi alterado.
     ```

   - **Se `ADMIN_PASSWORD` está vazio:**
     ```
     WARN ... — ADMIN_PASSWORD não definida: administrador inicial não será criado. 
               Defina a variável de ambiente ADMIN_PASSWORD para criar o administrador inicial.
     ```

## Como verificar

- Após a inicialização, tente fazer login em `http://localhost:8080/login` (ou a porta configurada) com o e-mail e senha definidos.

- Se o login funcionar, você verá a página "Seja bem-vindo" (a área protegida da aplicação).

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| Mensagem de aviso sobre `ADMIN_PASSWORD` não definida | A variável `ADMIN_PASSWORD` está vazia | Edite `.env` e preencha `ADMIN_PASSWORD` com uma senha; reinicie a aplicação |
| "Usuário ou senha inválidos" ao tentar login | E-mail não existe no banco ou senha está errada | Verifique as credenciais; se perdeu a senha, ver seção "Como trocar o admin" abaixo |
| Admin foi criado, mas a senha não funciona | Verificar logs se houve erro durante a criação | Consulte os logs (seção "Passo 4" acima); se necessário, apague o usuário no banco e reinicie |

## Como trocar o admin

> **A confirmar com o responsável:** Como os usuários são gerenciados? Existe tela para criar/editar usuários? Por enquanto, não há interface de gerenciamento; alterações devem ser feitas diretamente no banco.

Se precisar resetar a senha ou o e-mail do admin, conecte-se ao Postgres:

```bash
docker compose exec db psql -U login_base login_base
```

Exemplo de atualização de senha (use um hash bcrypt válido):

```sql
UPDATE usuarios SET senha = '{bcrypt}$2a$10...' WHERE email = 'seu-email@dominio.com';
```

Para apagar o usuário completamente, primeiro apague as dependências (há chaves estrangeiras):

```sql
DELETE FROM sessoes WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'seu-email@dominio.com');
DELETE FROM usuario_rel_perfis WHERE usuario_id = (SELECT id FROM usuarios WHERE email = 'seu-email@dominio.com');
DELETE FROM usuarios WHERE email = 'seu-email@dominio.com';
```

Então execute `make up PROFILE_APP=local` para que o `AdminInicialRunner` recrie o admin.

## Veja também

- [`docs/operacao/referencia/variaveis-de-ambiente.md`](../referencia/variaveis-de-ambiente.md) — lista completa de variáveis e descrições.
- [`docs/usuario/guias/entrar-e-sair-do-sistema.md`](../../usuario/guias/entrar-e-sair-do-sistema.md) — como usar a aplicação após o login.
