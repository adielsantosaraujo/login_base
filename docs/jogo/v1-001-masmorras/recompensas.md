# Recompensas

**Épico:** [masmorras.md](masmorras.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Vitória em masmorra concede ouro, recursos, chance de item, XP para guerreiros e pedras de bônus. Pedras só são obtidas em masmorras. O nível da masmorra determina as quantidades.

## Regras

- **R1** — Ouro: `40 × N + aleatório(0..20 × N)` [proposta] (seção 8.4).
- **R2** — Recursos: `N` sorteios; cada um dá `10 × N` unidades de 1 recurso entre Madeira, Pedra, Ferro, Couro curtido, Tecido (e Aço a partir de N6) [proposta] (seção 8.4).
- **R3** — Item: chance `min(80%; 10% × N)` de 1 item aleatório (arma, armadura ou joia) de nível L = N; qualidade pela tabela de margem 0–4 (N1–7) ou 15+ (N8–10); Divina permitida [proposta] (seção 8.4).
- **R4** — XP: cada guerreiro sobrevivente ganha `N` de XP de Guerreiro [proposta] (seção 8.4).
- **R5** — Pedras [req]: `1 + floor(N ÷ 3)` sorteios; tipo e qualidade pela tabela abaixo [proposta] (seção 8.4).

## Números e tabelas

### Fórmulas por nível

| Nível | Ouro mín. | Ouro máx. | Recursos sorteios | Recurso por sorteio | Item chance | Pedra sorteios |
|---|---|---|---|---|---|---|
| N1 | 40 | 60 | 1 | 10 | 10% | 1 |
| N2 | 80 | 120 | 2 | 20 | 20% | 1 |
| N3 | 120 | 180 | 3 | 30 | 30% | 2 |
| N4 | 160 | 240 | 4 | 40 | 40% | 2 |
| N5 | 200 | 300 | 5 | 50 | 50% | 2 |
| N6 | 240 | 360 | 6 | 60 | 60% | 3 |
| N7 | 280 | 420 | 7 | 70 | 70% | 3 |
| N8 | 320 | 480 | 8 | 80 | 80% | 3 |
| N9 | 360 | 540 | 9 | 90 | 80% | 4 |
| N10 | 400 | 600 | 10 | 100 | 80% | 4 |

Fonte: seção 8.4 (bíblia).

### Recursos possíveis

- N1–5: Madeira, Pedra, Ferro, Couro curtido, Tecido.
- N6–10: Madeira, Pedra, Ferro, Couro curtido, Tecido, Aço (em sorteios).

Fonte: seção 8.4 (bíblia).

### Qualidade de pedra por nível (sorteios)

| Níveis | Nada | Simples | Boa | Excelente | Divina |
|---|---|---|---|---|---|
| N1–3 | 50% | 45% | 5% | 0% | 0% |
| N4–6 | 30% | 45% | 20% | 5% | 0% |
| N7–9 | 20% | 30% | 30% | 17% | 3% |
| N10 | 10% | 20% | 35% | 27% | 8% |

Fonte: seção 8.4 (bíblia).

## Exemplos

### Exemplo N6

Vitória contra masmorra nível 6:
- **Ouro**: `40 × 6 + aleatório(0..120)` = 240 + 0–120 = **240–360 Ouro**.
- **Recursos**: 6 sorteios, 60 unidades cada.
  - Exemplo: 60 Madeira, 60 Pedra, 60 Ferro, 60 Couro curtido, 60 Tecido, 60 Aço.
- **Item**: 60% de chance de 1 item aleatório (arma/armadura/joia) **nível 6**, qualidade por margem 0–4 (Simples/Boa/Excelente, sem Divina fora de N3).
- **XP**: cada guerreiro vivo da tropa ganha **6 XP de Guerreiro**.
- **Pedras**: **3 sorteios** (1 + floor(6 ÷ 3) = 1 + 2 = 3); cada um dá 30% Nada, 45% Simples, 20% Boa, 5% Excelente.
  - Resultado típico: 1 Nada (não ganha), 1 Simples, 1 Boa (3 pedras ganhas).

## Interações com outros domínios

- [batalha.md](../v1-014-batalha/batalha.md) — XP e morte de guerreiros afetam recompensas (sobrevivência).
- [itens.md](../v1-011-itens-e-fabricacao/itens.md) — item sorteado gerado com nível N.
- [pedras-de-bonus.md](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — tipos e engaste de pedras.
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — XP de Guerreiro contribui ao PE efetivo.
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — recursos adicionados ao estoque.

## Questões em aberto

- Nenhuma [proposta] adicional neste domínio.
