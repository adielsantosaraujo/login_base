# Implantação e operação

| Campo | Valor |
|---|---|
| Versão | 1.3.0 |
| Data | 2026-09-28 |
| Status | Vigente — baseline do commit `454ae58` + changes `add-frontend-build` e `raise-building-max-level-100` implementadas |
| Modelo/norma | Diátaxis (referência + runbook) |
| Público | Desenvolvedores, operação |
| Fontes | `docker-compose.yml`, `Dockerfile`, `frontend/Dockerfile`, `Makefile`, `.env.example`, `application.properties`, `frontend/vite.config.ts`, `scripts/build_front.py`, `PaginaController.java`, `SecurityConfig.java`, `.gitignore` |

> Parte da [documentação do login_base](README.md). Descreve o ambiente Docker, configurações, variáveis e procedimentos para levantar, manter e resolver problemas comuns.

---

## 1. Topologia e arquitetura de deployment

### Diagrama dos serviços

```
┌─────────────────────────────────────────────────────────────────┐
│  Docker Compose (Profiles)                                      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  db                                                      │  │
│  │  postgres:17-trixie                                      │  │
│  │  - Porta: 5432:5432 (mapeada)                           │  │
│  │  - Volume: db-data:/var/lib/postgresql/data             │  │
│  │  - Profile: PROFILE_DB (padrão: local)                  │  │
│  │  - Dados: login_base / login_base / login_base          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                          ↑                                       │
│                      (TCP 5432)                                  │
│                          ↑                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  app (opcional)                                          │  │
│  │  Maven 3.9 → JRE 25 (multi-stage)                       │  │
│  │  - Porta: 80:80 (mapeada)                               │  │
│  │  - Profile: PROFILE_APP (padrão: desativado)            │  │
│  │  - Lê: DB_HOST, ADMIN_EMAIL, ADMIN_PASSWORD, etc.       │  │
│  │  - (Backend roda na IDE; este container é opcional)     │  │
│  └──────────────────────────────────────────────────────────┘  │
│                          ↑                                       │
│                    (TCP 80)                                      │
│                          ↑                                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  frontend                                                │  │
│  │  Node 26 + npm 12 (Vite dev server)                     │  │
│  │  - Porta: 5173:5173 (mapeada)                           │  │
│  │  - Volume: frontend-node-modules:/app/node_modules      │  │
│  │  - Volume: ./frontend:/app (mount bind, code editing)   │  │
│  │  - Profile: PROFILE_FRONTEND (padrão: local)            │  │
│  │  - Proxy para backend: /api, /login, /logout, /css, etc.│  │
│  │  - extra_hosts: host.docker.internal:host-gateway       │  │
│  └──────────────────────────────────────────────────────────┘  │
│                          ↑                                       │
│                    (HTTP 5173)                                   │
│                          ↑                                       │
│  ┌─────────────────────────────────────────────────────┐        │
│  │  Cliente (Navegador)                                │        │
│  │  http://localhost:5173                              │        │
│  └─────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

### Imagens base

| Serviço | Imagem | Origem | Versão | Nota |
|---|---|---|---|---|
| `db` | `postgres:17-trixie` | Docker Hub | PostgreSQL 17 (Debian trixie) | — |
| `app` | Build multi-stage | Local `Dockerfile` | Maven 3.9-eclipse-temurin-25-alpine → eclipse-temurin:25-jre-alpine | — |
| `frontend` | Node build | Local `frontend/Dockerfile` | node:26-trixie-slim + npm 12 | Dev server Vite |
| `frontend-build` | Node build | Local `frontend/Dockerfile` | node:26-trixie-slim + npm 12 | Build de produção (efêmero, profile `build`) |

>  O serviço `frontend-build` é um serviço efêmero que executa o build de produção (`npm run build`) via `docker compose run --rm --build frontend-build`. Volumes: mount bind `./frontend:/app`, volume anônimo `/app/node_modules` descartado após `--rm`.

---

## 2. Perfis do Compose

Docker Compose ativa serviços por **profiles**. Variáveis controlam qual profile é ligado.

### Variáveis de controle

| Variável | Padrão | Valores | Efeito |
|---|---|---|---|
| `PROFILE` | `local` | `local`, outro | Profile geral ativado por `make up` |
| `PROFILE_DB` | `local` | `local`, `desativado` | Serviço `db` (sempre use `local` para testes) |
| `PROFILE_APP` | `desativado` | `local`, `desativado` | Serviço `app` (manter `desativado` se backend roda na IDE) |
| `PROFILE_FRONTEND` | `local` | `local`, `desativado` | Serviço `frontend` (use `local` para dev com Vite) |
| `SERVER_PORT` | `8080` | número | Porta backend (Spring `server.port`; não repassada ao serviço `app`; veja D-16 abaixo) |

**Nota D-16 — Porta do backend:** `application.properties` define `server.port=${SERVER_PORT:8080}` com padrão 8080; `.env.example` tem `SERVER_PORT=8080`; o serviço Docker `app` não recebe esta variável e mapeia `"80:80"` (port host 80 → container port 80) → app em container provavelmente inacessível no host. Inconsistência pré-existente, correção de código fora desta change. Solução: IDE roda na porta 80 (com privilégios); container seria em 8080 ou com `SERVER_PORT` passado ao compose (futuro).

### Combinações de uso

| Cenário | Comando | Resultado |
|---|---|---|
| **Dev padrão** (IDE + Docker) | `make up` | `db` + `frontend` sobem; `app` não sobe |
| **Dev com app container** | `PROFILE_APP=local make up` | `db` + `app` + `frontend` sobem (3 serviços) |
| **Só banco** | `PROFILE_FRONTEND=desativado make up` | Apenas `db` sobe (para testes sem UI) |
| **Derrubar tudo** | `make down` | Todos os serviços descem (volumes preservados) |
| **Reset completo** | `make down_v` | Todos descem + volumes apagados |

### Perfis em docker-compose.yml

```yaml
db:
  profiles: ["${PROFILE_DB:-local}"]
  # Ativa se PROFILE_DB=local (padrão)

