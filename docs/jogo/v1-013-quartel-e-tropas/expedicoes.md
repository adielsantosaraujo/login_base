# Expedições

**Épico:** [tropas.md](tropas.md) · **Domínio:** [batalha.md](../v1-014-batalha/batalha.md), [recursos.md](../v1-010-recursos-e-producao/recursos.md)

## Resumo

Expedição é uma missão de uma tropa para atacar uma masmorra ativa. A tropa viaja até a masmorra (em turnos), resolve uma batalha ao chegar e retorna com o saque. Comida é consumida durante a viagem de ida e volta.

## Regras

- R1: Destino de expedição é sempre uma **região com masmorra ativa** [proposta] (seção 9.3).
- R2: **Turnos de viagem** (cada sentido, ida e volta) = `max(1; menor distância de Manhattan entre a região da masmorra e uma região possuída da vila)` [proposta] (seção 9.3).
- R3: Ao partir, a vila debita **`nº de membros × turnos de ida e volta` alimentos**; sem comida suficiente, expedição não parte [proposta] (seção 9.3).
- R4: Ao **chegar** à masmorra (fim de EM_VIAGEM_IDA), a batalha é **resolvida no mesmo turno** [proposta] (seção 9.3).
- R5: Se a masmorra **sumir antes** da tropa chegar (vitória de outra tropa), a tropa retorna sem lutar [proposta] (seção 9.3).
- R6: **Saque** é entregue ao estoque da vila na volta, **sem limite de carga** na v1 [proposta] (seção 9.3).

## Números e tabelas

### Distância de Manhattan

Distância entre regiões = diferença de linha + diferença de coluna.

Exemplo: região 01 (linha 0, coluna 0) → região 16 (linha 3, coluna 3) = 3 + 3 = 6.

### Consumo de comida

Fórmula: `nº de membros × (turnos de ida + turnos de volta)` alimentos (Refeição, Grãos ou Carne, conforme seção 3.4).

Exemplo: tropa com 5 membros, 2 turnos de ida, 2 turnos de volta → 5 × (2 + 2) = **20 alimentos**.

## Exemplos

### Exemplo 1: Cálculo de viagem

Vila possuída: regiões 01, 02, 05, 06 (agrupadas).
Masmorra em região 16 (linha 3, coluna 3).

Distância mínima de Manhattan de qualquer região possuída:
- Região 06 (linha 1, coluna 1) → região 16 (linha 3, coluna 3) = 2 + 2 = 4.

Turnos de viagem = max(1, 4) = **4 turnos em cada sentido** (ida e volta).

### Exemplo 2: Consumo total e expedição

Tropa com 8 guerreiros.
- Turnos de ida: 3
- Turnos de volta: 3
- Total de alimentos necessários: 8 × (3 + 3) = **48 alimentos**

Estoque da vila: 100 Refeições, 50 Grãos, 20 Carne. Ordem de consumo: Refeição → Grãos → Carne (seção 3.4).
- Consome 48 de Refeição: 100 − 48 = 52 Refeições sobram. Expedição autorizada.
- Estado da tropa: turno 1 EM_VIAGEM_IDA, turno 2 EM_VIAGEM_IDA, turno 3 EM_VIAGEM_IDA, turno 4 EM_VIAGEM_IDA, **turno 5 batalha** (chega ao destino).
- Após vitória ou derrota: estado EM_VIAGEM_VOLTA para 3 turnos, depois retorna a AQUARTELADA.

### Exemplo 3: Masmorra desaparece

Tropa A enviada para masmorra N5 na região 10; estimativa 2 turnos de viagem.
Tropa A está EM_VIAGEM_IDA, turno 1/2.

Outro jogador (ou a mesma vila, com outra tropa) derrota a masmorra. Ela é removida.

Na resolução do turno, Tropa A chega ao destino, mas não há masmorra. Ela retorna automaticamente a AQUARTELADA sem resolver batalha; saque vazio.

## Interações com outros domínios

- [tropas.md](tropas.md) — estados EM_VIAGEM_IDA e EM_VIAGEM_VOLTA controlam a disponibilidade da tropa.
- [batalha.md](../v1-014-batalha/batalha.md) — ao chegar, tropa resolve batalha contra inimigos da masmorra (seção 10.3, passos da resolução de turno 2.2 passo 8).
- [masmorras.md](../v1-001-masmorras/masmorras.md) — vitória limpa masmorra; derrota restaura inimigos.
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — consumo de alimentos (seção 3.4) é debitado ao partir.

## Questões em aberto

- Visualização de expedição em progresso no frontend: apenas barra de progresso ou detalhes de rota?
- Se vários quartéis, qual quartel a tropa retorna? (Proposta: quartel que enviou.)
