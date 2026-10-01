# H-004 · Tarefa 002 — Tela do painel do cidadão

**História:** [H-004 — Consultar painel do cidadão](h-004-consultar-painel-do-cidadao.md) · **Domínio:** [../cidadao.md](../cidadao.md) · **Depende de:** [h-004-tarefa-001-api-do-cidadao-e-distribuicao-de-pontos.md](h-004-tarefa-001-api-do-cidadao-e-distribuicao-de-pontos.md) · **Camada:** Frontend

## Objetivo

Criar tela Vue + PrimeVue que exibe painel completo do cidadão: dados básicos, características, profissões, pontos pendentes distribuíveis, e slots de equipamento.

## Contexto necessário

- [cidadao.md](../cidadao.md) — painel da pessoa
  > Mostra dados, características, profissões, família, trabalho, pontos pendentes, equipamento (seção 5.10).

## Backend

Endpoints implementados em tarefa 001: `GET /api/jogo/cidadao/{id}`, `POST /api/jogo/cidadao/{id}/distribuir-pontos`.

## Frontend

**Componentes PrimeVue**:
- `PainelCidadao`:
  - Card com abas: Informações, Características, Profissões, Equipamento.

- **Aba Informações**:
  - Exibir nome, idade (anos), sexo, estado, família.
  - Se ferido: mostrar "Ferido até turno XX".
  - Mostrar local de trabalho (se alocado em prédio).
  - Avatar ou ícone de sexo.

- **Aba Características**:
  - Tabela: VIT, FOR, VEL, INT, CAR.
  - Colunas: Base, Itens, Pedras, Total.
  - Ex.: VIT base 5, +1 joia, +2 pedra → total 8.
  - Se não tem itens/pedras em alguma: mostrar 0.

- **Aba Profissões**:
  - Tabela: Profissão, Base, Bônus (INT), Ferramenta, Itens, **Efetiva**, Experiência.
  - Ex.: Construtor, base 6, +3 INT, +2 Martelo L2 → efetiva 11.
  - Filtro: mostrar apenas profissões com > 0 PE base.
  - Se tem pontos pendentes: seção de distribuição (ver abaixo).

- **Distribuição de pontos** (dentro de Profissões e Características):
  - Se pontos_car_pendentes > 0:
    - 5 inputs: VIT, FOR, VEL, INT, CAR (InputNumber, min 0).
    - Contador: "X de Y pontos usados".
    - Validação live: avisar se Σ > Y.
    - Botão "Distribuir pontos característicos".
  - Se pontos_prof_pendentes > 0:
    - 12 inputs: uma por profissão (InputNumber, min 0, max 5).
    - Contador: "X de Y pontos usados".
    - Botão "Distribuir pontos de profissão".
  - POST em click → recarrega dados.

- **Aba Equipamento**:
  - 1 slot arma (lê GET e mostra item equipado ou "vazio").
  - 6 slots armadura (Peitoral, Capacete, Ombreiras, Luvas, Calças, Sapato).
  - 1 slot colar.
  - 2 slots anel.
  - 1 slot ferramenta.
  - Cada slot: exibe item equipado (nome, nível, bônus visuais) e botão "trocar".
    - Clica "trocar" → abre dialog de inventário da vila, filtra por categoria.
    - Escolhe novo item → POST `/api/jogo/cidadao/{id}/equipar` → recarrega painel.
  - Se cidadão em tropa em expedição: todos os slots desabilitados (read-only) com aviso.
  - Botão "desequipar" em cada slot (move item de volta ao inventário).

- **Inventário** (seção colapsada):
  - Lista de itens não equipados do cidadão (dono = cidadaoId).
  - Colunas: Nome, Nível, Categoria, Qualidade, Bônus.

**Fluxo**:
1. Tela abre GET `/api/jogo/cidadao/{id}`.
2. Exibe dados em abas.
3. Se pontos pendentes, mostra inputs de distribuição.
4. Clica "Distribuir" → POST com pontos → recarrega.
5. Clica "trocar" em slot → dialog de inventário → escolhe → POST equipar → recarrega.

## Arquivos prováveis

- [/frontend/src/views/jogo/PainelCidadao.vue](/frontend/src/views/jogo/PainelCidadao.vue) (novo)
- [/frontend/src/components/jogo/CaracteristicasTab.vue](/frontend/src/components/jogo/CaracteristicasTab.vue) (novo)
- [/frontend/src/components/jogo/ProfissoesTab.vue](/frontend/src/components/jogo/ProfissoesTab.vue) (novo)
- [/frontend/src/components/jogo/EquipamentoTab.vue](/frontend/src/components/jogo/EquipamentoTab.vue) (novo)

## Testes

- Teste E2E: abrir painel → exibir dados corretos.
- Teste E2E: tabela características mostra base, itens, pedras, total.
- Teste E2E: tabela profissões calcula efetiva corretamente.
- Teste E2E: com pontos pendentes, inputs aparecem; sem pontos, ocultados.
- Teste E2E: distribuir pontos característicos → POST bem-sucedido → inputs zerados.
- Teste E2E: clicar "trocar" em slot → dialog inventário abre.
- Teste E2E: selecionar item novo → POST equipar → painel atualiza.
- Teste E2E: membro em expedição → slots desabilitados + aviso.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA6.
- Build do frontend (`npm run build`) sem erros.
- Testes E2E listados passando.
- Layout responsivo em mobile.

## Fora de escopo

- Treino de profissão fora de prédios (será em história de experiência).
- Comparador de itens (mostrar diferença ao trocar).
