# Produção

**Épico:** [recursos.md](recursos.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

A produção é o motor econômico. Prédios rurais e de coleta produzem recursos brutos, enquanto fábricas convertem matérias-primas em produtos processados através de receitas. A fórmula de produção combina eficiência do trabalhador, número de produtivos e multiplicador do nível.

## Regras

- R1: Produção de prédio = Σ (eficiência do trabalhador) × base × multiplicador do nível (seção 4.3 da bíblia).
- R2: Fábricas funcionam por receitas; ciclos = execuções da receita por trabalhador por turno × eficiência × mult. nível.
- R3: Se faltar insumo em fábrica, executa os ciclos possíveis (fração).
- R4: Mudança de cultura/rebanho em Fazendas leva 1 turno sem produção.
- R5: Eficiência é capped em 3,0; trabalhadores produtivos = `min(alocados, floor(marcados ÷ 2))` em coleta.
- R6: Prédios de coleta devem ter ladrilhos marcados compatíveis.
- R7: Bônus de região da vila: cada ponto de bônus = +1% na produção ligada. Fator = `1 + bônus ÷ 100`, aplicado depois da eficiência e do nível, com arredondamento em 2 casas decimais.

## Números e tabelas

**Tabela 4.5 — Produção por trabalhador (eficiência 1,0, N1) (seção 4.5 da bíblia)**

| Prédio | Produção base por trabalhador/turno |
|---|---|
| Fazenda de plantio | 6 Grãos **ou** 4 Fibra (cultura escolhida pelo jogador; troca leva 1 turno sem produção) |
| Fazenda de criação | Gado: 3 Carne + 1 Couro **ou** Ovelhas: 2 Lã + 1 Carne (escolha do rebanho) |
| Acampamento de lenhadores | 5 Madeira |
| Pedreira | 4 Pedra |
| Barreiro | 4 Argila |
| Mina de ferro | 3 Minério de ferro |
| Mina de carvão | 3 Carvão |
| Salina | 3 Sal |
| Mina de enxofre | 2 Enxofre |
| Cabana de caça | 2 Carne + 1 Couro |

**Tabela 4.5 — Fábricas — receitas (seção 4.5 da bíblia)**

| Fábrica | Receita | Ciclos/trabalhador |
|---|---|---|
| Serraria | 2 Madeira → 1 Tábua | 3 |
| Olaria | 2 Argila + 1 Madeira → 2 Tijolo | 2 |
| Fundição | 2 Minério de ferro + 1 Carvão → 1 Ferro | 2 |
| Fundição (N2+) | 2 Ferro + 1 Carvão + 1 Enxofre → 1 Aço | 1 (jogador escolhe Ferro ou Aço por trabalhador) |
| Tecelagem | 2 Fibra **ou** 2 Lã → 1 Tecido | 2 |
| Curtume | 2 Couro + 1 Sal → 1 Couro curtido | 2 |
| Cozinha | 2 Grãos + 1 Carne → 5 Refeição | 2 |

**Tabela 4.3 — Multiplicador de produção por nível (seção 4.3 da bíblia)**

| Nível | Multiplicador de produção |
|---|---|
| N1 | ×1,0 |
| N2 | ×1,2 |
| N3 | ×1,5 |

**Tabela 4.6 — Bônus de região da vila — efeito na produção (R7)**

| Bônus | Prédio/Recurso afetado | Efeito | Código do serviço |
|---|---|---|---|
| FLORESTA | Acampamento de lenhadores, Cabana de caça | Produção de Madeira, Carne, Couro | `ProducaoService.processarProducaoColataRural` |
| BARREIRO | Barreiro | Produção de Argila | `ProducaoService.processarProducaoColataRural` |
| PLANTAÇÕES | Fazenda de plantio | Produção de Grãos ou Fibra | `ProducaoService.processarProducaoColataRural` |
| CRIAÇÕES | Fazenda de criação | Produção de Carne, Couro, Lã | `ProducaoService.processarProducaoColataRural` |
| ROCHA | Pedreira | Produção de Pedra | `ProducaoService.processarProducaoColataRural` |
| FERRO | Mina de ferro | Produção de Minério de ferro | `ProducaoService.processarProducaoColataRural` |
| CARVÃO | Mina de carvão | Produção de Carvão | `ProducaoService.processarProducaoColataRural` |
| SALINAS | Salina | Produção de Sal | `ProducaoService.processarProducaoColataRural` |
| ENXOFRE | Mina de enxofre | Produção de Enxofre | `ProducaoService.processarProducaoColataRural` |
| INDÚSTRIA | Fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha) | Ciclos disponíveis | `ProducaoService.processarProducaoFabricas` |
| COMÉRCIO | Imposto, Estalagem | Ouro | `OuroService` |
| DESENVOLVIMENTO | Obras e upgrades | Pontos de Obra (PO) | `ObraService` |
| MILITAR | Quartel | XP de treinamento | `TreinamentoQuartelService` |

