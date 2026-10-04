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
- R6: Prédios de coleta devem ter ladrilhos marcados do terreno do prédio.
- R7: Bônus do terreno: Cada prédio tem uma âncora (ladrilho designado). Se o terreno da âncora corresponder ao terreno do prédio, o bônus é o `bonus_total` da âncora (0–200); caso contrário, bônus = 0. Comércio (Mercado + Estalagem), Desenvolvimento (Casas) e Militar (Quartéis) usam a **média** dos `bonus_total` das âncoras dos prédios daquele grupo que estão no terreno certo; se nenhum estiver, a média é 0. Entram na média os prédios em estado Ativa ou Em aprimoramento; obras ainda não concluídas não entram. A média é arredondada em 2 casas. Fator = `1 + bônus ÷ 100` (coleta) ou `1 + média ÷ 100` (comércio/desenvolvimento/militar), aplicado depois da eficiência e do nível. Comércio multiplica só o ouro do imposto e renda da Estalagem (não altera o preço de venda e de compra do Mercado).

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

**Tabela 4.6 — Tipos de terreno — efeito na produção (R7)**

| Terreno | Prédio correspondente | Categoria | Efeito | Código do serviço |
|---|---|---|---|---|
| FLORESTA | Acampamento de lenhadores, Cabana de caça | Por prédio (âncora) | Produção de Madeira, Carne, Couro | `ProducaoService.processarProducaoColataRural` |
| BARREIRO | Barreiro | Por prédio (âncora) | Produção de Argila | `ProducaoService.processarProducaoColataRural` |
| PLANTAÇÕES | Fazenda de plantio | Por prédio (âncora) | Produção de Grãos ou Fibra | `ProducaoService.processarProducaoColataRural` |
| CRIAÇÕES | Fazenda de criação | Por prédio (âncora) | Produção de Carne, Couro, Lã | `ProducaoService.processarProducaoColataRural` |
| ROCHA | Pedreira | Por prédio (âncora) | Produção de Pedra | `ProducaoService.processarProducaoColataRural` |
| FERRO | Mina de ferro | Por prédio (âncora) | Produção de Minério de ferro | `ProducaoService.processarProducaoColataRural` |
| CARVÃO | Mina de carvão | Por prédio (âncora) | Produção de Carvão | `ProducaoService.processarProducaoColataRural` |
| SALINAS | Salina | Por prédio (âncora) | Produção de Sal | `ProducaoService.processarProducaoColataRural` |
| ENXOFRE | Mina de enxofre | Por prédio (âncora) | Produção de Enxofre | `ProducaoService.processarProducaoColataRural` |
| INDÚSTRIA | Fábricas (Serraria, Olaria, Fundição, Tecelagem, Curtume, Cozinha) | Por prédio (âncora) | Ciclos disponíveis | `ProducaoService.processarProducaoFabricas` |
| COMÉRCIO | Mercado, Estalagem | Grupo (média) | Média das âncoras: Ouro (imposto, Estalagem) | `OuroService` |
| DESENVOLVIMENTO | Casas | Grupo (média) | Média das âncoras: Pontos de Obra (PO) | `ObraService` |
| MILITAR | Quartel | Grupo (média) | Média das âncoras: XP de treinamento | `TreinamentoQuartelService` |

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
- Tábuas produzidas: **4 Tábuas** (+ 0,32 ciclo restante no próximo turno)
- Madeira consumida: 4 × 2 = 8 Madeira

**Exemplo 3: Fundição N1 (Ferro)**
- 2 Ferreiros, eficiência 1,0 cada
- Produção base: 2 Minério de ferro + 1 Carvão → 1 Ferro
- Ciclos: 2 × 1,0 × 1,0 = 2 ciclos
- Ferro produzido: **2 Ferro**
- Consumo: 4 Minério de ferro + 2 Carvão

**Exemplo 4: Bônus de terreno na coleta (Acampamento de lenhadores N1)**
- 2 trabalhadores de eficiência 1,0 cada
- Produção base: 5 Madeira
- Acampamento tem âncora no terreno Floresta
- Âncora tem bonus_base 30 + bonus_adjacente (2 vizinhos Floresta × 25) = 30 + 50 = 80 → bonus_total = 80
- Fator = 1 + 80 ÷ 100 = 1,80
- Produção: (2 × 1,0 × 5) × 1,80 = 10 × 1,80 = **18,00 Madeira**
- Comparação: se a âncora fosse em outro terreno (fator 1,0) = 10 Madeira

**Exemplo 5: Bônus Indústria nas fábricas (Serraria N1)**
- 1 Madeireiro de eficiência 1,0
- Serraria N1 com âncora em ladrilho Indústria, bonus_total 50
- Fator = 1 + 50 ÷ 100 = 1,50
- Ciclos base: 1 × 1,0 × 3 = 3 ciclos
- Ciclos disponíveis: 3 × 1,50 = **4,50 ciclos**
- Consumo: 4,5 ciclos × 2 Madeira = 9 Madeira consumida
- Produção: **4,5 Tábuas** (seguindo R3, fração para próximo turno)

**Exemplo 6: Bônus Militar no treino (Quartel N1)**
- 1 guerreiro aquartelado
- XP base do Quartel N1: 0,5 por turno
- Vila tem 2 Quartéis no terreno Militar:
  - Quartel 1 (âncora): bonus_total = 45
  - Quartel 2 (âncora): bonus_total = 35
  - Média = (45 + 35) ÷ 2 = 40 (Quartéis na Urbana não entram na média)
- Fator = 1 + 40 ÷ 100 = 1,40
- XP no turno: 0,5 × 1,40 = **0,70 XP**

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
