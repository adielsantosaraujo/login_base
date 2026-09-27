# 0022 — Testes de Integração Contra Postgres do Compose (Sem Testcontainers)

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-26 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/), [`add-city-builder-game`](../../openspec/changes/archive/2026-09-27-add-city-builder-game/) |

## Contexto e problema

Testes de serviço (`@DataJpaTest`, `@SpringBootTest`) precisam de banco de dados real (não embarcado). Há duas estratégias: (1) **Testcontainers** (container Docker para cada suite), (2) **banco compartilhado** (Postgres já rodando, compartilhado). Testcontainers é isolado mas custoso (startup lento); banco compartilhado é rápido mas exige setup prévio.

## Direcionadores da decisão

- Velocidade: suite de 239 testes deve executar em segundos
- Setup: desenvolvedor já faz `make up` (Postgres roda)
- Isolamento: testes sequenciais (com `@Transactional(rollback=true)`), não paralelos
- CI/CD: Não existem pipelines configurados; quando existirem, decidir Testcontainers
- Simplicidade: nenhuma dependência extra

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Banco compartilhado (Postgres do compose)** | Teste requer `make up` prévio; testes rollback automaticamente; sem dependência Testcontainers. |
| Testcontainers | Cada suite sobe container (isolado). Rejeitada: startup lento (~10s/suite); para 30 testes de classes = overhead. |
| H2 embarcado | Em-memory, rápido. Rejeitada: H2 != Postgres (DDL, tipos, constraints diferem). |

## Resultado da decisão

Adotou-se **banco compartilhado (Postgres do compose)**:

1. **Pré-requisito**:
   - `make up` deve estar rodando antes de `./mvnw test`
   - Não há profile de teste; os testes usam `application.properties` (banco `login_base` do compose, `DB_HOST` padrão `localhost`)

2. **Configuração**: não há `application-test.properties`. Os testes usam `application.properties`:
   - `spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/login_base`
   - Os `@DataJpaTest` usam `@AutoConfigureTestDatabase(replace = Replace.NONE)` para não substituir o datasource
   - Flyway aplica V1–V3 e o Hibernate valida

3. **Banco de testes**:
   - Compartilhado: Postgres rodando no compose com banco `login_base`
   - Flyway recria schema (ou trunca ao invés)

4. **Transações**:
   - `@DataJpaTest` faz rollback por padrão
   - Testes de concorrência usam `@Transactional(propagation = NOT_SUPPORTED)` e limpam dados explicitamente
   - `@SpringBootTest` não faz rollback por padrão

5. **Tipos de teste**:
   - **Unitários**: sem banco (Calculadora, Gerador de Loot, Motor)
   - **Camada JPA** (`@DataJpaTest`): apenas repositórios + JPA
   - **Camada web** (`@WebMvcTest`): controller + Security (serviços mockados)
   - **Integração** (`@SpringBootTest`): stack completo, Postgres necessário

### Consequências positivas
- **Rápido**: Postgres já roda; sem startup de container
- **Simples**: nenhuma dependência extra; testes standard
- **Desenvolvimento**: desenvolvedor já faz `make up`; mesma base de dados de dev

### Consequências negativas
- **Setup manual**: `make up` é pré-requisito (não automático via CI)
- **Isolamento limitado**: banco compartilhado, não por suite (mitigado: rollback automático)
- **CI/CD futura**: quando pipelines existirem, será necessário refatorar para Testcontainers
- **Limpeza incompleta**: testes podem deixar dados em `AUTOCOMMIT=false` (mitigado: rollback padrão)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Banco compartilhado | Rápido; simples; não requer dependência extra. | Setup manual; isolamento limitado; refatoração futura. |
| Testcontainers | Isolado; reprodutível. | Startup lento; dependência extra; overkill para projeto pequeno. |
| H2 embarcado | Não requer setup. | H2 != Postgres (ddl, tipos diferem). |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §13](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md), [`add-city-builder-game/design.md` §20–21](../../openspec/changes/archive/2026-09-27-add-city-builder-game/design.md)
- **Configuração**: [`src/main/resources/application.properties`](../../src/main/resources/application.properties) (os testes usam o mesmo datasource `login_base` do compose; `@AutoConfigureTestDatabase(replace = Replace.NONE)` nos `@DataJpaTest`)
- **POM**: [`pom.xml`](../../pom.xml) — sem Testcontainers
- **Testes**:
  - Unitários: `jogo/economia/CalculadoraProducaoTest.java`
  - JPA: `jogo/economia/VilaServiceTest.java` (`@DataJpaTest`)
  - Web: `jogo/api/VilaControllerWebMvcTest.java` (`@WebMvcTest`)
  - Integração: `LoginBaseApplicationTests.java` (`@SpringBootTest`)
- **Como rodar**: `make up` (se não rodando) + `./mvnw test`

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
