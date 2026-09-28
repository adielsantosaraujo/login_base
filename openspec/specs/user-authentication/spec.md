# user-authentication Specification

## Purpose

Define como o usuário se autentica na aplicação com e-mail ou celular e senha, mantendo o estado em sessão HTTP, como as rotas são protegidas, como as sessões são registradas e como o administrador inicial é disponibilizado.

## Requirements

### Requirement: Página de login pública
A aplicação SHALL servir em `GET /login` uma página de login renderizada no servidor, acessível sem autenticação, com um campo único "E-mail ou celular", um campo de senha e proteção contra CSRF. A página MUST exibir uma mensagem de erro genérica após falha de login e uma mensagem de confirmação após logout.

#### Scenario: Acesso anônimo à página de login
- **WHEN** um visitante não autenticado acessa `GET /login`
- **THEN** a resposta é HTTP 200 com o formulário contendo o campo "E-mail ou celular" e o campo de senha

#### Scenario: Mensagem após falha
- **WHEN** a página é exibida após uma tentativa de login malsucedida
- **THEN** ela mostra a mensagem "Usuário ou senha inválidos."

#### Scenario: Mensagem após logout
- **WHEN** a página é exibida logo após um logout
- **THEN** ela mostra a mensagem "Você saiu do sistema."

### Requirement: Autenticação por e-mail ou celular e senha
A aplicação SHALL autenticar o usuário por formulário enviado com `POST /login`, aceitando como identificador o e-mail ou o celular cadastrado e comparando a senha com o hash armazenado. O identificador informado MUST ser interpretado assim, após remover espaços nas pontas: se contém `@`, é um e-mail, comparado sem distinção entre maiúsculas e minúsculas; caso contrário, é um celular, do qual todos os caracteres não numéricos são descartados e o resultado MUST ter exatamente 11 dígitos para ser buscado. Qualquer que seja o identificador usado, o usuário autenticado MUST ser identificado pelo seu e-mail (por exemplo, na auditoria e no registro de sessões). Em caso de sucesso, MUST criar uma sessão HTTP autenticada e redirecionar para a página inicial segura (ou para a página protegida originalmente solicitada). Em caso de falha, MUST redirecionar de volta ao login com erro, sem revelar se o e-mail ou celular existe, se a senha está errada ou se o usuário está sem perfil vigente. Requisições `POST /login` sem token CSRF válido MUST ser rejeitadas.

#### Scenario: Credenciais válidas
- **WHEN** um usuário com perfil vigente envia e-mail e senha corretos pelo formulário
- **THEN** uma sessão autenticada é criada
- **AND** o usuário é redirecionado para `/`

#### Scenario: E-mail com caixa diferente
- **WHEN** o usuário cadastrado como `ana@exemplo.com` envia ` Ana@Exemplo.com ` com a senha correta
- **THEN** a autenticação é bem-sucedida

#### Scenario: Senha incorreta
- **WHEN** um usuário envia e-mail existente com senha incorreta
- **THEN** nenhuma sessão autenticada é criada
- **AND** o usuário é redirecionado para `/login?error`

#### Scenario: E-mail inexistente
- **WHEN** alguém envia um e-mail não cadastrado
- **THEN** o resultado é o mesmo da senha incorreta

#### Scenario: Login pelo celular
- **WHEN** o usuário `ana@exemplo.com`, com celular `11987654321` e perfil vigente, envia `11987654321` e a senha correta
- **THEN** a autenticação é bem-sucedida e ele é redirecionado para `/`
- **AND** o usuário autenticado é identificado como `ana@exemplo.com`

#### Scenario: Celular digitado com máscara
- **WHEN** o mesmo usuário envia `(11) 98765-4321` e a senha correta
- **THEN** a autenticação é bem-sucedida

#### Scenario: Celular inexistente ou incompleto
- **WHEN** alguém envia um celular não cadastrado ou com quantidade de dígitos diferente de 11
- **THEN** o resultado é o mesmo da senha incorreta

#### Scenario: Retorno à página solicitada
- **WHEN** um visitante anônimo tenta acessar uma página protegida, é levado ao login e se autentica com sucesso
- **THEN** ele é redirecionado para a página originalmente solicitada

#### Scenario: Sem token CSRF
- **WHEN** um `POST /login` é enviado sem token CSRF válido
- **THEN** a requisição é rejeitada e nenhuma sessão autenticada é criada

