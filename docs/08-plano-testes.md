# Plano de testes

| Campo | Valor |
|---|---|
| Versão | 1.0.0 |
| Data | 2026-09-27 |
| Status | Vigente — baseline do commit `454ae58` |
| Modelo/norma | ISO/IEC/IEEE 29119-3 (Test Plan + Test Strategy) |
| Público | QA, desenvolvedores |
| Fontes | `src/test/java/**`, `target/surefire-reports/`, `pom.xml`, `frontend/package.json`, design.md §20 |

> Parte da [documentação do login_base](README.md). Descreve a estratégia, níveis e inventário de testes automatizados; aborda lacunas de CI e cobertura; inclui roteiro manual de integração e como executar a suíte.

---

## 1. Contexto e escopo

### Itens de teste

- **Backend Java**: controladoras REST, serviços de lógica de jogo, repositórios JPA, validações, autenticação, autorização e auditoria.
- **Integração banco de dados**: migrações Flyway, persistência com Hibernate, operações sob concorrência com lock pessimista.
- **APIs do jogo**: vila, construção, fazenda, forja, quartel, masmorra, combate, loot.
- **Segurança**: autenticação por formulário, CSRF, sessão HTTP, gestão de usuários.

### Fora de escopo

- **Testes de carga/stress**: não configurados.
- **Segurança automatizada**: sem SAST, sem varredura de dependências.
- **Frontend**: sem testes unitários ou E2E automatizados; apenas `npm run build` com `vue-tsc` para checagem de tipos.
- **CI/CD**: sem pipeline (sem `.github/workflows/`, sem outro servidor de CI).

---

## 2. Estratégia de testes

### Níveis e técnicas

| Nível | Escopo | Anotação | Técnica | Exemplos |
|---|---|---|---|---|
| **Unitário puro** | Lógica sem dependências; método isolado | `@Test` sem contexto Spring | Valores-limite, sequências determinísticas | `CalculadoraProducaoTest`, `GeradorLootTest` |
| **Integração JPA** | Repositórios e persistência; banco real | `@DataJpaTest` | Ciclo completo cria→lê→atualiza; constraint do banco | `RepositoriosJogoTest`, `ConstrucaoServiceTest` |
| **Fatia web** | Controladoras REST + segurança | `@WebMvcTest` + `@WithMockUser` | Requisição HTTP simulada; validação de response | `VilaControllerWebMvcTest`, `MasmorraControllerWebMvcTest` |
| **Contexto completo** | Aplicação inteira; banco real | `@SpringBootTest` | Fluxo ponta a ponta; startup/shutdown | `LoginBaseApplicationTests` (1 teste contextLoad) |
| **Manual E2E** | Cenário humano; UI real | Navegador + `JOGO_VELOCIDADE=60` | Roteiro sequencial; observação visual; curl/Postman | Tarefa 8.2 deste plano |

### Padrões de teste

- **Dados determinísticos**: uso de `AleatorioSequencia` (sequência pré-definida) e `RelogioAjustavel` (relógio mockado) para reproduzir comportamentos do jogo (loot, produção, IA).
- **Cenários de specs**: testes derivados dos `Scenario` Gherkin das capabilities OpenSpec; cada RF tem ao menos uma verificação.
- **Valores-limite**: testes de fronteira (nível máximo 5, capacidade 0, recursos 0, fila cheia).
- **Erros esperados**: validação de códigos `RegraJogoException` (`RECURSOS_INSUFICIENTES`, `FILA_OCUPADA`, etc.) com HTTP 422.

---

## 3. Ambiente de teste

### Pré-requisitos

- **Java**: versão 25 (JDK configurado na IDE ou no `JAVA_HOME`).
- **Maven**: wrapper `./mvnw` incluso; não requer instalação.
- **PostgreSQL**: serviço `db` do compose obrigatório para testes `@DataJpaTest` e `@SpringBootTest`.
  - Subir com: `make up` (inicia `db` + `frontend`).
  - Profile padrão: `PROFILE_DB=local` (já ativo no `.env`).
  - Credenciais: `login_base` / `login_base` / `change-me` (do `.env.example`).
  - Porta: 5432 (mapeada do container).
- **Node.js** (opcional, só para frontend): Node 26 + npm 12 (rodam no container `frontend`).

### Inicialização

```bash
# Levantar o banco e frontend (backend não sobe, roda na IDE)
make up

# Para rodar testes (mvnw alcança db em localhost:5432)
./mvnw test

# Derrubar sem apagar volumes (mantém dados)
make down

# Derrubar e apagar volumes (reset completo)
make down_v
```

---

## 4. Inventário de testes

**27 classes de teste, 239 testes no total (execução 2026-09-26), 0 falhas.**

