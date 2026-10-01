# Catálogo de bônus [proposta]

**Épico:** [pedras-de-bonus.md](pedras-de-bonus.md) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Catálogo unificado de bônus disponíveis em itens e pedras. Cada bônus possui um código, um efeito e três faixas de magnitude (Baixa, Média, Alta). Os bônus são sorteados ao fabricar itens ou ao obter pedras em masmorras.

## Bônus (seção 6.2 da bíblia)

| Código | Efeito | Baixa | Média | Alta | Aplicável em |
|---|---|---|---|---|---|
| VIT | +N Vitalidade | +1 | +2 | +3 | Itens, Pedras |
| FOR | +N Força | +1 | +2 | +3 | Itens, Pedras |
| VEL | +N Velocidade | +1 | +2 | +3 | Itens, Pedras |
| INT | +N Inteligência | +1 | +2 | +3 | Itens, Pedras |
| CAR | +N Carisma | +1 | +2 | +3 | Itens, Pedras |
| ATK | +N% de ataque | +3% | +5% | +8% | Itens, Pedras |
| DEF | +N% de defesa | +3% | +5% | +8% | Itens, Pedras |
| VIDA | +N pontos de vida | +8 | +15 | +25 | Itens, Pedras |
| INI | +N iniciativa | +1 | +2 | +3 | Itens, Pedras |
| CRIT | +N chance de crítico (pontos percentuais) | +2% | +3% | +5% | Itens, Pedras |
| PROF | +N PE na profissão da ferramenta (em arma/armadura/joia: Guerreiro) | +1 | +2 | +3 | Itens, Pedras |
| PROD | +N% de eficiência no trabalho | +3% | +5% | +8% | Itens, Pedras |

## Faixas de magnitude por nível de item

Bônus intrínsecos sorteados ao fabricar itens recebem faixa conforme o nível do item:

| Nível de item | Faixa |
|---|---|
| L1–4 | Baixa |
| L5–7 | Média |
| L8–10 | Alta |

Exemplo: ao fabricar um Arco L5 (nível Bom), os bônus intrínsecos vêm com magnitude Média.

## Restrições de bônus por categoria de item

Nem todos os bônus são permitidos em todas as categorias. Subconjuntos permitidos (bíblia, seção 7.2):

### Armas

Bônus permitidos: FOR, VEL, INT, ATK, CRIT, INI.

Exemplos: Espada com [FOR +2, ATK +5%]; Arco com [VEL +3, CRIT +3%].

### Armaduras

Bônus permitidos: VIT, DEF, VIDA, VEL.

Exemplos: Peitoral com [VIT +1, DEF +3%]; Sapato com [VEL +2, INI +1] (INI é permitido via bônus de item, ex. sapato).

### Joias

Bônus permitidos: VIT, FOR, VEL, INT, CAR, VIDA, CRIT, PROD.

Exemplos: Colar com [VIDA +15]; Anel com [CAR +2, PROD +5%].

### Ferramentas

Bônus permitidos: PROF, PROD, INT, FOR, VEL.

Exemplos: Enxada com [PROD +5%, INT +1]; Martelo com [PROF +2].

## Catálogo centralizado

Todas as referências a bônus em toda a documentação e código devem usar os códigos e magnitudes desta tabela. A implementação centraliza o catálogo em `com.example.loginbase.jogo.catalogo.BonusEnum`.

## Exemplos numéricos

### Exemplo 1: Bônus de característica em combate

Um Guerreiro com FOR base 5 equipa um item com bônus FOR +2 (Média). Seu FOR efetivo passa a ser 5 + 2 = 7, aumentando seu Ataque conforme a fórmula 10.1 da bíblia.

### Exemplo 2: Bônus de eficiência em trabalho

Um Agricultor com 8 PE na profissão trabalha com uma Enxada L5 com bônus PROD +5%. Sua eficiência (fórmula 5.3 × PROD%) passa de 0,5 + 0,8 = 1,3 para 1,3 × 1,05 = 1,365.

### Exemplo 3: Múltiplos bônus de uma pedra

Uma pedra Divina sorteada em N10 contém 4 bônus: [VIDA +25, DEF +8%, VIT +3, CRIT +5%].
- Ao engastar em uma Armadura (qualidade Divina, 5 slots), a armadura ganham todos os 4 bônus.
- Cálculo de Defesa (fórmula 10.1): defesa base × (1 + DEF% + 0,08) = defesa base × 1,08.

## Interações com outros domínios

- [Itens e fabricação](../v1-011-itens-e-fabricacao/itens.md) — bônus intrínsecos de itens e slots de pedra.
- [Pedras de bônus](pedras-de-bonus.md) — sorteio de bônus em pedras.
- [Cidadãos](../v1-002-cidadaos/cidadao.md) — características totais incluem bônus de itens equipados.
- [Batalha](../v1-014-batalha/batalha.md) — atributos de combate incluem bônus de itens/pedras.

## Questões em aberto

- [proposta] Há limite de quantos bônus PROD podem acumular em um item/pedra? (ex., máx. +20% total)?
- [proposta] Bônus PROF em uma joia (ex., Anel com PROF +2 Guerreiro) aplica em itens da profissão Guerreiro ou em trabalho?