app:
  profiles: ["${PROFILE_APP:-desativado}"]
  # Só ativa se PROFILE_APP=local

frontend:
  profiles: ["${PROFILE_FRONTEND:-local}"]
  # Ativa se PROFILE_FRONTEND=local (padrão)

frontend-build:
  profiles: ["build"]
  # Só ativa explicitamente via `docker compose --profile build run frontend-build`
  # Ou via `make build_front` (que chama python3 ./scripts/build_front.py)
```

>  O profile `build` é usado exclusivamente para o build de produção do frontend; não é ativado por `make up` (usa `PROFILE=local` por padrão).

---

## 3. Referência de variáveis de ambiente

### Composição do .env

```bash
# Banco de dados
DB_NAME=login_base                          # Nome do banco PostgreSQL
DB_USER=login_base                          # Usuário Postgres
DB_PASSWORD=change-me                       # Senha Postgres (MUDE ANTES DE USAR)

# Profiles Docker Compose
PROFILE=local                               # Profile geral (docker compose --profile)
PROFILE_DB=local                            # Serviço db
PROFILE_APP=desativado                      # Serviço app (não sobe por padrão)
PROFILE_FRONTEND=local                      # Serviço frontend

# Frontend e Vite
VITE_PRIMEUI_LICENSE=                       # Licença PrimeUI (vazio = aviso)
BACKEND_URL=http://localhost                # Backend URL (IDE); no container: http://host.docker.internal

# Admin inicial
ADMIN_EMAIL=admin@loginbase.local           # E-mail admin
ADMIN_PASSWORD=                             # OBRIGATÓRIO definir; vazio = sem criar admin

