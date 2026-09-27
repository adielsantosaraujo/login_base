# 0023 — SPA Servida pelo Backend: Assets em `/app/**` e Index como View Thymeleaf

| Campo | Valor |
|---|---|
| Status | Proposta |
| Data | 2026-09-27 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [add-frontend-build](../../openspec/changes/add-frontend-build/) (aberta) |

## Contexto e Problema

Atualmente, a SPA Vue é servida apenas em desenvolvimento via proxy do Vite (ADR 0014). Para produção, é necessário construir o frontend e servi-lo pelo backend Spring Boot. Há múltiplas estratégias: (a) assets em `static/app/` + index como view Thymeleaf com lista explícita de rotas; (b) tudo em `static/` com index cru; (c) catch-all `/**`; (d) build no host com Node. A escolha impacta controle de acesso, testabilidade, complexidade do build e compatibilidade com Docker.

## Direcionadores da Decisão

- **Controle de acesso**: assets públicos (sem autenticação), index servido via controller (autentica antes de render).
- **Testabilidade**: testes não dependem do build; template de teste em `src/test/resources`.
- **Docker**: build isolado em container efêmero (`docker compose run --rm --build`); propagação correta do exit code.
- **Compatibilidade Git**: artefatos gerados em `.gitignore`; clone limpo não contém build.
- **Segurança**: `/app/**` evita que o request cache salve um asset como destino pós-login; `/` passa por autenticação; dados sensíveis não no bundle.

## Opções Consideradas

### Opção (a): Assets em `/app/` + index como view — **ESCOLHIDA**

- Assets em `src/main/resources/static/app/`, servidos em `/app/**` (público, sem autenticação).
- `index.html` em `src/main/resources/templates/sistema/seguro/index.html` (view devolvida por `PaginaController` para rotas `/`, `/fazenda`, `/forja`, `/quartel`, `/masmorras`, `/batalhas/{id}`).
- Vite: `base: '/app/'` apenas no build; dev mantém `base: '/'`.
- `PaginaController`: lista explícita de rotas (não catch-all).

**Prós**: Separação clara entre assets (imutáveis) e view (autenticada); controle granular; seguro; cache funciona bem.  
**Contras**: Dupla manutenção se rotas da SPA mudarem (também alterar `PaginaController`).

### Opção (b): Tudo em `static/` com index cru

- Index servido como arquivo estático puro em `/` (sem passar pelo controller).
- Risco: GET direto no `index.html` sem autenticação ou CSRF.
- Risco: Não passa por `PaginaController`, sem lógica de segurança.

**Prós**: Simples.  
**Contras**: Índice acessível sem autenticação; sem controle de acesso; viola segurança.

### Opção (c): Catch-all `/**`

- `PaginaController` com `@GetMapping("/**")` para servir index em qualquer rota desconhecida.
- Risco: Capturaria `/api`, `/error`, `/login` e 404 reais.
- Difícil diagnosticar problemas; conflitos com filtros.

**Prós**: Dinâmico.  
**Contras**: Perigoso; captura rotas do backend; causa bugs obscuros.

### Opção (d): Build no host com Node

- Executar `npm run build` na máquina de desenvolvimento / CI.
- Exige Node.js instalado.
- Risco: Diferença de versão entre dev, CI e produção.

**Prós**: Mais rápido (sem Docker).  
**Contras**: Dependência do ambiente; instabilidade; não isolado.

## Resultado da Decisão

**Decidimos pela Opção (a)**: Assets em `static/app/` + index como view Thymeleaf com lista explícita de rotas.

**Racional**:
1. **Segurança**: `/app/**` é público (assets sem dados sensíveis); `/` passa por autenticação via `PaginaController`.
2. **Testabilidade**: Template de teste em `src/test/resources/templates/sistema/seguro/index.html` permite rodar testes sem build.
3. **Consistência**: Adota padrão Spring (controllers para páginas, handlers estáticos para assets).
4. **Docker**: Build via `docker compose run --rm --build` é isolado e propaga exit code corretamente.
5. **Cache**: Separação assets/view permite que request cache funcione sem efeitos colaterais (evita que o request cache salve um asset como destino pós-login).

## Decisões Complementares (do Design)

### 1. Build via `scripts/build_front.py` + Docker Compose

