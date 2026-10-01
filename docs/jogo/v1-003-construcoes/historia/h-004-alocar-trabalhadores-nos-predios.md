# H-004 — Alocar trabalhadores nos prédios

**Épico:** [../construcoes.md](../construcoes.md) · **Domínio:** [../construcoes.md](../construcoes.md), [../../v1-002-cidadaos/cidadao.md](../../v1-002-cidadaos/cidadao.md)

## História

Como jogador, quero alocar cidadãos em um prédio para que trabalhem nele e gerem produção, respeitando vagas, idade e status (não em tropa).

## Contexto

- Cada prédio tem vagas: N1 = 2, N2 = 5, N3 = 10 (ou específico por tipo) (seção 4.3).
- Qualquer pessoa 14–64 anos pode trabalhar em qualquer prédio (seção 5.3).
- Quem tem 0 PE na profissão rende 0,5 eficiência (seção 5.3).
- Menores de 14 e 65+ não trabalham (seção 5.3).
- Membro de tropa não trabalha em prédios (seção 5.3).
- Cada pessoa trabalha em no máximo 1 prédio (seção 5.3).

## Critérios de aceite

### CA1 — Alocar cidadão com profissão correta

- **Dado** Serraria N1 (2 vagas); Madeireiro com PE 5, 30 anos
- **Quando** aloca cidadão à Serraria
- **Então** alocação criada; eficiência calculada; vagas restantes = 1

### CA2 — Rejeição: vaga cheia

- **Dado** Serraria N1 com 2 Madeireiros já alocados
- **Quando** tenta alocar 3º Madeireiro
- **Então** rejeitado "Sem vagas disponíveis"

### CA3 — Rejeição: idade fora do intervalo

- **Dado** Cidadão com 13 anos
- **Quando** tenta alocar a qualquer prédio
- **Então** rejeitado "Idade insuficiente (14–64)"

### CA4 — Rejeição: membro de tropa

- **Dado** Guerreiro em tropa aquartelada
- **Quando** tenta alocar a Quartel como instrutor
- **Então** rejeitado "Membro de tropa não pode alocar"

### CA5 — Desalocar cidadão

- **Dado** Cidadão alocado à Serraria
- **Quando** desaloca
- **Então** vagas aumentam; próximo turno, produção reduz

### CA6 — Armazém com mínimo de Carregadores

- **Dado** Armazém N1 sem alocações; 1 Carregador alocado (eficiência 0,8)
- **Quando** aplica bônus
- **Então** capacidade adicional = 0,8 × 500 = 400

## Tarefas

- [h-004-tarefa-001 — API de alocação](h-004-tarefa-001-api-de-alocacao.md)
- [h-004-tarefa-002 — Painel do prédio](h-004-tarefa-002-painel-do-predio.md)

## Fora de escopo

- Troca automática de alocação.
- Limite de trabalho por pessoa (já contemplado: 1 prédio por vez).
