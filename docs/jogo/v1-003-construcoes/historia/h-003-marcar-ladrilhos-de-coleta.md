# H-003 — Marcar ladrilhos de coleta

**Épico:** [../construcoes.md](../construcoes.md) · **Domínio:** [../construcoes.md](../construcoes.md), [../predios-de-coleta.md](../predios-de-coleta.md)

## História

Como jogador, quero marcar ladrilhos com jazida para um prédio de coleta, determinando quantos trabalhadores podem ser produtivos nele.

## Contexto

- Prédios de coleta exigem marcação de ladrilhos com jazida compatível (seção 4.6).
- Máximo de marcados por nível: N1 = 4, N2 = 10, N3 = 20 (seção 4.6).
- Trabalhadores produtivos = `min(alocados, floor(marcados ÷ 2))` (seção 4.6).
- Marcação é conectada ortogonalmente ao prédio (seção 4.6).
- Marcar/desmarcar é gratuito e vale a partir do próximo turno (seção 4.6).

## Critérios de aceite

### CA1 — Marcar ladrilho compatível

- **Dado** Acampamento de lenhadores N1 em (5, 5); ladrilho (5, 6) tem Floresta
- **Quando** marca (5, 6)
- **Então** ladrilho marcado; 1 trabalhador produtivo com 2 alocados (piso(2÷2)=1)

### CA2 — Rejeição: jazida incompatível

- **Dado** Pedreira N1 (requer Rocha); ladrilho (6, 5) tem Floresta
- **Quando** tenta marcar (6, 5)
- **Então** rejeitado "Jazida incompatível"

### CA3 — Rejeição: limite de marcados

- **Dado** Acampamento N1 (máximo 4), com 4 já marcados
- **Quando** tenta marcar 5º ladrilho
- **Então** rejeitado "Limite de marcados atingido"

### CA4 — Desmarcar ladrilho

- **Dado** Acampamento com 4 marcados
- **Quando** desmarca 1
- **Então** 3 marcados; próximo turno, produção usa 3

### CA5 — Conectividade ortogonal

- **Dado** Mina de ferro em (5, 5); ladrilho (7, 5) sem prédio entre eles
- **Quando** marca (7, 5)
- **Então** rejeitado "Não conectado ortogonalmente"

### CA6 — Upgrade muda limite

- **Dado** Acampamento N1→N2 com 4 marcados
- **Quando** upgrade conclui
- **Então** limite sobe para 10; pode marcar mais 6

## Tarefas

- [h-003-tarefa-001 — API de marcação de ladrilhos](h-003-tarefa-001-api-de-marcacao-de-ladrilhos.md)
- [h-003-tarefa-002 — Interface de marcação](h-003-tarefa-002-interface-de-marcacao.md)

## Fora de escopo

- Sugestão automática de ladrilhos.
- Undo/redo.
