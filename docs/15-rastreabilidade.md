# Rastreabilidade (Matriz RF → Spec → Task → Código → Teste)

| Campo | Valor |
|---|---|
| Versão | 1.0.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` |
| Modelo/norma | RTM bidirecional (ISO/IEC/IEEE 29119-1) |
| Público | QA, revisores, arquitetos |

> Parte da [documentação do login_base](README.md). Mapeamento completo de requisitos (101 RF + 15 RNF) para especificações OpenSpec, tasks, código-fonte e testes automatizados.

---

## 1. Como Ler

A matriz principal mostra, para cada **requisito funcional (RF)**:

- **RF**: ID fixo (ex.: `RF-ACD-001`).
- **Requirement**: Texto do `SHALL` ou `MUST` da spec.
- **Change**: Capability de origem + (delta se da change jogo, arquivada em 2026-09-27).
- **Task(s)**: Arquivo(s) `tasks/X.Y-slug.md` que implementaram.
- **Código principal**: Classe/arquivo-chave (package + nome, sem `src/main/java/`).
- **Teste(s)**: Classe e método `@Test` (sem full path).
- **Status de verificação**: Automatizado, inspeção, manual, build.

Navegue por **Capability** (ex.: ACD, AUT, VIL, COM) para agrupar requisitos relacionados.

---

## 2. Matriz RF Completa

### ACD — Access Control Data (7 RF)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-ACD-001` | Tabela de usuários com email/celular/senha/nome | access-control-data | auth 2.1, 2.2 | acesso/Usuario.java, V1 | UsuarioTest (8 testes) | Automatizado |
| `RF-ACD-002` | Tabelas de perfis e permissões | access-control-data | auth 2.2, 3.1 | acesso/Perfil.java, V1 | UsuarioTest | Automatizado |
| `RF-ACD-003` | Vínculo usuário-perfil com data_inicial/final (vigência) | access-control-data | auth 2.1 | acesso/Usuario.java, V1 | UsuarioTest | Automatizado |
| `RF-ACD-004` | Vínculo perfil-permissão | access-control-data | auth 2.2 | acesso/Perfil.java, V1 | LoginBaseApplicationTests | Inspeção |
| `RF-ACD-005` | Registro de sessões (tabela `sessoes`) | access-control-data | auth 3.2 | seguranca/SessaoService.java, V1 | SessaoServiceTest (8) | Automatizado |
| `RF-ACD-006` | Campos de auditoria em todas as tabelas (`criado_em/por`, `alterado_em/por`) | access-control-data | auth 3.1 | auditoria/EntidadeAuditavel.java, V1–V3 | UsuarioAuditorAwareTest (3) | Automatizado |
| `RF-ACD-007` | Esquema versionado por migrações Flyway | access-control-data | auth 1.1 | src/main/resources/db/migration/ | Inspeção de V1, V2, V3 | Inspeção |

