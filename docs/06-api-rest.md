# 06 — API REST

| Campo | Valor |
|---|---|
| Versão | 1.1.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` + change `add-frontend-build` implementada |
| Modelo/norma | Referência REST em Markdown (estilo OpenAPI) |
| Público | Desenvolvedores, QA |
| Fontes | `src/main/java/com/example/loginbase/jogo/api/*.java`, `src/main/java/com/example/loginbase/seguranca/SecurityConfig.java`, `src/main/java/com/example/loginbase/web/PaginaController.java`, `frontend/src/api/jogo.ts` |

> Parte da [documentação do login_base](README.md). Especifica os endpoints da API REST do jogo, formatos de requisição/resposta, tratamento de erros, autenticação e CSRF.

---

## Índice

1. [Autenticação e Sessões](#autenticação-e-sessões)
2. [Rotas de Página (Não-API)](#rotas-de-página-não-api)
3. [Endpoints de Jogo](#endpoints-de-jogo)
4. [Modelo de Erros](#modelo-de-erros)
5. [Códigos de Erro Específicos](#códigos-de-erro-específicos)

---

## Autenticação e Sessões

### Formulário de Login

**Método:** `POST`  
**URL:** `/login`  
**Content-Type:** `application/x-www-form-urlencoded`  
**Autenticação:** Não requerida (pública)  
**CSRF:** Parâmetro oculto `_csrf` obrigatório (fornecido pela página de login)

**Parâmetros:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `login` | string | E-mail ou número de celular do usuário |
| `senha` | string | Senha do usuário |
| `_csrf` | string | Token CSRF do formulário |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `302` | Redirecionamento: sucesso → `/` (página protegida), falha → `/login?error` |

**Comportamento:**
- Sucesso: Cria sessão HTTP (cookie `JSESSIONID` HttpOnly, SameSite=Lax; Secure só se `SESSION_COOKIE_SECURE=true`)
- Falha: Redirecionamento para `/login?error` (credenciais inválidas)
- Timeout: Sessão expira após inatividade (`SESSION_TIMEOUT`, padrão 30m, em `application.properties`)

---

### Logout

**Método:** `POST` (com CSRF ativo, GET /logout não encerra a sessão)  
**URL:** `/logout`  
**Autenticação:** Não exigida (`permitAll`)  
**CSRF:** Obrigatório (campo `_csrf` ou header `X-XSRF-TOKEN`)

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `302` | Redirecionamento → `/login?logout` |

**Comportamento:**
- Invalida a sessão HTTP
- Remove cookie `JSESSIONID`
- `SessaoEncerradaListener` registra término em `sessoes.data_fim`

---

### Verificação de Autenticação (Implícita)

Todos os endpoints `/api/**` verificam se o usuário está autenticado:
- **Anônimo:** HTTP `401 Unauthorized` (sem corpo, sem redirecionamento)
- **Autenticado:** Requisição prossegue com `Authentication` disponível

---

## Rotas de Página (Não-API)

Rotas mapeadas por `PaginaController` para servir pages/views (não JSON).

### GET /login

**Método:** `GET`  
**URL:** `/login`  
**Autenticação:** Não requerida (pública)  
**CSRF:** Não aplicável

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Página de login (template Thymeleaf) |

**Comportamento:**
- Retorna template `sistema/public/login.html` com formulário (campos `login`, `senha`, `_csrf`).
- Token CSRF gerado automaticamente por Thymeleaf.

---

### GET / — Página Inicial / SPA

**Método:** `GET`  
**URL:** `/`  
**Autenticação:** Requerida  
**CSRF:** Não aplicável (GET)

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | SPA servida (template `sistema/seguro/index.html`, gerado por `make build_front`) |
| `302` | Anônimo redirecionado para `/login` |

**Comportamento:**
- Devolve a view `sistema/seguro/index.html` (a SPA, gerada em build). Usuário anônimo é redirecionado para `/login` pelo `SecurityFilterChain`.

---

### GET /fazenda, /forja, /quartel, /masmorras, /batalhas/{id} — Rotas da SPA

> 

**Método:** `GET`  
**URL:** `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}`  
**Autenticação:** Requerida  
**CSRF:** Não aplicável (GET)

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | SPA servida (template `sistema/seguro/index.html`) — fallback do history mode |
| `302` | Anônimo redirecionado para `/login` |

**Comportamento:**
- Mapeadas em `PaginaController` com lista explícita de rotas.
- Retornam a mesma view `sistema/seguro/index.html` (index da SPA).
- O frontend Vue Router reconhece a rota original e navega internamente.
- Usuário anônimo é redirecionado para `/login`.

**Nota:** Um F5 (reload) do navegador numa rota interna (ex.: `/fazenda`) será servido pelo backend, que devolve `index.html` + assets de `/app/**`. O frontend carrega e navega para a rota original.

---

### GET /app/** — Assets da SPA

> 

**Método:** `GET`  
**URL:** `/app/**` (ex.: `/app/assets/main.js`, `/app/assets/index-<hash>.css`)  
**Autenticação:** Não requerida (pública)  
**CSRF:** Não aplicável

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Asset encontrado (JavaScript, CSS, imagens da SPA) |
| `404` | Asset não encontrado |

**Comportamento:**
- Assets servidos pelo handler estático padrão do Spring Boot (resources em `src/main/resources/static/app/`).
- Não requerem autenticação (públicos).
- Evita que o request cache salve um asset como destino pós-login.

---

## Endpoints de Jogo

### 1. Consultar Estado da Vila

**Método:** `GET`  
**URL:** `/api/jogo/vila`  
**Autenticação:** Requerida  
**CSRF:** Não aplicável (GET)

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Vila consultada com sucesso |
| `401` | Não autenticado |
| `404` | Vila não encontrada (usuário sem vila criada) |

**Response Body (200):**

```json
{
  "agora": "2026-09-27T14:30:00Z",
  "nome": "Vila do Adiel",
  "recursos": {
    "COMIDA": 500,
    "MADEIRA": 300,
    "PEDRA": 200,
    "FERRO": 100
  },
  "capacidade": {
    "COMIDA": 1000,
    "MADEIRA": 1000,
    "PEDRA": 1000,
    "FERRO": 1000
  },
  "producaoPorHora": {
    "COMIDA": 150,
    "MADEIRA": 30,
    "PEDRA": 20,
    "FERRO": 10
  },
  "masmorraNivelLiberado": 2,
  "batalhaAtivaId": null,
  "predios": [
    {
      "tipo": "CENTRO_VILA",
      "nivel": 1,
      "nivelMaximo": 5,
      "proximoNivel": {
        "nivel": 2,
        "custo": [
          { "recurso": "MADEIRA", "quantidade": 225 },
          { "recurso": "PEDRA", "quantidade": 225 }
        ],
        "tempoSegundos": 240
      },
      "ordemEmAndamento": null
    },
    {
      "tipo": "FAZENDA",
      "nivel": 2,
      "nivelMaximo": 5,
      "proximoNivel": {
        "nivel": 3,
        "custo": [
          { "recurso": "MADEIRA", "quantidade": 120 },
          { "recurso": "PEDRA", "quantidade": 60 }
        ],
        "tempoSegundos": 240
      },
      "ordemEmAndamento": {
        "concluiEm": "2026-09-27T15:00:00Z",
        "tempoRestanteSegundos": 1800
      }
    }
  ],
  "canteiros": [
    { "posicao": 1, "cultivo": "TRIGO", "producaoComidaPorHora": 20 },
    { "posicao": 2, "cultivo": "MILHO", "producaoComidaPorHora": 30 }
  ],
  "sementes": {
    "MILHO": 5,
    "BATATA": 2
  },
  "itens": [
    {
      "id": 101,
      "modelo": "ESPADA",
      "nivel": 2,
      "origem": "FORJA",
      "status": "DISPONIVEL",
      "ataque": 10,
      "defesa": 0,
      "alcance": 1
    }
  ],
  "unidades": [
    {
      "id": 201,
      "tipo": "SOLDADO",
      "status": "DISPONIVEL",
      "hp": 30,
      "ataque": 8,
      "defesa": 5,
      "alcance": 1,
      "movimento": 3,
      "armaId": 101,
      "armaduraId": 102
    }
  ],
  "capacidadeExercito": 9,
  "ordens": [
    {
      "id": 301,
      "categoria": "CONSTRUCAO",
      "alvo": "FAZENDA",
      "nivel": 3,
      "quantidade": 1,
      "iniciadaEm": "2026-09-27T13:00:00Z",
      "concluiEm": "2026-09-27T15:00:00Z"
    }
  ]
}
```

---

### 2. Obter Catálogo de Regras

**Método:** `GET`  
**URL:** `/api/jogo/catalogo`  
**Autenticação:** Requerida  
**CSRF:** Não aplicável (GET)

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Catálogo obtido com sucesso |
| `401` | Não autenticado |

**Response Body (200) — Amostra:**

```json
{
  "predios": {
    "CENTRO_VILA": [
      {
        "nivel": 1,
        "custo": [
          { "recurso": "MADEIRA", "quantidade": 150 },
          { "recurso": "PEDRA", "quantidade": 150 }
        ],
        "tempoSegundos": 120
      },
      {
        "nivel": 2,
        "custo": [
          { "recurso": "MADEIRA", "quantidade": 225 },
          { "recurso": "PEDRA", "quantidade": 225 }
        ],
        "tempoSegundos": 240
      }
    ],
    "FAZENDA": [ ... ]
  },
  "cultivos": {
    "TRIGO": {
      "comidaPorHora": 20,
      "exigeSemente": false,
      "nivelMasmorraParaSemente": 0
    },
    "MILHO": {
      "comidaPorHora": 30,
      "exigeSemente": true,
      "nivelMasmorraParaSemente": 1
    }
  },
  "modelosItem": {
    "ESPADA": {
      "categoria": "ARMA",
      "custoBase": [
        { "recurso": "MADEIRA", "quantidade": 20 },
        { "recurso": "FERRO", "quantidade": 30 }
      ],
      "tempoBaseSegundos": 60
    },
    "LANCA": {
      "categoria": "ARMA",
      "custoBase": [
        { "recurso": "MADEIRA", "quantidade": 40 },
        { "recurso": "FERRO", "quantidade": 20 }
      ],
      "tempoBaseSegundos": 60
    },
    "ARCO": {
      "categoria": "ARMA",
      "custoBase": [
        { "recurso": "MADEIRA", "quantidade": 50 },
        { "recurso": "FERRO", "quantidade": 5 }
      ],
      "tempoBaseSegundos": 60
    },
    "ARMADURA_COURO": {
      "categoria": "ARMADURA",
      "custoBase": [
        { "recurso": "COMIDA", "quantidade": 20 },
        { "recurso": "MADEIRA", "quantidade": 10 },
        { "recurso": "FERRO", "quantidade": 5 }
      ],
      "tempoBaseSegundos": 45
    },
    "ARMADURA_FERRO": {
      "categoria": "ARMADURA",
      "custoBase": [
        { "recurso": "MADEIRA", "quantidade": 10 },
        { "recurso": "FERRO", "quantidade": 40 }
      ],
      "tempoBaseSegundos": 90
    }
  },
  "tropas": {
    "SOLDADO": {
      "armaExigida": "ESPADA",
      "hp": 30,
      "defesaBase": 1,
      "movimento": 3,
      "comida": 50,
      "tempoTreinoSegundos": 60,
      "nivelMinimoQuartel": 1
    },
    "ARQUEIRO": {
      "armaExigida": "ARCO",
      "hp": 22,
      "defesaBase": 0,
      "movimento": 3,
      "comida": 50,
      "tempoTreinoSegundos": 60,
      "nivelMinimoQuartel": 2
    },
    "LANCEIRO": {
      "armaExigida": "LANCA",
      "hp": 40,
      "defesaBase": 2,
      "movimento": 2,
      "comida": 60,
      "tempoTreinoSegundos": 75,
      "nivelMinimoQuartel": 3
    }
  },
  "inimigos": {
    "GOBLIN": {
      "hp": 12,
      "ataque": 4,
      "defesa": 2,
      "alcance": 1,
      "movimento": 2
    }
  },
  "masmorras": {
    "1": {
      "nivel": 1,
      "mapa": {
        "largura": 10,
        "altura": 10,
        "obstaculos": [
          { "x": 3, "y": 4 },
          { "x": 7, "y": 8 }
        ],
        "posicoesJogador": [
          { "x": 1, "y": 1 }
        ],
        "spawnsInimigos": {
          "GOBLIN": { "x": 8, "y": 8 },
          "ORC": { "x": 9, "y": 9 }
        }
      },
      "composicaoInimigos": ["GOBLIN", "ORC"]
    }
  }
}
```

---

### 3. Iniciar Batalha (Masmorra)

**Método:** `POST`  
**URL:** `/api/jogo/masmorras/{nivel}/batalhas`  
**Autenticação:** Requerida  
**CSRF:** Requerido (token XSRF-TOKEN em cabeçalho X-XSRF-TOKEN)  
**Content-Type:** `application/json`

**Path Variables:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `nivel` | int | Nível da masmorra (1..5) |

**Request Body:**

```json
{
  "unidadeIds": [201, 202, 203]
}
```

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `unidadeIds` | array[long] | 1..4 elementos, não vazio | IDs das unidades do esquadrão |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `201` | Batalha iniciada com sucesso |
| `400` | Parâmetro inválido (esquadrão vazio ou > 4 unidades, enum inválido) |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Vila não encontrada |
| `422` | `BATALHA_EM_ANDAMENTO`, `MASMORRA_BLOQUEADA`, `UNIDADE_INDISPONIVEL`, `ESQUADRAO_INVALIDO` (ver [Códigos de Erro Específicos](#códigos-de-erro-específicos)) |

**Response Body (201):**

```json
{
  "id": 1001,
  "masmorraNivel": 1,
  "status": "EM_ANDAMENTO",
  "turno": 1,
  "turnoMaximo": 30,
  "largura": 8,
  "altura": 8,
  "obstaculos": [
    { "x": 3, "y": 2 },
    { "x": 4, "y": 2 },
    { "x": 5, "y": 3 },
    { "x": 6, "y": 3 },
    { "x": 2, "y": 5 },
    { "x": 3, "y": 5 },
    { "x": 6, "y": 6 },
    { "x": 7, "y": 6 }
  ],
  "combatentes": [
    {
      "id": "J1",
      "lado": "JOGADOR",
      "unidadeId": 201,
      "tipo": "SOLDADO",
      "x": 2,
      "y": 7,
      "hp": 30,
      "hpMaximo": 30,
      "ataque": 6,
      "defesa": 3,
      "alcance": 1,
      "movimento": 3,
      "defendendo": false,
      "moveu": false,
      "agiu": false,
      "vivo": true
    },
    {
      "id": "I1",
      "lado": "INIMIGO",
      "unidadeId": null,
      "tipo": "GOBLIN",
      "x": 3,
      "y": 0,
      "hp": 15,
      "hpMaximo": 15,
      "ataque": 6,
      "defesa": 1,
      "alcance": 1,
      "movimento": 3,
      "defendendo": false,
      "moveu": false,
      "agiu": false,
      "vivo": true
    }
  ],
  "log": [
    "Batalha iniciada na masmorra nível 1.",
    "Turno 1 começou."
  ],
  "loot": null
}
```

---

### 4. Consultar Batalha

**Método:** `GET`  
**URL:** `/api/jogo/batalhas/{id}`  
**Autenticação:** Requerida  
**CSRF:** Não aplicável (GET)

**Path Variables:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `id` | long | ID da batalha |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Batalha consultada com sucesso |
| `401` | Não autenticado |
| `404` | Batalha não encontrada ou não pertence ao usuário |

**Response Body (200):** Mesmo formato de [Iniciar Batalha](#3-iniciar-batalha-masmorra) (campo `loot` preenchido se `status == VITORIA`).

---

### 5. Executar Ação em Batalha

**Método:** `POST`  
**URL:** `/api/jogo/batalhas/{id}/acoes`  
**Autenticação:** Requerida  
**CSRF:** Requerido  
**Content-Type:** `application/json`

**Path Variables:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `id` | long | ID da batalha |

**Request Body:**

```json
{
  "tipo": "ATACAR",
  "combatenteId": "J1",
  "x": null,
  "y": null,
  "alvoId": "I1",
  "turno": 1
}
```

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `tipo` | enum | `MOVER`, `ATACAR`, `ENCERRAR_TURNO`, `DEFENDER`, `RENDER` | Tipo de ação |
| `combatenteId` | string | Obrigatório (exceto ENCERRAR_TURNO/RENDER) | ID do combatente agindo |
| `x` | integer | nullable, 0..largura | Coordenada X (só para MOVER) |
| `y` | integer | nullable, 0..altura | Coordenada Y (só para MOVER) |
| `alvoId` | string | nullable | ID do alvo (só para ATACAR) |
| `turno` | integer | Obrigatório, debe ser = turno_atual | Turno da batalha (anti-desincronia) |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Ação executada com sucesso |
| `400` | Enum inválido, parâmetro faltante |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Batalha não encontrada ou de outro usuário (combatente inexistente → 422 `ACAO_INVALIDA`) |
| `409` | `TURNO_DESATUALIZADO` (turno informado ≠ atual) ou `CONFLITO` (`@Version`) |
| `422` | Violação de regra de jogo (ação inválida, movimento bloqueado, etc.) |

**Response Body (200):** Mesmo formato de [Consultar Batalha](#4-consultar-batalha), refletindo o novo estado.

---

### 6. Melhorar Prédio

**Método:** `POST`  
**URL:** `/api/jogo/predios/{tipo}/melhorar`  
**Autenticação:** Requerida  
**CSRF:** Requerido  

**Path Variables:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `tipo` | enum | `CENTRO_VILA`, `ARMAZEM`, `FAZENDA`, `SERRARIA`, `PEDREIRA`, `MINA_FERRO`, `FORJA`, `QUARTEL` |

**Request Body:** Vazio

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Prédio melhorado, vila atualizada |
| `400` | Tipo de prédio inválido |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Vila não encontrada |
| `409` | Fila ocupada (outra ordem em andamento) |
| `422` | Violação de regra (recursos insuficientes, nível máximo atingido, etc.) |

**Response Body (200):** Estado completo da vila (mesmo formato de [Consultar Estado da Vila](#1-consultar-estado-da-vila)).

---

### 7. Plantar Cultivo

**Método:** `POST`  
**URL:** `/api/jogo/canteiros/{posicao}/plantar`  
**Autenticação:** Requerida  
**CSRF:** Requerido  
**Content-Type:** `application/json`

**Path Variables:**

| Nome | Tipo | Descrição |
|------|------|-----------|
| `posicao` | int | Posição do canteiro (1..5) |

**Request Body:**

```json
{
  "cultivo": "MILHO"
}
```

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `cultivo` | enum | `TRIGO`, `MILHO`, `BATATA`, `ABOBORA_DOURADA` | Cultivo a plantar |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Cultivo plantado com sucesso |
| `400` | Posição/cultivo inválido, enum desconhecido |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Canteiro não encontrado |
| `422` | Violação de regra (semente indisponível, canteiro não existe, etc.) |

**Response Body (200):** Estado completo da vila.

---

### 8. Forjar Item

**Método:** `POST`  
**URL:** `/api/jogo/forja/ordens`  
**Autenticação:** Requerida  
**CSRF:** Requerido  
**Content-Type:** `application/json`

**Request Body:**

```json
{
  "modelo": "ESPADA",
  "nivel": 2,
  "quantidade": 1
}
```

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `modelo` | enum | `ESPADA`, `LANCA`, `ARCO`, `ARMADURA_COURO`, `ARMADURA_FERRO` | Modelo de item |
| `nivel` | int | 1..5, <= nível máximo de FORJA | Nível de qualidade |
| `quantidade` | int | 1..`QUANTIDADE_MAXIMA_ORDEM` | Número de itens a forjar |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Ordem de forja criada |
| `400` | Parâmetro inválido (nível/quantidade fora do range) |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Vila não encontrada |
| `409` | Fila ocupada |
| `422` | Recursos insuficientes, nível de FORJA insuficiente, etc. |

**Response Body (200):** Estado completo da vila.

---

### 9. Treinar Tropa

**Método:** `POST`  
**URL:** `/api/jogo/quartel/ordens`  
**Autenticação:** Requerida  
**CSRF:** Requerido  
**Content-Type:** `application/json`

**Request Body:**

```json
{
  "tipo": "SOLDADO",
  "armaId": 101,
  "armaduraId": 102
}
```

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `tipo` | enum | `SOLDADO`, `ARQUEIRO`, `LANCEIRO` | Tipo de tropa |
| `armaId` | long | >= 1 | ID da arma a equipar |
| `armaduraId` | long | >= 1 | ID da armadura a equipar |

**Respostas:**

| Código | Descrição |
|--------|-----------|
| `200` | Ordem de treino criada |
| `400` | Parâmetro inválido |
| `401` | Não autenticado |
| `403` | Token CSRF ausente ou inválido |
| `404` | Vila, arma ou armadura não encontrada |
| `409` | Fila ocupada, capacidade do exército atingida |
| `422` | Item indisponível, nível de QUARTEL insuficiente, etc. |

**Response Body (200):** Estado completo da vila.

---

## Modelo de Erros

Todos os erros de aplicação retornam um corpo JSON padronizado:

```json
{
  "codigo": "RECURSOS_INSUFICIENTES",
  "mensagem": "Você não tem madeira suficiente para construir este nível."
}
```

### Estrutura (`ErroDto`)

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `codigo` | string | Código de erro (ex.: `RECURSOS_INSUFICIENTES`, `NAO_ENCONTRADO`, `ERRO_INTERNO`) |
| `mensagem` | string | Mensagem descritiva em português |

### Tratamento por Handler

| Tipo de Exceção | Código HTTP | Exemplo |
|-----------------|------------|---------|
| `RegraJogoException` (geral) | `422 Unprocessable Entity` | `RECURSOS_INSUFICIENTES`, `FILA_OCUPADA` |
| `RegraJogoException.TURNO_DESATUALIZADO` | `409 Conflict` | Turno da batalha não corresponde |
| `RecursoNaoEncontradoException` | `404 Not Found` | Vila, item, batalha não encontrado |
| `ObjectOptimisticLockingFailureException` | `409 Conflict` | Dados alterados por outra requisição |
| `MethodArgumentNotValidException` | `400 Bad Request` | Parâmetro obrigatório ausente, tipo inválido |
| `HttpMessageNotReadableException` | `400 Bad Request` | JSON malformado, enum desconhecido |
| Exceção genérica (não prevista) | `500 Internal Server Error` | `ERRO_INTERNO` |

---

## Códigos de Erro Específicos

### Enum `CodigoErro` — Todas as Violações de Regra de Jogo

```
RECURSOS_INSUFICIENTES
  → Tentativa de construir/forjar com recursos insuficientes
  → HTTP 422

FILA_OCUPADA
  → Tentativa de criar ordem (construção/forja/treino) enquanto fila ativa
  → HTTP 422

NIVEL_MAXIMO
  → Prédio/item já está no nível máximo (5)
  → HTTP 422

REQUISITO_NAO_ATENDIDO
  → Nível de prédio/quartel insuficiente para ação
  → HTTP 422

CANTEIRO_INEXISTENTE
  → Posição de canteiro não existe (e.g., posição > nível da FAZENDA)
  → HTTP 422

SEMENTE_INDISPONIVEL
  → Cultivo exige semente, mas estoque zerado
  → HTTP 422

ITEM_INDISPONIVEL
  → Item não existe, não é da vila ou não está DISPONIVEL
  → HTTP 422

CAPACIDADE_EXERCITO
  → Limite de tropas (capacidade do QUARTEL) atingido
  → HTTP 422

MASMORRA_BLOQUEADA
  → Nível de masmorra ainda não foi desbloqueado
  → HTTP 422

BATALHA_EM_ANDAMENTO
  → Tentativa de iniciar batalha enquanto já há uma ativa
  → HTTP 422

UNIDADE_INDISPONIVEL
  → Unidade não existe, não é da vila ou não está DISPONIVEL
  → HTTP 422

ESQUADRAO_INVALIDO
  → Esquadrão viola regra (duplicatas, > 4 unidades, etc.)
  → HTTP 422

ACAO_INVALIDA
  → Ação de batalha é inválida (movimento bloqueado, sem alvo, etc.)
  → HTTP 422

BATALHA_ENCERRADA
  → Tentativa de agir em batalha já concluída
  → HTTP 422

TURNO_DESATUALIZADO
  → Turno da requisição não corresponde ao turno atual
  → HTTP 409 (Conflict, não 422)

CONFLITO
  → Dados alterados por outro processo (optimistic lock)
  → HTTP 409

NAO_ENCONTRADO
  → Recurso genérico não encontrado
  → HTTP 404

REQUISICAO_INVALIDA
  → Parâmetro obrigatório faltante, tipo inválido, etc.
  → HTTP 400
```

---

## Notas Técnicas

### Proteção CSRF

- **Formulários (login):** Parâmetro oculto `_csrf` (gerado automaticamente por Thymeleaf)
- **Endpoints de API (POST, PUT, DELETE):** Token XSRF-TOKEN via cookie legível por JavaScript, aceito no cabeçalho `X-XSRF-TOKEN`
  - Configuração: `SecurityConfig.csrf().spa()`
  - O frontend (Vue 3/PrimeVue) lê o cookie automaticamente e inclui no cabeçalho

### Autenticação em Endpoints `/api/**`

- Acesso anônimo → `401 Unauthorized` (sem body, sem redirecionamento)
- O usuário autenticado é extraído via `Authentication.getName()` (e-mail normalizado)
- Cada requisição obtém o ID do usuário via `UsuarioAtual.id(auth)` (exception → `404`)

### Transações

Os três controllers do jogo são anotados com `@Transactional` na classe (GET e POST):
- Atomicidade: alterações são all-or-nothing
- Isolamento: lock pessimista por vila (`findByUsuarioIdParaAtualizacao`) em todas as ações; na batalha, `@Version` + número de turno

### Rate Limiting

(Não configurado nesta versão — recomendado em futuras releases)

---

**Referências:**
- `../src/main/java/com/example/loginbase/jogo/api/` — Controllers, DTOs, Request/Response
- `../src/main/java/com/example/loginbase/jogo/CodigoErro.java` — Enums de erro
- `../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java` — Configuração de segurança
- `../src/main/resources/templates/sistema/public/login.html` — Formulário de login

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: remove marcadores de previsto | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Adiciona seção "Rotas de página (não-API)" para change add-frontend-build (prevista, aberta) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
