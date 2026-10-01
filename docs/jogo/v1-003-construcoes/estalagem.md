# Estalagem

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Estalagem serve Refeições a viajantes, gerando Ouro, e oferece chance de imigração de novos cidadãos adultos. Cozinheiros ou Comerciantes trabalham na Estalagem, consumindo Refeições do estoque e trazendo renda passiva.

## Regras

- **R1**: Vagas: N1 = 2, N2 = 5, N3 = 10 Cozinheiros ou Comerciantes (seção 4.10) [proposta].
- **R2**: Por turno, consome até `5 × Σ eficiência × mult. nível` Refeições e gera **4 Ouro por Refeição servida** (seção 4.10) [proposta].
- **R3**: Imigração: cada turno, chance de `2% × nível` (N1 2%, N2 4%, N3 6%) de chegar viajante adulto (18–30 anos, 20 pontos característica + 10 profissão aleatórios), se existir núcleo livre em alguma casa (seção 4.10) [proposta].
- **R4**: Viajante que imigra vira núcleo próprio (solteiro) (seção 4.10) [proposta].
- **R5**: Estalagem ocupa 1x1 (N1), 2x2 (N2), 3x3 (N3) em Região Urbana (seção 1.4) [req].
- **R6**: Sem Refeições no estoque, nenhuma é servida e sem Ouro é gerado (seção 4.10) [proposta].

## Números e tabelas

### Custos por nível [proposta]

| Nível | Tamanho | Tábua | Tijolo | Tecido | PO |
|---|---|---|---|---|---|
| N1 | 1x1 | 30 | 20 | 10 | 8 |
| N2 | 2x2 | 75 (2,5×) | 50 (2,5×) | 25 (2,5×) | 20 |
| N3 | 3x3 | 150 (5×) | 100 (5×) | 50 (5×) | 40 |

### Produção de Ouro (Refeições servidas) [proposta]

| Nível | Multiplicador | Máx. Refeições/turno | Máx. Ouro/turno (4 por Refeição) |
|---|---|---|---|
| N1 | ×1,0 | 5 × eficiência × 1,0 | até 20 × eficiência |
| N2 | ×1,2 | 5 × eficiência × 1,2 | até 24 × eficiência |
| N3 | ×1,5 | 5 × eficiência × 1,5 | até 30 × eficiência |

### Probabilidade de imigração [proposta]

| Nível | Chance/turno |
|---|---|
| N1 | 2% |
| N2 | 4% |
| N3 | 6% |

## Exemplos

**Exemplo 1: Ouro passivo em Estalagem N1**
- Estalagem N1 com 2 Cozinheiros: um PE 4 (eficiência 0,9), outro PE 6 (eficiência 1,1).
- Refeições consumidas: `5 × (0,9 + 1,1) × 1,0 = 5 × 2,0 = 10 Refeições/turno`.
- Ouro gerado: `10 × 4 = 40 Ouro/turno`.

**Exemplo 2: Imigração bem-sucedida**
- Estalagem N2 em turno T. Vila tem 1 núcleo livre em alguma Casa.
- Sorteio: 4% × T (caso sucesso).
- Adulto de 24 anos chega com 20 pontos características + 10 profissão distribuídos aleatoriamente.
- Vira novo núcleo solteiro na casa com vaga.

**Exemplo 3: Imigração fracassa por falta de vaga**
- Estalagem N3, sorteia um viajante (6% chance).
- Todas as casas estão cheias (0 núcleos livres).
- Viajante não entra; fila para próximo turno (ou desaparece?).

**Exemplo 4: Sem Refeições**
- Estalagem N1 com 2 Cozinheiros. Estoque: 0 Refeição.
- Turno: nenhuma Refeição servida, 0 Ouro gerado.
- Próximo turno, se houver Refeição novamente, retoma.

## Interações com outros domínios

- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — Refeição, Ouro
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — profissões Cozinheiro/Comerciante, imigração, casas
- [construcoes.md](construcoes.md) — custos, PO
- [turnos.md](../v1-009-turnos/turnos.md) — Ouro passivo (passo 2), imigração (passo 9)

## Modelo de dados

Estalagem é um tipo de `construcao`. Evento de imigração gravado em `evento_turno`: vila_id, turno, tipo (IMIGRACAO), mensagem, dados (JSON com cidadao_id novo).

## Questões em aberto

- Viajante rejeitado por falta de vaga: desaparece ou tenta próximo turno automaticamente?
- Características e profissões do imigrante: totalmente aleatório ou há restrições?
- Imigrante chega com família ou sempre solteiro?