## Exemplos

**Exemplo 1: Fazenda de plantio (Grãos) N1**
- 2 Agricultores alocados, ambos com PE Agricultor efetivo 5
- Eficiência de cada um: 0,5 + 0,1 × 5 = 1,0
- Produção base: 6 Grãos
- Produção do turno: 2 × 1,0 × 6 × 1,0 = **12 Grãos**

**Exemplo 2: Serraria N2**
- 3 Madeireiros alocados
- Eficiência média: 1,2
- Produção base: 2 Madeira → 1 Tábua
- Ciclos por turno: 3 × 1,2 × 1,2 = 4,32 ciclos
- Tábuas produzidas: **4 Tábuas** (+ 0,32 carvão restante no próximo turno)
- Madeira consumida: 4 × 2 = 8 Madeira

**Exemplo 3: Fundição N1 (Ferro)**
- 2 Ferreiros, eficiência 1,0 cada
- Produção base: 2 Minério de ferro + 1 Carvão → 1 Ferro
- Ciclos: 2 × 1,0 × 1,0 = 2 ciclos
- Ferro produzido: **2 Ferro**
- Consumo: 4 Minério de ferro + 2 Carvão

**Exemplo 4: Bônus Floresta na coleta (Acampamento de lenhadores N1)**
- 2 trabalhadores de eficiência 1,0 cada
- Produção base: 5 Madeira
- Vila tem Floresta 42 → fator = 1 + 42 ÷ 100 = 1,42
- Produção: (2 × 1,0 × 5) × 1,42 = 10 × 1,42 = **14,20 Madeira**
- Comparação: sem bônus (Floresta 0) = 10 Madeira

**Exemplo 5: Bônus Indústria nas fábricas (Serraria N1)**
- 1 Madeireiro de eficiência 1,0
- Ciclos base: 3
- Vila tem Indústria 30 → fator = 1 + 30 ÷ 100 = 1,30
- Ciclos disponíveis: (1 × 1,0 × 3) × 1,30 = 3 × 1,30 = **3,90 ciclos**

**Exemplo 6: Bônus Militar no treino (Quartel N1)**
- 1 guerreiro aquartelado
- XP base do Quartel N1: 0,5 por turno
- Vila tem Militar 20 → fator = 1 + 20 ÷ 100 = 1,20
- XP no turno: 0,5 × 1,20 = **0,60 XP**

## Interações com outros domínios

- [recursos.md](recursos.md) — estoque de matérias-primas
- [alimentacao.md](alimentacao.md) — Cozinha e Refeição
- [comercio.md](comercio.md) — venda de recursos em excesso
- [predios-de-coleta.md](../v1-003-construcoes/predios-de-coleta.md) — marcação de ladrilhos e produção de coleta
- [../../v1-002-cidadaos/cidadao.md](../v1-002-cidadaos/cidadao.md) — eficiência de trabalhadores

## Modelo de dados (resumo)

Não requer tabela específica; produção é calculada no turno a partir de:
- Construção: tipo, nível, estado
- Alocação: cidadão_id, construcao_id
- Estoque: quantidade por recurso

## Questões em aberto

- A fórmula da eficiência máxima 3,0 se aplica antes ou depois dos multiplicadores de nível? (Testado: antes, durante a soma dos bônus)
