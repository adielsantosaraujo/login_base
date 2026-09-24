# Design

## Context

Ver proposal.md - Why/What Changes. Restrições relevantes para o "como":

- Os arquivos do projeto ficam em `D:\desenvolvimento\projetos\login_base`, acessível no WSL como `/mnt/d/desenvolvimento/projetos/login_base` (DrvFs).
- O Docker deve rodar sempre a partir do WSL (Docker Desktop com integração WSL2 habilitada para a distro em uso, ou Docker Engine nativo instalado dentro da distro) — comandos `docker`/`docker compose` devem ser executados em um shell WSL, nunca no PowerShell/cmd, independentemente de os arquivos estarem no Windows.
- A IDE é o IntelliJ, aberto apontando para o caminho Windows do projeto.
- O `pom.xml` de referência usa Spring Boot 4.1.1 e `java.version=25`, uma versão de JDK muito recente — a disponibilidade de imagens Docker públicas com Java 25 precisa ser confirmada no momento da implementação.

## Goals / Non-Goals

**Goals:**
- Projeto Maven padrão, abrível no IntelliJ sem configuração exótica.
- `docker compose up` sobe um Postgres (e, opcionalmente, a aplicação) usando o Docker do WSL, com os arquivos permanecendo no drive Windows.
- Build da imagem da aplicação é reprodutível via multi-stage Dockerfile, sem depender de Maven instalado no host.
- Fluxo de debug produtivo no IntelliJ (rodar/depurar a aplicação localmente contra o Postgres containerizado).

**Non-Goals:**
- Não inclui pipeline de CI/CD.
- Não define regras de negócio da aplicação (login, autenticação etc.) — isso fica para changes futuras.
- Não cobre live-reload avançado (DevTools/hot reload dentro do container) nesta primeira iteração.

## Decisions

1. **Naming**: manter `groupId=com.example` do pom de referência e trocar `artifactId`/pacote para `login-base` / `com.example.loginbase`, já que é o nome da pasta/projeto atual.
   - Alternativa considerada: manter `demo`/`com.example.demo` — rejeitada por não identificar o projeto real.

2. **Estratégia de execução em desenvolvimento**: `docker-compose.yml` define dois serviços — `db` (Postgres) e `app` (a aplicação, buildada pelo Dockerfile) — permitindo `docker compose up` para rodar a stack completa. Para o dia a dia no IntelliJ, o fluxo recomendado é rodar a aplicação diretamente pela IDE (Run/Debug) apontando para o Postgres exposto pelo `db` (porta publicada no host WSL/Windows), preservando debugger, hot-swap e breakpoints do IntelliJ.
   - Alternativa considerada: sempre rodar a app dentro do container e usar remote debug (JDWP). Rejeitada como padrão por adicionar fricção ao ciclo de desenvolvimento; documentada como opção alternativa nas tasks/README.

3. **Dockerfile multi-stage**: stage `build` com imagem Maven+JDK (via wrapper `./mvnw -q -DskipTests package`) e stage `runtime` com uma imagem JRE mínima, copiando apenas o jar final.
   - Alternativa considerada: build fora do container (`mvnw package` no host/WSL) e Dockerfile só de runtime — rejeitada porque quebra a reprodutibilidade (build passa a depender do ambiente do host).

4. **Persistência/config do banco**: variáveis de conexão do Postgres via arquivo `.env` (não versionado) consumido pelo `docker-compose.yml`; `application.properties` usa `spring.datasource.url=jdbc:postgresql://db:5432/${DB_NAME}` com host `db` (nome do serviço na rede do compose).

5. **Maven Wrapper**: mantido (`mvnw`, `mvnw.cmd`, `.mvn/`) para permitir build tanto dentro do container quanto manualmente no WSL, sem exigir Maven instalado globalmente.

## Risks / Trade-offs

- [Bind mount de pasta Windows (DrvFs) para dentro de um container Linux via WSL2 tem I/O mais lento que um volume nativo do Linux] → Mitigar montando apenas o necessário (código-fonte) e usando um **named volume** do Docker (não bind mount) para o cache do Maven (`~/.m2`) dentro do stage de build/execução, evitando releitura lenta de dependências a cada build.
- [Java 25 é muito recente; tags de imagem Docker (`eclipse-temurin:25-jdk`, `:25-jre` ou equivalentes) podem não existir/estar estáveis ainda] → Validar as tags disponíveis no momento da implementação (tasks.md inclui esse passo); usar uma imagem base alternativa (ex.: Eclipse Temurin/Amazon Corretto na versão LTS mais próxima) apenas se a tag 25 não existir, documentando o ajuste.
- [Docker Desktop sem integração WSL habilitada para a distro atual faria `docker`/`docker compose` falharem dentro do WSL] → Etapa de verificação nas tasks (`docker info`/`docker context ls` rodado de dentro do WSL) antes de prosseguir com o restante do setup.
- [IntelliJ aberto via caminho Windows pode não enxergar automaticamente um JDK instalado só dentro do WSL] → Documentar nas tasks que o JDK 25 usado pelo IntelliJ pode ser tanto um JDK Windows nativo quanto um SDK "WSL" configurado em Project Structure; a escolha não afeta o Docker, que roda sempre no WSL.

## Open Questions

- Confirmar, no momento da implementação, quais tags de imagem Docker com JDK/JRE 25 estão disponíveis publicamente (Eclipse Temurin, Amazon Corretto, etc.) para fixar a base image do Dockerfile.
