# 0004 — Vue 3 + Vite + PrimeVue 5 em Container Node 26

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-23 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-vue-frontend`](../../openspec/changes/archive/2026-09-23-add-vue-frontend/) |

## Contexto e problema

O projeto precisa de um frontend interativo. Há necessidade de escolher: framework Vue vs. React; bundler Vite vs. Webpack; integração com componentes de UI (PrimeVue); e como executar tudo em desenvolvimento sem exigir Node no host Windows. Hot reload é crítico para produtividade.

## Direcionadores da decisão

- Framework: Vue 3.5 é moderno, escalável, suportado em WSL2/Docker
- Bundler: Vite 8 é rápido e suporta hot reload por polling (WSL2 + DrvFs = inotify não funciona)
- UI: PrimeVue 5 oferece componentes prontos (Aura theme) com licença gratuita community
- Isolamento: Node 26 containerizado com volume `node_modules` em Docker, sem precisar Node no host
- TypeScript: suporte completo via template Vite

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Vue 3 + Vite + PrimeVue 5 em container** | Scaffold oficial Vite em Node 26, PrimeVue via MCP (`get_setup`), volume de node_modules. |
| React + Next.js | Alternativa moderna, mas não foi pedida. |
| Angular | Mais complexo; não alinha com simplicidade do projeto. |
| Bootstrap/Tailwind ao invés de PrimeVue | Componentes customizados vs. library pronta. |

## Resultado da decisão

Adotou-se **Vue 3.5 + Vite 8 + PrimeVue 5.0**:

1. **Scaffold**: `npm create vite@latest frontend -- --template vue-ts` (executado em container Node 26 com npm 12)
2. **PrimeVue**: instalação via MCP (`get_setup vite`), configuração em `main.ts`:
   ```typescript
   import PrimeVue from 'primevue/config';
   app.use(PrimeVue, {
     theme: { preset: 'aura' },
     license: import.meta.env.VITE_PRIMEUI_LICENSE
   });
   ```
3. **Dockerfile**: `FROM node:26-trixie-slim`, `RUN npm install -g npm@12`, volume `/app/node_modules`
4. **Hot reload via polling**: `VITE_USE_POLLING=true` (definido em `docker-compose.yml`), ativa `server.watch.usePolling` em `vite.config.ts`
5. **Licença**: `VITE_PRIMEUI_LICENSE` via variável de ambiente (`.env`), com aviso se ausente

### Consequências positivas
- **Hot reload funcional**: polling permite desenvolvimento sem editar arquivos manualmente
- **Node isolado**: `node_modules` em volume Docker, código no DrvFs (rápido)
- **Scaffold oficial**: Vite template traz `tsconfig` correto, sem desvios
- **PrimeVue fiel ao MCP**: documentação oficial (via MCP) garante compatibilidade

### Consequências negativas
- **Node 26 não é LTS**: pode ter incompatibilidades pontuais (mitigado com testes de build)
- **Polling consome CPU**: aceitável para projeto pequeno
- **Aviso de licença**: sem chave, PrimeUI mostra mensagem (documentado no README)
- **BREAKING**: `make up` passa a subir frontend por padrão (`PROFILE_FRONTEND=desativado` volta ao anterior)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Vue 3 + Vite + PrimeVue | Moderno; scaffold oficial; hot reload; PrimeVue pronto; Node isolado em container. | Node 26 não é LTS; polling tem overhead; aviso de licença sem chave. |
| Vue 3 + Webpack | Mais estável, Webpack é conhecido. | Mais lento que Vite; mais configuração. |
| React + Vite | Moderno, rápido. | Não foi pedido; PrimeReact seria alternativa de UI. |

## Mais informações

- **Design**: [`add-vue-frontend/design.md`](../../openspec/changes/archive/2026-09-23-add-vue-frontend/design.md)
- **Frontend**: [`frontend/`](../../frontend/) — Dockerfile, `vite.config.ts`, `package.json`, `src/main.ts`
- **Docker Compose**: [`docker-compose.yml`](../../docker-compose.yml) — serviço `frontend` com volume de node_modules

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