### Requirement: Perfis vigentes e permissões como autoridades
Ao autenticar, o usuário SHALL receber como autoridades `ROLE_<nome do perfil>` para cada perfil com vínculo vigente na data atual e o nome de cada permissão associada a esses perfis. Um usuário sem nenhum perfil vigente MUST ser tratado como conta desabilitada e MUST NOT conseguir se autenticar.

#### Scenario: Autoridades do usuário
- **WHEN** um usuário com vínculo vigente ao perfil `ADMIN`, que possui a permissão `USUARIO_LER`, se autentica
- **THEN** suas autoridades incluem `ROLE_ADMIN` e `USUARIO_LER`

#### Scenario: Perfil expirado não concede autoridade
- **WHEN** um usuário tem um vínculo vigente ao perfil `ADMIN` e um vínculo expirado ao perfil `FINANCEIRO`
- **THEN** suas autoridades incluem `ROLE_ADMIN` e não incluem `ROLE_FINANCEIRO`

#### Scenario: Usuário sem perfil vigente
- **WHEN** um usuário cujos vínculos estão todos expirados envia e-mail e senha corretos
- **THEN** a autenticação falha com a mesma mensagem genérica de credenciais inválidas

### Requirement: Proteção de rotas
Toda rota da aplicação SHALL exigir autenticação, exceto a página de login, o processamento do login e os recursos estáticos públicos (CSS, JS, imagens). Um visitante não autenticado que acessa uma rota protegida MUST ser redirecionado para `/login`. Requisições anônimas a endpoints de API (`/api/**`) MUST receber HTTP 401 sem redirecionamento, e MUST NOT ser guardadas como página de retorno do login — após autenticação, o usuário é redirecionado para `/` (página inicial), não para um endpoint de API.

#### Scenario: Acesso anônimo à página inicial
- **WHEN** um visitante não autenticado acessa `GET /`
- **THEN** ele é redirecionado para `/login`

#### Scenario: Recurso estático público
- **WHEN** um visitante não autenticado requisita um arquivo em `/css/`
- **THEN** o arquivo é servido sem redirecionamento para o login

#### Scenario: Acesso anônimo a endpoint de API
- **WHEN** um visitante não autenticado acessa `GET /api/jogo/vila`
- **THEN** a resposta é HTTP 401 sem redirecionamento

#### Scenario: Retorno à página inicial após login de acesso a API
- **WHEN** um visitante anônimo tenta acessar um endpoint de API, é negado com 401, faz login e se autentica com sucesso
- **THEN** ele é redirecionado para `/` (página inicial), não para a API

### Requirement: Página inicial segura
A aplicação SHALL servir em `GET /` a página da SPA do frontend (o `index.html` gerado pelo build do frontend, entregue pela view Thymeleaf `sistema/seguro/index`), acessível apenas a usuários autenticados. O placeholder "Seja bem vindo" deixa de existir.

#### Scenario: Usuário autenticado acessa a página inicial
- **WHEN** um usuário autenticado acessa `GET /`
- **THEN** a resposta é HTTP 200 com a view `sistema/seguro/index` (a SPA)

#### Scenario: Visitante anônimo acessa a página inicial
- **WHEN** um visitante não autenticado acessa `GET /`
- **THEN** ele é redirecionado para `/login`

### Requirement: Logout
A aplicação SHALL encerrar a autenticação com `POST /logout` (com token CSRF), invalidando a sessão HTTP, removendo o cookie de sessão e redirecionando para `/login?logout`.

#### Scenario: Logout bem-sucedido
- **WHEN** um usuário autenticado envia `POST /logout` com token CSRF válido
- **THEN** a sessão HTTP é invalidada
- **AND** ele é redirecionado para `/login?logout`
- **AND** um novo acesso a `/` redireciona para `/login`

### Requirement: Sessão HTTP segura
A autenticação SHALL ser mantida em sessão HTTP no servidor. O identificador da sessão MUST ser trocado no momento do login (proteção contra fixação de sessão), o cookie de sessão MUST ser `HttpOnly`, e a sessão MUST expirar após 30 minutos de inatividade, tempo configurável.

#### Scenario: Troca do identificador no login
- **WHEN** um visitante com uma sessão anônima existente se autentica com sucesso
- **THEN** o identificador de sessão após o login é diferente do anterior

#### Scenario: Expiração por inatividade
- **WHEN** um usuário autenticado fica sem fazer requisições além do tempo de inatividade configurado
- **THEN** a próxima requisição a uma rota protegida é redirecionada para `/login`