| Classe | Nível | Nº Testes | Requisitos afetados |
|---|---|---|---|
| `LoginBaseApplicationTests` | `@SpringBootTest` | 1 | Contexto de boot |
| `acesso.UsuarioTest` | Unitário | 8 | RF-ACD-001 (usuário, e-mail) |
| `auditoria.UsuarioAuditorAwareTest` | Unitário | 3 | RNF-AUD-001 (campos `criado_por`, `alterado_por`) |
| `jogo.JogoPropertiesTest` | Unitário | 4 | RF-VIL-006 (velocidade) |
| `jogo.api.AcoesVilaControllerWebMvcTest` | `@WebMvcTest` | 12 | RF-PRD, RF-FAZ, RF-FOR, RF-EXE (actions) |
| `jogo.api.MasmorraControllerWebMvcTest` | `@WebMvcTest` | 19 | RF-COM-001..010 (iniciar, ação em combate) |
| `jogo.api.VilaControllerWebMvcTest` | `@WebMvcTest` | 8 | RF-VIL-007 (consultar vila) |
| `jogo.catalogo.CatalogoTest` | Unitário | 11 | RF-VIL-008 (catálogo de regras) |
| `jogo.construcao.ConstrucaoServiceTest` | `@DataJpaTest` | 10 | RF-PRD-003..007 (fila, validação, custo) |
| `jogo.dominio.RepositoriosJogoTest` | `@DataJpaTest` | 8 | RF-DAD-001..006 (tabelas, constraints) |
| `jogo.economia.CalculadoraProducaoTest` | Unitário | 18 | RF-VIL-003..005 (produção, capacidade) |
| `jogo.economia.VilaServiceTest` | `@DataJpaTest` | 9 | RF-VIL-001..007 (vila, isolamento) |
| `jogo.fazenda.FazendaServiceTest` | `@DataJpaTest` | 7 | RF-FAZ-001..004 (canteiro, cultivo) |
| `jogo.forja.ForjaServiceTest` | `@DataJpaTest` | 9 | RF-FOR-001..006 (item, receita, fila) |
| `jogo.masmorra.GeradorLootTest` | Unitário | 17 | RF-LOO-001..006 (loot, sementes) |
| `jogo.masmorra.MasmorraServiceTest` | `@DataJpaTest` | 10 | RF-COM-002, RF-VIL-007 (masmorra, estado) |
| `jogo.masmorra.combate.MotorCombateTest` | Unitário | 13 | RF-COM-003..010 (motor, IA, dano, defesa, turnos) |
| `jogo.quartel.QuartelServiceTest` | `@DataJpaTest` | 12 | RF-EXE-001..006 (tropa, treino, capacidade) |
| `seguranca.AdminInicialRunnerTest` | Unitário | 7 | RF-AUT-009 (admin inicial por variável) |
| `seguranca.IdentificadorLoginTest` | Unitário | 7 | RF-AUT-002 (normalização e-mail/celular) |
| `seguranca.RegistroSessaoSuccessHandlerTest` | Unitário | 2 | RF-AUT-008 (registro em tabela `sessoes`) |
| `seguranca.SessaoEncerradaListenerTest` | Unitário | 2 | RF-AUT-007 (logout e encerramento) |
| `seguranca.SessaoServiceTest` | Unitário | 8 | RF-AUT-007 (sessão HTTP, fechamento) |
| `seguranca.SessoesAbertasRunnerTest` | Unitário | 1 | RF-AUT-008 (startup cleanup) |
| `seguranca.UsuarioDetailsServiceTest` | Unitário | 10 | RF-AUT-003 (autoridades, perfil) |
| `web.ApiSegurancaWebMvcTest` | `@WebMvcTest` | 5 | RNF-SEG-001 (401 em `/api/**`), RNF-SEG-002 (CSRF) |
| `web.AutenticacaoWebMvcTest` | `@WebMvcTest` | 18 | RF-AUT-001..006 (login, logout, formulário) |

**Suporte (não são testes)**:
- `jogo.suporte.AleatorioSequencia`: implementação determinística de `Aleatorio` com sequência pré-definida.
- `jogo.suporte.JogoTestConfig`: `@TestConfiguration` com `Clock` e `Aleatorio`.
- `jogo.suporte.RelogioAjustavel`: `java.time.Clock` ajustável com avanço manual.
- `web.ControladorTesteApi`: controller auxiliar para testes de segurança.

**Dependências de teste**:
- JUnit 5 (Jupiter).
- AssertJ (asserções fluentes).
- Mockito (mocks e spies).
- spring-security-test (`@WithMockUser`, etc.).

---

## 5. Última execução (2026-09-26)

