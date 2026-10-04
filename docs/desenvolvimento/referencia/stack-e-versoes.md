---
titulo: Stack e versões
publico: desenvolvimento
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - Dockerfile
  - frontend/Dockerfile
  - frontend/package.json
  - docker-compose.yml
---

# Stack e versões

Lista dos componentes do projeto e suas versões, com o arquivo ou localização onde estão definidas.

## Backend (Java / Spring Boot)

| Componente | Versão | Onde está definida |
|---|---|---|
| Java | 25 | `pom.xml` (propriedade `java.version`) |
| Spring Boot | 4.1.1 | `pom.xml` (parent `spring-boot-starter-parent`) |
| Spring Data JPA | (mesmo do Boot) | `spring-boot-starter-data-jpa` |
| Spring Security | (mesmo do Boot) | `spring-boot-starter-security` |
| Spring Web MVC | (mesmo do Boot) | `spring-boot-starter-webmvc` |
| Thymeleaf | (mesmo do Boot) | `spring-boot-starter-thymeleaf` |
| Thymeleaf Security extras | (mesmo do Boot) | `thymeleaf-extras-springsecurity6` |
| Flyway | (mesmo do Boot) | `spring-boot-starter-flyway` |
| Flyway PostgreSQL | (mesmo do Boot) | `flyway-database-postgresql` |
| springdoc OpenAPI | 3.1.1 | `pom.xml` (propriedade `springdoc.version`) |
| PostgreSQL JDBC Driver | (mesmo do Boot) | `postgresql` dependency |
| Lombok | (mesmo do Boot) | `org.projectlombok:lombok` |
| Maven Wrapper | 3.3.4 (wrapper) / 3.9.16 (Maven) | `.mvn/wrapper/maven-wrapper.properties` (wrapper); `Dockerfile` usa image `maven:3.9-eclipse-temurin-25-alpine` |

## Banco de dados

| Componente | Versão | Onde está definida |
|---|---|---|
| PostgreSQL | 17 | `docker-compose.yml` (image `postgres:17-trixie`) |

## Frontend (JavaScript / Node)

| Componente | Versão | Onde está definida |
|---|---|---|
| Node.js | 26 | `frontend/Dockerfile` (image `node:26-trixie-slim`) |
| npm | 12 | `frontend/Dockerfile` (instalado via `npm install -g npm@12`) |
| Vue | 3.5.42 | `frontend/package.json` (dependencies) |
| vue-router | 5.3.1 | `frontend/package.json` (dependencies) |
| PrimeVue | 5.0.1 | `frontend/package.json` (dependencies) |
| @primeuix/themes | 3.0.1 | `frontend/package.json` (dependencies) |
| primeicons | 8.0.2 | `frontend/package.json` (dependencies) |
| Vite | 8.3.0 | `frontend/package.json` (devDependencies) |
| TypeScript | 6.0.2 | `frontend/package.json` (devDependencies) |
| Vitest | 5.0.3 | `frontend/package.json` (devDependencies) |
| @vue/test-utils | 2.5.1 | `frontend/package.json` (devDependencies) |

**Nota:** As versões do `frontend/package.json` são ranges (ex.: `^3.5.42` ou `~5.0.3`), não versões exatas. O símbolo `^` permite minor e patch; `~` permite só patch.

## Docker

| Componente | Versão | Onde está definida |
|---|---|---|
| Temurin (JRE) | 25 | `Dockerfile` (image `eclipse-temurin:25-jre-alpine`) |

## Veja também

- [Executar os testes](../guias/executar-os-testes.md)
- [Configurar o Claude Code](../guias/configurar-o-claude-code.md)