# Jogo
JOGO_VELOCIDADE=1                           # Multiplicador (1=normal, 60=rápido)
JOGO_EXPOENTE_CURVA=1.5                     # Expoente curva: 1,0–2,0 (múltiplo de 0,25); inválido impede inicialização
```

### Variáveis opcionais (não constam em `.env.example`)

```bash
# Vite (WSL2)
VITE_USE_POLLING=true                       # Fixado no docker-compose.yml; não se sobrescreve via .env

# Sessão (aplicação Spring Boot)
SESSION_TIMEOUT=30m                         # Timeout sessão HTTP; Spring lê via spring.config.import=optional:file:.env
SESSION_COOKIE_SECURE=false                 # Cookie seguro (true só com HTTPS); idem
```

**Nota:** `VITE_USE_POLLING` é fixado em `docker-compose.yml:14` para WSL2; a variável `.env` é ignorada pelo serviço `app` (não a lê). `SESSION_TIMEOUT` e `SESSION_COOKIE_SECURE` são lidas apenas pelo backend, não pelos serviços do compose.
```

### Onde cada variável é lida

| Variável | Lido por | Exemplo | Nota |
|---|---|---|---|
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | `docker-compose.yml` (serviço `db`), `application.properties` | `POSTGRES_PASSWORD: ${DB_PASSWORD:-login_base}` | Compose define variáveis de env; Spring lê via properties |
| `DB_HOST` | `application.properties` (Spring) | `jdbc:postgresql://${DB_HOST:localhost}:5432/...` | Compose não define; Spring padrão `localhost` para IDE, service `db` para container |
| `PROFILE*` | `Makefile`, `docker-compose.yml` | `docker compose --profile "$(PROFILE)"` | Makefile passa para compose |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | `application.properties` (Spring) | `app.admin.email=${ADMIN_EMAIL:...}` | Spring cria admin via `ApplicationRunner` se não existir |
| `JOGO_VELOCIDADE` | `application.properties` (Spring); `docker-compose.yml` repassa ao serviço `app` | `app.jogo.velocidade=${JOGO_VELOCIDADE:1}` | Backend usa; frontend não (D-07) |
| `JOGO_EXPOENTE_CURVA` | `application.properties` (Spring); `docker-compose.yml` repassa ao serviço `app` | `app.jogo.expoente-curva=${JOGO_EXPOENTE_CURVA:1.5}` | Backend valida na inicialização (1,0–2,0, múltiplo de 0,25); afeta custo e capacidade acima do nível 5 |
| `SESSION_TIMEOUT`, `SESSION_COOKIE_SECURE` | `application.properties` (Spring) | `server.servlet.session.timeout=${SESSION_TIMEOUT:30m}` | Configuração HTTP/cookies |
| `VITE_PRIMEUI_LICENSE`, `VITE_USE_POLLING`, `BACKEND_URL` | `docker-compose.yml` (serviço `frontend`) | Passadas como ENV ao container Node | `vite.config.ts` lê `VITE_USE_POLLING` e `BACKEND_URL` via `process.env`; `src/main.ts` lê `import.meta.env.VITE_PRIMEUI_LICENSE` |

---

## 4. Referência do Makefile

```bash
# Listar alvos disponíveis (padrão)
make help
# ou simplesmente: make

# Levantar serviços do profile ativo (PROFILE=local por padrão)
make up
# Exemplo com variável: PROFILE_APP=local make up

# Derrubar todos os serviços (mantém volumes)
make down

# Derrubar e apagar volumes (reset completo)
make down_v

# Acompanhar logs do frontend (Ctrl+C para sair)
make logs_front

# Gerar o build de produção do frontend
make build_front

# Abrir menu interativo de alvos
make executar
# ou: make e (atalho)
```

>  O alvo `make build_front` executa `python3 ./scripts/build_front.py`, que roda `docker compose run --rm --build frontend-build` (serviço efêmero com profile `build`). Requer Docker ativo.

---

## 5. Build de artefatos

### Backend (JAR)

