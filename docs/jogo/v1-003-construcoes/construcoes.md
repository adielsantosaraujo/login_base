# Construções

**Épico:** [v1-003-construcoes](construcoes.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

O coração da vila é formado por prédios: casas para moradia, fazendas para produção, fábricas para processamento, oficinas para manufatura de itens e instalações militares e comerciais. Cada prédio tem 3 níveis (N1, N2, N3), começa em N1 e evolui por upgrade. As obras consomem tempo (pontos de obra) e trabalhadores especializados; durante a construção o prédio não funciona. Marcação de ladrilhos em prédios de coleta e alocação de cidadãos determinam a produção efetiva.

## Regras

- **R1**: Toda construção ocupa 1x1 (N1), 2x2 (N2) ou 3x3 (N3) ladrilhos contíguos de uma região (seção 4.1).
- **R2**: Construção nasce sempre em N1; N2 e N3 só por upgrade (seção 4.1).
- **R3**: Upgrade exige área 2x2 (N2) ou 3x3 (N3) que contenha a atual, com demais ladrilhos livres, mesma região (seção 4.1).
- **R4**: Custo de upgrade: N1→N2 = 2,5 × custo N1; N2→N3 = 5 × custo N1; arredondado para cima (seção 4.1).
- **R5**: Durante obra (construção ou upgrade) o prédio não funciona; trabalhadores ficam ociosos (seção 4.1).
- **R6**: Demolir devolve 25% dos recursos gastos (total acumulado) (seção 4.1).
- **R7**: Cada construção tem PO total; por turno, cada Construtor alocado à obra adiciona `eficiência × 1,0` PO; Carregador adiciona `eficiência × 0,5` PO (seção 4.2). Bônus Desenvolvimento da vila aumenta PO em +1% por ponto (seção 4.2).
- **R8**: Máximo de trabalhadores por obra: 2 (N1), 4 (N2), 6 (N3) (seção 4.2).
- **R9**: Recursos debitados ao iniciar a obra; cancelar devolve 50% (seção 4.2).
- **R10**: Todos os prédios precisam de pessoas para funcionar, exceto Casa (funciona com moradores) (seção 4.1) [req].
- **R11**: Bonificação por nível — N1: 2 vagas, ×1,0 produção, 4 ladrilhos de coleta; N2: 5 vagas, ×1,2 produção, 10 ladrilhos; N3: 10 vagas, ×1,5 produção, 20 ladrilhos (seção 4.3) [proposta].
- **R12**: Alocação de trabalhadores respeita vagas do prédio (seção 5.3).
- **R13**: Cada prédio é permitido apenas em regiões cujo tipo tenha o bônus associado ao prédio; prédios sem bônus associado são urbanos (só Urbana) (seção 4.1).

## Números e tabelas

### Tabela de custos e PO (N1) [proposta]

Custos de N2/N3 seguem multiplicadores 2,5× e 5× (seção 4.1).

| Construção | Região | Bônus | Profissão | Madeira | Pedra | Argila | Tábua | Ferro | Tijolo | Tecido | PO N1 | PO N2 | PO N3 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Casa | Urbana | — | — | 20 | 10 | 10 | — | — | — | — | 4 | 10 | 20 |
| Armazém | Urbana | — | Carregador | 30 | 20 | — | — | — | — | — | 6 | 15 | 30 |
| Serraria | Urbana | Indústria | Madeireiro | 30 | 10 | — | — | — | — | — | 6 | 15 | 30 |
| Olaria | Urbana | Indústria | Construtor | 20 | 20 | 10 | — | — | — | — | 6 | 15 | 30 |
| Fundição | Urbana | Indústria | Ferreiro | — | 30 | — | 20 | — | 20 | — | 8 | 20 | 40 |
| Tecelagem | Urbana | Indústria | Costureiro | — | 10 | — | 20 | — | — | — | 6 | 15 | 30 |
| Curtume | Urbana | Indústria | Costureiro | — | — | — | 20 | — | 10 | — | 6 | 15 | 30 |
| Cozinha | Urbana | Indústria | Cozinheiro | — | — | — | 15 | — | 15 | — | 6 | 15 | 30 |
| Ferraria | Urbana | — | Ferreiro | — | — | — | 20 | 10 | 20 | — | 8 | 20 | 40 |
| Alfaiataria | Urbana | — | Costureiro | — | — | — | 20 | 5 | 10 | 5 | 6 | 15 | 30 |
| Carpintaria | Urbana | — | Madeireiro | — | 10 | — | 30 | — | — | — | 6 | 15 | 30 |
| Mercado | Urbana | — | Comerciante | — | 20 | — | 30 | — | — | — | 6 | 15 | 30 |
| Estalagem | Urbana | — | Cozinheiro/Comerciante | — | — | — | 30 | — | 20 | 10 | 8 | 20 | 40 |
| Quartel | Urbana | — | Guerreiro | — | 40 | — | 30 | 10 | — | — | 8 | 20 | 40 |
| Fazenda de plantio | Floresta, Planície | Plantações | Agricultor | 15 | — | — | — | — | — | — | 4 | 10 | 20 |
| Fazenda de criação | Planície | Criações | Fazendeiro | 25 | — | — | — | — | — | — | 4 | 10 | 20 |
| Acampamento de lenhadores | Floresta, Planície | Floresta | Madeireiro | 15 | 5 | — | — | — | — | — | 4 | 10 | 20 |
| Pedreira | Montanha | Rocha | Mineiro | 20 | — | — | — | — | — | — | 4 | 10 | 20 |
| Barreiro | Floresta | Barreiro | Mineiro | 15 | — | — | — | — | — | — | 4 | 10 | 20 |
| Mina de ferro | Montanha | Ferro | Mineiro | 30 | 20 | — | — | — | — | — | 6 | 15 | 30 |
| Mina de carvão | Montanha | Carvão | Mineiro | 30 | 20 | — | — | — | — | — | 6 | 15 | 30 |
| Salina | Litoral | Salinas | Mineiro | 20 | 10 | — | — | — | — | — | 4 | 10 | 20 |
| Mina de enxofre | Litoral | Enxofre | Mineiro | 30 | 30 | — | — | 5 | — | — | 8 | 20 | 40 |
| Cabana de caça | Floresta, Planície | Floresta | Caçador | 15 | — | — | — | — | — | — | 4 | 10 | 20 |

Exemplo de upgrade: Casa N1→N2 = 50 Madeira, 25 Pedra, 25 Argila, 10 PO; N2→N3 = 100 Madeira, 50 Pedra, 50 Argila, 20 PO.

### Bonificação por nível [proposta]

| Nível | Área | Vagas de trabalho | Multiplicador de produção | Ladrilhos de coleta (máx.) |
|---|---|---|---|---|
| N1 | 1x1 | 2 | ×1,0 | 4 |
| N2 | 2x2 | 5 | ×1,2 | 10 |
| N3 | 3x3 | 10 | ×1,5 | 20 |

## Exemplos

**Exemplo 1: Construção de Casa N1**
- Custador inicia obra: debita 20 Madeira, 10 Pedra, 10 Argila.
- PO total: 4. Com 2 Construtores de eficiência 1,0 cada: 2 PO/turno → conclusão em 2 turnos.
- Casa conclui com 4 vagas, 1 núcleo familiar, multiplicador ×1,0 de produção (não é produtiva).

**Exemplo 2: Upgrade de Casa N1→N2**
- Custo: 50 Madeira, 25 Pedra, 25 Argila, 10 PO.
- Área 2x2 que contenha a actual (1x1), com demais ladrilhos livres.
- Durante a obra, a casa não recebe novos moradores e os residentes não trabalham.
- Após conclusão: 2 núcleos, 10 vagas.

**Exemplo 3: Produção em Fazenda de plantio N1 com 1 Agricultor**
- Eficiência do Agricultor: base 0,5 + 0,1 × PE Agricultor. Supor PE 5 → eficiência 1,0.
- Produção: 1,0 × 6 Grãos × 1,0 (N1) = 6 Grãos/turno.
- Com N2: eficiência 1,0 × 6 × 1,2 = 7,2 Grãos.

**Exemplo 4: Obra com bônus Desenvolvimento**
- Obra de Casa N1 com 1 Construtor de eficiência 1,0 e vila com bônus Desenvolvimento 12.
- PO por turno: 1,0 × 1,0 × (1 + 12/100) = 1,12 PO/turno.
- Sem bônus: mesma obra produziria 1,0 PO/turno.

## Interações com outros domínios

- [vila.md](../v1-008-vila-e-mapa/vila.md) — criação de vila, regiões iniciais, casas iniciais
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — custo em recursos, armazenamento, produção
- [cidadao.md](../v1-002-cidadaos/cidadao.md) — alocação de trabalhadores, eficiência
- [fabricacao.md](../v1-011-itens-e-fabricacao/fabricacao.md) — oficinas para itens
- [pedras-de-bonus.md](../v1-012-pedras-de-bonus/pedras-de-bonus.md) — engaste na Ferraria
- [turnos.md](../v1-009-turnos/turnos.md) — resolução de obras e produção no passo 5 do turno

## Modelo de dados (resumo)

- `construcao`: id, regiao_id, tipo (enum), nivel (N1/N2/N3), x, y, tamanho, estado (EM_OBRA/ATIVA/EM_UPGRADE), po_total, po_atual, configuracao (JSON: cultura/rebanho/receita selecionados).
- `construcao_ladrilho`: construcao_id, x, y (marca ladrilhos ocupados).
- `construcao_marcacao` (coleta): construcao_id, x, y (marca ladrilhos de coleta vinculados).
- `construcao_alocacao` (proposto): construcao_id, cidadao_id, profissao (alocação de trabalhadores).

## Histórias

- [h-001-construir-predio-nivel-1.md](historia/h-001-construir-predio-nivel-1.md)
- [h-002-melhorar-predio-de-nivel.md](historia/h-002-melhorar-predio-de-nivel.md)
- [h-003-marcar-ladrilhos-de-coleta.md](historia/h-003-marcar-ladrilhos-de-coleta.md)
- [h-004-alocar-trabalhadores-nos-predios.md](historia/h-004-alocar-trabalhadores-nos-predios.md)

## Questões em aberto

- Nomes dos arquivos do template (`enchada.md` → `enxada.md`): confirmar ortografia.
- Pastas com maiúscula (`v1-006-Armaduras`, `v1-007-Joias`) vs. minúsculas demais: confirmar padrão.
