# H-001 — Construir prédio nível 1

**Épico:** [../construcoes.md](../construcoes.md) · **Domínio:** [../construcoes.md](../construcoes.md), [../../v1-010-recursos-e-producao/recursos.md](../../v1-010-recursos-e-producao/recursos.md)

## História

Como jogador, quero construir um prédio (Casa, Fazenda, Fábrica, etc.) em nível 1 em uma região da minha vila, para que eu possa começar a produzir recursos, abrigar cidadãos ou treinar guerreiros.

## Contexto

- Cada prédio tem nível (N1, N2, N3) e começa sempre em N1 (seção 4.1 de construcoes.md).
- Construção ocupa 1x1 (N1), 2x2 (N2) ou 3x3 (N3) ladrilhos contíguos (seção 4.1).
- Recursos são debitados ao iniciar a obra; PO (pontos de obra) acumulam por turno (seção 4.2).
- Durante a obra, o prédio não funciona; trabalhadores ficam ociosos (seção 4.1).
- Prédio só funciona se na região apropriada (seção 1.4) [req].

## Critérios de aceite

### CA1 — Rejeição por tipo de região errado

- **Dado** jogador em região Floresta com autorização para construir Quartel (tipo Urbana)
- **Quando** submete ordem de construção do Quartel
- **Então** API retorna erro 400 com mensagem "Quartel só pode ser construído em região Urbana"

### CA2 — Rejeição por ladrilho ocupado

- **Dado** Fazenda de plantio N1 em (5, 5) da região Planície 10
- **Quando** tenta construir Acampamento de lenhadores N1 em (5, 5)
- **Então** API retorna erro 400 "Ladrilho ocupado"

### CA3 — Débito de recursos e criação bem-sucedida

- **Dado** jogador com estoque: 300 Madeira, 150 Pedra, 100 Argila, 300 Ouro; região Urbana 1 vazia em (0, 0)
- **Quando** inicia construção de Casa N1 (custo 20 Mad, 10 Ped, 10 Arg)
- **Então** recursos debitados imediatamente: 280 Mad, 140 Ped, 90 Arg restantes; Casa criada com estado EM_OBRA, 4 PO totais

### CA4 — Progresso de obra com Construtores

- **Dado** Casa N1 em construção (EM_OBRA, 4 PO totais, 0 atuais) com 2 Construtores (eficiência 1,0 cada)
- **Quando** turno é processado
- **Então** progresso: 2 PO (1,0 × 1 + 1,0 × 1 = 2 PO/turno); após 2 turnos = 4 PO, obra conclui, estado = ATIVA

### CA5 — Recurso insuficiente

- **Dado** jogador com estoque: 10 Madeira, 100 Pedra, 100 Argila
- **Quando** tenta construir Casa N1 (20 Madeira necessária)
- **Então** API retorna erro 400 "Recursos insuficientes"

### CA6 — Prédio em região sem bônus associado

- **Dado** região Floresta (tipo Floresta, sem bônus de Minério)
- **Quando** tenta construir Mina de ferro N1 (requer bônus de Minério)
- **Então** rejeitado: "Mina de ferro só pode ser construído em região Montanha"

## Tarefas

- [h-001-tarefa-001 — Catálogo de construções](h-001-tarefa-001-catalogo-de-construcoes.md)
- [h-001-tarefa-002 — API de construção e posicionamento](h-001-tarefa-002-api-de-construcao-e-posicionamento.md)
- [h-001-tarefa-003 — Progresso de obra no turno](h-001-tarefa-003-progresso-de-obra-no-turno.md)
- [h-001-tarefa-004 — Tela de construção na região](h-001-tarefa-004-tela-de-construcao-na-regiao.md)

## Fora de escopo

- Upgrade e demolição (história h-002).
- Marcação de ladrilhos de coleta (história h-003).
- Alocação de trabalhadores e seu impacto em PO/produção (história h-004).
- Cancelamento de obra.
