# Glossário

| Campo | Valor |
|---|---|
| Versão | 1.3.0 |
| Data | 2026-09-28 |
| Status | Vigente — baseline do commit `454ae58` + changes `add-frontend-build`, `add-soldier-names-batch-slots` e `raise-building-max-level-100` implementadas |
| Modelo/norma | Glossário alfabético técnico e de negócio |
| Público | todos (desenvolvedores, QA, jogadores) |
| Fontes | `openspec/changes/archive/2026-09-27-add-city-builder-game/design.md`; `jogo/catalogo/*.java`; `jogo/dominio/*.java`; `CLAUDE.md`; `.claude/skills/dev-subagentes/SKILL.md`; `openspec/changes/add-frontend-build/design.md` e `proposal.md` |

> Parte da [documentação do login_base](README.md). Definições de termos de jogo, acesso, técnicos e de processo usados em todo o projeto.

---

## Termos de jogo (domínio)

### Alcance
Distância máxima (em Manhattan) que uma arma pode atacar. Ex.: Espada tem alcance 1; Arco tem alcance 3. Código: `AtributosItem.alcance()`.

### Arma
Tipo de item forjável que confere ataque e alcance a uma tropa. Modelos: Espada, Lança, Arco. Status: `DISPONIVEL`, `RESERVADO`, `EQUIPADO`. Origem: `FORJA` ou `MASMORRA`. Classe: `Item`.

### Armadura
Tipo de item forjável que confere defesa extra a uma tropa. Modelos: Armadura de Couro, Armadura de Ferro. Restrições de nível aplicáveis. Classe: `Item`.

### Armazém
Prédio que aumenta a capacidade de estoque de recursos (madeira, pedra, ferro, comida) por nível. **Níveis 1–5:** `500 × 2^(N-1)` unidades; **níveis 6–100:** `round_half_up(8000 × (N/5)^p)` onde p é `JOGO_EXPOENTE_CURVA` (padrão 1,5). Não é pré-requisito de outros prédios. Classe: `TipoPredio.ARMAZEM`.

### Ataque (atributo)
Número que representa o dano base de uma arma. Calculado como `6 + 2(L-1)` para Espada N1+. Reduzido pela defesa efetiva do alvo. Código: `AtributosItem.ataque()`, `Combatente.ataque()`.

### Batalha
Encontro tático entre o jogador (até 4 unidades) e inimigos em masmorra. Persiste em `jogo_batalhas` com estado/log/loot em JSON. Máximo 30 turnos. Estados: `EM_ANDAMENTO`, `VITORIA`, `DERROTA`. Classe: `Batalha`.

### Canteiro
Posição na Fazenda (1 a N, N = número de canteiros; máx. 24 no nível 100) onde o jogador planta cultivos. **Níveis 1–5:** N canteiros = nível. **Níveis 6–100:** N = 5 + ⌊(nível − 5)/5⌋. Cada canteiro produz comida segundo o cultivo. Classe: `Canteiro`.

### Capacidade
Limite máximo de um recurso no armazém (comida, madeira, pedra, ferro). Calculada por `CalculadoraProducao.capacidadeMaxima(nível_armazem)`. **Níveis 1–5:** `500 × 2^(N-1) × 1000` milésimos; **níveis 6–100:** `round_half_up(8000 × (N/5)^p) × 1000` milésimos onde p é `JOGO_EXPOENTE_CURVA`. Quando cheia, produção para. Coluna: `jogo_vilas.{comida,madeira,pedra,ferro}`.

### Capacidade do exército
Número máximo de unidades que podem estar na vila (treinadas ou em treino). Baseada em `3 × nível_quartel`. Verificado em `QuartelService.treinar()`.

### Centro da vila
Prédio que não tem efeitos de nível, mas é obrigatório para qualquer melhoria e limita os demais: nenhum prédio passa do nível do centro. Classe: `TipoPredio.CENTRO_VILA`.

### Combatente
Participante de uma batalha (unidade do jogador ou inimigo). Cada combatente tem ID (ex.: `J1`, `I2`), tipo, HP, atributos (ataque, defesa, alcance, movimento), posição (x, y), flags de turno (moveu, agiu, defendendo). Classe: `Combatente`.

### Comida
Recurso medido em milésimos. Produzida pelos canteiros. Consumida no treino de tropas (ex.: Soldado consome 50 comida = 50 000 milésimos). Coluna: `jogo_vilas.comida`.

