# 0001 — Spring Boot 4.1 + Java 25 + Maven Wrapper

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-23 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`setup-java-project`](../../openspec/changes/archive/2026-09-23-setup-java-project/) |

## Contexto e problema

Há necessidade de estabelecer a base técnica do projeto: linguagem, framework web e ferramenta de build. O projeto será rodado em ambiente local (WSL2) e Docker, com Java muito recente (versão 25) ainda não amplamente testada em imagens Docker públicas.

## Direcionadores da decisão

- Compatibilidade com Spring Boot e Spring Security para autenticação em mudanças futuras.
- Reprodutibilidade do build via Docker, sem depender de ferramentas instaladas no host Windows.
- Suporte a migrações de banco de dados via Flyway.
- Uso de Maven Wrapper para permitir build tanto dentro de container quanto manualmente.

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Spring Boot 4.1 + Java 25 + Maven** | Usar versões mais recentes, buildadas pelo Maven 3.9 dentro do Dockerfile multi-stage. |
| Gradle ao invés de Maven | Rejeitada: Maven é mais direto para migrações Flyway e configuração de banco. |
| Maven instalado no host WSL | Rejeitada: quebra reprodutibilidade (ambiente do host afeta build). |
| Java 21 LTS em vez de 25 | Considerada, mas Java 25 foi explicitamente pedido; validação de disponibilidade de imagens Docker foi incluída nas tasks. |

## Resultado da decisão

Adotou-se **Spring Boot 4.1.1** com **Java 25** (via Eclipse Temurin), **Maven 3.9** e **Maven Wrapper** (`mvnw`/`.mvn/`). O `pom.xml` referencia:
- `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-flyway`
- Driver PostgreSQL
- Lombok para redução de boilerplate
- Dependências de teste (`spring-boot-starter-test`, `spring-security-test`)

Nomeamento: `groupId=com.example.loginbase`, `artifactId=login-base`, pacote raiz `com.example.loginbase`.

### Consequências positivas
- Build **reprodutível**: executado dentro do Dockerfile multi-stage sem exigir Maven no host
- **Compatibilidade futura**: Spring Security 7 e Hibernate 7 já suportam Postgres 17 e Java 25
- **Maven Wrapper**: permite `./mvnw` em qualquer lugar, com a versão fixa
- **Lombok**: reduz verbosidade de getters/setters e equals/hashCode

### Consequências negativas
- Java 25 é **muito recente**: potencial falta de imagens Docker estáveis (endereçado na task 1.2)
- **Build multi-stage** no Dockerfile adiciona complexidade: stage de build com Maven + stage runtime apenas com JRE
- **Tamanho da imagem**: JRE mínima mas ainda Java 25 pode ser maior que versões LTS

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Spring Boot 4.1 + Java 25 + Maven | Versões recentes; Spring 4.1 traz Spring Security 7.1 necessário para CSP/CSRF SPA; maven.wrapper segura reprodutibilidade. | Java 25 não é LTS; imagens Docker podem não estar estáveis (validação necessária). |
| Java 21 LTS | Mais maduro; imagens Docker abundantes. | Não atende ao requisito explícito de Java 25. |
| Gradle | Sintaxe alternativa. | Maven é mais direto para Flyway; usuário pediu Maven. |

## Mais informações

- **Design**: [`setup-java-project/design.md` §Decisions 1–5](../../openspec/changes/archive/2026-09-23-setup-java-project/design.md)
- **POM**: [`pom.xml`](../../pom.xml)
- **Dockerfile**: [`Dockerfile`](../../Dockerfile) (multi-stage, stage build + stage runtime)

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
