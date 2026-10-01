# Recursos

**Épico:** [v1-010-recursos-e-producao](recursos.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Os recursos são o núcleo da economia da vila, produzidos por prédios e consumidos na construção, fabricação e alimentação. A capacidade de armazenamento limita a quantidade que pode ser guardada, com bônus pelos Armazéns. A gestão de estoque e eficiência de produção são críticas.

## Regras

- R1: Cada recurso tem uma categoria (Bruto, Alimento, Rural, Processado) e um preço base em Ouro, conforme tabela 3.1.
- R2: Quantidades são guardadas com 2 casas decimais; a tela mostra inteiro arredondado para baixo.
- R3: Capacidade base da vila: 500 por recurso; Ouro é ilimitado.
- R4: Cada Armazém soma capacidade extra por nível: N1 +500, N2 +1.500, N3 +4.000, multiplicada por `min(1,5; eficiência média dos Carregadores alocados)`.
- R5: Sem o mínimo de Carregadores (N1 1, N2 2, N3 4) o Armazém não soma nada.
- R6: Itens e pedras ficam no inventário da vila, sem limite.
- R7: No passo 4 do turno, excedente acima da capacidade é perdido (registrado no relatório).
- R8: Imposto: 0,5 Ouro por cidadão ≥18 anos por turno [proposta].

## Números e tabelas

**Tabela 3.1 — Lista de recursos (seção 3.1 da bíblia)**

| Recurso | Categoria | Origem | Preço base (Ouro) |
|---|---|---|---|
| Madeira | Bruto | Acampamento de lenhadores | 1 |
| Pedra | Bruto | Pedreira | 1 |
| Argila | Bruto | Barreiro | 1 |
| Minério de ferro | Bruto | Mina de ferro | 2 |
| Carvão | Bruto | Mina de carvão | 2 |
| Sal | Bruto | Salina | 3 |
| Enxofre | Bruto | Mina de enxofre | 4 |
| Grãos | Alimento | Fazenda de plantio | 1 |
| Fibra (linho) | Rural | Fazenda de plantio | 1 |
| Carne | Alimento | Fazenda de criação, Cabana de caça | 2 |
| Couro | Rural | Fazenda de criação, Cabana de caça | 2 |
| Lã | Rural | Fazenda de criação | 2 |
| Tábua | Processado | Serraria | 3 |
| Tijolo | Processado | Olaria | 3 |
| Ferro (lingote) | Processado | Fundição | 6 |
| Aço | Processado | Fundição N2+ | 20 |
| Tecido | Processado | Tecelagem | 4 |
| Couro curtido | Processado | Curtume | 6 |
| Refeição | Alimento | Cozinha | 1 |
| Ouro | Moeda | Imposto, Mercado, Estalagem, masmorras | — |

**Tabela 3.2 — Recursos iniciais (seção 3.2 da bíblia)**

Vila nova recebe:
- Madeira 200
- Pedra 100
- Argila 50
- Tábua 20
- Grãos 200
- Carne 40
- Ouro 200
- Demais = 0

## Exemplos

**Exemplo de armazenamento N1:**
- Capacidade base: 500 Madeira
- Armazém N1 com 1 Carregador (eficiência 1,0): +500 × 1,0 = 500 → total 1.000 Madeira
- Armazém N1 sem carregador mínimo: +0 → continua 500 Madeira

**Exemplo de armazenamento N2:**
- Base: 500 Madeira
- 2 Armazéns N2, cada um com 2 Carregadores (eficiência média 1,2): +1.500 × 1,2 = 1.800 cada → +3.600 → total 4.100 Madeira
- Limitador: eficiência máxima 1,5 (regra R5), então máximo +1.500 × 1,5 = 2.250 por Armazém

## Interações com outros domínios

- [produção.md](producao.md) — fórmula e cálculo de produção por prédio
- [alimentação.md](alimentacao.md) — consumo de alimentos e fome
- [comercio.md](comercio.md) — preços e negociação no Mercado
- [../../v1-003-construcoes/armazens.md](../v1-003-construcoes/armazens.md) — Armazém e sua capacidade
- [../../v1-003-construcoes/construcoes.md](../v1-003-construcoes/construcoes.md) — custo de construções
- [../../v1-011-itens-e-fabricacao/fabricacao.md](../v1-011-itens-e-fabricacao/fabricacao.md) — custo de itens

## Modelo de dados (resumo)

**Tabela estoque**
- vila_id: chave estrangeira
- recurso: enum (MADEIRA, PEDRA, ARGILA, MINÉRIO_DE_FERRO, CARVÃO, SAL, ENXOFRE, GRÃOS, FIBRA, CARNE, COURO, LÃ, TÁBUA, TIJOLO, FERRO, AÇO, TECIDO, COURO_CURTIDO, REFEIÇÃO, OURO)
- quantidade: numeric(14,2)

## Histórias

- [historia/h-001-consultar-estoque-de-recursos.md](historia/h-001-consultar-estoque-de-recursos.md)
- [historia/h-002-produzir-recursos-nos-predios.md](historia/h-002-produzir-recursos-nos-predios.md)
- [historia/h-003-alimentar-a-populacao.md](historia/h-003-alimentar-a-populacao.md)
- [historia/h-004-negociar-recursos-no-mercado.md](historia/h-004-negociar-recursos-no-mercado.md)

## Questões em aberto

- Preços base de venda/compra no Mercado são fixos conforme tabela 3.1?