```bash
# Build com Maven (sem rodar testes)
./mvnw clean package -DskipTests

# Resultado
# target/login-base-0.0.1-SNAPSHOT.jar

# Tamanho esperado: ~100 MB (com dependências)
```

### Backend (imagem Docker)

```bash
# Build multi-stage (Maven → JRE)
docker compose --profile local build app

# Resultado: imagem `login-base-app` (ou tag da sua registry)
# Tamanho esperado: ~200 MB (JRE only, sem fonte)
```

### Frontend (distribuição)

#### Hoje (vigente)

```bash
# Dev server Vite (sem build de produção)
# Rodando em http://localhost:5173 com proxy para backend
# Nenhum asset versionado gerado; tudo é transitório
```

#### Fluxo de build

> 

```bash
# 1. Gerar build de produção
make build_front

# 2. Package do backend (que agora inclui a SPA gerada)
./mvnw clean package -DskipTests

# 3. Build da imagem Docker
docker compose --profile local build app

# Resultado
# - src/main/resources/static/app/  (assets: CSS, JS, imagens — público em /app/**)
# - src/main/resources/templates/sistema/seguro/index.html  (template: view Thymeleaf)
# - target/login-base-0.0.1-SNAPSHOT.jar (JAR com SPA incluída)
# - imagem `login-base-app` (com os artefatos acima do COPY src)
```

**Destinos:**

| Artefato | Origem | Destino | Servido em | Nota |
|---|---|---|---|---|
| Assets (JS, CSS, imagens) | `frontend/dist/` (exceto `index.html`) | `src/main/resources/static/app/` | `/app/**` | Público, sem autenticação |
| Template SPA | `frontend/dist/index.html` (gerado) | `src/main/resources/templates/sistema/seguro/index.html` | `/`, `/fazenda`, etc. | View Thymeleaf, roteamento history mode |
| Build Vite | `npm run build` (vue-tsc -b && vite build) | `frontend/dist/` (GERADO, ignorado) | — | Apenas durante o build |

**Base Vite:**

- **Dev server** (`make up`): base `/` (acesso raiz, assets em `/`).
- **Build** (`make build_front`): base `/app/` (assets com hash em `/app/**`; configurável em `frontend/vite.config.ts` na lógica `command === 'build'`).

#### Ordem de build (clone limpo / CI)

```bash
# Pré-requisito: Docker ativo
git clone https://github.com/adielsantosaraujo/login_base.git
cd login_base

# 1. Configurar variáveis
cp .env.example .env
# Editar .env: ADMIN_PASSWORD (obrigatório para criar admin; vazio apenas não cria)

# 2. Build de produção do frontend
make build_front

# 3. Package Maven
./mvnw clean package -DskipTests

# 4. Build da imagem Docker (opcional, se planeja deploy em container)
docker compose --profile local build app

# Resultado: aplicação pronta para deploy (JAR com SPA incluída)
```

**CI/CD (inexistente — R-08):** quando existir pipeline de CI, deve:
1. Executar `make build_front` primeiro (requer Docker).
2. Depois `./mvnw clean package` (lê a SPA já copiada).
3. Depois `docker compose build` (inclui a SPA na imagem).

#### Avulso (sem docker integrado)

```bash
# Possível, mas não integrado: build do frontend requer Docker
npm run build  # Gera frontend/dist/, mas não copia para o backend

# ✅ SOLUÇÃO INTEGRADA: usar Docker via make build_front (copia automaticamente)
```

---

## 6. Produção (não definida)

### Estado atual

- **SPA servida pelo backend** (implementado): `/` e rotas da SPA (`/fazenda`, `/forja`, etc.) devolvem a view `sistema/seguro/index` (Thymeleaf); assets em `/app/**` são públicos.
- **Sem pipeline de implantação**: sem GitHub Actions, sem GitLab CI, sem outro servidor CI/CD.
- **Sem HTTPS**: `SESSION_COOKIE_SECURE=false` fixado; produção exigiria SSL/TLS.
- **Sem observabilidade**: sem logs centralizados, sem métricas, sem APM.
- **Sem backup**: volume Docker `db-data` não tem snapshot automático.

