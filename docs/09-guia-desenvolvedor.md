# Guia do desenvolvedor

| Campo | Valor |
|---|---|
| Versão | 1.0.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` |
| Modelo/norma | Diátaxis (tutorial + how-tos) |
| Público | Novos desenvolvedores, colaboradores |
| Fontes | `README.md`, `Makefile`, `.env.example`, `docker-compose.yml`, `pom.xml`, `CLAUDE.md` |

> Parte da [documentação do login_base](README.md). Guia prático para clonar o projeto, subir o ambiente, rodar testes, e implementar novas funcionalidades.

---

## 1. Pré-requisitos

Antes de começar, certifique-se de ter:

### Sistema operacional e virtualizador
- **Windows + WSL2**: projeto rodando em `D:\desenvolvimento\projetos\login_base` (drive Windows).
- **Docker Desktop** com integração WSL2 habilitada para a distribuição em uso, **ou** Docker Engine nativo do WSL2.
- **Sempre executar comandos `docker compose` a partir de um terminal WSL**, nunca PowerShell/cmd.

### Ferramentas locais
- **JDK 25**: instalado no sistema ou via IDE.
  - Verificar: `java -version` (output: `java version "25..."`).
  - IDE com suporte Java (IntelliJ IDEA recomendado): File → Open → `pom.xml` do projeto.
- **make**: instalado no WSL.
  - Verificar: `make --version`.
- **Python 3**: para `make executar` (menu interativo de alvos).
  - Verificar: `python3 --version`.

### Ferramentas opcionais (para Claude Code no projeto)
- **Node.js** no WSL: só se for usar o plugin PrimeVue do Claude Code (não obrigatório).
  - Instalar: `npx -y @primeui/cli plugin install --tool claude --library primevue`.

---

## 2. Tutorial: do clone ao primeiro acesso

### 2.1 Clonar o repositório

```bash
# No terminal WSL
cd ~/projetos  # ou pasta de preferência
git clone https://github.com/adielsantosaraujo/login_base.git
cd login_base
```

### 2.2 Configurar ambiente

```bash
# Copiar template de variáveis
cp .env.example .env

# Editar .env com sua senha de admin
# Campos obrigatórios:
#   - ADMIN_PASSWORD: senha do administrador inicial (não pode estar vazia para criar o admin)
# Campos recomendados:
#   - DB_PASSWORD: senha do banco (padrão: login_base)
#   - JOGO_VELOCIDADE: 1 (normal) ou 60+ (para testes rápidos)
```

Exemplo de `.env` funcional:

```bash
DB_NAME=login_base
DB_USER=login_base
DB_PASSWORD=login_base
PROFILE=local
PROFILE_DB=local
PROFILE_APP=desativado
PROFILE_FRONTEND=local
ADMIN_EMAIL=admin@loginbase.local
ADMIN_PASSWORD=minhasenha123
JOGO_VELOCIDADE=1
```

### 2.3 Levantar o ambiente

```bash
# Subir PostgreSQL e frontend Vite (backend roda na IDE)
make up

# Verificar:
docker ps
# Deve exibir 'db' e 'frontend' rodando em portas 5432 e 5173
```

### 2.4 Rodar o backend na IDE

**IntelliJ IDEA:**
1. Abrir projeto: File → Open → `pom.xml`.
2. Maven sincroniza dependências.
3. Executar: Run → Run 'LoginBaseApplication' (ícone verde de play).
4. Aguardar: "Tomcat started on port(s): 8080 with context path '/'".
5. Acessar: `http://localhost:5173` no navegador.

**Configurar variáveis de ambiente na IDE:**
- Run → Edit Configurations → LoginBaseApplication.
- Environment variables: `JAVA_HOME=/path/to/jdk25` (se necessário).
- O `application.properties` já lê `.env` via `spring.config.import=optional:file:.env[.properties]`.

### 2.5 Primeiro acesso e criação de vila

