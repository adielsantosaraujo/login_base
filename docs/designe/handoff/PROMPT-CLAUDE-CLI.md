# Prompts para o Claude CLI

Copie a pasta `handoff/` para `login_base/docs/designe/handoff/`. Depois, rode `claude` na raiz do projeto e use os prompts abaixo, **um por vez**, revisando o resultado de cada etapa antes de seguir.

---

### 0. Contexto
```
Leia docs/designe/handoff/README.md e todos os arquivos referenciados nele
(regras/, telas/, api/, referencia/). Abra os protótipos em docs/designe/handoff/prototipos/
como referência visual. Compare com o código atual do frontend (tela criar-vila e
distribuição de pontos) e do backend (vila, regiões, cidadãos). Não altere nada ainda:
me devolva um plano de implementação, listando arquivos a criar/alterar e conflitos
com docs/jogo/ e com o código existente.
```

### 1. Backend — geração do mapa
```
Implemente no backend a geração do mapa conforme docs/designe/handoff/regras/regras-regioes-v2.md,
portando docs/designe/handoff/referencia/geracao-mapa.js para Java (Random com semente).
Crie os enums TipoRegiao (FLORESTA, PLANICIE, URBANA, LITORAL, MONTANHA) e BonusRegiao (13 valores).
Escreva testes unitários cobrindo R8–R15 (rodar ~1000 sementes).
```

### 2. Backend — prévia e criação da vila
```
Implemente os endpoints §1, §2 e §3 de docs/designe/handoff/api/contratos-api.md
(POST/GET /api/jogo/vila/previa e POST /api/jogo/vila), com as validações e códigos de erro da tabela.
Persistir tipo e bônus das 16 regiões. Inclua migração de banco e testes de integração.
```

### 3. Frontend — tela Criar vila
```
Reimplemente a tela /app/jogo/criar-vila seguindo docs/designe/handoff/telas/tela-01-criar-vila.md
e docs/designe/handoff/telas/design-tokens.md, usando o protótipo
docs/designe/handoff/prototipos/Criar Vila.dc.html como referência visual exata.
Use Vue 3 + PrimeVue e os padrões já existentes no frontend. Integre com a API da etapa 2.
```

### 4. Backend — população
```
Implemente §4 e §5 de docs/designe/handoff/api/contratos-api.md seguindo
docs/designe/handoff/regras/regras-populacao-v1.md. Porte docs/designe/handoff/referencia/distribuicao-populacao.js
para Java (distribuição sugerida + validação). Limites: só os totais (20 característica / 10 profissão),
sem máximo por atributo. Mínimos: 2 Construtores e 2 Carregadores principais. Testes unitários e de integração.
```

### 5. Frontend — tela Distribuir população
```
Implemente /app/jogo/distribuir-populacao seguindo docs/designe/handoff/telas/tela-02-distribuir-populacao.md,
com o protótipo docs/designe/handoff/prototipos/Distribuir Populacao.dc.html como referência visual.
Use docs/designe/handoff/referencia/distribuicao-populacao.js (pode ser importado como módulo) para
redistribuir e trocar profissão principal sem ida ao servidor. Adicione os guardas de rota do contratos-api.md.
```

### 6. Atualizar documentação do jogo
```
Atualize docs/jogo/v1-008-vila-e-mapa/regioes.md, a história h-001 de vila e suas tarefas,
docs/jogo/v1-002-cidadaos/cidadao.md e h-001-tarefa-003 para refletir
docs/designe/handoff/regras/*.md, mantendo o formato existente (Regras R#, [req]/[proposta], tabelas, exemplos).
```
