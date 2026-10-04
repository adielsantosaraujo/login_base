# Aplicações seguras (com autenticação)

Cada pasta dentro de `seguro/` contém um app Vue/Vite servido em uma rota protegida (exige login).

## Estrutura

```
frontend/seguro/
├── <nome>/                       # Nome único, minúsculas com dígitos, _ e - (a-z, 0-9, _, -)
│   ├── src/
│   │   ├── views/
│   │   ├── components/
│   │   ├── router/
│   │   └── main.ts
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── ... (outros arquivos)
```

## Como a rota é mapeada

Cada app em `frontend/seguro/<nome>/` é:

1. **Buildado** com `make build_front`: executa `npm run build` e gera `dist/`.
2. **Copiado** para o backend:
   - Assets (`dist/*` exceto `index.html`) → `src/main/resources/static/<nome>/`
   - Template (`dist/index.html`) → `src/main/resources/templates/sistema/seguro/<nome>/index.html`
3. **Roteado** pelo `PaginaController` e protegido por `SecurityConfig`:
   - `GET /<nome>/index` → renderiza o template (exige login)
   - `GET /<nome>/**` (até 5 níveis) → serve a SPA (vue-router rota no cliente, exige login)
   - `GET /<nome>/assets/**` → serve estáticos (exige login)

**Exemplo:** app `patrimonio` fica acessível em:
- `http://localhost:8080/patrimonio/index` (entrada, redireciona para login se anônimo)
- `http://localhost:8080/patrimonio/*` (rotas da SPA, exigem login)
- `http://localhost:8080/patrimonio/assets/*` (CSS, JS, fontes, exigem login)

## Autenticação e autorização

A proteção é garantida por `SecurityConfig.java`:
- Rotas `/<nome>/**` exigem autenticação (`@PreAuthorize("isAuthenticated()")` ou `SecurityConfig.authorizeRequests()`)
- Após login bem-sucedido, o usuário é redirecionado para `PAGINA_INICIAL` (definida em `PaginaController`)
- Sessões são registradas em tabela do banco (auditoria)

## Criando um app novo

1. **Copiar um app existente:**
   ```bash
   cp -r frontend/seguro/patrimonio frontend/seguro/seu-novo-app
   ```

2. **Editar `package.json`:**
   - Mudar `name` para o novo nome

3. **Atualizar `vite.config.ts`:**
   - Definir `base: process.env.VITE_FRONT_APP_NOME ? `/${process.env.VITE_FRONT_APP_NOME}/` : '/'`
   - (ou usar o template já definido)

4. **Editar `frontend/src/router/index.ts` e `main.ts`:**
   - Ajustar as rotas e o setup global conforme necessário

5. **Buildar:**
   ```bash
   make build_front seu-novo-app
   ```

6. **Mapear no `PaginaController.java`:**
   - Adicionar um novo método (analogamente a `indexPatrimonio()`):
   ```java
   @GetMapping({ "/seu-novo-app/index",
       "/seu-novo-app/{s1:[^.]+}",
       // ... até 5 níveis
   })
   String indexSeuNovoApp() {
       return "sistema/seguro/seu-novo-app/index";
   }
   ```

7. **Proteger em `SecurityConfig.java` (se necessário):**
   - Adicionar autorizações específicas (por defaut, tudo sob `/seu-novo-app/**` exige login)

8. **Testar:**
   - Acessar `http://localhost:8080/seu-novo-app/index` (deve redirecionar para login)
   - Fazer login e verificar se acessa a SPA

## Restrições e convenções

- **Nome:** Use minúsculas com dígitos, `_` e `-` (`^[a-z0-9_-]+$`). Ex.: `meu-app`, `patrimonio`, `app2`.
- **Unicidade:** O nome deve ser único dentro de `frontend/seguro/` E `frontend/public/`.
- **Com login:** Apenas usuários autenticados acessam estas rotas.
- **Assets:** Todos os assets estão em `/<nome>/assets/**`; refira-se assim no `vite.config.ts`.
- **Página inicial:** `PAGINA_INICIAL` em `PaginaController` define para onde redirecionar após login. Atualize se este app for a entrada principal.

## Build e deploy

```bash
# Buildar este app ou todos
make build_front seu-novo-app    # Apenas este
make build_front                  # Todos

# Resultado
# - Templates em src/main/resources/templates/sistema/seguro/<nome>/
# - Estáticos em src/main/resources/static/<nome>/
```

Antes de fazer o JAR final:
```bash
make build_front                  # Builda todos os apps
./mvnw -DskipTests package        # Compila o JAR
```

## Veja também

- [`frontend/public/README.md`](../public/README.md) — apps sem autenticação
- [`docs/operacao/referencia/comandos-make-e-scripts.md`](../../docs/operacao/referencia/comandos-make-e-scripts.md) — detalhes de `make build_front`
- [`docs/desenvolvimento/guias/desenvolver-o-frontend.md`](../../docs/desenvolvimento/guias/desenvolver-o-frontend.md) — guia prático de desenvolvimento
- [`docs/desenvolvimento/explicacoes/autenticacao-e-sessoes.md`](../../docs/desenvolvimento/explicacoes/autenticacao-e-sessoes.md) — detalhes de autenticação