### Custo
Quantidades de recursos (madeira, pedra, ferro, comida) necessários para uma ação (construção, forja, etc.). Armazenado em `Custo` record. Fórmula escalável por nível: `base × 1,5^(N-1)`. Classe: `Custo`.

### Cultivo
Tipo de planta em um canteiro (Trigo, Milho, Batata, Abóbora Dourada). Cada cultivo tem taxa de produção fixa (comida/h) e requisito de semente (exceto Trigo). Produção é acumulativa se trocar cultivo. Enum: `Cultivo`.

### Defesa (atributo)
Número que reduz dano recebido. Defesa efetiva = defesa base × 2 se defendendo, senão defesa base. Soma-se defesa do tipo + defesa da armadura. Código: `AtributosItem.defesa()`, `Combatente.defesaEfetiva()`.

### Defesa efetiva
Defesa aplicada no cálculo de dano. Normalmente = defesa base. Se defendendo, = defesa base × 2. Dano = `max(1, ataque - defesa_efetiva)`. Método: `Combatente.defesaEfetiva()`.

### Derrota
Resultado de batalha quando: (1) todas as unidades do jogador morrem, (2) 30 turnos transcorrem sem vitória, ou (3) o jogador se rende. Nenhum loot é ganho. Enum: `EstadoBatalha.Resultado.DERROTA`.

### Dano
Valor de HP reduzido de um combatente após um ataque. Calculado como `max(1, ataque_atacante - defesa_efetiva_alvo)`. Sempre ≥1. Método: `MotorCombate.aplicarAtaque()`.

### Equilíbrio/Balanceamento
Ajuste de números (custos, tempos, atributos) para manter jogo justo e progressão suave. Ver [GDD §14 — Exemplos numéricos](12-gdd/12.14-gdd-exemplos-numericos.md).

### Expoente da curva
Variável `JOGO_EXPOENTE_CURVA` (padrão 1,5) que controla o crescimento de custos e capacidades acima do nível 5. Faixa permitida: 1,0–2,0 em múltiplos de 0,25 (1,0 · 1,25 · 1,5 · 1,75 · 2,0). Validada na inicialização da aplicação. Aplicada nas fórmulas de custo (expoente p) e capacidade do armazém (expoente p). Propriedade: `app.jogo.expoente-curva` em `application.properties`.

### Esquadrão
Seleção de até 4 unidades para uma batalha. Ordem fixa (J1, J2, J3, J4 = ID na ordem de seleção). Persiste durante a batalha. Classe: `IniciarBatalhaRequest.unidadeIds`.

### Esqueleto Arqueiro
Tipo de inimigo de masmorra. HP 12, ataque 6, defesa 0, alcance 3, movimento 2. Aparece a partir de Masmorra N2. Enum: `TipoInimigo.ESQUELETO_ARQUEIRO`.

### Estado da batalha
Snapshot de uma batalha em progresso: combatentes, turno, mapa, log, loot (se fim). Serializado em JSON na coluna `estado` de `jogo_batalhas`. Classe: `EstadoBatalha`.

### Estoque de sementes
Contagem de sementes de cada cultivo (Milho, Batata, Abóbora). Armazenado em `jogo_estoque_sementes` por vila + cultivo. Consumido ao plantar. Classe: `EstoqueSemente`.

### Fazenda
Prédio que oferece canteiros. Nível N = N canteiros (máx. 5). Sem pré-requisito. Classe: `TipoPredio.FAZENDA`.

### Fila
Sequência de ordens de uma categoria (construção, forja, treino). Uma fila por categoria; apenas 1 ordem ativa por vez. Ordem na fila: cronológica (mais antiga sai primeiro). Coluna: `CategoriaOrdem`.

### Ferro
Recurso medido em milésimos. Produzido pela Mina de Ferro. Consumido na forja de armas/armaduras de ferro. Coluna: `jogo_vilas.ferro`.

### Forja
Prédio que habilita forja de itens. Nível N = itens até nível N forjáveis. Pré-requisito: Mina de ferro N≥1. Classe: `TipoPredio.FORJA`.

### Forjador
Serviço que valida e cria ordens de forja. Verifica nível da Forja, disponibilidade de recursos, fila ocupada. Classe: `ForjaService`.

### Goblin
Tipo de inimigo de masmorra. HP 15, ataque 6, defesa 1, alcance 1, movimento 3. Primeiro inimigo (Masmorra N1). Enum: `TipoInimigo.GOBLIN`.

