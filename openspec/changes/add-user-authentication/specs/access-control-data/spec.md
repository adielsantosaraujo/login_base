# Spec Delta

## Purpose

Define o modelo persistido de controle de acesso — usuários, perfis, permissões, vínculos com vigência e registro de sessões — e os campos de auditoria comuns a todas essas tabelas.

## ADDED Requirements

### Requirement: Tabela de usuários
O banco SHALL ter a tabela `usuarios` com as colunas `id` (chave primária gerada), `nome` (obrigatório), `email` (obrigatório), `senha` (obrigatório) e `celular` (opcional). O `email` MUST ser único sem distinção entre maiúsculas e minúsculas. O `celular`, quando informado, MUST conter exatamente 11 dígitos numéricos (DDD com 2 dígitos + número com 9 dígitos), sem máscara, espaços ou outros caracteres, e MUST ser único entre os usuários; vários usuários MAY ficar sem celular. A coluna `senha` MUST armazenar apenas o hash da senha, nunca o texto puro.

#### Scenario: E-mail duplicado com caixa diferente
- **WHEN** já existe um usuário com e-mail `ana@exemplo.com` e se tenta gravar outro usuário com e-mail `Ana@Exemplo.com`
- **THEN** a gravação é rejeitada por violar a unicidade do e-mail

#### Scenario: Celular válido
- **WHEN** se grava um usuário com celular `11987654321`
- **THEN** a gravação é aceita e o celular fica armazenado como `11987654321`

#### Scenario: Celular duplicado
- **WHEN** já existe um usuário com celular `11987654321` e se tenta gravar outro usuário com o mesmo celular
- **THEN** a gravação é rejeitada por violar a unicidade do celular

#### Scenario: Celular com quantidade de dígitos diferente de 11
- **WHEN** se tenta gravar um usuário com celular `1198765432` (10 dígitos) ou `119876543210` (12 dígitos)
- **THEN** a gravação é rejeitada

#### Scenario: Celular com máscara ou letras
- **WHEN** se tenta gravar um usuário com celular `(11) 98765-4321` ou `1198765432a`
- **THEN** a gravação é rejeitada

#### Scenario: Usuários sem celular
- **WHEN** já existe um usuário sem celular e se grava outro usuário também sem celular
- **THEN** a gravação é aceita

#### Scenario: Senha gravada como hash
- **WHEN** um usuário é criado com a senha `segredo123`
- **THEN** o valor gravado em `usuarios.senha` é diferente de `segredo123`
- **AND** o valor identifica o algoritmo de hash usado

### Requirement: Tabelas de perfis e permissões
O banco SHALL ter a tabela `perfis` com as colunas `id`, `nome` (obrigatório e único) e `descricao` (opcional), e a tabela `permissoes` com as colunas `id`, `nome` (obrigatório e único) e `descricao` (opcional).

#### Scenario: Nome de perfil duplicado
- **WHEN** já existe o perfil `ADMIN` e se tenta gravar outro perfil com nome `ADMIN`
- **THEN** a gravação é rejeitada

#### Scenario: Nome de permissão duplicado
- **WHEN** já existe a permissão `USUARIO_LER` e se tenta gravar outra permissão com nome `USUARIO_LER`
- **THEN** a gravação é rejeitada

### Requirement: Vínculo usuário-perfil com vigência
O banco SHALL ter a tabela `usuario_rel_perfis` com as colunas `id`, `usuario_id` (referência obrigatória a `usuarios`), `perfil_id` (referência obrigatória a `perfis`), `data_inicial` (obrigatória) e `data_final` (opcional). Um vínculo MUST ser considerado vigente em uma data D quando `data_inicial <= D` e (`data_final` é nula ou `data_final >= D`). `data_final`, quando informada, MUST NOT ser anterior a `data_inicial`.

#### Scenario: Vínculo sem data final
- **WHEN** um vínculo tem `data_inicial` igual a ontem e `data_final` nula
- **THEN** o vínculo é vigente hoje

#### Scenario: Vínculo expirado
- **WHEN** um vínculo tem `data_final` igual a ontem
- **THEN** o vínculo não é vigente hoje

