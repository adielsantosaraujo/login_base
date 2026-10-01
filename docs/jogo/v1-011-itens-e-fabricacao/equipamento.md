# Equipamento

**Épico:** [itens.md](itens.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cidadãos equipam itens (armas, ferramentas, armaduras, joias) no painel da pessoa para melhorar seu desempenho em combate ou trabalho. Requisitos mínimos de idade e PE precisam ser atendidos. O inventário da vila armazena itens não equipados, sem limite de quantidade.

## Regras

### Requisitos para equipar

- R1: Pessoa ≥14 anos pode equipar ferramentas e joias [proposta]
- R2: Pessoa ≥16 anos pode equipar armas e armaduras [proposta]
- R3: Arma e armadura de nível L: PE efetivo Guerreiro ≥ L − 1 [proposta]
- R4: Ferramenta de nível L: PE base na profissão da ferramenta ≥ L − 1 [proposta]
- R5: Joias sem requisito [proposta]

### Inventário da vila

- R6: Itens não equipados ficam no inventário da vila; cada item tem dono opcional [proposta]
- R7: Inventário tem capacidade ilimitada em v1 [proposta]
- R8: Itens de pessoas mortas voltam ao inventário (seção 5.5)

### Equipar e trocar

- R9: No painel da pessoa: escolher item do inventário para um slot [req + proposta]
- R10: O item anterior no slot volta ao inventário [proposta]
- R11: Não é permitido trocar equipamento de membro de tropa em expedição [proposta]

### Limitações especiais

- R12: Máximo 1 Colar por pessoa [proposta]
- R13: Máximo 2 Anéis por pessoa [proposta]
- R14: Membros de tropa em expedição não podem trocar equipamento [proposta]

## Números e tabelas

### Requisitos de PE por nível

| Nível | PE Guerreiro (armas/armaduras) | PE profissão (ferramentas) |
|---|---|---|
| L1 | ≥0 | ≥0 |
| L2 | ≥1 | ≥1 |
| L3 | ≥2 | ≥2 |
| L4 | ≥3 | ≥3 |
| L5 | ≥4 | ≥4 |
| L6 | ≥5 | ≥5 |
| L7 | ≥6 | ≥6 |
| L8 | ≥7 | ≥7 |
| L9 | ≥8 | ≥8 |
| L10 | ≥9 | ≥9 |

(Seção 7.5 da bíblia; fórmula: PE ≥ L − 1)

## Exemplos

### Exemplo 1: Equipar Espada L5

**Setup:**
- Guerreiro com PE efetivo 8 em Guerreiro
- Idade 22 anos
- Slot de Arma vazio

**Verificação:**
- Idade ≥16? Sim ✓
- PE efetivo ≥ L − 1 = 5 − 1 = 4? Sim, 8 ≥ 4 ✓
- Ação: Equipar Espada L5
- Resultado: Espada L5 entra no slot; nenhum item sai (slot estava vazio)

### Exemplo 2: Trocar ferramenta em Agricultor

**Setup:**
- Agricultor com PE base 3 na profissão (sem bônus de itens)
- Enxada L3 já equipada
- Enxada L5 disponível no inventário
- Idade 30 anos

**Verificação:**
- Idade ≥14? Sim ✓
- PE base ≥ L − 1 = 5 − 1 = 4? Não, 3 < 4 ✗
- Ação: Rejeitar equipamento
- Motivo: PE insuficiente

**Alternativa:** Se Agricultor ganhasse +1 PE (p. ex., por herança ou experiência), atingiria PE base 4:
- PE base ≥ 4? Sim ✓
- Trocar: Enxada L3 volta ao inventário; Enxada L5 entra no slot

### Exemplo 3: Equipar 2 Anéis

**Setup:**
- Pessoa com 0 anéis equipados
- Anel L3 de Força e Anel L2 de Inteligência disponíveis

**Verificação:**
- Limite de anéis: 2 ✓
- Equipar Anel L3 de Força no slot Anel 1
- Equipar Anel L2 de Inteligência no slot Anel 2
- Resultado: 2 anéis equipados; bônus somam (+2 FOR + INT do anel L2)

**Tentativa de equipar 3º anel:**
- Limite atingido → Rejeitar
- Mensagem: "Máximo 2 anéis por pessoa"

## Interações com outros domínios

- [itens.md](itens.md) — categorias, níveis, requisitos de PE
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — cidadãos e suas características/profissões
- [../../v1-013-quartel-e-tropas/tropas.md](../v1-013-quartel-e-tropas/tropas.md) — restrição de troca durante expedição
- [../../v1-014-batalha/batalha.md](../v1-014-batalha/batalha.md) — bônus de itens afetam combate

## Questões em aberto

- [proposta] Remover um item (deixar slot vazio) é permitido? (não mencionado)
- [proposta] Trocar de slot (p. ex., Anel 1 ↔ Anel 2) é permitido? (deve ser, sem custo)
