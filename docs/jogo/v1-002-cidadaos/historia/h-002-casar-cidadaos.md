# H-002 — Casar cidadãos

**Épico:** [../cidadao.md](../cidadao.md) · **Domínio:** [../familias.md](../familias.md)

## História

Como jogador, quero casar cidadãos para formar novos núcleos familiares e expandir minha população.

## Contexto

Casamento cria um novo núcleo familiar na casa escolhida. Exige núcleo livre e ambos os cônjuges ≥18 anos, solteiros, não parentes. Casal homem+mulher pode reproduzir. O sobrenome do novo núcleo é escolhido pelo jogador entre os dois.

**Regras** (seção 5.7):
- Exige núcleo livre em uma casa.
- Ambos ≥18 anos, solteiros, vivos, da mesma vila, não parentes de 1º grau nem irmãos.
- Casal se muda para a casa escolhida e forma novo núcleo; sobrenome escolhido pelo jogador.
- Viúvos podem casar novamente.

## Critérios de aceite

### CA1 — Validar requisitos de casamento

- **Dado** dois cidadãos A e B.
- **Quando** o jogador tenta casá-los.
- **Então**:
  - Se idade < 18: rejeitado com mensagem "Ambos devem ter ≥18 anos".
  - Se um ou ambos não solteiros: rejeitado com mensagem "Ambos devem ser solteiros".
  - Se parentes de 1º grau (pai/mãe/irmão/irmã): rejeitado com mensagem "Não podem ser parentes".
  - Se um ou ambos mortos: rejeitado com mensagem "Ambos devem estar vivos".
  - Se de vilas diferentes: rejeitado com mensagem "Devem ser da mesma vila".
  - Caso contrário, casamento permitido.

### CA2 — Exigir núcleo livre

- **Dado** casal elegível e zero núcleos livres em todas as casas.
- **Quando** o jogador tenta casar.
- **Então** a operação é rejeitada com mensagem "Nenhum núcleo livre em nenhuma casa".

### CA3 — Registrar casamento com novo sobrenome

- **Dado** casal elegível (A de Silva, B de Pereira) e uma casa com núcleo livre.
- **Quando** o jogador escolhe a casa e o sobrenome "Silva" (um dos dois).
- **Então**:
  - Novo núcleo criado com sobrenome "Silva".
  - Ambos têm conjugeId apontando um para o outro.
  - Ambos são transferidos para a nova família (familiaId do novo núcleo).
  - Núcleo anterior fica desfeito (campo de referência na casa zera, ou núcleo deletado).
  - Status: casamento efetuado no turno corrente.

### CA4 — Casal em casa diferente da origem

- **Dado** casal casado em nova casa.
- **Quando** o turno avança.
- **Então** ambos residem na nova casa (construcaoId não afetado, mas familiaId e casa atualizados).

## Tarefas

- [h-002-tarefa-001 — Regra e API de casamento](h-002-tarefa-001-regra-e-api-de-casamento.md)
- [h-002-tarefa-002 — Tela de famílias e casamento](h-002-tarefa-002-tela-de-familias-e-casamento.md)

## Fora de escopo

- Divórcio.
- Casamento entre mais de 2 pessoas (v1 heteronormativa).
- Autorização de terceiros (pais vivos).