1. Abrir `http://localhost:5173` no navegador.
2. Fazer login: e-mail `admin@loginbase.local`, senha definida em `ADMIN_PASSWORD`.
3. Página redireciona para `/` (SPA Vue).
4. **Primeira requisição GET `/api/jogo/vila`**: vila criada automaticamente com nome padrão "Vila de [nome]".
5. Observar:
   - Painel de recursos (comida, madeira, pedra, ferro).
   - Prédios listados (CENTRO_VILA, ARMAZEM, FAZENDA, SERRARIA, PEDREIRA, MINA_FERRO nível 0).
   - Menu navegação: Vila, Fazenda, Forja, Quartel, Masmorras, Sair.

---

## 3. Estrutura do repositório

```
login_base/
├── docs/                              # Documentação (este arquivo)
├── src/
│   ├── main/
│   │   ├── java/com/example/loginbase/
│   │   │   ├── acesso/               # Modelo de usuários, perfis, permissões
│   │   │   ├── auditoria/            # Campos criado_por, alterado_por, listeners
│   │   │   ├── seguranca/            # Autenticação, sessão, admin inicial
│   │   │   ├── web/                  # Controladoras web (login, Home)
│   │   │   ├── jogo/
│   │   │   │   ├── api/              # Controladoras REST (@RestController)
│   │   │   │   ├── economia/         # VilaService, CalculadoraProducao
│   │   │   │   ├── construcao/       # ConstrucaoService, validações
│   │   │   │   ├── fazenda/          # FazendaService, plantio
│   │   │   │   ├── forja/            # ForjaService, forjaria
│   │   │   │   ├── quartel/          # QuartelService, treino
│   │   │   │   ├── masmorra/         # Masmorras, combate
│   │   │   │   ├── catalogo/         # Catálogo de regras (enums)
│   │   │   │   ├── dominio/          # Entidades JPA (Vila, Predio, etc.)
│   │   │   │   └── config/           # Configuração (Clock, Aleatorio)
│   │   │   └── ...
│   │   └── resources/
│   │       ├── application.properties # Configuração Spring
│   │       ├── db/migration/          # Scripts Flyway (V1, V2, V3)
│   │       └── templates/             # Template Thymeleaf (login.html)
│   └── test/java/...                 # Testes JUnit 5
├── frontend/
│   ├── src/
│   │   ├── components/               # Componentes Vue (PainelRecursos, etc.)
│   │   ├── views/                    # Telas (VilaView, FazendaView, etc.)
│   │   ├── composables/              # useVila (estado reativo)
│   │   ├── api/                      # http.ts, jogo.ts, tipos.ts
│   │   ├── router/                   # Roteamento (router/index.ts)
│   │   ├── App.vue                   # Componente raiz
│   │   └── main.ts                   # Entry point
│   ├── package.json                  # Dependências (Vue, Vite, PrimeVue)
│   ├── vite.config.ts                # Configuração Vite (proxy)
│   └── Dockerfile                    # Imagem Node 26
├── scripts/
│   ├── executar.py                   # Menu interativo do Makefile
│   └── cores.py                      # Suporte a cores em scripts Python
├── Dockerfile                        # Build multi-stage Java (Maven → JRE)
├── docker-compose.yml                # Serviços: db, app, frontend
├── Makefile                          # Alvos: up, down, down_v, logs_front, e
├── .env.example                      # Template de variáveis
├── .gitignore                        # Arquivos ignorados
├── pom.xml                           # Dependências Maven
├── README.md                         # Primeiros passos
└── CLAUDE.md                         # Instruções para agentes Claude
```

---

## 4. How-tos

### 4.1 Rodar testes automatizados

```bash
# Levantar banco (obrigatório)
make up

# Rodar todos os testes (239 testes, ~1–2 min)
./mvnw test

# Rodar teste específico
./mvnw -Dtest=MotorCombateTest test

# Rodar método específico
./mvnw -Dtest=MotorCombateTest#ataqueComDanoMinimoDeUm test

# Limpar reports Surefire
rm -rf target/surefire-reports/

# Derrubar ambiente (mantém volumes)
make down

# Reset completo (apaga volumes)
make down_v
```

**Resultado esperado**: todos os testes passam (0 falhas).

### 4.2 Depurar backend no IntelliJ

1. **Breakpoint**: clicar na margem da linha no código (ponto vermelho aparece).
2. **Debug mode**: Run → Debug 'LoginBaseApplication' (ícone de bug).
3. **Navegar no app**: fazer requisição; execução para no breakpoint.
4. **Painel Variables**: inspecionar valores de local, this, globals.
5. **Resume**: Ctrl+F5 (ou botão de play), continua até próximo breakpoint.

