# Aplicações públicas (sem login)

Cada pasta dentro de `public/` contém um app Vue/Vite servido em uma rota pública (sem exigência de autenticação).

## Estrutura

```
frontend/public/
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

Cada app em `frontend/public/<nome>/` é:

1. **Buildado** com `make build_front`: executa `npm run build` e gera `dist/`.
2. **Copiado** para o backend:
   - Assets (`dist/*` exceto `index.html`) → `src/main/resources/static/<nome>/`
   - Template (`dist/index.html`) → `src/main/resources/templates/sistema/public/<nome>/index.html`
3. **Roteado** pelo `PaginaController`:
   - `GET /<nome>/index` → renderiza o template
   - `GET /<nome>/**` (até 5 níveis) → serve a SPA (vue-router rota no cliente)
   - `GET /<nome>/assets/**` → serve estáticos

**Exemplo:** app `cadastro_usuario` fica acessível em:
- `http://localhost:8080/cadastro_usuario/index` (entrada)
- `http://localhost:8080/cadastro_usuario/*` (rotas da SPA)
- `http://localhost:8080/cadastro_usuario/assets/*` (CSS, JS, fontes)

## Criando um app novo

1. **Copiar um app existente:**
   ```bash
   cp -r frontend/public/cadastro_usuario frontend/public/seu-novo-app
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
   - Adicionar um novo método (analogamente a `indexCadastroUsuario()`):
   ```java
   @GetMapping({ "/seu-novo-app/index",
       "/seu-novo-app/{s1:[^.]+}",
       // ... até 5 níveis
   })
   String indexSeuNovoApp() {
       return "sistema/public/seu-novo-app/index";
   }
   ```

7. **Testar:**
   - Acessar `http://localhost:8080/seu-novo-app/index`

## Restrições e convenções

- **Nome:** Use minúsculas com dígitos, `_` e `-` (`^[a-z0-9_-]+$`). Ex.: `meu-app`, `cadastro_usuario`, `app2`.
- **Unicidade:** O nome deve ser único dentro de `frontend/public/` E `frontend/seguro/`.
- **Sem login:** Qualquer pessoa pode acessar estas rotas.
- **Assets:** Todos os assets estão em `/<nome>/assets/**`; refira-se assim no `vite.config.ts`.

## Build e deploy

```bash
# Buildar este app ou todos
make build_front seu-novo-app    # Apenas este
make build_front                  # Todos

# Resultado
# - Templates em src/main/resources/templates/sistema/public/<nome>/
# - Estáticos em src/main/resources/static/<nome>/
```

Antes de fazer o JAR final:
```bash
make build_front                  # Builda todos os apps
./mvnw -DskipTests package        # Compila o JAR
```

## Veja também

- [`frontend/seguro/README.md`](../seguro/README.md) — apps com autenticação
- [`docs/operacao/referencia/comandos-make-e-scripts.md`](../../docs/operacao/referencia/comandos-make-e-scripts.md) — detalhes de `make build_front`
- [`docs/desenvolvimento/guias/desenvolver-o-frontend.md`](../../docs/desenvolvimento/guias/desenvolver-o-frontend.md) — guia prático de desenvolvimento
