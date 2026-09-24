# Proposal

## Why

O diretório do projeto (`login_base`) está vazio e precisa se tornar um projeto Java/Spring Boot funcional, usando como base o `pom.xml` gerado em `C:\Users\adiel\Downloads\demo\pom.xml` (Spring Boot 4.1.1, Java 25). O desenvolvimento será feito no IntelliJ, mas os serviços de apoio (banco de dados, execução em contêiner) devem rodar no Docker Engine do WSL, mesmo com os arquivos do projeto residindo em uma pasta do Windows (`D:\...`, montada em `/mnt/d` no WSL). É preciso estruturar o projeto e o ambiente de desenvolvimento antes de qualquer funcionalidade de negócio ser implementada.

## What Changes

- Criar a estrutura padrão Maven (`src/main/java`, `src/main/resources`, `src/test/java`) a partir do `pom.xml` de referência, adaptando `groupId`/`artifactId`/pacote para o projeto `login_base`.
- Trazer o `pom.xml` com as dependências já definidas (Spring Web MVC, Data JPA, Security, HATEOAS, Thymeleaf + thymeleaf-extras-springsecurity6, PostgreSQL driver, Lombok) e os respectivos starters de teste.
- Adicionar Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) para build reprodutível sem depender de Maven instalado localmente.
- Criar `Dockerfile` multi-stage (build com Maven/JDK, runtime com JRE) para empacotar a aplicação.
- Criar `docker-compose.yml` com o serviço da aplicação e um serviço PostgreSQL, pensado para rodar via Docker Engine do WSL.
- Adicionar `.gitignore`/`.gitattributes` alinhados ao HELP.md padrão do Spring Initializr, mais entradas específicas do IntelliJ (`.idea/`, `*.iml`).
- Documentar (README ou seção de tasks) o fluxo de uso: abrir o projeto no IntelliJ (via caminho Windows) enquanto o Docker roda no WSL, incluindo como apontar o Docker context/daemon do WSL a partir do host Windows.
- **BREAKING**: não aplicável (projeto novo, sem estado anterior).

## Capabilities

Esta change é puramente de scaffolding/infraestrutura de desenvolvimento (estrutura de projeto, build e ambiente Docker) e não introduz nem altera comportamento observável de uma aplicação — a aplicação gerada é o esqueleto padrão do Spring Initializr, sem regras de negócio. Por isso, não há specs de capacidade a declarar; `skip_specs: true` será definido em `.openspec.yaml`.

### New Capabilities
(nenhuma — sem comportamento de aplicação neste momento)

### Modified Capabilities
(nenhuma)

## Impact

- **Código**: novo diretório `src/` com `pom.xml`, classe principal `*Application.java` e classe de teste; nenhum código pré-existente é afetado (pasta estava vazia).
- **Build/Dependências**: Maven Wrapper + dependências do `pom.xml` de referência (Spring Boot 4.1.1 parent, Java 25).
- **Infra local**: novos arquivos `Dockerfile` e `docker-compose.yml`; passa a existir dependência de um Docker Engine acessível a partir do WSL (ex.: Docker Desktop com integração WSL, ou Docker instalado nativamente na distro).
- **IDE**: projeto pensado para ser aberto no IntelliJ a partir do caminho Windows (`D:\desenvolvimento\projetos\login_base`), com build/execução podendo ocorrer via terminal WSL dentro do próprio IntelliJ (WSL integration) ou via `docker compose`.
