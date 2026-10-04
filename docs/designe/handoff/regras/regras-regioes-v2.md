# Regiões — v2 (proposta da tela "Criar minha vila")

**Substitui (quando validado):** [regioes.md](../../../jogo/v1-008-vila-e-mapa/regioes.md) · **Afeta:** [H-001 — Criar vila escolhendo regiões iniciais](../../../jogo/v1-008-vila-e-mapa/historia/h-001-criar-vila-escolhendo-regioes-iniciais.md) · **Design:** [Criar Vila.dc.html](../prototipos/Criar%20Vila.dc.html)

## Resumo

O mapa da vila é uma grade 4×4 de 16 regiões. Cada região é de um de 5 tipos e tem 3 bônus de região. O jogo sorteia primeiro os tipos de todas as regiões e depois o valor de cada bônus. Para fundar a vila, o jogador escolhe 3 regiões vizinhas, sendo pelo menos 1 Urbana. Antes de escolher, ele pode gerar um novo mapa.

## Regras

### Grade e escolha inicial
- R1: A grade é 4×4, com 16 regiões numeradas de 01 a 16 [req].
- R2: Regiões são vizinhas só na horizontal ou na vertical (diagonal não conta) [req].
- R3: O jogador escolhe 3 regiões. A 1ª pode ser qualquer uma; a 2ª e a 3ª precisam ser vizinhas de uma região já escolhida [req].
- R4: Pelo menos 1 das 3 regiões escolhidas deve ser do tipo Urbana [req].
- R5: O tipo da região é definido pela geração do mapa, não pelo jogador [proposta].

### Tipos de região
- R6: Existem 5 tipos: **Floresta, Planície, Urbana, Litoral, Montanha** [proposta].
- R7: Cada tipo tem exatamente 3 bônus de região, conforme a tabela "Bônus por tipo" [proposta].

### Geração do mapa — etapa 1: tipos
- R8: Cada um dos 5 tipos aparece em **2 a 4** regiões, com a quantidade sorteada [proposta].
- R9: A soma das quantidades é sempre 16 [proposta].
- R10: No máximo **2 tipos** podem ter 4 regiões. Se 2 tipos já têm 4, os outros têm no máximo 3 cada [proposta].
- R11: Definidas as quantidades, os tipos são espalhados de forma aleatória nas 16 posições da grade [proposta].

### Geração do mapa — etapa 2: bônus
- R12: Só depois que os tipos das 16 regiões estão definidos o jogo sorteia os bônus [proposta].
- R13: Em cada região, sorteia-se a **ordem** dos 3 bônus do tipo (qual será o 1º, o 2º e o 3º) [proposta].
- R14: Depois, sorteia-se o **valor** de cada bônus pela posição que ele ganhou [proposta]:
  - 1º bônus: **35 a 50**
  - 2º bônus: **16 a 34**
  - 3º bônus: **5 a 15**
- R15: Os valores são inteiros e os limites entram no sorteio [proposta].
- R16: Todos os bônus têm o mesmo peso; nenhum é especial ou separado dos outros [proposta].

### Bônus da vila
- R17: Os bônus de região da vila são a **soma** dos bônus das 3 regiões escolhidas, bônus por bônus [proposta].

### Gerar novo mapa
- R18: Antes de criar a vila, o jogador pode pedir um novo mapa. Isso repete as etapas 1 e 2 e limpa a seleção atual [proposta].
- R19: A geração deve ser feita no servidor (com semente), e o mapa exibido deve ser o mesmo que será gravado [proposta].

## Números e tabelas

### Bônus por tipo

| Tipo | Bônus 1 | Bônus 2 | Bônus 3 |
|---|---|---|---|
| Floresta | Floresta | Barreiro | Plantações |
| Planície | Plantações | Criações | Floresta |
| Urbana | Indústria | Comércio | Desenvolvimento |
| Litoral | Salinas | Enxofre | Militar |
| Montanha | Rocha | Ferro | Carvão |