### 4.3 Subir o app no Docker (opcional)

Por padrão, o backend roda na IDE (mais rápido e fácil de debugar). Para rodar também em container:

```bash
# Subir db, app (container) e frontend
PROFILE_APP=local make up

# Backend acessível em http://localhost:8080 do host
# (container mapeia porta 8080)

# Verificar logs
make logs_front  # Ctrl+C para sair
# ou: docker compose logs -f app
```

**Nota**: ao rodar app no Docker, a IDE já não consegue debugar. Use para testes de integração ou produção.

### 4.4 Acelerar testes manuais (velocidade do jogo)

```bash
# Multiplicador de velocidade: tempos reduzem; produção acelera
# 1 = normal, 60 = 60× mais rápido

# Subir com velocidade 60
JOGO_VELOCIDADE=60 make up

# Ou editar .env e rodar
JOGO_VELOCIDADE=60 make up
```

A velocidade é lida em:
- `application.properties`: `app.jogo.velocidade`
- Calculadora de produção: construtor `CalculadoraProducao(int velocidade)` e `produzirAte(...)`
- Tempos de construção, forja, treino: divididos por velocidade

**Exemplo**: construir MINA_FERRO nível 1 (tempo base 90 s):
- Velocidade 1: 90 s.
- Velocidade 60: ceil(90 / 60) = 2 s.

### 4.5 Criar migração Flyway

Flyway controla o esquema via scripts SQL em `src/main/resources/db/migration/`.

**Nunca editar V1, V2 ou V3**: histórico seria quebrado. Sempre criar nova versão.

```bash
# Exemplo: criar V4 para adicionar tabela
# Arquivo: src/main/resources/db/migration/V4__descricao.sql
```

Conteúdo de exemplo:

```sql
-- V4__adicionar_tabela_exemplo.sql
CREATE TABLE exemplo (
    id bigint,
    nome varchar(255) NOT NULL UNIQUE,
    criado_em timestamptz not null,
    criado_por varchar(150) not null,
    alterado_em timestamptz not null,
    alterado_por varchar(150) not null,
    constraint pk_exemplo primary key (id)
);

CREATE INDEX ix_exemplo_nome ON exemplo(nome);
```

**Validação**: ao subir o app, Flyway valida contra Hibernate (`ddl-auto=validate`). Se houver erro, revisar a migração.

### 4.6 Adicionar endpoint do jogo

Exemplo: novo endpoint POST `/api/jogo/decoracoes` (hipotético).

**1. Criar request DTO** (`src/main/java/.../jogo/api/DecoracaoRequest.java`):

```java
@Data
public class DecoracaoRequest {
    @NotNull
    private Integer tipo;
    
    @Min(1) @Max(10)
    private Integer quantidade;
}
```

**2. Criar ou alterar service** (`jogo/economia/VilaService.java`):

```java
public VilaDto adicionarDecoracao(Long usuarioId, DecoracaoRequest req) 
    throws RegraJogoException {
    Vila vila = vilaRepository.findByUsuarioIdParaAtualizacao(usuarioId)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Vila não encontrada"));
    
    // Validações
    if (req.getTipo() < 1 || req.getTipo() > 5) {
        throw new RegraJogoException(CodigoErro.REQUISICAO_INVALIDA, 
            "Tipo de decoração inválido");
    }
    
    // Lógica
    vila.adicionarDecoracao(req.getTipo());
    vila = vilaRepository.save(vila);
    
    EstadoVila estado = EstadoVila.do(vila, clock);
    return jogoMapper.toVilaDto(estado, clock);
}
```

**3. Expor em controller** (`jogo/api/AcoesVilaController.java`):

```java
@PostMapping("/decoracoes")
public ResponseEntity<VilaDto> adicionarDecoracao(
    Authentication auth,
    @Valid @RequestBody DecoracaoRequest req) {
    
    VilaDto result = vilaService.adicionarDecoracao(UsuarioAtual.id(auth), req);
    return ResponseEntity.ok(result);
}
```