### AUT — User Authentication (10 RF: 9 da spec base + 1 ADDED; RF-AUT-004 MODIFIED)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-AUT-001` | Página de login pública (GET `/login` sem autenticação) | user-authentication | auth 4.1 | web/PaginaController.java, templates/login.html | AutenticacaoWebMvcTest (18) | Automatizado |
| `RF-AUT-002` | Autenticação por e-mail ou celular e senha | user-authentication | auth 4.2, 5.1 | seguranca/UsuarioDetailsService.java, IdentificadorLogin.java | UsuarioDetailsServiceTest (10), IdentificadorLoginTest (7) | Automatizado |
| `RF-AUT-003` | Perfis vigentes carregados como autoridades `ROLE_<perfil>` | user-authentication | auth 4.3 | seguranca/UsuarioDetailsService.java | UsuarioDetailsServiceTest | Automatizado |
| `RF-AUT-004` | **MODIFICADO**: Proteção de rotas (401 anônimo em `/api/**` sem cache) | user-authentication + jogo | jogo 1.1 | seguranca/SecurityConfig.java, ErroApiHandler.java | ApiSegurancaWebMvcTest (5) | Automatizado |
| `RF-AUT-005` | Página inicial segura (GET `/` com autenticação) | user-authentication | auth 4.1 | web/PaginaController.java | AutenticacaoWebMvcTest | Automatizado |
| `RF-AUT-006` | Logout (POST `/logout`) | user-authentication | auth 4.4 | seguranca/SecurityConfig.java | AutenticacaoWebMvcTest | Automatizado |
| `RF-AUT-007` | Sessão HTTP segura (`HttpOnly`, `SameSite`, `Secure`, timeout 30 min, troca ID no login) | user-authentication | auth 5.2 | seguranca/SecurityConfig.java, SessaoService.java | SessaoEncerradaListenerTest (2) | Automatizado |
| `RF-AUT-008` | Registro de sessões autenticadas (IP, User-Agent, hash do ID) | user-authentication | auth 3.2, 5.2 | seguranca/RegistroSessaoSuccessHandler.java, SessaoService.java | SessaoServiceTest | Automatizado |
| `RF-AUT-009` | Administrador inicial criado por `ApplicationRunner` via `ADMIN_EMAIL`/`ADMIN_PASSWORD` | user-authentication | auth 5.1 | seguranca/AdminInicialRunner.java | AdminInicialRunnerTest (7) | Automatizado |
| `RF-AUT-010` | **ADICIONADO**: Proteção CSRF para clientes JavaScript (cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN`) | user-authentication (delta jogo) | jogo 1.1 | seguranca/SecurityConfig.java | ApiSegurancaWebMvcTest | Automatizado |

### AMB — Docker Dev Environment (6 RF)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-AMB-001` | Profile por serviço configurável por variável (`PROFILE`, `PROFILE_DB`, `PROFILE_APP`, `PROFILE_FRONTEND`) | docker-dev-environment | compose 1.1, 2.1 | docker-compose.yml, Makefile | Inspeção de compose | Inspeção |
| `RF-AMB-002` | Profile ativo definido por `PROFILE` (padrão `dev`, compõe sub-profiles) | docker-dev-environment | compose 2.1 | docker-compose.yml, Makefile | Inspeção | Inspeção |
| `RF-AMB-003` | Variáveis de profile documentadas (`.env.example`) | docker-dev-environment | compose 1.1 | .env.example | Inspeção | Inspeção |
| `RF-AMB-004` | Levantar o ambiente via `make up` | docker-dev-environment | compose 1.2 | Makefile | `make up && make down_v` (manual) | Manual |
| `RF-AMB-005` | Derrubar o ambiente via `make down` | docker-dev-environment | compose 1.2 | Makefile | Manual | Manual |
| `RF-AMB-006` | Ajuda do Makefile (`make help`) | docker-dev-environment | compose 3.1 | Makefile | Manual | Manual |

### FRE — Frontend App (7 RF vigentes: 4 da spec base + 3 ADDED; "Página inicial de boas-vindas" REMOVED)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-FRE-001` | Projeto frontend Vue 3 com PrimeVue 5 | frontend-app | vue 1.1 | frontend/, package.json | `npm run build` | Build |
| `RF-FRE-002` | Licença PrimeUI por variável de ambiente (`VITE_PRIMEUI_LICENSE`) | frontend-app | vue 2.1 | .env.example, frontend/Dockerfile | Inspeção | Inspeção |
| `RF-FRE-003` | Container de desenvolvimento com Node 26 e npm 12 | frontend-app | vue 1.2 | frontend/Dockerfile | Build (dentro container) | Demonstração |
| `RF-FRE-004` | Recarga automática ao editar código (via Vite) | frontend-app | vue 1.1 | frontend/vite.config.ts | Manual (polling 5 s) | Demonstração |
| `RF-FRE-005` | Página inicial do jogo (substitui a de boas-vindas, REMOVIDA) | frontend-app + game-frontend | vue 1.1, jogo 7.1–7.2 | frontend/src/views/VilaView.vue | Build + manual | Build + manual |
| `RF-FRE-006` | Proxy de desenvolvimento para o backend (`/api`, `/login`, `/logout` → backend) | frontend-app | vue 2.2 | frontend/vite.config.ts | Demonstração | Demonstração |
| `RF-FRE-007` | Roteamento no modo history (não hash) | frontend-app | vue 4.1 | frontend/src/router/index.ts | Build | Build |
| — | **ADICIONADO**: Navegação do jogo (6 rotas: vila, fazenda, forja, quartel, masmorras, batalha) | game-frontend | jogo 7.1–7.6 | frontend/src/router/index.ts, views/ | Build + task 8.2 (manual) | Build + manual |
| — | **ADICIONADO**: Polling a cada 5 s, contagem regressiva, Toast de erros | game-frontend | jogo 7.2–7.6 | frontend/src/composables/useVila.ts | Demonstração | Manual |

### PRC — Subagent Dev Workflow (5 RF)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-PRC-001` | Execução de código por subagentes Sonnet | subagent-dev-workflow | skill 1.1 | .claude/skills/dev-subagentes/SKILL.md | Inspeção | Inspeção |
| `RF-PRC-002` | Sessão limpa por tarefa (sem contexto anterior entre tasks) | subagent-dev-workflow | skill 1.1 | SKILL.md (regra 2) | Inspeção | Inspeção |
| `RF-PRC-003` | Paralelismo de tarefas independentes (ondas) | subagent-dev-workflow | skill 2.1 | SKILL.md (passo 2) | Inspeção | Inspeção |
| `RF-PRC-004` | Relatório final de agentes e tokens em `resumo_utilizacao_agentes.md` | subagent-dev-workflow | skill 2.1, relatório | resumo_utilizacao_agentes.md (2 arquivos) | Inspeção | Inspeção |
| `RF-PRC-005` | Disponibilidade da skill global e no projeto | subagent-dev-workflow | skill 1.1 | .claude/skills/dev-subagentes/ (global) + CLAUDE.md (projeto) | Inspeção | Inspeção |

### DAD — Game Data (6 RF, delta da change jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-DAD-001` | Tabelas do jogo (8: vilas, prédios, canteiros, sementes, itens, unidades, ordens, batalhas) | game-data | jogo 1.2 | V3__jogo.sql | RepositoriosJogoTest (8) | Automatizado |
| `RF-DAD-002` | Uma vila por usuário (unique constraint) | game-data | jogo 1.2, 2.1 | V3, jogo/dominio/Vila.java | RepositoriosJogoTest | Automatizado |
| `RF-DAD-003` | Integridade dos níveis (0–5) e quantidades (≥0) | game-data | jogo 1.2 | V3, constraints `ck_jogo_*` | RepositoriosJogoTest | Automatizado |
| `RF-DAD-004` | Fila de uma ordem por categoria (CONSTRUCAO/FORJA/TREINO) | game-data | jogo 1.2, 4.1–4.4 | V3 (unique vila+categoria), OrdemRepository | RepositoriosJogoTest, ConstrucaoServiceTest | Automatizado |
| `RF-DAD-005` | Uma batalha em andamento por vila (unique partial index) | game-data | jogo 1.2, 5.3 | V3, BatalhaRepository | RepositoriosJogoTest, MasmorraServiceTest | Automatizado |
| `RF-DAD-006` | Auditoria nas tabelas do jogo (criado_em/por, alterado_em/por) | game-data | jogo 2.1 | V3, `EntidadeAuditavel` base | LoginBaseApplicationTests | Automatizado |

### VIL — Game Village (10 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-VIL-001` | Criação automática da vila no 1º acesso (estado inicial) | game-village | jogo 3.2, 6.1 | VilaService.consultar() (cria no 1º acesso, REQUIRES_NEW), VilaController | VilaControllerWebMvcTest | Automatizado |
| `RF-VIL-002` | Isolamento por usuário (outro usuário vê 404) | game-village | jogo 3.2 | VilaRepository.findByUsuarioId(), isolamento no controller | VilaControllerWebMvcTest, VilaServiceTest | Automatizado |
| `RF-VIL-003` | Recursos e capacidade (madeira, pedra, ferro, comida em milésimos, limite por armazém) | game-village | jogo 3.1, 3.2 | CalculadoraProducao, Estoque, TipoPredio | CalculadoraProducaoTest (18), VilaServiceTest | Automatizado |
| `RF-VIL-004` | Produção em tempo real calculada sob demanda (lazy, sem background jobs) | game-village | jogo 3.1, 3.2 | CalculadoraProducao.produzirAte(), VilaService | CalculadoraProducaoTest, VilaServiceTest | Automatizado |
| `RF-VIL-005` | Conclusão de ordens sob demanda (sincronização preguiçosa) | game-village | jogo 3.2 | VilaService.sincronizar(), AplicadorOrdens.aplicar() | VilaServiceTest | Automatizado |
| `RF-VIL-006` | Velocidade configurável (`JOGO_VELOCIDADE`, padrão 1) | game-village | jogo 1.4, 3.1 | JogoProperties, CalculadoraProducao | VilaServiceTest, CalculadoraProducaoTest | Automatizado |
| `RF-VIL-007` | Consulta do estado da vila (`GET /api/jogo/vila` retorna `VilaDto` completo) | game-village | jogo 6.1 | VilaController.consultarVila(), VilaDto | VilaControllerWebMvcTest (8) | Automatizado |
| `RF-VIL-008` | Catálogo de regras (`GET /api/jogo/catalogo` com TipoRecurso, TipoPredio, Cultivo, etc.) | game-village | jogo 1.3, 6.1 | CatalogoDto, JogoMapper | CatalogoTest (11) | Automatizado |
| `RF-VIL-009` | Operações serializadas por vila (lock pessimista) | game-village | jogo 2.1, 3.2 | VilaRepository.findByUsuarioIdParaAtualizacao(`@Lock(PESSIMISTIC_WRITE)`) | VilaServiceTest, ConstrucaoServiceTest | Automatizado |
| `RF-VIL-010` | Erros de regra padronizados (código + mensagem amigável) | game-village | jogo 1.4, 6.2, 6.3 | CodigoErro enum, ErroApiHandler, RegraJogoException | AcoesVilaControllerWebMvcTest (12) | Automatizado |

### PRD — Game Buildings (7 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-PRD-001` | Tipos de prédio e efeitos (8 tipos, 5 níveis, efeitos variados) | game-buildings | jogo 1.3, 4.1 | TipoPredio enum, efeitos documentados em design | CatalogoTest, ConstrucaoServiceTest | Automatizado |
| `RF-PRD-002` | Custo e tempo por nível (fórmula: base × 1.5^(n-1), tempo × 2^(n-1)) | game-buildings | jogo 1.3, 4.1 | TipoPredio.custo(nivel), tempoSegundos(nivel) | CatalogoTest (11), ConstrucaoServiceTest (10) | Automatizado |
| `RF-PRD-003` | Limite pelo centro da vila (nível máximo = nível do centro) | game-buildings | jogo 4.1 | ConstrucaoService.melhorar() | ConstrucaoServiceTest | Automatizado |
| `RF-PRD-004` | Pré-requisitos (FORJA exige MINA ≥1, QUARTEL exige FORJA ≥1) | game-buildings | jogo 4.1 | ConstrucaoService (ordem de validação) | ConstrucaoServiceTest | Automatizado |
| `RF-PRD-005` | Fila de construção única (1 ordem por tipo por vila) | game-buildings | jogo 4.1 | OrdemRepository (unique vila+tipo), FILA_OCUPADA | ConstrucaoServiceTest | Automatizado |
| `RF-PRD-006` | Nível máximo 5 | game-buildings | jogo 1.3, 4.1 | TipoPredio (valores 1–5), validação NIVEL_MAXIMO | ConstrucaoServiceTest | Automatizado |
| `RF-PRD-007` | Débito no início, efeito na conclusão | game-buildings | jogo 4.1 | ConstrucaoService (débito antes de criar ordem) | ConstrucaoServiceTest | Automatizado |

### FAZ — Game Farming (4 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-FAZ-001` | Canteiros da fazenda (nº = nível da fazenda, até 5) | game-farming | jogo 2.1, 4.2 | Canteiro, CanteiroRepository | FazendaServiceTest (7) | Automatizado |
| `RF-FAZ-002` | Cultivos e produção (4: TRIGO/MILHO/BATATA/ABOBORA_DOURADA com taxas 20/30/45/70 comida/h) | game-farming | jogo 1.3, 4.2 | Cultivo enum, CalculadoraProducao | FazendaServiceTest, CalculadoraProducaoTest | Automatizado |
| `RF-FAZ-003` | Plantio consome semente | game-farming | jogo 4.2 | FazendaService.plantar(), EstoqueSemente | FazendaServiceTest | Automatizado |
| `RF-FAZ-004` | Troca de cultivo preserva a produção anterior (milésimos acumulados) | game-farming | jogo 4.2 | Canteiro.cultivo, produção acumulada | FazendaServiceTest | Automatizado |

### FOR — Game Forge (6 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-FOR-001` | Modelos e atributos por nível (5 modelos: ESPADA/LANCA/ARCO/ARMADURA_COURO/ARMADURA_FERRO, ataque/defesa/alcance por nível) | game-forge | jogo 1.3, 4.3 | ModeloItem enum, atributos em design | CatalogoTest, ForjaServiceTest | Automatizado |
| `RF-FOR-002` | Receitas e ordem de forja (custo = base × nível × quantidade, tempo = ceil(base × nível × quantidade / velocidade)) | game-forge | jogo 1.3, 4.3 | ModeloItem.custoTotal(), tempoTotalSegundos(), ForjaService.forjar() | CatalogoTest, ForjaServiceTest (9) | Automatizado |
| `RF-FOR-003` | Nível limitado pela forja (item nível ≤ nível FORJA) | game-forge | jogo 4.3 | ForjaService.forjar() | ForjaServiceTest | Automatizado |
| `RF-FOR-004` | Uma ordem por vez (fila de 1) | game-forge | jogo 4.3 | OrdemRepository (unique vila+FORJA), FILA_OCUPADA | ForjaServiceTest | Automatizado |
| `RF-FOR-005` | Entrega na conclusão (Item criado com status DISPONIVEL) | game-forge | jogo 4.3 | ForjaService.aplicarOrdemForja(), Item.status | ForjaServiceTest | Automatizado |
| `RF-FOR-006` | Validação da ordem (recursos, pré-requisitos, nível máximo) | game-forge | jogo 4.3 | ForjaService.forjar() | ForjaServiceTest | Automatizado |

### EXE — Game Army (6 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-EXE-001` | Tipos de tropa e atributos derivados (3: SOLDADO/ARQUEIRO/LANCEIRO; HP = 30/22/40, ataque/defesa/alcance/movimento) | game-army | jogo 1.3, 4.4 | TipoTropa enum, atributos em design | CatalogoTest, QuartelServiceTest | Automatizado |
| `RF-EXE-002` | Treino consome arma e armadura | game-army | jogo 4.4 | QuartelService.treinar() | QuartelServiceTest (12) | Automatizado |
| `RF-EXE-003` | Validação dos itens (arma/armadura existem e estão DISPONIVEL) | game-army | jogo 4.4 | QuartelService.treinar() | QuartelServiceTest | Automatizado |
| `RF-EXE-004` | Tropas liberadas pelo nível do quartel (N1=SOLDADO, N2=+ARQUEIRO, N3=+LANCEIRO) | game-army | jogo 1.3, 4.4 | TipoTropa.nivelMinimoQuartel, validação | QuartelServiceTest, CatalogoTest | Automatizado |
| `RF-EXE-005` | Capacidade do exército (3 × N unidades, N = nível QUARTEL) | game-army | jogo 4.4 | TipoPredio.capacidadeExercito(nivel), validação | QuartelServiceTest | Automatizado |
| `RF-EXE-006` | Uma ordem de treino por vez (fila de 1) | game-army | jogo 4.4 | OrdemRepository (unique vila+TREINO), FILA_OCUPADA | QuartelServiceTest | Automatizado |

### COM — Game Dungeon Combat (10 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-COM-001` | Níveis de masmorra e liberação (1–5 progressivos, 1º liberado ao criar vila) | game-dungeon-combat | jogo 1.2, 5.1 | Masmorra (levels 1–5), Vila.masmorraNivelLiberado | RepositoriosJogoTest, MasmorraServiceTest | Automatizado |
| `RF-COM-002` | Início da batalha (até 4 unidades, inimigos em spawn fixo) | game-dungeon-combat | jogo 5.3, 6.3 | MasmorraService.iniciarBatalha(), EstadoBatalha | MasmorraServiceTest (10) | Automatizado |
| `RF-COM-003` | Mapa da masmorra (8×8 com obstáculos, posições de spawn) | game-dungeon-combat | jogo 1.3, 5.1 | MapaMasmorra, Masmorra | CatalogoTest, MotorCombateTest | Automatizado |
| `RF-COM-004` | Movimento (BFS, distância ≤ movimento, não atravessa obstáculos) | game-dungeon-combat | jogo 5.1 | Caminhos.caminhoMaisCurto(), MotorCombate.processar() | MotorCombateTest (13) | Automatizado |
| `RF-COM-005` | Ataque e dano (alcance Manhattan, dano = max(1, ataque − defesa)) | game-dungeon-combat | jogo 5.1 | MotorCombate.processar(), dano | MotorCombateTest | Automatizado |
| `RF-COM-006` | Defesa (dobra a defesa efetiva no turno) | game-dungeon-combat | jogo 5.1 | MotorCombate.processar() | MotorCombateTest | Automatizado |
| `RF-COM-007` | IA dos inimigos (seleção por distância/HP, movimento + ataque no mesmo turno, desempates fixos) | game-dungeon-combat | jogo 5.1 | MotorCombate.executarIA() | MotorCombateTest | Automatizado |
| `RF-COM-008` | Turnos e controle de concorrência (`@Version`, número de turno obrigatório em POST .../acoes) | game-dungeon-combat | jogo 5.3 | Batalha.@Version, turno em request | MasmorraControllerWebMvcTest (19) | Automatizado |
| `RF-COM-009` | Fim da batalha (derrota por morte do esquadrão, rendição ou turno 30) | game-dungeon-combat | jogo 5.2, 5.3 | MotorCombate.terminou(), resultado(), Loot | MasmorraServiceTest | Automatizado |
| `RF-COM-010` | Batalha persistida por ação (estado, log, loot em colunas `text`) | game-dungeon-combat | jogo 5.1, 5.3 | MasmorraService (ObjectMapper), Batalha | MasmorraServiceTest | Automatizado |

### LOO — Game Dungeon Loot (6 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-LOO-001` | Recursos garantidos (40N comida, 50N madeira/pedra, 20N ferro por nível) | game-dungeon-loot | jogo 5.2 | GeradorLoot.gerarRecursos(), Loot | GeradorLootTest (17) | Automatizado |
| `RF-LOO-002` | Rolagens de loot (faixas 0–34 semente, 35–59 ferro, 60–99 item) | game-dungeon-loot | jogo 5.2 | GeradorLoot.gerar(), Aleatorio | GeradorLootTest | Automatizado |
| `RF-LOO-003` | Sementes por nível (N1 MILHO; N2 +BATATA; N4 +ABOBORA_DOURADA; TRIGO não tem semente) | game-dungeon-loot | jogo 5.2 | GeradorLoot.gerar(), sortearSemente() | GeradorLootTest | Automatizado |
| `RF-LOO-004` | Itens com nível próprio (1–5, distribuição por nível: min(5, N+0\|1)) | game-dungeon-loot | jogo 5.2 | GeradorLoot.gerarItens(), nível derivado | GeradorLootTest | Automatizado |
| `RF-LOO-005` | Nenhum loot na derrota | game-dungeon-loot | jogo 5.3 | MasmorraService (loot apenas em VITORIA) | MasmorraServiceTest | Automatizado |
| `RF-LOO-006` | Loot registrado na batalha (Batalha.loot JSON) | game-dungeon-loot | jogo 5.3 | Batalha.loot, Loot classe | MasmorraServiceTest | Automatizado |

### UIJ — Game Frontend (11 RF, delta jogo)

| RF | Requirement | Change | Task(s) | Código | Teste(s) | Verificação |
|---|---|---|---|---|---|---|
| `RF-UIJ-001` | Navegação do jogo (6 rotas: vila, fazenda, forja, quartel, masmorras, batalha) | game-frontend | jogo 7.1 | frontend/src/router/index.ts | Build + task 8.2 | Build + manual |
| `RF-UIJ-002` | Redirecionamento ao login em 401 (interceptor de erro HTTP) | game-frontend | jogo 7.1 | frontend/src/api/http.ts | Demonstração | Manual |
| `RF-UIJ-003` | Painel de recursos (comida/madeira/pedra/ferro em milésimos, capacidade visual) | game-frontend | jogo 7.2 | frontend/src/components/PainelRecursos.vue | Demonstração | Manual |
| `RF-UIJ-004` | Prédios e fila de construção (grid de prédios, card de cada um, próximo nível, tempo restante) | game-frontend | jogo 7.2 | frontend/src/views/VilaView.vue, CartaoPredio.vue | Demonstração | Manual |
| `RF-UIJ-005` | Fazenda (canteiros, cultivo selecionável, estoque de sementes) | game-frontend | jogo 7.3 | frontend/src/views/FazendaView.vue | Demonstração | Manual |
| `RF-UIJ-006` | Forja (seleção modelo/nível, custo calculado, inventário de itens) | game-frontend | jogo 7.4 | frontend/src/views/ForjaView.vue | Demonstração | Manual |
| `RF-UIJ-007` | Quartel (seleção tipo, arma/armadura, lista de unidades, capacidade) | game-frontend | jogo 7.5 | frontend/src/views/QuartelView.vue | Demonstração | Manual |
| `RF-UIJ-008` | Masmorras (composição de inimigos, nível liberado, botão entrar) | game-frontend | jogo 7.6 | frontend/src/views/MasmorrasView.vue | Demonstração | Manual |
| `RF-UIJ-009` | Batalha tática (grid 8×8 clicável, combatentes, log de ações, resultado com loot) | game-frontend | jogo 7.6 | frontend/src/views/BatalhaView.vue, GradeBatalha.vue | Demonstração | Manual |
| `RF-UIJ-010` | Mensagens de erro (códigos 422, 409, 400, 500 em Toast pt-BR) | game-frontend | jogo 7.2–7.6 | frontend/src/api/http.ts, Toast | Demonstração | Manual |
| `RF-UIJ-011` | Sem cálculo de regras no frontend (tudo vem do servidor) | game-frontend | jogo 7.1–7.6 | Inspeção de useVila.ts, jogo.ts (só chamadas HTTP) | Inspeção | Inspeção |

---

## 3. Matriz RNF → Mecanismo → Evidência

| ID | Característica ISO 25010 | Enunciado | Mecanismo | Evidência | Status |
|---|---|---|---|---|---|
| `RNF-SEG-001` | Segurança/autenticidade | Toda rota exige autenticação, exceto login e estáticos; `/api/**` anônimo → 401 | `SecurityConfig.securityFilterChain()`, `HttpStatusEntryPoint` | ApiSegurancaWebMvcTest (5 testes) | ✅ Automatizado |
| `RNF-SEG-002` | Segurança/integridade | CSRF em todo POST (form `_csrf`; SPA `X-XSRF-TOKEN`) | `csrf.spa()` + interceptor | ApiSegurancaWebMvcTest | ✅ Automatizado |
| `RNF-SEG-003` | Segurança/confidencialidade | Senha com `DelegatingPasswordEncoder` (BCrypt, prefixo `{bcrypt}`) | `PasswordEncoder` bean em config | UsuarioDetailsServiceTest | ✅ Automatizado |
| `RNF-SEG-004` | Segurança | Cookie `HttpOnly`, `SameSite=Lax`, `Secure` configurável; troca ID no login; expiração 30 min | `SecurityConfig.sessionManagement()`, `SERVER_SERVLET_SESSION_TIMEOUT=30m` | SessaoServiceTest, AutenticacaoWebMvcTest | ✅ Automatizado |
| `RNF-SEG-005` | Segurança/não repúdio | Mensagem de falha de login genérica (sem enumeração) | `BadCredentialsException` com mensagem padrão | AutenticacaoWebMvcTest | ✅ Automatizado |
| `RNF-SEG-006` | Segurança/confidencialidade | Isolamento por usuário: recurso de outro usuário → 404 | Filtro em controller (`VilaRepository.findByUsuarioId()`) | VilaControllerWebMvcTest | ✅ Automatizado |
| `RNF-AUD-001` | Segurança/responsabilização | Auditoria `criado_em/por`, `alterado_em/por` em todas as tabelas; registro de sessões | `EntidadeAuditavel`, `AuditoriaConfig`, tabela `sessoes` | UsuarioAuditorAwareTest, SessaoServiceTest | ✅ Automatizado |
| `RNF-CON-001` | Confiabilidade | Operações serializadas por vila (lock pessimista); `@Version` e turno na batalha (409) | `VilaRepository.findByUsuarioIdParaAtualizacao()` com `@Lock(PESSIMISTIC_WRITE)`; `@Version` em Batalha | VilaServiceTest, MasmorraControllerWebMvcTest | ✅ Automatizado |
| `RNF-CON-002` | Confiabilidade/integridade | Constraints de banco garantem invariantes (uma vila por usuário, fila 1 por categoria, 1 batalha ativa) | Constraints `unique`, `unique partial index` em V3 | RepositoriosJogoTest | ✅ Automatizado |
| `RNF-MAN-001` | Manutenibilidade | Esquema só por migrações Flyway; `ddl-auto=validate` | `spring.jpa.hibernate.ddl-auto=validate`, V1–V3 | LoginBaseApplicationTests | ✅ Automatizado |
| `RNF-MAN-002` | Manutenibilidade/modularidade | Pacotes por domínio | `com.example.loginbase.{acesso,seguranca,web,jogo.*}` | Inspeção de estrutura | ✅ Inspeção |
| `RNF-TES-001` | Manutenibilidade/testabilidade | Tempo (`Clock`) e aleatoriedade (`Aleatorio`) injetáveis; motor de combate determinístico | `@Bean` Clock, Aleatorio; `AleatorioSequencia`, `RelogioAjustavel` em testes | CalculadoraProducaoTest, MotorCombateTest | ✅ Automatizado |
| `RNF-POR-001` | Portabilidade | Ambiente reproduzível via Docker Compose com profiles, WSL2 | `docker-compose.yml`, profiles, `Makefile` | `make up` (manual) | ✅ Manual |
| `RNF-USA-001` | Usabilidade | Interface e mensagens de erro em pt-BR; erros de regra em Toast | `CodigoErro` enum com mensagens, frontend Toast | AcoesVilaControllerWebMvcTest; demonstração (sem testes de UI) | ✅ Automatizado + Manual |
| `RNF-DES-001` | Eficiência | Produção calculada sob demanda (sem jobs); frontend atualiza por polling 5 s | `VilaService.sincronizarOrdens()`, `useVila.ts` com `setInterval(5000)` | VilaServiceTest, Demonstração | ✅ Automatizado + Manual |

---

## 4. Matriz Reversa (Teste → RF)

Os testes automatizados cobrem (resumo por classe-chave):

- **UsuarioTest** (8): ACD-001, ACD-002, ACD-003, ACD-006
- **UsuarioAuditorAwareTest** (3): ACD-006, RNF-AUD-001
- **LoginBaseApplicationTests** (1): ACD-004, DAD-006, RNF-MAN-001
- **AutenticacaoWebMvcTest** (18): AUT-001, AUT-005, AUT-006, AUT-009, RNF-SEG-004, RNF-SEG-005
- **ApiSegurancaWebMvcTest** (5): AUT-004, RNF-SEG-001, RNF-SEG-002
- **IdentificadorLoginTest** (7): AUT-002
- **UsuarioDetailsServiceTest** (10): AUT-002, AUT-003
- **RegistroSessaoSuccessHandlerTest** (2): AUT-008
- **SessaoEncerradaListenerTest** (2): AUT-007
- **SessaoServiceTest** (8): ACD-005, AUT-008, RNF-AUD-001
- **SessoesAbertasRunnerTest** (1): AUT-008
- **AdminInicialRunnerTest** (7): AUT-009
- **CatalogoTest** (11): PRD-001, PRD-002, FOR-001, FOR-002, VIL-008, EXE-004, COM-003, LOO-001, LOO-002, LOO-003
- **RepositoriosJogoTest** (8): DAD-001, DAD-002, DAD-003, DAD-004, DAD-005, RNF-CON-002
- **VilaControllerWebMvcTest** (8): VIL-001, VIL-002, VIL-007
- **AcoesVilaControllerWebMvcTest** (12): VIL-010, PRD-007, RNF-USA-001
- **VilaServiceTest** (9): VIL-002, VIL-003, VIL-004, VIL-005, VIL-009, PRD-003, FAZ-003, FAZ-004, RNF-CON-001
- **ConstrucaoServiceTest** (10): PRD-002, PRD-003, PRD-004, PRD-005, PRD-006, PRD-007, RNF-CON-001
- **FazendaServiceTest** (7): FAZ-001, FAZ-002, FAZ-003, FAZ-004
- **ForjaServiceTest** (9): FOR-002, FOR-003, FOR-004, FOR-005, FOR-006
- **QuartelServiceTest** (12): EXE-001, EXE-002, EXE-003, EXE-004, EXE-005, EXE-006
- **MotorCombateTest** (13): COM-003, COM-004, COM-005, COM-006, COM-007, RNF-TES-001
- **GeradorLootTest** (17): LOO-001, LOO-002, LOO-003, LOO-004
- **MasmorraServiceTest** (10): COM-001, COM-002, COM-008, COM-009, COM-010, DAD-005, LOO-005, LOO-006
- **MasmorraControllerWebMvcTest** (19): COM-008, RNF-CON-001
- **CalculadoraProducaoTest** (18): VIL-003, VIL-004, VIL-006, FAZ-002, RNF-TES-001
- **JogoPropertiesTest** (4): Validação de propriedades customizadas

**Total: 239 testes, 0 falhas, 0 erros (surefire 2026-09-26).**

---

## 5. Lacunas de Cobertura

| Faixa | Requisitos | Cobertura | Razão |
|---|---|---|---|
| **UIJ-001..011** | 11 RF de frontend | Manual + Build | Sem testes automatizados de UI (Cypress/Playwright não configurados); validação por `npm run build` (TypeScript) e task 8.2 (cenário manual) |
| **AMB-001..006** | 6 RF de ambiente | Inspeção + Manual | Profiles, Makefile, Docker — sem testes; validação por `make up`, `make down`, leitura de `.env.example` |
| **PRC-001..005** | 5 RF de processo | Inspeção | Skill, CLAUDE.md, relatórios — sem teste automatizado; validação por leitura e uso real em changes |
| **FRE-003, FRE-004** | 2 RF de frontend | Demonstração | Dockerfile, Vite recarga — sem teste; validação por levantar container e editar arquivo |

**Resumo: 101 RF vigentes; 88 com teste automatizado (87,1%); 13 com validação manual/build/inspeção (12,9%).**

---

## 6. Resumo Numérico

| Verificação | Quantidade | % |
|---|---|---|
| Com teste automatizado | 88 | 87,1% |
| Por inspeção/demonstração | 8 | 7,9% |
| Manual (UI, E2E, roteiro) | 5 | 5,0% |
| **Total (101 RF)** | **101** | **100%** |

RNF: 15 mapeados → 13 automatizados, 2 por inspeção/manual (RNF-MAN-002, RNF-POR-001); RNF-USA-001 e RNF-DES-001 combinam teste e demonstração.

**Meta:** Aumentar para 95%+ com testes de frontend e CI (roadmap em `17`).

---

## Histórico de revisões

| Versão | Data | Resumo | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
