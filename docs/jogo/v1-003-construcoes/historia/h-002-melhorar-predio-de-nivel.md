# H-002 — Melhorar prédio de nível

**Épico:** [../construcoes.md](../construcoes.md) · **Domínio:** [../construcoes.md](../construcoes.md)

## História

Como jogador, quero melhorar um prédio de N1 para N2 ou N2 para N3, para aumentar sua capacidade e eficiência, desde que eu tenha espaço disponível e recursos suficientes.

## Contexto

- Upgrade requer área 2x2 (N2) ou 3x3 (N3) que contenha a atual, com ladrilhos livres (seção 4.1).
- Custo: N1→N2 = 2,5 × custo N1; N2→N3 = 5 × custo N1 (seção 4.1).
- Durante upgrade, prédio não funciona (seção 4.1).
- Jogador escolhe a posição da nova área (seção 4.1).

## Critérios de aceite

### CA1 — Posição válida de upgrade

- **Dado** Casa N1 em (0, 0); espaço (0, 0), (1, 0), (0, 1), (1, 1) livres
- **Quando** solicita upgrade para N2 com posição (0, 0)
- **Então** upgrade criado: Casa N2 em (0, 0), estado EM_UPGRADE, custo 2,5× debitado

### CA2 — Rejeição: espaço ocupado

- **Dado** Casa N1 em (0, 0); ladrilho (1, 1) ocupado por Fazenda
- **Quando** tenta upgrade para N2 com posição (0, 0)
- **Então** API retorna erro "Ladrilhos insuficientes ou ocupados"

### CA3 — Rejeição: recurso insuficiente

- **Dado** Casa N1; estoque com 30 Madeira (insuficiente para 50 needed)
- **Quando** tenta upgrade N1→N2
- **Então** API retorna erro "Recursos insuficientes"

### CA4 — Progresso de upgrade no turno

- **Dado** Casa N2 em EM_UPGRADE, 10 PO totais, com 2 Construtores (eficiência 1,0 cada)
- **Quando** turno processado
- **Então** 2 PO acumulados, após 5 turnos = 10 PO, conclui para ATIVA

### CA5 — Prédio não funciona durante upgrade

- **Dado** Casa N1 com 4 moradores em upgrade
- **Quando** turno é processado
- **Então** Casa não oferece novas vagas ou bônus até conclusão

### CA6 — Downgrade: não permitido

- **Dado** Casa N3 em estado ATIVA
- **Quando** tenta downgrade para N2
- **Então** API rejeita operação "Downgrade não permitido"

## Tarefas

- [h-002-tarefa-001 — Regra e API de upgrade](h-002-tarefa-001-regra-e-api-de-upgrade.md)
- [h-002-tarefa-002 — Interface de upgrade](h-002-tarefa-002-interface-de-upgrade.md)

## Fora de escopo

- Downgrade.
- Cancelamento de upgrade iniciado.
- Transferência de recursos durante upgrade.