**4. Adicionar código de erro** (se necessário) (`jogo/CodigoErro.java`):

```java
public enum CodigoErro {
    // ... existing
    DECORACAO_INVALIDA,
    // ...
}
```

**5. Escrever teste** (`jogo/api/AcoesVilaControllerWebMvcTest.java`):

```java
@Test
void testAdicionarDecoracaoValida() throws Exception {
    // Setup, mock usuário, etc.
    
    mockMvc.perform(post("/api/jogo/decoracoes")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "tipo": 1,
              "quantidade": 3
            }
            """)
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").exists());
}
```

**6. Documentar** (ver [`06-api-rest.md`](06-api-rest.md#endpoints-de-jogo)).

### 4.7 Adicionar tela Vue

Exemplo: nova tela `/aldeia` (hipotética).

**1. Criar view** (`frontend/src/views/AldeiaView.vue`):

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useVila } from '@/composables/useVila'
import { ComoVer } from '@/api/tipos'

const vila = useVila()
const decoracoes = ref<ComoVer[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  // Buscar dados
  decoracoes.value = (await vila.vila.value?.decoracoes) || []
  loading.value = false
})
</script>

<template>
  <div class="aldeia">
    <h1>Aldeia</h1>
    <div v-if="loading" class="loading">Carregando...</div>
    <div v-else class="decoracoes">
      <div v-for="d in decoracoes" :key="d.id" class="decoracao">
        {{ d.nome }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.aldeia { padding: 1rem; }
.decoracoes { display: grid; gap: 1rem; }
</style>
```

**2. Adicionar rota** (`frontend/src/router/index.ts`):

```typescript
{
  path: '/aldeia',
  component: () => import('../views/AldeiaView.vue'),
  meta: { requiresAuth: true }
}
```

**3. Adicionar menu** (`frontend/src/App.vue`):

```vue
<router-link to="/aldeia">Aldeia</router-link>
```

**4. Atualizar tipos** (`frontend/src/api/tipos.ts`):

```typescript
export interface Decoracao {
  id: number
  tipo: number
  quantidade: number
}

export interface VilaDto {
  // ... existing fields
  decoracoes?: Decoracao[]
}
```

### 4.8 Usar componentes PrimeVue com Claude Code

O projeto já tem **PrimeVue 5** + plugin Claude Code (opcional).

**Instalar plugin** (se ainda não instalado):

```bash
npx -y @primeui/cli plugin install --tool claude --library primevue
```

**No Claude Code**: digitar `/primevue:` + espaço para ver skills disponíveis.

Exemplo: `/primevue:primevue-component-implementation` → skill que ajuda implementar componente.

**Exemplo de componente** (`frontend/src/components/CartaoDecoracao.vue`):

```vue
<script setup lang="ts">
import { defineProps } from 'vue'
import Card from 'primevue/card'

interface Props {
  decoracao: { nome: string; descricao: string }
}

defineProps<Props>()
</script>

<template>
  <Card>
    <template #title>
      {{ decoracao.nome }}
    </template>
    <p>{{ decoracao.descricao }}</p>
  </Card>
</template>
```

### 4.9 Resetar banco de dados

```bash
# Opção 1: apagar volume Docker (mais rápido)
make down_v

# Opção 2: apagar volume e subir novamente
docker volume rm login_base_db-data
make up
```

**Resultado**: próximas migrations Flyway (V1, V2, V3) rodará do zero; admin criado se `ADMIN_PASSWORD` estiver definido.

---

## 5. Convenções de código

### 5.1 Linguagem e nomenclatura
- **Nomes de domínio** em português do Brasil (classes, variáveis, campos BD): `Vila`, `Predio`, `Canteiro`, `producaoPorHora`.
- **Código e comentários** em inglês (quando necessário) ou português consistente dentro do arquivo.
- **Imports e keywords** Java: inglês (não traduzir).

### 5.2 Backend Java
- **Lombok**: use `@Data`, `@RequiredArgsConstructor`, `@ToString(exclude = ...)` para reduzir boilerplate.
- **Records para DTOs**: `public record VilaDto(String nome, int recursos) {}` (conciso e imutável).
- **@Transactional**: nas handlers de comando (`@PostMapping`) para garantir consistência.
- **Mensagens de erro**: pt-BR, descritivas, sem expor stack trace na API.
- **i18n**: usar `MessageSource` ou literais pt-BR diretos (projeto usa pt-BR fixo).

### 5.3 Frontend Vue
- **Composables**: `useVila()` para estado compartilhado da vila (reatividade).
- **Setup syntax**: `<script setup lang="ts">` (moderno, limpo).
- **Type safety**: tipos em `api/tipos.ts`, importar em components.
- **PrimeVue**: usar componentes de `primevue/*` para UI (Button, Card, Dialog, Toast, etc.).

### 5.4 Banco de dados
- **Convenção de nomes**: `snake_case` em pt-BR (`jogo_vilas`, `criado_em`, `criado_por`).
- **PK**: `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`.
- **Auditoria**: toda tabela tem `criado_em`, `criado_por`, `alterado_em`, `alterado_por` (NOT NULL).
- **Enums**: `VARCHAR` com `EnumType.STRING` (legível em SQL).
- **Índices**: nomeados explicitamente (`ix_`, `ux_` para unique).

---

## 6. Ferramentas Claude Code

### 6.1 Plugin PrimeVue (opcional)

Após instalar (seção 1), usar skills do PrimeVue:

- `primevue:primevue-component-implementation`: ajuda implementar componente Vue + PrimeVue.
- `primevue:primevue-setup-installation`: diagnosticar instalação.
- `primevue:primevue-theming-customization`: customizar tema Aura.
- `primevue:primevue-router`: integrar PrimeVue com router.

Usar via `/primevue:nome-da-skill` no chat.

### 6.2 Skills do projeto

Ver `.claude/skills/`:
- `dev-subagentes`: orquestração com Opus (planejamento), Sonnet (código), Haiku (docs).
- `openspec-explore`, `openspec-propose`, `openspec-apply`: workflow de specs.

Usar via `/opsx:explore`, `/opsx:propose`, `/opsx:apply` ou comandos equivalentes.

### 6.3 Diagnóstico de ambiente

No projeto:

```bash
# Verificar instalação do plugin PrimeVue
npx -y @primeui/cli doctor --tool claude --library primevue

# Listar skills disponíveis
/find-skills primevue
```

---

## 7. Variáveis de ambiente

Lidas de `.env` (e passadas ao Docker):

| Variável | Padrão | Onde é lida | Descrição |
|---|---|---|---|
| `JAVA_HOME` | — | IDE (opcional) | Caminho JDK (só se IDE não encontrar) |
| `DB_NAME` | `login_base` | compose, Spring | Nome banco PostgreSQL |
| `DB_USER` | `login_base` | compose, Spring | Usuário banco |
| `DB_PASSWORD` | `login_base` | compose, Spring | Senha banco |
| `DB_HOST` | `localhost` | Spring (compose define `db`) | Host banco (compose: `db`, IDE: `localhost`) |
| `PROFILE` | `local` | Makefile | Profile ativado em `make up` |
| `PROFILE_DB` | `local` | Makefile | Profile serviço `db` |
| `PROFILE_APP` | `desativado` | Makefile | Profile serviço `app` (`local` para subir em container) |
| `PROFILE_FRONTEND` | `local` | Makefile | Profile serviço `frontend` |
| `ADMIN_EMAIL` | `admin@loginbase.local` | Spring | E-mail do admin inicial |
| `ADMIN_PASSWORD` | — | Spring | **Obrigatório** para criar admin inicial (vazio = aviso, sem criar) |
| `JOGO_VELOCIDADE` | 1 | Spring, Makefile | Multiplicador de velocidade (1=normal, 60=rápido) |
| `SESSION_TIMEOUT` | `30m` | Spring | Timeout de sessão HTTP |
| `SESSION_COOKIE_SECURE` | `false` | Spring | Cookie seguro (true apenas em HTTPS) |
| `VITE_PRIMEUI_LICENSE` | — | compose/frontend | Chave licença PrimeUI (vazio = aviso no console) |
| `VITE_USE_POLLING` | `true` | compose | Usar polling em dev (fixo para WSL) |
| `BACKEND_URL` | `http://localhost:8080` | compose/Vite | URL backend (compose: `http://host.docker.internal:8080`) |

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
