# Tasks

## 1. Verificação do ambiente WSL/Docker

- [x] 1.1 A partir de um shell WSL (não PowerShell/cmd), rodar `docker info` e `docker compose version` e verificar que ambos respondem sem erro, confirmando que o Docker Engine é acessível de dentro do WSL
- [x] 1.2 Confirmar que a pasta do projeto é acessível no WSL em `/mnt/d/desenvolvimento/projetos/login_base` (`ls` retorna os arquivos esperados)
- [x] 1.3 Verificar quais tags de imagem Docker com JDK/JRE 25 estão disponíveis publicamente (ex.: `eclipse-temurin:25-jdk`, `eclipse-temurin:25-jre`) e decidir a base image a usar no Dockerfile; registrar a escolha final no Dockerfile (comentário não necessário, apenas a tag usada)

## 2. Estrutura do projeto Maven

- [x] 2.1 Copiar o Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`) do projeto de referência para a raiz de `login_base` e verificar que `./mvnw -v` executa sem erro dentro do WSL
- [x] 2.2 Criar `pom.xml` na raiz do projeto baseado no de referência, com `groupId=com.example`, `artifactId=login-base`, mantendo `java.version=25` e todas as dependências (data-jpa, hateoas, security, thymeleaf + thymeleaf-extras-springsecurity6, webmvc, postgresql runtime, lombok, e os starters `-test` correspondentes) e o plugin `spring-boot-maven-plugin` + `maven-compiler-plugin` com annotation processor do Lombok
- [x] 2.3 Criar `src/main/java/com/example/loginbase/LoginBaseApplication.java` (classe `@SpringBootApplication` equivalente à `DemoApplication`) e verificar que o caminho de pacotes bate com `groupId`/`artifactId`
- [x] 2.4 Criar `src/test/java/com/example/loginbase/LoginBaseApplicationTests.java` equivalente ao teste de contexto padrão do Spring Initializr
- [x] 2.5 Criar `src/main/resources/application.properties` com `spring.application.name=login-base` e as propriedades de datasource apontando para `jdbc:postgresql://db:5432/${DB_NAME}` usando variáveis de ambiente (`${DB_USER}`, `${DB_PASSWORD}`)
- [x] 2.6 Rodar `./mvnw -q -DskipTests package` dentro do WSL e verificar que o build gera `target/login-base-0.0.1-SNAPSHOT.jar` sem erros

## 3. Ambiente Docker

- [x] 3.1 Criar `Dockerfile` multi-stage: stage `build` (imagem Maven+JDK compatível com Java 25, rodando `./mvnw -q -DskipTests package`) e stage `runtime` (imagem JRE mínima copiando o jar gerado e definindo `ENTRYPOINT ["java","-jar","app.jar"]`)
- [x] 3.2 Criar `.dockerignore` excluindo `target/`, `.idea/`, `.git/` e outros artefatos que não devem ir para o contexto de build
- [x] 3.3 Criar `docker-compose.yml` com serviço `db` (Postgres, imagem `postgres:16-alpine`, variáveis `POSTGRES_DB`/`POSTGRES_USER`/`POSTGRES_PASSWORD` vindas de `.env`, porta publicada e volume nomeado para dados) e serviço `app` (build a partir do `Dockerfile`, depende de `db`, publica a porta 8080, usa variáveis de ambiente de conexão com o banco)
- [x] 3.4 Configurar um named volume Docker para o cache do Maven (`~/.m2`) usado no stage de build, para evitar que dependências sejam baixadas via bind mount lento do DrvFs a cada build (implementado como cache mount do BuildKit `--mount=type=cache,target=/root/.m2` no `Dockerfile`, que evita bind mount do DrvFs para o `.m2` a cada build)
- [x] 3.5 Criar `.env.example` documentando `DB_NAME`, `DB_USER`, `DB_PASSWORD` (sem valores reais) e adicionar `.env` ao `.gitignore`
- [x] 3.6 A partir do WSL, rodar `docker compose build` e verificar que a imagem da aplicação é construída com sucesso
- [x] 3.7 A partir do WSL, rodar `docker compose up -d` e verificar com `docker compose ps`/`curl localhost:8080` (ou endpoint equivalente exposto pela aplicação) que os serviços `db` e `app` sobem corretamente; em seguida `docker compose down`

## 4. Arquivos de suporte e Git

- [x] 4.1 Criar `.gitignore` cobrindo `target/`, `.idea/`, `*.iml`, `*.iws`, `*.ipr`, `.mvn/wrapper/maven-wrapper.jar` (exceto o necessário para o wrapper funcionar) e `.env`, seguindo o padrão do `.gitignore` do projeto de referência
- [x] 4.2 Criar `.gitattributes` equivalente ao do projeto de referência (normalização de line endings)
- [x] 4.3 Criar um `README.md` curto descrevendo: como abrir o projeto no IntelliJ (caminho Windows), como rodar `docker compose` a partir do WSL, e como rodar/depurar a aplicação localmente pelo IntelliJ contra o Postgres containerizado

## 5. Validação final

- [x] 5.1 Confirmar no IntelliJ que o `pom.xml` é reconhecido como projeto Maven válido (import automático, sem erros de dependência não resolvida)
- [x] 5.2 Com `docker compose up -d db` rodando (apenas o banco), executar a aplicação pelo IntelliJ (Run) e verificar nos logs que a conexão com o Postgres é estabelecida com sucesso
- [x] 5.3 Rodar `openspec archive setup-java-project` após confirmar que todas as tarefas acima foram concluídas, arquivando a change
