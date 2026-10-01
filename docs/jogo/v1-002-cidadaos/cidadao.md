# Cidadão

**Épico:** [v1-002-cidadaos](#) · **Roadmap:** [../../roadmap.md](../../roadmap.md)

## Resumo

Cada cidadão é uma pessoa na vila, com idade, características e profissões. Todas têm 5 características (Vitalidade, Força, Velocidade, Inteligência, Carisma) que afetam o desempenho nas profissões. Durante a vida, ganham pontos de característica e profissão, envelhecem, casam, reproduzem e eventualmente morrem.

## Regras

**R1 — Características [req]** (seção 5.1): Vitalidade (VIT), Força (FOR), Velocidade (VEL), Inteligência (INT), Carisma (CAR).

**R2 — PE efetivo [proposta]** (seção 5.2): `PE efetivo = PE base + Σ floor(característica total ÷ 5) das características ligadas + bônus de ferramenta + bônus PROF de itens/pedras`. Característica total = base + bônus de itens/pedras.

**R3 — Cada 5 pontos em característica = 1 ponto de bônus [req]** nas profissões ligadas a ela; bônus de cada característica ligada soma.

**R4 — Faixa etária de trabalho [proposta]** (seção 5.3): qualquer pessoa 14–64 anos pode trabalhar em qualquer prédio; quem tem 0 PE na profissão rende 0,5. Menores de 14 e pessoas com 65+ não trabalham.

**R5 — Eficiência [proposta]** (seção 5.3): `eficiência = (0,5 + 0,1 × PE efetivo)` limitada a 3,0, depois multiplicada por:
- ×0,5 se 14–17 anos
- ×0,5 se faminto
- ×1,10 se a vila está bem alimentada
- × (1 + bônus do líder)
- × (1 + PROD% de itens/pedras)

**R6 — Membros de tropa não trabalham** em prédios (seção 5.3).

**R7 — Idade em meses** (seção 5.5): +1 mês por turno; aniversário a cada 12 turnos.

**R8 — Teste de morte anual** (seção 5.5): a partir de 50 anos, chance = `max(0; 1% × (idade − 49) − 0,2% × VIT)`. Aos 90 anos morre com certeza.

**R9 — Família líder [proposta]** (seção 5.6): o adulto mais velho da família líder é o líder da vila. Bônus: +1% de eficiência em toda a vila a cada 2 pontos de CAR do líder, máx. +10%.

**R10 — Sucessão [proposta]** (seção 5.6): morto o líder, assume o adulto mais velho da família líder (cônjuges inclusive); se não houver adulto, o jogador escolhe nova família líder.

**R11 — Pontos de crescimento [proposta]** (seção 5.9): até 18 anos, +1 característica/ano e +1 profissão a cada 2 anos; os pontos ficam pendentes e o jogador distribui sem limite por atributo após a criação.

**R12 — Após 18 anos [proposta]** (seção 5.9): só ganha PE por experiência — a cada 24 turnos trabalhando em prédio da mesma profissão, +1 PE base nela; Guerreiro ganha por XP (seção 9.4).

**R13 — Itens equipados de mortos** (seção 5.5): voltam ao inventário da vila.

## Números e tabelas

### População inicial [req + proposta] (seção 5.4)

| Aspecto | Valor |
|---|---|
| Estrutura | 4 famílias × 4 membros |
| Idades | Pai e mãe com 40 anos; filho e filha com 18 anos |
| Pontos distribuídos por pessoa | 20 de característica, 10 de profissão |
| Limites na criação | Máx. 10 por característica; máx. 5 por profissão; base 0 em tudo |

### Tabela profissão × características (seção 5.2)

| Profissão | Características ligadas | Onde trabalha |
|---|---|---|
| Construtor | INT | Obras; Olaria |
| Carregador | FOR, VEL | Armazém; apoio a obras |
| Agricultor | INT | Fazenda de plantio |
| Fazendeiro | INT | Fazenda de criação |
| Mineiro | FOR | Pedreira, Barreiro, Minas, Salina |
| Madeireiro | FOR, VIT [proposta] | Acampamento de lenhadores, Serraria, Carpintaria |
| Ferreiro | INT | Fundição, Ferraria |
| Cozinheiro | VEL, CAR | Cozinha, Estalagem |
| Costureiro | VEL, CAR | Tecelagem, Curtume, Alfaiataria |
| Caçador | VIT, VEL, CAR | Cabana de caça |
| Guerreiro | FOR, VIT, VEL | Quartel (instrutor), tropas |
| Comerciante | CAR | Mercado, Estalagem |

### Exemplos numéricos

**Exemplo 1 — Construtor com INT 15**
- PE base: 5 (distribuído)
- Bônus de INT: floor(15 ÷ 5) = 3
- PE efetivo: 5 + 3 = 8
- Eficiência (sem modificadores): 0,5 + 0,1 × 8 = 1,3

**Exemplo 2 — Agricultor com INT 12, idade 16, bem alimentado**
- PE base: 4
- Bônus de INT: floor(12 ÷ 5) = 2
- PE efetivo: 4 + 2 = 6
- Eficiência base: 0,5 + 0,1 × 6 = 1,1
- Com modificadores (×0,5 por idade 14–17, ×1,10 bem alimentado): 1,1 × 0,5 × 1,10 = 0,605

**Exemplo 3 — Morte por idade**
- Cidadão com 60 anos, VIT 5: chance = 1% × (60 − 49) − 0,2% × 5 = 11% − 1% = 10%
- Cidadão com 70 anos, VIT 5: chance = 1% × (70 − 49) − 0,2% × 5 = 21% − 1% = 20%

## Interações com outros domínios

- [construcoes.md](../v1-003-construcoes/construcoes.md) — cidadãos trabalham em prédios e precisam de casas para morar
- [recursos.md](../v1-010-recursos-e-producao/recursos.md) — consumo de comida e imposto
- [itens.md](../v1-011-itens-e-fabricacao/itens.md) — equipamento de cidadãos
- [armas.md](../v1-004-armas/armas.md) — guerreiros equipam armas
- [armaduras.md](../v1-006-Armaduras/armaduras.md) — guerreiros equipam armaduras
- [joias.md](../v1-007-Joias/joias.md) — cidadãos equipam joias
- [ferramentas.md](../v1-005-ferramentas/ferramentas.md) — cidadãos de profissão equipam ferramentas
- [tropas.md](../v1-013-quartel-e-tropas/tropas.md) — guerreiros formam tropas
- [masmorras.md](../v1-001-masmorras/masmorras.md) — cidadãos podem morrer em batalha

## Modelo de dados (resumo)

### Tabela `familia`

| Campo | Tipo | Descrição |
|---|---|---|
| id | PK | Identificador único |
| vila_id | FK | Referência à vila |
| sobrenome | string | Sobrenome do núcleo familiar |
| casa_id | FK | Casa onde mora (pode estar vazia se núcleo desfeito) |

### Tabela `cidadao`

| Campo | Tipo | Descrição |
|---|---|---|
| id | PK | Identificador único |
| vila_id | FK | Referência à vila |
| familia_id | FK | Referência à família |
| nome | string | Nome da pessoa |
| sexo | char(1) | 'M' ou 'F' |
| idade_meses | int | Idade em meses; +1 por turno |
| vit, for, vel, int, car | int | Características base |
| pontos_car_pendentes | int | Acumulador de pontos de característica a distribuir |
| pontos_prof_pendentes | int | Acumulador de pontos de profissão a distribuir |
| conjuge_id | FK | Referência ao cônjuge (nulo se solteiro) |
| pai_id | FK | Referência ao pai (nulo se geração inicial) |
| mae_id | FK | Referência à mãe (nulo se geração inicial) |
| vivo | boolean | true se vivo; false se morto (registro mantido para genealogia) |
| estado | enum | SAUDAVEL, FERIDO |
| ferido_ate_turno | int | Turno até o qual fica ferido |
| faminto_turnos | int | Contador de turnos seguidos de fome |
| gestacao_turnos | int | Contador de turnos de gestação (nulo se não grávida) |
| construcao_id | FK | Prédio onde trabalha (nulo se não alocado) |
| tropa_id | FK | Tropa de que é membro (nulo se não em tropa) |
| xp_guerreiro | int | XP da profissão Guerreiro |

### Tabela `cidadao_profissao`

| Campo | Tipo | Descrição |
|---|---|---|
| cidadao_id | PK, FK | Referência ao cidadão |
| profissao | PK, string | Nome da profissão |
| pontos_base | int | PE base (distribuído) |
| turnos_experiencia | int | Contador para ganho por experiência |

## Histórias

- [h-001 — Gerar famílias e distribuir pontos iniciais](historia/h-001-gerar-familias-e-distribuir-pontos-iniciais.md)
- [h-002 — Casar cidadãos](historia/h-002-casar-cidadaos.md)
- [h-003 — Nascimento e crescimento](historia/h-003-nascimento-e-crescimento.md)
- [h-004 — Consultar painel do cidadão](historia/h-004-consultar-painel-do-cidadao.md)

## Questões em aberto

- [proposta] Madeireiro ligado a FOR + VIT: validar trade-off com profissões rurais.
- [proposta] Limite de +10% do bônus líder (2 pontos CAR): balanceamento de poder do jogador.
- [proposta] Morte anual a partir de 50 anos: taxa acelerada após 70 anos desejável?
