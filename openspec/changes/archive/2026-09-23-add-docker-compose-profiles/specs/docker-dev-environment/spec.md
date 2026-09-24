# Spec Delta

## Purpose

Define como o ambiente Docker de desenvolvimento escolhe quais serviços sobem, usando profiles configuráveis por variáveis de ambiente, e como ele é levantado e derrubado com comandos curtos do Makefile.

## ADDED Requirements

### Requirement: Profile por serviço configurável por variável
Cada serviço do `docker-compose.yml` SHALL pertencer a exatamente um profile do Docker Compose, cujo nome vem de uma variável de ambiente própria do serviço: `PROFILE_DB` para o serviço `db` e `PROFILE_APP` para o serviço `app`. Quando a variável não estiver definida, o profile MUST assumir o padrão `local` para o `db` e `desativado` para o `app`.

#### Scenario: Valores padrão
- **WHEN** nenhuma das variáveis `PROFILE_DB` e `PROFILE_APP` está definida
- **THEN** o serviço `db` pertence ao profile `local`
- **AND** o serviço `app` pertence ao profile `desativado`

#### Scenario: Aplicação habilitada no profile local
- **WHEN** `PROFILE_APP=local` está definida
- **THEN** o serviço `app` pertence ao profile `local`

### Requirement: Profile ativo definido por PROFILE
O ambiente SHALL ativar ao subir apenas os serviços cujo profile é igual ao valor da variável `PROFILE`, cujo padrão é `local`. Serviços de outros profiles MUST NOT ser iniciados.

#### Scenario: Subir com os padrões
- **WHEN** o ambiente é levantado com `PROFILE=local`, `PROFILE_DB=local` e `PROFILE_APP=desativado`
- **THEN** somente o serviço `db` é iniciado
- **AND** o serviço `app` não é iniciado

#### Scenario: Subir aplicação e banco juntos
- **WHEN** o ambiente é levantado com `PROFILE=local`, `PROFILE_DB=local` e `PROFILE_APP=local`
- **THEN** os serviços `db` e `app` são iniciados

### Requirement: Variáveis de profile documentadas
O arquivo `.env.example` SHALL declarar as variáveis `PROFILE=local`, `PROFILE_DB=local` e `PROFILE_APP=desativado`, para que um `.env` copiado dele reproduza o comportamento padrão.

#### Scenario: Novo ambiente a partir do exemplo
- **WHEN** o desenvolvedor copia `.env.example` para `.env` sem alterações e levanta o ambiente
- **THEN** somente o serviço `db` é iniciado

### Requirement: Levantar o ambiente pelo Makefile
O projeto SHALL ter um `Makefile` na raiz com um alvo `up` que inicia em segundo plano os serviços do profile ativo (`PROFILE`), considerando os valores do `.env` quando ele existir. Valores passados na linha de comando do `make` MUST ter precedência sobre os do `.env`.

#### Scenario: make up com os padrões
- **WHEN** o desenvolvedor executa `make up` sem `.env` e sem variáveis na linha de comando
- **THEN** o serviço `db` é iniciado em segundo plano
- **AND** o serviço `app` não é iniciado

#### Scenario: Sobrescrever pela linha de comando
- **WHEN** o desenvolvedor executa `make up PROFILE_APP=local`
- **THEN** os serviços `db` e `app` são iniciados em segundo plano

### Requirement: Derrubar o ambiente pelo Makefile
O `Makefile` SHALL ter um alvo `down` que para e remove os containers de todos os serviços do projeto, qualquer que seja o profile de cada um, preservando os volumes de dados.

#### Scenario: Derrubar com aplicação iniciada em outro profile
- **WHEN** os serviços `db` e `app` estão rodando e o desenvolvedor executa `make down` com os valores padrão
- **THEN** os containers de `db` e `app` são parados e removidos
- **AND** o volume de dados do Postgres é mantido

### Requirement: Ajuda do Makefile
O `Makefile` SHALL ter um alvo `help`, executado por padrão quando `make` é chamado sem alvo, que lista os alvos disponíveis com uma descrição curta.

#### Scenario: make sem alvo
- **WHEN** o desenvolvedor executa `make` sem argumentos
- **THEN** são exibidos os alvos `up`, `down` e `help` com suas descrições
- **AND** nenhum container é iniciado ou parado
