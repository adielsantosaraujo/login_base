# H-004 — Consultar painel do cidadão

**Épico:** [../cidadao.md](../cidadao.md) · **Domínio:** [../cidadao.md](../cidadao.md)

## História

Como jogador, quero ver os detalhes de um cidadão (características, profissões, PE, pontos pendentes) e gerenciar seu equipamento (armas, armaduras, joias, ferramentas).

## Contexto

O painel mostra:
- Dados básicos: nome, idade, família, sexo, estado (saudável/ferido).
- Características base e total (com bônus de itens/pedras).
- Profissões: base, efetiva (com bônus), experiência.
- Pontos pendentes de característica e profissão (distribuíveis).
- Equipamento: arma, armaduras (6 peças), colar, anéis (2), ferramenta.
- Inventário de itens (não equipados).

**Requisito** (seção 5.10): painel permite adicionar/trocar arma, armaduras, ferramenta e joias.

## Critérios de aceite

### CA1 — Mostrar dados básicos

- **Dado** um cidadão qualquer.
- **Quando** a tela abre o painel.
- **Então**:
  - Nome, idade (em anos), família, sexo, estado (SAUDAVEL ou FERIDO, se sim, até quando).
  - Visto de imediato.

### CA2 — Mostrar características (base e total)

- **Dado** cidadão com VIT base 5, joia com +1 VIT, pedra com +2 VIT.
- **Quando** painel abre.
- **Então**:
  - VIT base = 5.
  - VIT total = 5 + 1 + 2 = 8.
  - Exibir ambas as colunas.

### CA3 — Mostrar PE efetivo por profissão

- **Dado** Construtor PE base 6, INT total 18 (→ bônus +3), Martelo L2 (→ +2).
- **Quando** painel abre.
- **Então**:
  - PE efetivo = 6 + 3 + 2 = 11.
  - Exibir base e efetivo.

### CA4 — Distribuir pontos pendentes

- **Dado** cidadão com 3 pontos de característica pendentes.
- **Quando** o jogador aloca +1 VIT, +1 FOR, +1 CAR.
- **Então**:
  - Pontos pendentes = 0.
  - VIT, FOR, CAR incrementados (base).
  - Eficiências em trabalhos afetadas no próximo turno.

### CA5 — Equipar/trocar items

- **Dado** cidadão com Espada L1 equipada; Espada L2 no inventário.
- **Quando** o jogador clica "trocar" na Espada L2.
- **Então**:
  - Espada L2 equipada.
  - Espada L1 volta ao inventário.
  - Atributos de combate atualizados (se guerreiro).

### CA6 — Bloquear trocar equipamento de membro em expedição

- **Dado** guerreiro em tropa em expedição.
- **Quando** tela carrega o painel.
- **Então**:
  - Slots de equipamento aparecem desabilitados (read-only).
  - Mensagem: "Não é permitido trocar equipamento de membro em expedição".

## Tarefas

- [h-004-tarefa-001 — API do cidadão e distribuição de pontos](h-004-tarefa-001-api-do-cidadao-e-distribuicao-de-pontos.md)
- [h-004-tarefa-002 — Tela do painel do cidadão](h-004-tarefa-002-tela-do-painel-do-cidadao.md)

## Fora de escopo

- Treino de profissão fora de prédios.
- Histórico de batalhas (será em relatório de tropa/batalha).