### Loot
Recompensa gerada após vitória em masmorra: recursos garantidos + rolagens (sementes/ferro/itens). Nenhum loot em derrota. Serializado em JSON na coluna `loot` de `jogo_batalhas`. Classe: `Loot`.

### Madeira
Recurso medido em milésimos. Produzida pela Serraria. Consumida em construção e forja. Coluna: `jogo_vilas.madeira`.

### Masmorra
Encontro tático com composição fixa de inimigos por nível (1–5). Liberação: jogador começa com nível 1 desbloqueado; vitória em nível N libera min(5, N+1). Classe: `Masmorra`.

### Milésimo
Unidade de recursos (÷1000). Usado internamente para suportar decimais em ponto fixo. Interface mostra valores ÷1000 (ex.: 300.000 milésimos = 300 comida). Coluna: `long`.

### Mina de ferro
Prédio que produz ferro. Nível N = +10×N ferro/h. Sem pré-requisito. Classe: `TipoPredio.MINA_FERRO`.

### Modelo (de item)
Tipo de item (Espada, Lança, Arco, Armadura de Couro, Armadura de Ferro). Cada modelo tem atributos por nível (ataque/defesa/alcance) e custo base. Enum: `ModeloItem`.

### Movimento
Distância máxima (em Manhattan) que um combatente pode se mover por turno. Tropas: 2–3. Inimigos: 1–3. Inteiros. Código: `TipoTropa.movimento()`, `TipoInimigo.movimento()`.

### Nivel de prédio
Nível de progresso (0–5) de um prédio. Nível 0 = não construído. Custo e tempo escalam exponencialmente: `base × 1,5^(N-1)`, `base × 2^(N-1)`. Coluna: `jogo_predios.nivel`.

### Nivel liberado de masmorra
Maior nível de masmorra que o jogador desbloqueou (1–5). Inicialmente 1. Incrementa com vitórias. Coluna: `jogo_vilas.masmorra_nivel_liberado`.

### Nivel máximo forjável
Nível máximo de item que pode ser forjado, limitado pelo nível da Forja. **Até nível 10 da Forja:** N; **níveis 11–50:** 10 + ⌊(N − 10)/5⌋; **níveis 51–100:** 18 + ⌊(N − 50)/10⌋. Exemplos: Forja N15 → N11 forjável; Forja N50 → N18; Forja N100 → N23. Campo: `VilaDto.nivelMaximoForjavel`.

### Nivel máximo
Nível 100. Limite superior para prédios. Hard cap na lógica de negócio. Constante: `TipoPredio.NIVEL_MAXIMO`. Masmorras e loot permanecem limitados a nível 5 (comportamento atual preservado).

### Obstáculo
Célula no mapa de masmorra que não pode ser ocupada ou atravessada. 8 obstáculos fixos no mapa padrão. Bloqueiam movimento e pathfinding. Array: `MapaMasmorra.obstaculos()`.

### Ordem
Ação agendada de uma categoria (CONSTRUCAO, FORJA, TREINO). Cada ordem tem alvo, nível, quantidade, tempo de conclusão, status (ativa/concluída). Fila serializada por categoria. Classe: `Ordem`.

### Orc
Tipo de inimigo de masmorra. HP 30, ataque 9, defesa 3, alcance 1, movimento 2. Aparece a partir de Masmorra N3. Enum: `TipoInimigo.ORC`.

### Pedra
Recurso medido em milésimos. Produzida pela Pedreira. Consumida em construção e forja. Coluna: `jogo_vilas.pedra`.

### Pedreira
Prédio que produz pedra. Nível N = +20×N pedra/h. Sem pré-requisito. Classe: `TipoPredio.PEDREIRA`.

### Posição
Coordenada (x, y) no mapa de masmorra (0–7, 0–7). Usada para localizar combatentes e obstáculos. Record: `MapaMasmorra.Posicao`.

### Prédio
Construção na vila (um de cada tipo). Tem nível (0–100), tipo, efeitos associados (capacidade, produção, custo, etc.). Classe: `Predio`.

### Produção
Taxa de geração de recursos por hora. Calculada sob demanda por `CalculadoraProducao`, baseada em níveis de prédios produtivos e cultivos. Afetada por `JOGO_VELOCIDADE`.