- Script Python no estilo de `executar.py` (padronização).
- `docker compose run --rm --build frontend-build` executa o build.
- Validação: confere existência de `frontend/dist/index.html`.
- Log: `build.log` persistente na raiz (truncado a cada execução).

### 2. Serviço Dedicado `frontend-build` com Profile `build`

- Não interfere no volume nomeado `frontend-node-modules` (dev).
- Volume anônimo `/app/node_modules` (build) é descartado no `--rm`.
- Profile `build` evita que o serviço suba automaticamente em `make up`.

### 3. Vite: `base: '/app/'` Apenas no Build

```typescript
defineConfig(({ command }) => ({
  base: command === 'build' ? '/app/' : '/',
  // ...
}))
```

- Dev continua com `base: '/'` (proxy não precisa de ajuste).
- Build seta `base: '/app/'` (assets referenciam `/app/**`).

### 4. PaginaController: Lista Explícita de Rotas

```java
@Controller
public class PaginaController {
  @GetMapping({"/", "/fazenda", "/forja", "/quartel", "/masmorras", "/batalhas/{id}"})
  public String spaIndex() {
    return "sistema/seguro/index";
  }
}
```

- Evita conflitos com `/api/**`, `/login`, `/error`.
- Seguro; fácil de diagnosticar problemas.
- Trade-off: rota nova exige alterar controller (documentado como DT-14).

### 5. SecurityConfig: `/app/**` em `permitAll`

- Assets são públicos, sem dados sensíveis.
- Evita que request cache salve um asset como destino pós-login (bug comum em SPAs).

### 6. Artefatos no `.gitignore`

- `build.log`, `frontend/dist/`, `src/main/resources/static/app/`, `src/main/resources/templates/sistema/seguro/index.html`.
- Clone limpo exige `make build_front` antes de `./mvnw package` ou `docker build`.

### 7. Testes: Template Próprio em `src/test/resources`

- Não dependem do build.
- `AutenticacaoWebMvcTest`:
  - `usuarioAutenticadoVeSejaBemVindo` passa a verificar `status 200 + view "sistema/seguro/index"`.
  - Novos testes: `/fazenda` autenticado → view; `/forja` anônimo → redirect `/login`; `/app/assets/qualquer.js` anônimo → não é redirecionado (≠ 302).

### Consequências Positivas

- **Separação de responsabilidades**: assets (imutáveis) vs. view (autenticada).
- **Segurança**: `/app/**` público sem dados sensíveis; `PaginaController` filtra rotas válidas.
- **Testabilidade**: testes rodam sem build; template de teste isolado.
- **Docker isolado**: build em container efêmero; sem poluição de volumes.
- **Rastreabilidade**: lista explícita de rotas facilita auditoria.
- **Compatibilidade**: clone limpo reproducível; artefatos não no git.

### Consequências Negativas

- **BREAKING em `/`**: deixa de servir placeholder "Seja bem vindo"; passa a servir SPA.
- **Duplicação de lista**: rotas da SPA também em `frontend/src/router/index.ts` e `PaginaController` (DT-14 — futura refatoração com catch-all se escopo crescer).
- **Clone limpo exige build**: sem `make build_front`, `GET /` → HTTP 500 (template não existe).
- **Build depende de Docker**: sem Docker, não há build de produção (aceitável para deploy containerizado).

## Alternativas Não Registradas (Design)

Design.md da change registra as 4 opções e justificativas detalhadas em "Decisions 1–10" e "Risks / Trade-offs".

## Mais Informações

- **Change de origem**: [add-frontend-build](../../openspec/changes/add-frontend-build/proposal.md)
- **Design relacionado**: [add-frontend-build/design.md](../../openspec/changes/add-frontend-build/design.md)
- **ADR relacionada**: [ADR 0014 — SPA via proxy do Vite](0014-spa-mesma-origem-proxy-vite.md) (complementada por esta)
- **Código principal**:
  - Backend: [`web/PaginaController.java`](../../src/main/java/com/example/loginbase/web/PaginaController.java)
  - Config: [`seguranca/SecurityConfig.java`](../../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java)
  - Build: `scripts/build_front.py` (previsto, ainda não existe)
  - Compose: [`docker-compose.yml`](../../docker-compose.yml)
  - Vite: [`frontend/vite.config.ts`](../../frontend/vite.config.ts)
- **Testes**: [`web/AutenticacaoWebMvcTest.java`](../../src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java)

---

## Histórico de Revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
