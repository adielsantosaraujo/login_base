# Guerreiros

**Épico:** [cidadao.md](cidadao.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Guerreiro é a profissão de combate. Guerreiros formam tropas, treinam no Quartel, ganham experiência em batalhas e são o pilar do sistema militar.

## Características ligadas

- **Força (FOR)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Guerreiro.
- **Vitalidade (VIT)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Guerreiro.
- **Velocidade (VEL)** [req] (seção 5.2): a cada 5 pontos, +1 PE de Guerreiro.

## Onde trabalha

- Quartel: instrução de outros guerreiros (instrutor) e treinamento no quartel.
- Tropas: formação e combate em expedições.

## Arma e armadura

- **Arma**: Espada, Lança, Arco ou Besta (seção 7.8).
- **Armadura**: até 6 peças (Peitoral, Capacete, Ombreiras, Luvas, Calças, Sapato) (seção 7.10).
- **Joias**: Colar (1) e Anéis (2) (seção 7.11).
- **Requisito para equipar**: armas e armaduras de nível L exigem PE efetivo Guerreiro ≥ L − 1 (seção 7.5).

## Como evolui

- Base: distribuição na criação.
- Até 18 anos: +1 profissão a cada 2 anos (máx. 9 pontos ao atingir 18).
- Após 18: **+1 PE por XP de Guerreiro**. 10 XP = +1 PE base de Guerreiro (seção 9.4).
- XP vem do treino no quartel (0,5/1,0/1,5 por turno conforme nível N1/N2/N3) e de vitórias em batalha (+1 XP por nível da masmorra para cada sobrevivente) (seção 8.4).

## Formação de tropa

**Requisitos [req + proposta]** (seção 9.1):
- PE Guerreiro base ≥ 1 [req].
- Arma equipada [req].
- Idade 16–54 anos [proposta].

**Posição** [proposta]: Frente ou Retaguarda (seção 9.1).

**Limites** [proposta] (seção 9.1 + 4.12):
- Quartel N1: até 1 tropa com até 5 membros.
- Quartel N2: até 2 tropas com até 8 membros cada.
- Quartel N3: até 4 tropas com até 10 membros cada.
- Vários quartéis somam capacidade.
- Máximo 1 tropa por pessoa; membro de tropa não trabalha em prédios (seção 5.3).

**Estados da tropa** [proposta] (seção 9.2):
- `AQUARTELADA` → `EM_VIAGEM_IDA` → (batalha) → `EM_VIAGEM_VOLTA` → `AQUARTELADA`.

## Atributos de combate

**Cálculo [proposta]** (seção 10.1):
G = PE efetivo de Guerreiro.

| Atributo | Fórmula |
|---|---|
| **PV máx.** | `30 + 5 × VIT + 3 × G + Σ VIDA` |
| **Ataque** | `[ATQarma(L) × (1 + 0,05 × atributo-chave) + 2 × G] × (1 + Σ ATK%)` |
| **Defesa** | `[Σ DEFpeça(L) + VIT + G + 2 (se espada)] × (1 + Σ DEF%)` |
| **Iniciativa** | `2 × VEL + G + mod. arma + Σ INI (+1 do sapato)`; mais 1d6 cada rodada |
| **Crítico** | `5% + 0,5% × VEL + Σ CRIT`; dano crítico ×1,5 |

## Exemplo de PE efetivo e de combate

**Guerreiro**: FOR 12, VIT 10, VEL 14, PE base 5, com Espada L2 (Ataque 18), Peitoral L2 (Defesa 14,4), sem outros itens.

**PE efetivo**:
- Bônus FOR: floor(12 ÷ 5) = 2.
- Bônus VIT: floor(10 ÷ 5) = 2.
- Bônus VEL: floor(14 ÷ 5) = 2.
- PE efetivo: 5 + 2 + 2 + 2 = 11.

**Atributos de combate**:
- PV máx.: 30 + 5 × 10 + 3 × 11 = 30 + 50 + 33 = 113.
- Ataque: [18 × 1,30 + 22] × 1 = 45,4.
- Defesa: [14,4 + 10 + 11 + 2] × 1 = 37,4.
- Iniciativa base: 2 × 14 + 11 + 0 = 39.
- Crítico: 5% + 0,5% × 14 = 12%.

## Progressão de Guerreiro

**Tabela de XP** (seção 9.4):
- 10 XP = +1 PE base de Guerreiro.

**Fontes de XP**:
- Treino no Quartel (seção 4.12): N1 0,5 XP/turno; N2 1,0 XP/turno; N3 1,5 XP/turno (aquartelado).
- Vitória em batalha (seção 8.4): +N de XP para cada sobrevivente, onde N = nível da masmorra.

**Exemplo**: Guerreiro treina em Quartel N2 (1,0 XP/turno). Após 10 turnos, 10 XP → +1 PE base.

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — Quartel
- [armas.md](../v1-004-armas/armas.md) — usa armas em combate
- [armaduras.md](../v1-006-Armaduras/armaduras.md) — usa armaduras em combate
- [joias.md](../v1-007-Joias/joias.md) — usa joias
- [tropas.md](../v1-013-quartel-e-tropas/tropas.md) — forma tropas
- [batalha.md](../v1-014-batalha/batalha.md) — regras de combate
- [masmorras.md](../v1-001-masmorras/masmorras.md) — recebe XP em vitórias

## Questões em aberto

- [proposta] Faixa etária 16–54: validar limites de entrada/saída de tropas.
- [proposta] Instrução no Quartel N1: suficiente para vila inicial (n=4)?
