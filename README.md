# login-base

Plataforma de autenticação e controle de acesso construída com Java 25, Spring Boot 4.1.1, Vue 3, PostgreSQL e Docker. Fornece login por e-mail ou celular, modelo de perfis/permissões com vigência, auditoria de criação/última alteração e registro de sessões.

## Início rápido

Execute estes comandos no WSL para ter a aplicação rodando:

```bash
cp .env.example .env
# Edite .env e defina ADMIN_PASSWORD=sua-senha
make up
make build_front
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

Depois, abra `http://localhost:8080/login` e entre com `admin@loginbase.local` e sua senha. Após o login, você será redirecionado para `http://localhost:8080/patrimonio/index`.

## Documentação

A documentação completa está em [`docs/README.md`](docs/README.md), organizada por público:

- **Desenvolvedores:** [Primeiros passos](docs/desenvolvimento/tutoriais/primeiros-passos.md) e [guias de desenvolvimento](docs/desenvolvimento/guias/configurar-o-claude-code.md).
- **Operadores:** [Guias de operação](docs/operacao/guias/subir-e-derrubar-o-ambiente-docker.md) e [referência de variáveis](docs/operacao/referencia/variaveis-de-ambiente.md).
- **Usuários:** [Como fazer login](docs/usuario/tutoriais/primeiro-acesso.md).
- **Negócio:** [Visão geral do produto](docs/negocio/explicacoes/visao-geral-do-produto.md).

## Desenvolvendo com Claude Code

Se você usa o Claude Code, instale o plugin PrimeVue e as skills do projeto seguindo o guia em [`docs/desenvolvimento/guias/configurar-o-claude-code.md`](docs/desenvolvimento/guias/configurar-o-claude-code.md).