### Quartel
Prédio que habilita treino de tropas. Libera tropas por nível mínimo (Soldado 1, Arqueiro 2, Lanceiro 3) e capacidade 3×N. Pré-requisito: Forja N≥1. Classe: `TipoPredio.QUARTEL`.

### Render-se
Ação em combate que encerra a batalha em derrota imediata. Sem penalidade além de perder unidades já mortas. Método: `MotorCombate.render()`.

### Rolagem
Sorteio de loot após vitória (0–99). Faixas: 0–34 semente, 35–59 ferro extra, 60–99 item. Quantidade: `1 + ceil(N/2)` por nível de masmorra. Método: `GeradorLoot.gerar()`.

### Semente
Item consumível. Necessário para plantar cultivos que não sejam Trigo. Obtido em loot de masmorras. Armazenado em `EstoqueSemente`. Enum: `Cultivo`.

### Serraria
Prédio que produz madeira. Nível N = +30×N madeira/h. Sem pré-requisito. Classe: `TipoPredio.SERRARIA`.

### Sincronização
Cálculo lazy do estado da vila: aplicar produção e ordens vencidas até o instante atual. Executado sempre antes de ler ou modificar a vila. Método: `VilaService.sincronizar()`.

### Soldado
Tipo de tropa. HP 30, defesa base 1, movimento 3, comida 50, tempo 60s, quartel N≥1. Arma exigida: Espada. Enum: `TipoTropa.SOLDADO`.

### Spawn (ponto de)
Posição inicial de um inimigo no mapa. 5 pontos: S1, S2, S3, S4, S5. Inimigos aparecem sequencialmente nestes pontos. Map: `MapaMasmorra.spawnsInimigos()`.

### Status de item
Estado de um item (DISPONIVEL, RESERVADO, EQUIPADO). Muda durante treino (DISPONIVEL → RESERVADO) e equipe em batalha (→ EQUIPADO). Enum: `StatusItem`.

### Tempo de construção
Duração de uma ordem de construção. **Níveis 1–5:** `tempoBase × 2^(N-1)` segundos. **Níveis 6–100:** `ceil(tempoBase × 16 × N / 5)` segundos. Afetado por `JOGO_VELOCIDADE` (divide o valor). Coluna: `jogo_ordens.conclui_em`.

### Tropa
Unidade de combate treinada no Quartel. Composição: tipo + arma + armadura. Atributos derivados: HP (tipo), ataque/alcance (arma), defesa (tipo + armadura), movimento (tipo). Classe: `Unidade`.

### Troll
Tipo de inimigo de masmorra. HP 70, ataque 13, defesa 5, alcance 1, movimento 2. Pior inimigo (Masmorra N5). Enum: `TipoInimigo.TROLL`.

### Turno
Unidade de tempo em combate. Turno do jogador: mover + agir. Turno dos inimigos: IA executa. Máximo 30 turnos por batalha. Coluna: `jogo_batalhas.turno`.

### Unidade
Ver **Tropa**. Termo intercambiável. Classe: `Unidade`.

### Velocidade
Configuração global `JOGO_VELOCIDADE` (padrão: 1). Multiplica todas as taxas de produção e divide tempos de construção/forja/treino (arredonda para cima). Ex.: velocidade 60 = 60× mais rápido. Propriedade: `JogoProperties.velocidade`.

### Vigência
Intervalo de datas (data_inicial, data_final) em que um vínculo de usuário-perfil é válido. Perfil sem vigência ativa = usuário desabilitado. Coluna: `usuario_rel_perfis.{data_inicial,data_final}`.

### Vila
Estrutura central do jogo. Cada usuário tem uma vila. Contém: prédios, recursos, canteiros, sementes, itens, unidades, ordens, batalhas. Classe: `Vila`.

### Vitória
Resultado de batalha quando todos os inimigos morrem. Jogador colhe loot garantido + rolagens. Masmorra N desbloqueará N+1. Enum: `EstadoBatalha.Resultado.VITORIA`.

---

## Termos de acesso e segurança

### Admin inicial
Usuário criado automaticamente no startup via `ApplicationRunner`. Credenciais: `ADMIN_EMAIL` e `ADMIN_PASSWORD` (variáveis de ambiente). Perfil: `ADMIN`. Classe: `AdminInicialRunner`.

### Auditoria
Rastreamento de criação/edição: colunas `criado_em`, `criado_por`, `alterado_em`, `alterado_por`. Aplicado via `@EntityListeners(AuditingEntityListener.class)`. Autor: email ou `"sistema"`. Tabelas: todas em `V1` e `V3`.

