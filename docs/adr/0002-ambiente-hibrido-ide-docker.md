# 0002 — Backend na IDE, Postgres em Docker

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-23 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`setup-java-project`](../../openspec/changes/archive/2026-09-23-setup-java-project/) |

## Contexto e problema

O projeto roda em WSL2 com arquivos no Windows (`D:\`). Há múltiplas formas de executar o backend durante desenvolvimento: rodá-lo dentro do container Docker (com remote debug) ou rodar diretamente pela IDE contra o Postgres containerizado. A escolha afeta produtividade (hot reload, breakpoints) versus simplificação (tudo em container).

## Direcionadores da decisão

- Ciclo de desenvolvimento ágil: breakpoints, hot-swap, inspeção de variáveis no IntelliJ
- Persistência (Postgres) deve ser reprodutível e isolada do host
- Dockerfiles multi-stage devem permitir build tanto manual quanto dentro do container
- Fluxo recomendado deve ser documentado, mas alternativas (remote debug) devem ser possíveis

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Backend na IDE, Postgres em Docker** | Backend roda pelo IntelliJ apontando para `db` na rede do compose (porta publicada em host). Dockerfile permite `docker compose build app` se necessário. |
| Remote debug via JDWP | Alternativamente documentado: rodar `app` em container com suporte a debug remoto. |

## Resultado da decisão

Adotou-se estratégia **hibrida**:

1. **Desenvolvimento recomendado**: `docker-compose.yml` define `db` (Postgres), `frontend` (Node/Vue) e `app` (imagem buildada, com valor padrão `profiles: ["desativado"]`). O fluxo padrão é:
   - `make up` sobe `db` e `frontend` (padrão); `app` só com `PROFILE_APP=local`
   - Backend roda pela IDE (Run/Debug) com env apontando para `db` via `localhost:5432` (host WSL)
   - Breakpoints, hot reload e inspeção funcionam nativamente no IntelliJ

2. **Dockerfile multi-stage**:
   - **Stage `build`**: `maven:3.9-eclipse-temurin-25-alpine`, executa `./mvnw -q -DskipTests package`
   - **Stage `runtime`**: `eclipse-temurin:25-jre-alpine`, copia apenas o JAR final
   - Permite `docker compose --profile local build app` para criar imagem reprodutível sem Maven no host

### Consequências positivas
- **Produtividade de desenvolvimento**: debugger completo, hot-swap, breakpoints no IntelliJ
- **Reprodutibilidade**: Dockerfile multi-stage garante build idêntico em qualquer ambiente (sem Maven/JDK no host)
- **Banco isolado**: Postgres em volume Docker, facilmente resetável (`make down_v`)
- **Flexibilidade**: alternativa de remote debug fica documentada para quem preferir container completo

### Consequências negativas
- **Complexidade inicial**: Dockerfile multi-stage é menos trivial que um único stage
- **Desincronização potencial**: IDE no Windows, código no `/mnt/d`, pode causar issues de I/O se não bem configurado
- **Remote debug não é padrão**: aumenta atrito para quem quer tudo containerizado

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Backend na IDE, Postgres em Docker | Debugger completo; ciclo rápido; Dockerfile multi-stage garante reprodutibilidade. | Postgres local diferente de produção (se houver); I/O em DrvFs pode ser lento. |
| Tudo em container com remote debug | Ambiente totalmente containerizado; mais próximo da produção. | Atrito ao debugar (JDWP slower); ciclo mais lento; maior fricção na IDE. |

## Mais informações

- **Design**: [`setup-java-project/design.md` §Decisions 2–3](../../openspec/changes/archive/2026-09-23-setup-java-project/design.md)
- **Dockerfile**: [`Dockerfile`](../../Dockerfile) — multi-stage build
- **Docker Compose**: [`docker-compose.yml`](../../docker-compose.yml) — serviços `db` e `app`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