```
[INFO] -------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
[INFO] Finished at: 2026-09-26T...
[INFO] -------------------------------------------------------

Resumo:
  - Testes executados: 239
  - Falhas: 0
  - Erros: 0
  - Ignorados: 0
  - Taxa de sucesso: 100%
```

**Data do relatório**: 2026-09-26 (baseline commit `454ae58`).
**Ambiente**: PostgreSQL 17 em container, backend Java 25, JUnit 5 + AssertJ.

---

## 6. Cobertura de testes

### Status atual

**Não há ferramenta de cobertura instalada** (JaCoCo ausente do `pom.xml`). A cobertura é expressa **por requisito funcional** (ver [`15-rastreabilidade.md`](15-rastreabilidade.md) §5):

- **Requisitos com teste automatizado**: RF-ACD-001, RF-AUT-001..010, RF-VIL-001..008, RF-PRD-001..007, RF-FAZ-001..004, RF-FOR-001..006, RF-EXE-001..006, RF-COM-001..010, RF-LOO-001..006, RF-DAD-001..006, RF-JOG (aplicação inteira).
- **Requisitos com verificação manual**: RF-UIJ-001..011 (frontend, observação visual).
- **Requisitos com inspeção/demonstração**: RF-AMB-001..006 (Docker, Makefile), RF-PRC-001..005 (processo).

### Frontend

Não há testes unitários ou E2E automatizados. Verificação por:
- **Checagem de tipos**: `npm run build` (dentro do container: `docker compose exec frontend npm run build`) executa `vue-tsc -b`, validando TypeScript.
- **Demonstração manual**: cenário da tarefa 8.2 (seção §8 deste plano).

---

## 7. Como executar

### Rodar todos os testes

```bash
# Levantar o banco (obrigatório)
make up

# Testes completos com saída padrão
./mvnw test

# Testes com saída verbosa (surefire)
./mvnw -X test

# Gerar relatório Surefire (em target/surefire-reports/)
./mvnw test surefire-report:report
```

### Executar teste específico

```bash
# Por classe de teste completa
./mvnw -Dtest=MotorCombateTest test

# Por método específico (padrão)
./mvnw -Dtest=MotorCombateTest#ataqueComDanoMinimoDeUm test

# Por padrão de nome
./mvnw -Dtest=*Producao* test
```

### Build e checagem frontend

```bash
# Dentro do container frontend
docker compose exec frontend npm run build

# Ou localmente (requer Node 26 + npm 12)
cd frontend
npm install --no-audit --no-fund
npm run build
cd ..
```

### Validação OpenSpec

```bash
# Valida spec da change (índice e slugs)
openspec validate add-city-builder-game --strict
```

### Limpeza

```bash
# Apagar relatórios Surefire
rm -rf target/surefire-reports/

# Reset de banco (volume) para próxima execução
make down_v
```

---

## 8. Roteiro de teste manual (E2E)

### Preparação

```bash
# Derrubar ambiente anterior (se houver)
make down

# Levantar com velocidade acelerada (60×)
JOGO_VELOCIDADE=60 make up
```

### Fluxo de teste (duração estimada: 3 minutos)

**1. Login**
- Abrir `http://localhost:5173` no navegador.
- Fazer login com `admin@loginbase.local` / `<ADMIN_PASSWORD>` (do `.env`).
- Redireciona para `/` (SPA).

**2. Vila criada**
- Página exibe "Vila de [nome]".
- Estado inicial: `CENTRO_VILA` nível 1, `ARMAZEM` N1, `FAZENDA` N1, `SERRARIA` N1, `PEDREIRA` N1, `MINA_FERRO` N0, `FORJA` N0, `QUARTEL` N0.
- Recursos: 300 comida, 400 madeira, 300 pedra, 50 ferro.

**3. Construir Mina de Ferro (nível 1)**
- Navegação: → Vila → Melhorar `MINA_FERRO`.
- Tempo: ~1–2 s (velocidade 60×).
- Observar: ProgressBar avança; ordem conclui.
- Verificação: nível sobe para 1; produção +10 ferro/hora visível.

**4. Construir Forja (nível 1)**
- Pré-requisito `MINA_FERRO` N1 satisfeito.
- Tempo: ~2 s.
- Verificação: `FORJA` nível sobe para 1.

**5. Construir Quartel (nível 1)**
- Pré-requisito `FORJA` N1 satisfeito.
- Tempo: ~2 s.
- Verificação: `QUARTEL` nível sobe para 1.

**6. Forjar itens**
- Navegação: → Forja.
- Forjar `ESPADA` nível 1, quantidade 1.
  - Tempo: ~1 s.
  - Inventário mostra "ESPADA N1", status DISPONIVEL.