>  Clone limpo requer `make build_front` antes do package (pré-requisito para deploy). Sem o build, `GET /` retorna HTTP 500 (template ausente). **Rollback:** reverter commit e redeploy jar/imagem anterior (placeholder volta).

### Roadmap (futuro)

Itens planejados em [`17-riscos-divida-roadmap.md`](17-riscos-divida-roadmap.md):
- Adicionar CI/CD (GitHub Actions); deve rodar `make build_front` antes do package.
- TLS/HTTPS com certificado válido.
- Observabilidade (logs, métricas).
- Backup/DR automático.

---

## 7. Runbook de problemas comuns

### Problema: "no service selected"

**Causa**: nenhum perfil ativado ou sintaxe errada.

**Ação**:

```bash
# Verificar .env
cat .env | grep PROFILE

# Esperado: PROFILE=local (ou outro valor)

# Se vazio ou ausente:
# 1. Copiar .env.example → .env
# 2. Editar PROFILE=local
# 3. Rodar make up novamente

# Teste:
docker compose --profile local ps
# Deve listar serviços com profile "local"
```

### Problema: "Flyway migration failed" ou "ddl-auto=validate falha"

**Causa**: volume antigo com schema desatualizado; ou migrações renumeradas.

**Ação**:

```bash
# 1. Derrubar e limpar
make down_v

# Verifica:
docker volume ls | grep login_base
# Não deve constar db-data (apagado)

# 2. Subir novamente
make up

# Migrations rodam do zero: V1 → V2 → V3 → V4 → V5
# Hibernate valida contra nova estrutura (sucesso esperado)
```

**Nota específica — Checksum divergente em V3:** Ao reverter `V3__jogo.sql` para remover a constraint `0..100` (de um commit anterior), Flyway acusará checksum divergente no banco de desenvolvimento **que já aplicou a versão editada**. **Não** executar `flyway repair` (reescreve histórico). Em vez disso:

```bash
# Executar make down_v (acima) para limpar o volume inteiro
# Banco será recriado com todas as migrações no novo formato (V3 com constraint 0..5 → V5 com 0..100)
```

### Problema: Admin não criado; aviso no log "ADMIN_PASSWORD is empty"

**Causa**: variável `ADMIN_PASSWORD` vazia ou não definida no `.env`.

**Ação**:

```bash
# 1. Editar .env
ADMIN_PASSWORD=minhasenha123

# 2. Reiniciar aplicação
# (Se backend roda na IDE: Ctrl+C + Run novamente)
# (Se container: docker compose restart app)

# 3. Verificar logs
docker compose logs app | grep "admin"
# Esperado: "Criando admin inicial..." ou similar

# 4. Logar
# Email: admin@loginbase.local
# Senha: minhasenha123
```

### Problema: node_modules desatualizado; erro "Cannot find module..."

**Causa**: `package-lock.json` divergiu ou mudança em `package.json`.

**Ação**:

```bash
# 1. Dentro do container
docker compose exec frontend npm ci

# ou, se quiser fazer localmente:
cd frontend
npm ci
npm run build
cd ..

# Verifica:
docker compose exec frontend ls node_modules | wc -l
# Deve haver ~300+ módulos
```

### Problema: Aviso no console "Evaluation of @PRimeUI license..."

**Causa**: `VITE_PRIMEUI_LICENSE` vazio (esperado; não é erro).

**Ação**: se quiser remover aviso, fornecer chave de licença:

```bash
# .env
VITE_PRIMEUI_LICENSE=seu-token-aqui

# Ou deixar vazio (aviso no console é inofensivo)
```

### Problema: Frontend não consegue alcançar backend (CORS, timeout, 502)

**Causa**: `BACKEND_URL` errado ou proxy misconfigured.

**Verificação**:

