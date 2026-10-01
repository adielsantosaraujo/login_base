# H-002 · Tarefa 002 — Tela de famílias e casamento

**História:** [H-002 — Casar cidadãos](h-002-casar-cidadaos.md) · **Domínio:** [../familias.md](../familias.md) · **Depende de:** [h-002-tarefa-001-regra-e-api-de-casamento.md](h-002-tarefa-001-regra-e-api-de-casamento.md) · **Camada:** Frontend

## Objetivo

Criar tela Vue + PrimeVue para listar famílias e seus membros, e permitir casar dois cidadãos elegíveis escolhendo a casa destino e o novo sobrenome.

## Contexto necessário

- [familias.md](../familias.md) — casamento
  > Exige núcleo livre; nova família com sobrenome escolhido.

## Backend

Endpoint já implementado em tarefa 001: `POST /api/jogo/casamento`.

## Frontend

**Componentes PrimeVue**:
- `FamiliasList`:
  - Tabela ou TreeTable listando famílias da vila.
  - Colunas: Sobrenome, Membros (count), Casa, Ações.
  - Ações: expandir/contraír para ver membros.

- `MembrosTable` (dentro de cada família):
  - Colunas: Nome, Idade (anos), Sexo, Profissões, Status (solteiro/casado), Ações.
  - Ações: selecionar para casamento.

- `CasamentoDialog`:
  - Abre ao clicar "Casar" em um cidadão.
  - Dropdown: escolher segundo cidadão (amostra apenas solteiros ≥18 elegíveis).
  - Dropdown: escolher casa destino (apenas com núcleo livre).
  - Dropdown: escolher sobrenome (entre os dois).
  - Botão "Casar" → POST `/api/jogo/casamento`.
  - Validação local (feedback) + backend (enforce).
  - Sucesso: dialog fecha, lista atualiza.
  - Erro: exibe mensagem do backend.

**Fluxo**:
1. Tela abre com lista de famílias.
2. Jogador expande uma família.
3. Jogador clica "Casar" em um cidadão solteiro ≥18.
4. Dialog abre com dropdown de candidatos elegíveis.
5. Jogador escolhe segundo cidadão, casa, sobrenome.
6. Clica "Casar" → POST → sucesso → lista atualiza.

## Arquivos prováveis

- [/frontend/src/components/jogo/FamiliasList.vue](/frontend/src/components/jogo/FamiliasList.vue) (novo)
- [/frontend/src/components/jogo/CasamentoDialog.vue](/frontend/src/components/jogo/CasamentoDialog.vue) (novo)

## Testes

- Teste E2E: listar famílias da vila.
- Teste E2E: expandir família → mostrar membros.
- Teste E2E: clicar "Casar" em cidadão solteiro ≥18 → dialog abre.
- Teste E2E: dropdown de candidatos mostra apenas solteiros ≥18.
- Teste E2E: dropdown de casas mostra apenas com núcleo livre.
- Teste E2E: escolher candidato, casa, sobrenome → clicar "Casar" → POST bem-sucedido → lista atualiza.
- Teste E2E: tentar casar < 18 → desabilitado ou erro.

## Definição de pronto

- Critérios de aceite da história cobertos por esta tarefa: CA1–CA4.
- Build do frontend (`npm run build`) sem erros.
- Testes E2E listados passando.
- Feedback local de validações.

## Fora de escopo

- Múltiplos casamentos simultâneos.
- Histórico de casamentos.
