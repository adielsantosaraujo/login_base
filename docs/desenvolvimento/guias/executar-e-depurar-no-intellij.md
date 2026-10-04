---
titulo: Executar e depurar no IntelliJ
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - README.md
  - src/main/resources/application.properties
  - docker-compose.yml
---

# Executar e depurar no IntelliJ

## Quando usar

Use este guia quando quiser desenvolver no IntelliJ IDEA com debugging visual, breakpoints e execução passo a passo da aplicação Spring Boot.

## Pré-requisitos

- IntelliJ IDEA Community ou Ultimate Edition instalado.
- Projeto aberto no IntelliJ (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Banco de dados PostgreSQL rodando no Docker (`make up PROFILE_FRONTEND=desativado`).
- Arquivo `.env` configurado na raiz do projeto com:
  - `ADMIN_EMAIL=admin@loginbase.local`
  - `ADMIN_PASSWORD=sua-senha`
  - `DB_HOST=localhost`, `DB_NAME=login_base`, `DB_USER=login_base`, `DB_PASSWORD=change-me`

## Passos

1. **Abra o projeto no IntelliJ.**

   Abra a pasta do projeto usando o caminho Windows (`D:\desenvolvimento\projetos\login_base`) via **File > Open** e selecione o `pom.xml`. O IntelliJ importa o projeto Maven automaticamente.

   Aguarde a indexação terminar (barra de progresso no canto inferior direito).

2. **Configure as variáveis de ambiente na run configuration.**

   Clique em **Run > Edit Configurations** (ou use `Shift + Alt + F10` para abrir o seletor):

   - Procure ou crie uma configuração do tipo **Spring Boot** chamada `LoginBaseApplication`.
   - Se não existir, clique em `+` → **Spring Boot** → configure o **Main class** como `com.example.loginbase.LoginBaseApplication`.

   No formulário da configuração:

   - Aba **Configuration:**
     - **Main class:** `com.example.loginbase.LoginBaseApplication`
     - **VM options:** deixe vazio (as variáveis vêm do `.env`)

   - Aba **Environment variables** (ou seção **Environment**):
     - Deixe vazio ou marque **Use .env file** (o Spring lê `spring.config.import=optional:file:.env[.properties]`).
     - Alternativamente, exporte manualmente aqui:
       ```
       ADMIN_EMAIL=admin@loginbase.local
       ADMIN_PASSWORD=sua-senha
       DB_HOST=localhost
       DB_NAME=login_base
       DB_USER=login_base
       DB_PASSWORD=change-me
       SERVER_PORT=8080
       ```

   Clique em **Apply** e depois **OK**.

3. **Inicie a aplicação.**

   Clique em **Run > Run 'LoginBaseApplication'** (ou `Shift + F10`).

   A aplicação compila e inicia. Na aba **Run** você verá:

   ```
   Started LoginBaseApplication in 2.345 seconds
   ```

   O banco é migrado automaticamente (Flyway) e o administrador inicial é criado (se não existir):

   ```
   Administrador inicial criado com e-mail admin@loginbase.local.
   ```

4. **Acesse a aplicação.**

   Abra seu navegador:

   ```
   http://localhost:8080/login
   ```

   Você verá a tela de login. Faça login com as credenciais configuradas.

5. **Depure usando breakpoints.**

   Para adicionar um breakpoint, clique na margem esquerda de um arquivo `.java` na linha desejada (um pequeno círculo vermelho aparecerá).

   Clique em **Run > Debug 'LoginBaseApplication'** (ou `Shift + F9`). A execução pausará em breakpoints.

   Use os ícones da barra de debugging:
   - **Step Over** (`F8`): avança uma linha.
   - **Step Into** (`F7`): entra em uma função.
   - **Step Out** (`Shift + F8`): sai de uma função.
   - **Resume Program** (`F9`): continua até o próximo breakpoint.

   Inspecione variáveis na aba **Variables**. Para avaliar expressões, use **Run > Evaluate Expression** (`Alt + F8`).

## Alternativa: Linha de comando

Se preferir não usar a IDE visual, rode direto no terminal WSL:

```bash
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

Isso carrega o `.env` e inicia a aplicação. Não há debugging visual, mas é mais rápido para iterações simples.

## Como verificar

Se a aplicação iniciar com sucesso:

- ✓ Nenhuma mensagem de erro no **Run** tab do IntelliJ.
- ✓ Você consegue acessar `http://localhost:8080/login` e ver a página de login.
- ✓ Consegue fazer login com as credenciais de `ADMIN_EMAIL`/`ADMIN_PASSWORD`.
- ✓ Breakpoints disparam quando a execução os atinge.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `org.postgresql.util.PSQLException: Connection refused` | Banco não está rodando no Docker | Execute `make up PROFILE_FRONTEND=desativado` antes. |
| `Flyway validation failed` | Esquema do banco divergiu de `V1_` e `V2_` | Execute `make down_v` para apagar o volume e `make up PROFILE_FRONTEND=desativado` para recriá-lo. |
| `ADMIN_PASSWORD não definida: administrador inicial não será criado.` | `ADMIN_PASSWORD` não foi definida ou está vazia | Defina uma senha não-vazia em `ADMIN_PASSWORD` no `.env`. |
| Debugger não para em breakpoints | Debugger não está ativo | Clique em **Run > Debug...** em vez de **Run...** |
| `JAVA_HOME não configurado` ou JDK não encontrado | Caminho do JDK inválido | No IntelliJ, vá em **File > Project Structure > Project** e escolha um SDK válido (Java 25 ou superior). |

## Veja também

- [Primeiros passos](../tutoriais/primeiros-passos.md) — como clonar e configurar o ambiente.
- [Configurar o Claude Code](./configurar-o-claude-code.md) — se preferir usar Claude Code.
- [Executar os testes](./executar-os-testes.md) — para rodar testes via terminal ou container.
