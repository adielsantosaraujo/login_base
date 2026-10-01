# Quartel e Tropas

**Épico:** [v1-013-quartel-e-tropas](/docs/jogo/v1-013-quartel-e-tropas/tropas.md) · **Roadmap:** [../../roadmap.md](/docs/roadmap.md)

## Resumo

O quartel é um prédio urbano onde guerreiros são treinados e organizados em tropas. Cada tropa tem um limite de membros definido pelo nível do quartel e é formada por cidadãos com PE de Guerreiro e armas equipadas. Tropas podem ser enviadas em expedições para atacar masmorras, viajando até o destino e retornando com saque.

## Regras

### Formação de tropas

- R1: Apenas cidadãos com **PE Guerreiro ≥ 1** (base) e **arma equipada** podem ser membros de tropa [req] (seção 9.1).
- R2: Membros devem estar na faixa etária **16–54 anos** [proposta] (seção 9.1).
- R3: Cada pessoa está em **no máximo 1 tropa** [req]; membros de tropa não trabalham em outros prédios (seção 9.1).
- R4: Cada membro tem **posição**: Frente ou Retaguarda [proposta] (seção 9.1).
- R5: Na formação o jogador pode equipar armaduras e joias como atalho [req] (seção 9.1).
- R6: Tropa pode ser desfeita quando está aquartelada no quartel (seção 9.1).

### Capacidade do quartel

| Nível | Instrutores | Capacidade total | Tropas simultaneamente |
|---|---|---|---|
| N1 | 1 | até 5 membros | 1 tropa |
| N2 | 2 | até 8 membros | 2 tropas |
| N3 | 3 | até 10 membros | 4 tropas |

- Vagas de instrutor (Guerreiro) são dedicadas: sem instrutor o quartel não forma tropas nem treina (seção 4.12).
- Vários quartéis da vila somam sua capacidade (seção 4.12).

### Estados da tropa

As tropas passam pelos seguintes estados [proposta] (seção 9.2):
- **AQUARTELADA**: em repouso no quartel, membros recebem XP de treinamento.
- **EM_VIAGEM_IDA**: viajando para a masmorra.
- **EM_VIAGEM_VOLTA**: retornando com saque após batalha.

### Treino e progressão

- R7: Cada membro de tropa aquartelada (não em expedição) ganha **XP de Guerreiro por turno** conforme o nível do quartel [req + proposta] (seção 4.12):
  - N1: 0,5 XP/turno
  - N2: 1,0 XP/turno
  - N3: 1,5 XP/turno
- R8: **10 XP = +1 PE base de Guerreiro** [proposta] (seção 9.4).
- R9: XP também vem de vitórias em masmorras (seção 9.4).

## Números e tabelas

### Composição de tropa

Ver tabela em "Capacidade do quartel" acima. Regra de capacidade cumulativa: se a vila tem 1 quartel N1 e 1 quartel N2, capacidade total = 5 + 8 = 13 membros e 1 + 2 = 3 tropas simultaneamente.

### Tabela de custo do quartel (seção 4.4)

| Construção | Nível | Custo | PO | Instrutores |
|---|---|---|---|---|
| Quartel | N1 | 30 Tábua, 40 Pedra, 10 Ferro | 8 | 1 |
| Quartel | N2 | 75 Tábua, 100 Pedra, 25 Ferro | 20 | 2 |
| Quartel | N3 | 150 Tábua, 200 Pedra, 50 Ferro | 40 | 3 |

Custo N2 = 2,5 × N1; Custo N3 = 5 × N1.

## Exemplos

### Exemplo 1: Formação e estados

Uma vila tem 1 Quartel N2 (2 instrutores alocados). O jogador forma 2 tropas:
- Tropa A: 5 Guerreiros → AQUARTELADA, ganham 1,0 XP/turno cada (3 em Frente, 2 em Retaguarda).
- Tropa B: 4 Guerreiros → AQUARTELADA, ganham 1,0 XP/turno cada (2 em Frente, 2 em Retaguarda).

Se o jogador envia Tropa A para uma masmorra N5, Tropa A passa a EM_VIAGEM_IDA (não ganha XP enquanto viaja).

### Exemplo 2: XP e progressão

Um guerreiro com 3 PE base, sem itens/pedras. A cada 10 turnos (10 × 1 XP de N2) ele recebe +1 PE base de Guerreiro (total 4), aumentando seu Ataque e Defesa.

## Interações com outros domínios

- [batalha.md](../v1-014-batalha/batalha.md) — tropas em expedição resolvem batalha ao chegar na masmorra.
- [masmorras.md](../v1-001-masmorras/masmorras.md) — regiões com masmorra ativa são destino de expedição.
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — guerreiros são cidadãos com profissão Guerreiro; requisitos de idade e arma.
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — expedição consome comida (3.4).
- [construcoes.md](../v1-003-construcoes/construcoes.md) — quartel é prédio urbano; instrutores são Guerreiros (seção 4.4).

## Modelo de dados (resumo)

Ver seção 11.2 da bíblia para detalhes. Tabelas envolvidas:

| Tabela | Campos-chave |
|---|---|
| tropa | id, vila_id, quartel_id, nome, estado (AQUARTELADA/EM_VIAGEM_IDA/EM_VIAGEM_VOLTA), masmorra_id, turnos_restantes |
| tropa_membro | tropa_id, cidadao_id, posicao (FRENTE/RETAGUARDA) |

Estado `AQUARTELADA` → `EM_VIAGEM_IDA` → (batalha) → `EM_VIAGEM_VOLTA` → `AQUARTELADA`.

Campo `masmorra_id` referencia a masmorra alvo; `turnos_restantes` controla viagem.

## Histórias

- [h-001-formar-tropa-no-quartel.md](historia/h-001-formar-tropa-no-quartel.md) — formar tropa com membros elegíveis, posições e limite do quartel.
- [h-002-treinar-guerreiros-no-quartel.md](historia/h-002-treinar-guerreiros-no-quartel.md) — treino em turno aumenta XP; 10 XP = +1 PE.
- [h-003-enviar-tropa-em-expedicao.md](historia/h-003-enviar-tropa-em-expedicao.md) — enviar tropa para masmorra, viagem com consumo de comida, retorno com saque.

## Questões em aberto

- Nomes de trovas: gerados pelo jogo ou escolhidos pelo jogador? [proposta: gerado + editável]
- Ordem de ação dentro de uma tropa (além de Frente/Retaguarda): vínculo com Iniciativa em batalha (seção 10.3).