- Forjar `ARMADURA_COURO` nível 1, quantidade 1.
  - Tempo: ~1 s.
  - Inventário mostra "ARMADURA_COURO N1", status DISPONIVEL.

**7. Treinar Soldado**
- Navegação: → Quartel.
- Selecionar tipo `SOLDADO`, arma `ESPADA N1`, armadura `ARMADURA_COURO N1`.
- Clique "Treinar".
  - Tempo: ~1 s.
  - Capacidade do exército: 3 (1 unidade em atividade, 0 na fila).
- Ordem conclui: unidade "SOLDADO" listada em "Unidades" com HP 30, ataque 6 (pela arma), defesa 3.
- Itens: status muda para EQUIPADO.

> **Correção D-10**: ID do combatente é `J1` (ordem no esquadrão), não `J<unidadeId>` por ID de banco.

**8. Entrar em Masmorra N1**
- Navegação: → Masmorras.
- Nível 1 liberado; composição: 3 goblins.
- Select unidade `SOLDADO`, clique "Entrar".
- Página redireciona para `/batalha/{id}`, turno 1.
- Grid 8×8 carregado: jogador `J1` em posição (2,7); 3 goblins em spawns S1–S3.

**9. Combate tático**
- Ações disponíveis: Mover, Atacar, Defender, Encerrar Turno, Render.
- Mover `J1` para (2,6) — caminho válido (BFS funciona).
- Encerrar turno: goblin se aproxima; turno incrementa de 1 para 2.
- Repetir mover/atacar até todos os goblins mortos.
- IA demonstrada: goblins minimizam Manhattan, atacam quando no alcance.
- Dano: `max(1, ataque_jogador − defesa_goblin)` aplicado.

**10. Vitória e Loot**
- Status muda para VITORIA; diálogo mostra loot:
  - Recursos garantidos: 40 comida, 50 madeira, 50 pedra, 20 ferro.
  - Sementes ou item (rolagem).
- Masmorra nível 2 desbloqueada (`masmorraNivelLiberado` = 2).

**11. Retorno e verificação**
- Voltar a `/masmorras`: nível 2 agora acessível.
- Voltar a `/`: vila com recursos aumentados, nível liberado visível.

### Verificações observáveis

- Cada GET `/api/jogo/vila` retorna `agora` (timestamp), recursos atualizados, ordens em progressão, batalha status.
- Velocidade aplicada: com `JOGO_VELOCIDADE=60`, tempos reduzem visualmente (~1 segundo visível = ~60 segundos de lógica).
- Log de batalha: eventos acumulam (mover, atacar, dano).
- Sem erros HTTP 500; erros 422/409/404 mostrados em Toast (se houver ações inválidas).
- Navegação fluida; frontend comunica com backend por polling 5 s.

---

## 9. Critérios de entrada, saída e suspensão

### Critério de entrada
- Ambiente levantado: `make up` OK, Postgres acessível, frontend compilado.
- Código compilado: `./mvnw clean package` sem erros.
- Specs validadas: `openspec validate --strict` OK.

### Critério de saída
- `./mvnw test` com sucesso (0 falhas).
- `npm run build` sem erros (frontend).
- Cenário manual 8.2 completável em ~3 minutos.
- Nenhum erro 500 em logs.

### Critério de suspensão
- Postgres não sobe: verificar `docker ps`, portas, volume.
- Erro de Flyway: volume antigo; corrigir com `make down_v`.
- Falha de compilação: lint/type-check; corrigir fonte.

---

## 10. Riscos e lacunas conhecidas

| Lacuna | Impacto | Mitigação |
|---|---|---|
| **Sem CI/CD** | Testes só rodam localmente; sem gate automático em PR. | Adicionar `.github/workflows/test.yml` (roadmap). |
| **Sem JaCoCo** | Impossível medir cobertura de linha; difícil identificar código morto. | Ativar JaCoCo (dependência + plugin Maven) em change futura. |
| **Sem testes E2E automatizados** | Cenário manual é tedioso; regressões do frontend só descobertas visualmente. | Adicionar Playwright/Cypress (fora de escopo). |
| **Sem testes de frontend** | Componentes Vue não têm verificação automática. | Adicionar Vitest (fora de escopo). |
| **Sem Testcontainers** | Testes dependem de banco local/container; não portável para CI. | Adicionar Testcontainers + TC (roadmap, risco de performance). |
| **Sem rate limiting nos testes** | Login bruteforce não testado. | Adicionar rate limit ao backend e teste correspondente. |

---

## 11. Rastreabilidade

Matriz de cobertura por capability: ver [`15-rastreabilidade.md`](15-rastreabilidade.md) §2–§5.

Cada RF tem pelo menos uma verificação (automatizada ou manual); RNF aparecem como testes de integração e segurança.

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
