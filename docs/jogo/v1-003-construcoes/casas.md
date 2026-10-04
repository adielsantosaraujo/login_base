# Casas

**Épico:** [construcoes.md](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Casas são a habitação da vila, determinando quantos cidadãos podem viver nela e quantos núcleos familiares cabem. A vila começa com 4 casas N1 na primeira região Urbana escolhida, fornecendo 4 casas iniciais. Cada nível de casa oferece mais núcleos e vagas. Casas com âncora em ladrilho Desenvolvimento entram na média do bônus Desenvolvimento da vila.

## Regras

- **R1**: Casa não precisa de trabalhadores especializados para funcionar; funciona com moradores (seção 4.1) [req].
- **R2**: Cada Casa tem núcleos familiares: N1 = 1 núcleo, N2 = 2 núcleos, N3 = 4 núcleos (seção 4.7) [proposta].
- **R3**: Cada Casa tem vagas (pessoas): N1 = 4, N2 = 10, N3 = 24 (seção 4.7) [proposta] [req].
- **R4**: Casal novo exige núcleo livre em alguma casa (seção 4.7) [req].
- **R5**: Reprodução exige vaga livre na casa do casal (seção 4.7) [req].
- **R6**: As 4 casas N1 iniciais já vêm construídas na 1ª região Urbana escolhida, nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura (y = 0..9, x = 0..9) (seção 4.7) [req].

## Bônus — Desenvolvimento

Casas recebem bônus pelo terreno Desenvolvimento. O fator Desenvolvimento da vila (PO das obras) = 1 + (média do bonus_total das âncoras das Casas que estão em ladrilho Desenvolvimento) ÷ 100. Casas em ladrilho Indústria ou Comércio não entram na média; se nenhuma estiver em De, fator = 1,0.

## Números e tabelas

### Capacidade por nível [proposta]

| Nível | Núcleos familiares | Vagas (pessoas) | Custo N1 | PO N1 | PO N2 | PO N3 |
|---|---|---|---|---|---|---|
| N1 | 1 [req] | 4 [req] | 20 Mad, 10 Ped, 10 Arg | 4 | 10 | 20 |
| N2 | 2 [proposta] | 10 [proposta] | — (upgrade) | — | — | — |
| N3 | 4 [proposta] | 24 [proposta] | — (upgrade) | — | — | — |

### Custos por nível

- **N1**: 20 Madeira, 10 Pedra, 10 Argila, 4 PO.
- **N1→N2**: 50 Madeira, 25 Pedra, 25 Argila, 10 PO (2,5× N1).
- **N2→N3**: 100 Madeira, 50 Pedra, 50 Argila, 20 PO (5× N1).

## Exemplos

**Exemplo 1: Casas iniciais**
Vila criada com 3 regiões: uma Urbana, uma de outro tipo, uma terceira. As 4 casas N1 são colocadas na primeira região Urbana escolhida nos 4 primeiros ladrilhos Desenvolvimento em ordem de varredura (y = 0..9, x = 0..9), totalizando 4 núcleos e 16 vagas para os 16 cidadãos iniciais (4 famílias × 4 membros). Todas as 4 casas entram na média de Desenvolvimento.

**Exemplo 2: Expansão de casa**
Jogador escolhe expandir Casa N1 para N2 no ladrilho (0,0), marcando área 2x2 com (0,0), (1,0), (0,1), (1,1) livres. Custo: 50 Madeira, 25 Pedra, 25 Argila. Com 2 Construtores de eficiência 1,0: 10 PO / 2 PO/turno = 5 turnos.

**Exemplo 3: Imposição de casas para reprodução**
Vila tem 2 núcleos livres em Casa N1 (2 vagas totais) e 4 núcleos livres em Casa N2 (8 vagas). Casal quer ter filho; a Reprodução exige vaga livre na mesma casa do casal. Se o casal está em Casa N1 com vaga, reprodução prossegue; caso contrário, nega.

## Interações com outros domínios

- [construcoes.md](construcoes.md) — tabela de custos, PO, upgrade
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — famílias, núcleos, capacidade
- [vila.md](../v1-008-vila-e-mapa/vila.md) — casas iniciais na criação

## Modelo de dados

Casa é um tipo de `construcao`; no nível, armazena capacidade de núcleos e vagas.

## Questões em aberto

- Mudança de casa ao casar: o casal se muda para a casa escolhida na marcação de núcleo.
- Demolição de casa: devolvem itens equipados dos moradores ao inventário?