```bash
# 1. Confirmar BACKEND_URL em .env
cat .env | grep BACKEND_URL

# 2. Se backend roda na IDE:
#    BACKEND_URL=http://localhost (correto)
# 3. Se backend roda no container:
#    BACKEND_URL=http://host.docker.internal (correto; definido automaticamente)

# 4. Testar conectividade do container frontend
docker compose exec frontend curl -v http://host.docker.internal/login

# 5. Se 502/timeout: backend pode estar desligado
docker compose ps | grep app
```

**Ação**:

- Backend na IDE: verificar se `LoginBaseApplication` está rodando (console deve exibir "Tomcat started on port 80").
- Backend no container: `docker compose logs app` (checar erros).
- Vite proxy config: ver `frontend/vite.config.ts` (rotas `/api`, `/login`, `/logout`, etc. devem ter `target: backendUrl`).

### Problema: 401 loop (redireciona infinitamente a `/login`)

**Causa**: sessão expirada (padrão 30 min) ou cookie não configurado.

**Ação**:

```bash
# 1. Limpar cookies do navegador
# DevTools → Application → Cookies → deletar JSESSIONID e XSRF-TOKEN

# 2. Fazer logout e logar novamente
# Ou abrir incógnito

# 3. Se persistir, verificar configuração de sessão
cat src/main/resources/application.properties | grep session
# Esperado: server.servlet.session.timeout=${SESSION_TIMEOUT:30m}

# 4. Aumentar timeout se necessário
# .env
SESSION_TIMEOUT=4h
```

### Problema: Porta 80 já está em uso

**Causa**: IDE e container `app` tentando bind na mesma porta; ou outra aplicação.

**Ação**:

```bash
# 1. Parar backend na IDE (Ctrl+C)
# 2. Verificar processos em 80
lsof -i :80  # macOS/Linux
netstat -ano | findstr 80  # Windows (PowerShell)

# 3. Se quiser rodar app no container mesmo assim:
# Não recomendado: ide + container juntos é incompat
# Opção: usar container em outra porta (editar docker-compose.yml)
ports:
  - "8081:80"  # container em 8081, forward para 80 interno
```

