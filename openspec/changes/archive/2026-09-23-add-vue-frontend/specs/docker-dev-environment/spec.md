## MODIFIED Requirements

### Requirement: Profile por serviço configurável por variável
Cada serviço do `docker-compose.yml` SHALL pertencer a exatamente um profile do Docker Compose, cujo nome vem de uma variável de ambiente própria do serviço: `PROFILE_DB` para o serviço `db`, `PROFILE_APP` para o serviço `app` e `PROFILE_FRONTEND` para o serviço `frontend`. Quando a variável não estiver definida, o profile MUST assumir o padrão `local` para o `db`, `desativado` para o `app` e `local` para o `frontend`.

#### Scenario: Valores padrão
- **WHEN** nenhuma das variáveis `PROFILE_DB`, `PROFILE_APP` e `PROFILE_FRONTEND` está definida
- **THEN** o serviço `db` pertence ao profile `local`
- **AND** o serviço `app` pertence ao profile `desativado`
- **AND** o serviço `frontend` pertence ao profile `local`

#### Scenario: Aplicação habilitada no profile local
- **WHEN** `PROFILE_APP=local` está definida
- **THEN** o serviço `app` pertence ao profile `local`

#### Scenario: Frontend desativado
- **WHEN** `PROFILE_FRONTEND=desativado` está definida
- **THEN** o serviço `frontend` pertence ao profile `desativado`

### Requirement: Profile ativo definido por PROFILE
O ambiente SHALL ativar ao subir apenas os serviços cujo profile é igual ao valor da variável `PROFILE`, cujo padrão é `local`. Serviços de outros profiles MUST NOT ser iniciados.

#### Scenario: Subir com os padrões
- **WHEN** o ambiente é levantado com `PROFILE=local`, `PROFILE_DB=local`, `PROFILE_APP=desativado` e `PROFILE_FRONTEND=local`
- **THEN** os serviços `db` e `frontend` são iniciados
- **AND** o serviço `app` não é iniciado

#### Scenario: Subir aplicação e banco juntos
- **WHEN** o ambiente é levantado com `PROFILE=local`, `PROFILE_DB=local`, `PROFILE_APP=local` e `PROFILE_FRONTEND=local`
- **THEN** os serviços `db`, `app` e `frontend` são iniciados

#### Scenario: Subir só o banco
- **WHEN** o ambiente é levantado com `PROFILE=local`, `PROFILE_DB=local`, `PROFILE_APP=desativado` e `PROFILE_FRONTEND=desativado`
- **THEN** somente o serviço `db` é iniciado

### Requirement: Variáveis de profile documentadas
O arquivo `.env.example` SHALL declarar as variáveis `PROFILE=local`, `PROFILE_DB=local`, `PROFILE_APP=desativado` e `PROFILE_FRONTEND=local`, para que um `.env` copiado dele reproduza o comportamento padrão.

#### Scenario: Novo ambiente a partir do exemplo
- **WHEN** o desenvolvedor copia `.env.example` para `.env` sem alterações e levanta o ambiente
- **THEN** os serviços `db` e `frontend` são iniciados
- **AND** o serviço `app` não é iniciado

### Requirement: Levantar o ambiente pelo Makefile
O projeto SHALL ter um `Makefile` na raiz com um alvo `up` que inicia em segundo plano os serviços do profile ativo (`PROFILE`), considerando os valores do `.env` quando ele existir. Valores passados na linha de comando do `make` MUST ter precedência sobre os do `.env`.

#### Scenario: make up com os padrões
- **WHEN** o desenvolvedor executa `make up` sem `.env` e sem variáveis na linha de comando
- **THEN** os serviços `db` e `frontend` são iniciados em segundo plano
- **AND** o serviço `app` não é iniciado

#### Scenario: Sobrescrever pela linha de comando
- **WHEN** o desenvolvedor executa `make up PROFILE_APP=local`
- **THEN** os serviços `db`, `app` e `frontend` são iniciados em segundo plano

#### Scenario: Desativar o frontend pela linha de comando
- **WHEN** o desenvolvedor executa `make up PROFILE_FRONTEND=desativado`
- **THEN** somente o serviço `db` é iniciado em segundo plano

### Requirement: Derrubar o ambiente pelo Makefile
O `Makefile` SHALL ter um alvo `down` que para e remove os containers de todos os serviços do projeto, qualquer que seja o profile de cada um, preservando os volumes de dados.

#### Scenario: Derrubar com aplicação iniciada em outro profile
- **WHEN** os serviços `db`, `app` e `frontend` estão rodando e o desenvolvedor executa `make down` com os valores padrão
- **THEN** os containers de `db`, `app` e `frontend` são parados e removidos
- **AND** o volume de dados do Postgres é mantido

#### Scenario: Derrubar serviço que não está no profile ativo
- **WHEN** o serviço `frontend` foi iniciado com `PROFILE_FRONTEND=local` e o desenvolvedor executa `make down PROFILE_FRONTEND=desativado`
- **THEN** o container de `frontend` é parado e removido