### Requirement: Registro das sessões autenticadas
A cada login bem-sucedido a aplicação SHALL gravar um registro em `sessoes` com o usuário, `data_inicio` igual ao momento do login, o IP de origem da requisição, o dispositivo (cabeçalho `User-Agent`) e um `token` derivado da sessão HTTP. Quando a sessão HTTP termina — por logout ou expiração — a aplicação MUST preencher `data_fim` do registro correspondente. Falha ao gravar o registro MUST NOT impedir nem desfazer o login. Na inicialização da aplicação, registros ainda abertos MUST ser encerrados, pois as sessões HTTP anteriores não sobrevivem ao reinício.

#### Scenario: Registro criado no login
- **WHEN** um usuário se autentica a partir do IP `10.0.0.5` com `User-Agent` `Mozilla/5.0`
- **THEN** existe um registro em `sessoes` para esse usuário com `ip` `10.0.0.5`, `dispositivo` `Mozilla/5.0`, `data_inicio` preenchida e `data_fim` nula

#### Scenario: Registro encerrado no logout
- **WHEN** o usuário faz logout
- **THEN** o registro de `sessoes` correspondente tem `data_fim` preenchida

#### Scenario: Registro encerrado na expiração
- **WHEN** a sessão HTTP do usuário expira por inatividade
- **THEN** o registro de `sessoes` correspondente tem `data_fim` preenchida

#### Scenario: Registros abertos no reinício
- **WHEN** a aplicação é reiniciada e existem registros de `sessoes` com `data_fim` nula
- **THEN** esses registros passam a ter `data_fim` preenchida

### Requirement: Administrador inicial
Na inicialização, a aplicação SHALL garantir a existência do perfil `ADMIN`. Quando as variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_PASSWORD` estiverem definidas e não houver usuário com esse e-mail, a aplicação MUST criar esse usuário com a senha em hash e vínculo vigente ao perfil `ADMIN` a partir da data atual, sem data final. Se o usuário já existir, MUST NOT alterá-lo. Se `ADMIN_PASSWORD` não estiver definida, MUST NOT criar o usuário e MUST registrar um aviso no log.

#### Scenario: Primeira inicialização com variáveis
- **WHEN** a aplicação inicia com `ADMIN_EMAIL=admin@exemplo.com` e `ADMIN_PASSWORD=Troque123` e esse usuário não existe
- **THEN** o usuário `admin@exemplo.com` é criado com vínculo vigente ao perfil `ADMIN`
- **AND** é possível autenticar com essas credenciais

#### Scenario: Reinicialização
- **WHEN** a aplicação reinicia com as mesmas variáveis e o usuário já existe, inclusive com senha alterada
- **THEN** nenhum usuário é criado ou alterado

#### Scenario: Sem senha definida
- **WHEN** a aplicação inicia sem `ADMIN_PASSWORD`
- **THEN** nenhum usuário administrador é criado
- **AND** um aviso é registrado no log

### Requirement: Proteção CSRF para clientes JavaScript
A aplicação MUST proteger requisições POST, PUT, DELETE a endpoints de API (`/api/**`) contra ataques CSRF por meio de um token enviado por JavaScript. O token MUST ser disponibilizado em um cookie `XSRF-TOKEN` (legível por JavaScript, não `HttpOnly`) e aceito no cabeçalho `X-XSRF-TOKEN` de qualquer requisição autenticada. O formulário de login Thymeleaf continua usando o parâmetro oculto `_csrf` como antes. Requisições autenticadas sem o token válido MUST ser rejeitadas com HTTP 403.

#### Scenario: Token CSRF em cookie
- **WHEN** um navegador carrega a página raiz (SPA)
- **THEN** a resposta inclui um cookie `XSRF-TOKEN` legível por JavaScript

#### Scenario: Requisição POST autenticada sem token
- **WHEN** um usuário autenticado envia `POST /api/jogo/predios/SERRARIA/melhorar` sem o cabeçalho `X-XSRF-TOKEN`
- **THEN** a resposta é HTTP 403

#### Scenario: Requisição POST autenticada com token válido
- **WHEN** um usuário autenticado envia `POST /api/jogo/predios/SERRARIA/melhorar` com o cabeçalho `X-XSRF-TOKEN` igual ao valor do cookie
- **THEN** a requisição é processada normalmente

#### Scenario: Formulário de login com _csrf
- **WHEN** um visitante anônimo envia `POST /login` com o parâmetro `_csrf` (formulário Thymeleaf)
- **THEN** a autenticação é bem-sucedida (proteção CSRF tradicional funciona)
