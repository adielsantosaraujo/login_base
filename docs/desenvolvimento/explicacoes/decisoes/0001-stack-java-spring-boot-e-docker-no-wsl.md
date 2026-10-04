---
titulo: Stack Java Spring Boot e Docker no WSL
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-23
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - Dockerfile
  - Makefile
  - docker-compose.yml
---

# 0001 — Stack Java Spring Boot e Docker no WSL

**Status:** Aceita · **Data:** 2026-09-23

## Contexto

O projeto roda num ambiente WSL (Windows Subsystem for Linux) com arquivos no drive Windows (`D:\desenvolvimento\projetos\login_base`), acessível em `/mnt/d`. A IDE é o IntelliJ aberta no caminho Windows. O Docker Desktop está configurado com integração WSL2 para executar `docker`/`docker compose` sempre dentro do WSL.

Necessário escolher: qual Java, qual Spring Boot, como fazer build da imagem de forma reprodutível sem depender de Maven no host, e como permitir debug produtivo no IntelliJ contra um banco containerizado.

## Decisão

- **Java 25** com Spring Boot **4.1.1** e Maven Wrapper (`./mvnw`).
- **Docker Compose** define serviços `db` (Postgres 17) e `app` (imagem multi-stage).
- No dia a dia em desenvolvimento: a aplicação roda **localmente via IntelliJ**, apontando para o Postgres que sobe via `docker compose`.
- **Dockerfile multi-stage**: stage `build` com Maven+JDK (`mvn -q -DskipTests package`) e stage `runtime` com JRE mínima, copiando apenas o jar.
- Maven Wrapper na raiz para permitir build também manualmente em linha de comando no WSL, sem exigir Maven instalado globalmente.

## Alternativas descartadas

- **JDWP remoto (debug no container)** — Rejeitada como padrão por adicionar fricção ao ciclo de desenvolvimento; documentada como opção alternativa.
- **Build fora do container** (`mvnw package` no host) com Dockerfile só de runtime — Rejeitada porque quebra a reprodutibilidade (build passa a depender do ambiente do host WSL).

## Consequências

### Positivas

- **Reprodutibilidade:** o build acontece sempre da mesma forma, dentro de um container, independente do que o desenvolvedor tem instalado no WSL.
- **Debugger, hot-swap e breakpoints do IntelliJ** funcionam no fluxo diário, mantendo produtividade.
- **Banco separado:** o Postgres segue containerizado e pode ser compartilhado com outros serviços futuros (frontend, cache).
- **Maven Wrapper:** permite que desenvolvedores rodem testes e builds em linha de comando sem instalar Maven globalmente.

### Negativas

- **Java 25 é muito recente:** tags de imagem Docker (`maven:3.9-eclipse-temurin-25-alpine` para o stage build, `eclipse-temurin:25-jre-alpine` para o stage runtime) podem não estar estáveis ou completamente testadas.
- **I/O em DrvFs:** bind mount do drive Windows para dentro de um container Linux tem I/O mais lento que volume nativo. Mitigado usando cache do BuildKit (`RUN --mount=type=cache,target=/root/.m2`) para Maven, combinado com `COPY src` para o código-fonte, sem bind mount.
- **Espaço em disco:** a imagem multi-stage deixa artefatos de build temporários; usar `docker system prune` periodicamente para liberar espaço.