#### Scenario: Vínculo futuro
- **WHEN** um vínculo tem `data_inicial` igual a amanhã
- **THEN** o vínculo não é vigente hoje

#### Scenario: Data final anterior à inicial
- **WHEN** se tenta gravar um vínculo com `data_final` anterior a `data_inicial`
- **THEN** a gravação é rejeitada

#### Scenario: Referência inexistente
- **WHEN** se tenta gravar um vínculo com `usuario_id` ou `perfil_id` que não existe
- **THEN** a gravação é rejeitada

### Requirement: Vínculo perfil-permissão
O banco SHALL ter a tabela `perfis_rel_permissoes` com as colunas `id`, `perfil_id` (referência obrigatória a `perfis`) e `permissao_id` (referência obrigatória a `permissoes`). O par (`perfil_id`, `permissao_id`) MUST ser único.

#### Scenario: Permissão repetida no mesmo perfil
- **WHEN** o perfil `ADMIN` já tem a permissão `USUARIO_LER` e se tenta vinculá-la novamente
- **THEN** a gravação é rejeitada

### Requirement: Registro de sessões
O banco SHALL ter a tabela `sessoes` com as colunas `id`, `usuario_id` (referência obrigatória a `usuarios`), `data_inicio` (data e hora, obrigatória), `data_fim` (data e hora, opcional), `token` (obrigatório e único), `ip` (opcional) e `dispositivo` (opcional). Uma sessão com `data_fim` nula MUST ser considerada aberta. O `token` MUST NOT conter o identificador de sessão HTTP em texto puro.

#### Scenario: Sessão aberta
- **WHEN** um registro de `sessoes` tem `data_fim` nula
- **THEN** a sessão é considerada aberta

#### Scenario: Token não reutilizável
- **WHEN** um registro de `sessoes` é gravado para uma sessão HTTP
- **THEN** o valor de `token` não é igual ao identificador da sessão HTTP

### Requirement: Campos de auditoria em todas as tabelas
Todas as tabelas `usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes` e `sessoes` SHALL ter as colunas `criado_em`, `criado_por`, `alterado_em` e `alterado_por`. Na inclusão, `criado_em` e `criado_por` MUST ser preenchidos automaticamente; em toda inclusão e alteração, `alterado_em` e `alterado_por` MUST ser preenchidos automaticamente. `criado_por`/`alterado_por` MUST conter o e-mail do usuário autenticado que fez a operação, ou `sistema` quando não há usuário autenticado. `criado_em` e `criado_por` MUST NOT mudar em alterações posteriores.

#### Scenario: Inclusão sem usuário autenticado
- **WHEN** um registro é incluído por um processo sem usuário autenticado (por exemplo, a carga inicial)
- **THEN** `criado_por` e `alterado_por` são `sistema`
- **AND** `criado_em` e `alterado_em` têm a data e hora da inclusão

#### Scenario: Alteração por usuário autenticado
- **WHEN** o usuário `ana@exemplo.com`, autenticado, altera um registro criado anteriormente por `sistema`
- **THEN** `alterado_por` passa a ser `ana@exemplo.com` e `alterado_em` a data e hora da alteração
- **AND** `criado_por` e `criado_em` permanecem com os valores originais

### Requirement: Esquema versionado por migrações
O esquema dessas tabelas SHALL ser criado e evoluído por migrações versionadas aplicadas automaticamente na inicialização da aplicação. A aplicação MUST NOT criar ou alterar tabelas a partir do mapeamento das entidades, e MUST falhar na inicialização se o mapeamento não corresponder ao esquema.

#### Scenario: Banco vazio
- **WHEN** a aplicação inicia apontando para um banco sem as tabelas
- **THEN** as migrações criam todas as tabelas de controle de acesso
- **AND** a aplicação conclui a inicialização

#### Scenario: Reinício com banco já migrado
- **WHEN** a aplicação é reiniciada sobre um banco já migrado
- **THEN** nenhuma migração é reaplicada e os dados existentes são preservados