A ordem da tabela é só a lista de bônus do tipo; a posição de cada um em cada região vem do sorteio (R13).

### Lista completa de bônus de região (13)

Floresta, Barreiro, Plantações, Criações, Rocha, Ferro, Carvão, Salinas, Enxofre, Militar, Indústria, Comércio, Desenvolvimento.

### Faixas de valor

| Posição | Mín. | Máx. |
|---|---|---|
| 1º | 35 | 50 |
| 2º | 16 | 34 |
| 3º | 5 | 15 |

- Por região: soma mínima 56, soma máxima 99.
- Um mesmo bônus na vila (3 regiões): máximo de 150.

### Distribuições válidas de quantidade por tipo

As quantidades possíveis, em qualquer ordem entre os tipos, são:
- 4, 4, 3, 3, 2
- 4, 3, 3, 3, 3

A distribuição 4, 4, 4, 2, 2 é inválida (3 tipos com 4).

## Exemplos

**Exemplo 1 — Sorteio de uma região Montanha**
- Ordem sorteada: Ferro (1º), Carvão (2º), Rocha (3º).
- Valores: Ferro 44, Carvão 21, Rocha 9.

**Exemplo 2 — Bônus da vila**
- Região 06 (Urbana): Comércio 47, Indústria 30, Desenvolvimento 12.
- Região 07 (Litoral): Salinas 38, Militar 20, Enxofre 6.
- Região 10 (Planície): Criações 41, Floresta 25, Plantações 14.
- Total da vila: Comércio 47, Criações 41, Salinas 38, Indústria 30, Floresta 25, Militar 20, Plantações 14, Desenvolvimento 12, Enxofre 6. Os demais ficam em 0.

**Exemplo 3 — Quantidades por tipo**
- Válido: Floresta 4, Planície 4, Urbana 3, Litoral 3, Montanha 2 (soma 16, dois tipos com 4).
- Inválido: Floresta 4, Planície 4, Montanha 4, Urbana 2, Litoral 2 (três tipos com 4).

## Diferenças em relação ao regioes.md atual

| Tema | Atual (v1) | Proposta (v2) |
|---|---|---|
| Tipos | Rural, Urbana, Coleta | Floresta, Planície, Urbana, Litoral, Montanha |
| Quem define o tipo | Jogador, na escolha | Geração do mapa |
| Recursos | Jazidas em % dos ladrilhos | 3 bônus por região (35–50 / 16–34 / 5–15) |
| Campo | Jazida neutra | Removido (ver questões em aberto) |
| Novo mapa | Semente fixa | Botão "Gerar novo mapa" |

## Interações com outros domínios

- [vila.md](../../../jogo/v1-008-vila-e-mapa/vila.md): criação da vila e 4 casas N1 na 1ª região Urbana.
- [construcoes.md](../../../jogo/v1-003-construcoes/construcoes.md): construções permitidas por tipo de região, que precisam ser remapeadas para os 5 tipos.
- [producao.md](../../../jogo/v1-010-recursos-e-producao/producao.md): como os bônus afetam a produção.

## Questões em aberto

- **Fazendas × Plantações:** "Fazendas" substitui "Campo". Plantações deve ser renomeado para Fazendas?
- **Efeito dos bônus:** o valor (por exemplo, Ferro 44) é % de produção, quantidade por turno ou nº de ladrilhos de jazida?
- **Ladrilhos 10×10:** as jazidas por ladrilho continuam existindo ou são substituídas pelos bônus?
- **Construções por tipo:** quais prédios cada um dos 5 tipos permite? Hoje as fábricas exigem "Urbana" e as minas exigem "Coleta".
- **Limite de novos mapas:** o jogador pode gerar mapas ilimitados ou há um limite ou custo?
- **Anexação:** regiões anexadas depois mantêm o tipo e os bônus sorteados, ou o jogador escolhe na hora (como diz a regra atual)?