### Autoridade
Papel de segurança Spring: `ROLE_<nome_perfil>`. Ex.: `ROLE_ADMIN` para perfil Admin. Carregada do banco em `UsuarioDetailsService`. Não usada em rotas (apenas autenticação simples).

### Contato
Identificador de login: **e-mail** ou **celular** (11 dígitos). Normalizado: email em minúsculas, celular validado por regex `^[0-9]{11}$`. Classe: `IdentificadorLogin`.

### CSRF
Cross-Site Request Forgery. Mitigação: token `_csrf` em formulários POST (login), cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN` em SPAs. Middleware: `csrf.spa()`. Coluna: `_csrf`.

### JSESSIONID
Identificador de sessão HTTP. Cookie `HttpOnly`, `SameSite=Lax`, `Secure=false` (dev). Troca de ID no login. Registrado em tabela `sessoes` com SHA-256. Duração: 30 minutos inatividade.

### Permissão
Ação ou recurso que um perfil pode executar. Exemplos: "ler_user", "criar_order". Não usadas em rotas (estruturado mas não aplicado). Tabela: `permissoes`.

### Perfil
Papel de usuário (ex.: Admin, Jogador). Define autoridades. Vínculo com usuário é temporal (data_inicial, data_final). Tabela: `perfis`.

### Sessão
Conexão autenticada HTTP. Controle: cookie `JSESSIONID`. Registro em `sessoes` com IP, User-Agent, timestamp. Fechamento em logout/expiração/startup. Classe: `Sessao`.

### Usuário
Conta de login. Campos: nome, email (único em minúscula), celular (opcional, único), senha (`DelegatingPasswordEncoder` BCrypt). Tabela: `usuarios`. Classe: `Usuario`.

### Username
Campo identificador de login. No projeto = **e-mail** (normalizado para minúsculo). Nunca é celular. Usado em autenticação. Código: `IdentificadorLogin.extrairEmail()`.

### Vigência de perfil
Ver **Vigência** (acesso). Data_inicial ≤ hoje ≤ data_final = ativo. Validação em `UsuarioDetailsService`.

### XSRF-TOKEN
Cookie enviado pelo servidor; contém token CSRF. Cliente o lê e envia como header `X-XSRF-TOKEN` em requisições SPA POST. Validado por Spring. Nome: `XSRF-TOKEN`.

---

## Termos técnicos (comportamento futuro)

### Assets (`/app/**`)

> 

Arquivos estáticos (CSS, JS, imagens) do frontend Vue/Vite após build de produção. Servidos em `/app/**` pelo backend Spring sem autenticação.

### Base do Vite


Propriedade `base` do `vite.config.ts` que define o caminho raiz dos assets. Dev server: `/` (sem restrição). Build de produção: `/app/` (quando `command === 'build'`), permitindo que o frontend referencie assets em `/app/**` sem erro 404.

### Build de produção do frontend (`make build_front`)


Alvo Makefile que executa `python3 ./scripts/build_front.py`, que por sua vez roda `docker compose run --rm --build frontend-build` (serviço Docker efêmero com profile `build`). Gera `frontend/dist/`, copia assets para `src/main/resources/static/app/` e index HTML para `src/main/resources/templates/sistema/seguro/index.html`. Tudo em `.gitignore`. Obrigatório em clone limpo antes de `./mvnw package` (do contrário, template não existe e GET `/` retorna HTTP 500).

### `build.log`


Arquivo de log gerado pelo script `scripts/build_front.py` após cada execução de `make build_front`. Registra saída de `docker compose run` e erros. Truncado a cada execução. Listado em `.gitignore`.

### Fallback do history mode


Mecânica do Vue Router em modo history (sem hash): quando o navegador acessa uma rota como `/fazenda` diretamente (F5 ou link externo), o backend devolve o mesmo HTML de índice (`index.html`). Implementado no `PaginaController` (mapeando `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}` para a view `sistema/seguro/index`), permitindo que o router no cliente decida qual página mostrar.

### `frontend-build` (serviço)


Serviço Docker no `docker-compose.yml` com profile `build` (não sobe em `make up`). Executa `npm run build` do frontend em ambiente isolado (`build: ./frontend` com base `node:26-trixie-slim`). Volume anônimo `/app/node_modules` descartado após `--rm`, garantindo build limpo.

### Profile `build`


Profile no `docker-compose.yml` que ativa o serviço `frontend-build` apenas quando explicitamente requisitado (`docker compose --profile build ...`). Não sobe automaticamente em `make up`. Garante que dev não é afetado.

---

## Termos técnicos (vigentes)

### Arc42
Modelo arquitetural em 12 seções (contexto, blocos, tempo de execução, imple­mentação, etc.). Adotado para [04-arquitetura.md](04-arquitetura.md). Template: arc42.org.

### C4
Modelo de representação de arquitetura em 4 níveis (contexto, container, componente, código). Diagramas em Mermaid (flowchart em vez de C4Context experimental). Usado em [04-arquitetura.md](04-arquitetura.md).

### DTO
Data Transfer Object. Classes que transportam dados entre controllers e clientes (JSON). Exemplos: `VilaDto`, `BatalhaDto`, `LootDto`. Padrão: records no projeto. Localização: `jogo/api/*.java`.

### JSON
Formato de serialização de dados. Estado e loot da batalha serializados com `ObjectMapper` (Jackson) em colunas `text`; o log é texto com uma linha por evento. Determinístico (sem UUID).

### Lock pessimista
Estratégia de concorrência: `SELECT FOR UPDATE` ao ler e manter até fim da transação. Aplicado em `VilaRepository.findByUsuarioIdParaAtualizacao()` com `@Lock(PESSIMISTIC_WRITE)`. Impede conflitos.

### MADR
Template de ADR (Architecture Decision Record) versão 4.0. Seções: status, contexto, direcionadores, opções, decisão, consequências. Usado em `docs/adr/0001…0022.md`.

### Mapa de características (quality attribute tree)
Estrutura hierárquica de metas de qualidade. Raiz: qualidade; filhos: performance, segurança, testabilidade, etc. Usado em [04-arquitetura.md](04-arquitetura.md) §10.

### Migração
Arquivo SQL versionado (Flyway) que altera o esquema do banco. Naming: `VN__descricao.sql`. Projeto tem V1 (acesso), V2 (admin), V3 (jogo). Imutável; nunca editar. Localização: `src/main/resources/db/migration/`.

### Otimistic locking
Ver versioning de entidade. Anotação `@Version` em campo `long`. Hibernate incrementa a cada atualização. Choque detectado via `ObjectOptimisticLockingFailureException` → 409 CONFLITO. Usado em `Batalha`.

### Performance
Velocidade de resposta. RNF-DES-001 exige "produção calculada sob demanda" (sem jobs em background). Polling do frontend é 5 segundos.

### REST
Estilo arquitetural de API. Rotas: `GET /api/jogo/vila`, `POST /api/jogo/predios/{tipo}/melhorar`, etc. Sem versionamento em path. Autenticação: `JSESSIONID`. CSRF: header `X-XSRF-TOKEN`. Formato: JSON.

### RTM (Rastreabilidade)
Matriz bidirecional RF → código → testes → verificação. Documento [15-rastreabilidade.md](15-rastreabilidade.md). Permite auditar se requisito foi implementado e testado.

### Segurança (RNF)
Requisito não-funcional. Aspectos: autenticação (login/sessão), autorização (rotas protegidas), confidencialidade (HTTPS em produção, senha hash), integridade (CSRF), não-repúdio (auditoria). Documento: [07-seguranca.md](07-seguranca.md).

### SPA

Single Page Application. Frontend Vue 3:
- **Dev:** servido pelo Vite dev server na porta 5173, com proxy para backend na porta 80.
- **Produção:** servido pelo Spring Boot em `/` (view `sistema/seguro/index` via Thymeleaf, gerada pelo `make build_front`) e assets em `/app/**`.
Roteamento em history mode (sem hash). Não usa Pinia; estado em composable `useVila`.

### Sprint
Iteração de desenvolvimento (não usado aqui; apenas OpenSpec changes).

### Testabilidade
Qualidade de ser testável. RNF-TES-001 exige Clock e Aleatorio injetáveis. Motor de combate é pure (sem side effects). Testes: unitários, JPA slices, web slices, integração.

### Transação
Atomicidade de múltiplas operações no banco. `@Transactional` em `VilaService` encapsula leitura com lock + modificação + persistência. Rollback automático em exceção. Propagação: padrão `REQUIRED`, alguns pontos usam `REQUIRES_NEW`.

### Velocidade/velocidade do jogo
Ver **Velocidade**. Multiplica produção, divide tempos.

### Versioning (otimista)
Anotação `@Version` em entidade. Hibernate mantém número de versão que incrementa em updates. Conflito de versão gera `ObjectOptimisticLockingFailureException` → 409 `CONFLITO`. (`TURNO_DESATUALIZADO` vem do campo `turno` da requisição.)

---

## Termos de processo (OpenSpec)

### Change
Unidade de trabalho em OpenSpec. Contém proposal (escopo/objetivo), design (arquitetura), specs (requisitos por capability), tasks (ações concretas), relatório de agentes. Localização: `openspec/changes/<data>-<nome>/` ou `archive/`.

### Capability
Funcionalidade ou área (ex.: `access-control-data`, `user-authentication`, `game-village`). Agrupador de requisitos. Cada capability tem uma spec (`spec.md`). Tabela: [README §4.6](README.md#46-identificadores-fixos-contrato-entre-lotes--usar-exatamente).

### Design document (design.md)
Documento dentro de cada change descrevendo decisões, trade-offs, riscos, não-goals. Seções: contexto, problema, direcionadores, opções, decisão, consequências, riscos. Escrito na fase de proposta.

### Especificação/Spec
Arquivo `spec.md` dentro de uma capability. Contém requisitos estruturados (Purpose, Requirement SHALL/MUST, Scenario Gherkin). Exemplo: `openspec/specs/access-control-data/spec.md`. Fonte de verdade.

### Harness
Camada de orquestração/tooling (não domínio de negócio). Exemplos: `.claude/`, `CLAUDE.md`, `Makefile`, scripts de CI. Distinto de "Negócio" (código do jogo e specs).

### Negócio
Lógica de domínio (código Java/TypeScript, banco de dados, UX, regras de jogo). Distinto de "Harness" (infraestrutura).

### Orquestrador
Agente (sessão principal) que coordena trabalho paralelo de subagentes Opus (design), Sonnet (código), Haiku (docs). Só o orquestrador marca `[x]` no `tasks.md`. Usa skill `dev-subagentes`.

### Onda
Batch de tarefas independentes executadas em paralelo por subagentes. Exemplo: onda 1 = Opus faz design; onda 2 = Sonnet implementa código; onda 3 = Haiku escreve docs.

### Proposal
Documento inicial de uma change descrevendo problema, solução proposta, objetivos, escopo. Avaliado antes de design/implementação. Arquivo: `proposal.md`.

### Requirement (RF)
Requisito funcional. Estrutura: ID (ex.: `RF-ACD-001`), enunciado SHALL/MUST, cenários. Tabela em [02-requisitos.md §3](02-requisitos.md).

### Scenario
Exemplo de teste em formato Gherkin (Dado/Quando/Então). Um scenario por caso de uso/RF. Localização: seções `Scenario` em `spec.md` ou exemplos em `tasks/*.md`.

### Spec delta
Especificação adicional em uma change (arquivada em 2026-09-27) que modifica specs anteriores. Marca: `ADDED`, `MODIFIED`, `REMOVED`. Exemplo: `openspec/specs/user-authentication/spec.md` (delta do RF-AUT-010).

### Subagente
Agente Claude dedicado a um tipo de trabalho (Opus = design, Sonnet = código, Haiku = docs). Sessão limpa por tarefa; sem context carryover. Recebe instruções e entrega resultado.

### Sync (sincronizar)
Operação que copia specs de uma change completa para `openspec/specs/` (tornando-a permanente). Só após archive. Comando: `openspec sync <change>`.

### Task
Unidade concreta de trabalho em uma change. Arquivo autocontido (ex.: `tasks/1.1-autenticacao-login.md`) com objetivo, arquivos a criar/alterar, verificação. Índice em `tasks.md`. Comando: `/opsx:apply <change> <id>`.

### Template de task
Arquivo modelo em `openspec/templates/task.md` com seções padrão (objetivo, arquivos, verificação). Redator copia este padrão para cada task da change.

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.3.0 | 2026-09-28 | Change raise-building-max-level-100 implementada: verbetes de armazém, capacidade, canteiros, prédios, tempo com duas faixas; novo verbete expoente curva e nível máximo forjável | Adiel, com apoio de agentes Claude |
| 1.2.0 | 2026-09-27 | Change add-frontend-build implementada: remove marcadores de previsto | Adiel, com apoio de agentes Claude |
| 1.1.0 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta) | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