**Nota sobre privilégios de porta 80:** no Linux, WSL e macOS, a porta 80 requer privilégio elevado (root/sudo) ou configuração especial (ex.: `setcap`). No Windows, pode ser usada normalmente. Se não conseguir rodar na porta 80 sem privilégios, veja as alternativas em [docs/09-guia-desenvolvedor.md](09-guia-desenvolvedor.md#rodar-o-backend-na-ide) (seção sobre rodar backend na IDE).

### Problema: Hot reload não funciona (Vue/TypeScript não atualiza ao editar)

**Causa**: `VITE_USE_POLLING` desabilitado ou módulos desincronizados.

**Ação**:

```bash
# 1. Confirmar polling ativado
cat docker-compose.yml | grep -A 2 VITE_USE_POLLING
# Esperado: "true" (fixado no compose para WSL2)

# 2. Se editar arquivo e mudança não aparece:
#    Esperar 2–3 segundos (polling intervalo)
#    Ou forçar reload navegador (F5)

# 3. Se problema persistir: reiniciar frontend
docker compose restart frontend

# 4. Verificar volume bind
docker compose exec frontend pwd
# Deve sair /app
# e arquivos dentro devem ser os do host (mount bind)
```

### Problema: HTTP 500 em `/` (TemplateInputException: template não encontrado)

> 

**Causa**: `src/main/resources/templates/sistema/seguro/index.html` ausente ou vazio. Ocorre em clone limpo sem `make build_front`.

**Ação**:

```bash
# 1. Executar build
make build_front

# 2. Verificar log
cat build.log

# 3. Confirmar template criado
ls -la src/main/resources/templates/sistema/seguro/index.html
# Deve ter conteúdo (minificado)

# 4. Reiniciar backend (IDE: Ctrl+C + Run; container: docker compose restart app)

# 5. Testar
curl http://localhost/ | head -c 100
# Esperado: HTML do template (<!DOCTYPE html> ou similar)
```

### Problema: `make build_front` falha

> 

**Causa comum:** erro de compilação TypeScript ou Docker não ativo.

**Ação**:

```bash
# 1. Verificar Docker
docker ps
# Esperado: Docker rodando (ao menos containers ativos)

# 2. Verificar build log
cat build.log

# 3. Procurar erro (vue-tsc ou vite build)
# Exemplo: `src/components/MyComponent.vue:42: Type 'X' is not assignable to type 'Y'`

# 4. Corrigir erro no código do frontend
# (Exemplo: adicionar type annotation faltante)

# 5. Rodar build novamente
make build_front

# 6. Se persistir, verificar Node
docker compose --profile build run --rm frontend-build node --version
```

### Problema: Página em branco ou 404 em `/app/assets/...`

> 

**Causa**: assets não foram copiados para `src/main/resources/static/app/`.

**Ação**:

```bash
# 1. Verificar se build foi executado
ls -la src/main/resources/static/app/
# Esperado: diretório com assets (*.js, *.css, imagens)

# 2. Se vazio ou ausente: rodar build
make build_front

# 3. Verificar log
cat build.log | grep -i "erro\|fail\|copy"

# 4. Se tudo OK, reiniciar backend
# (IDE: Ctrl+C + Run; container: docker compose restart app)
```

### Problema: F5 em rota da SPA dá 404 (ex.: `/fazenda`)

> 

**Causa**: rota não está listada em `PaginaController` (R-12).

**Ação**:

```bash
# 1. Verificar PaginaController
grep -A 5 "@GetMapping" src/main/java/com/example/loginbase/web/PaginaController.java
# Esperado: {"/", "/fazenda", "/forja", "/quartel", "/masmorras", "/batalhas/{id}"}

# 2. Se rota faltante: adicionar ao @GetMapping
# Exemplo: @GetMapping({"/", "/fazenda", "/forja", "/aldeia"})

# 3. Recompilar e rodar backend novamente
./mvnw compile spring-boot:run

# 4. Testar
curl -H "Cookie: JSESSIONID=seu-session" http://localhost/aldeia
# Esperado: 200 + conteúdo HTML (não 404)
```

---

## 8. Dúvida em aberto: Licença PrimeUI no bundle

> 

**Questão:** se `VITE_PRIMEUI_LICENSE` for passada ao serviço `frontend-build` (para evitar aviso no console), a chave de licença fica pública no bundle de produção (`/app/assets/*.js`). Decisão pendente:

1. **Opção A:** não passar chave ao `frontend-build` → aviso no console, bundle sem chave (seguro).
2. **Opção B:** passar chave → bundle sem aviso, mas chave pública em `/app/**` (risco se a chave for sensível).

**Ação recomendada:** implementador decidir ao revisar a change (Opção C complexa: build com chave inviável pois `VITE_*` entra no bundle).

---

## 9. Backup e restauração (não definido)

### Status atual

- **Sem procedimento automático**: apenas volume Docker `db-data` persiste entre restarts.
- **Sem snapshot**: sem backup em nuvem ou armazenamento externo.
- **Sem DR** (disaster recovery): perda do volume = perda de dados.

### Backup manual (não produzido regularmente)

```bash
# Exportar dados do banco para SQL
docker compose exec db pg_dump -U login_base login_base > backup.sql

# Restaurar (destruindo volume atual)
make down_v
make up
docker compose exec db psql -U login_base login_base < backup.sql
```

### Roadmap

- Adicionar backup automático (cron + S3 ou similar).
- Teste regular de restauração.
- Política de retenção de backups.

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.3.0 | 2026-09-28 | Change raise-building-max-level-100 implementada: JOGO_EXPOENTE_CURVA documentada, nota sobre checksum do V3 com estratégia de cleanup | Adiel, com apoio de agentes Claude |
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: SPA servida pelo backend, remove marcadores de previsto | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
