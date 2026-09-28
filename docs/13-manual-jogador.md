# Manual do Jogador

| Campo | Valor |
|---|---|
| Versão | 1.2.0 |
| Data | 2026-09-28 |
| Status | Vigente — baseline do commit `454ae58` + changes `add-frontend-build` e `add-soldier-names-batch-slots` implementadas |
| Modelo/norma | Diátaxis (tutorial + how-to) |
| Público | jogador final (linguagem acessível) |
| Fontes | `frontend/src/views/*.vue`; `src/main/resources/templates/sistema/public/login.html`; `jogo/CodigoErro.java`; `openspec/changes/archive/2026-09-27-add-city-builder-game/design.md` §3–§11 |

> Parte da [documentação do login_base](README.md). Guia prático: como jogar, navegar as telas, interpretar mensagens de erro e estratégias básicas.

---

## 1. Acesso e login

### Como entrar no jogo

1. Abra o navegador e acesse `http://localhost:5173` em desenvolvimento (Vite dev server), ou `http://localhost/` em produção (servido pelo backend após `make build_front`).
2. Na tela de login, informe seus dados:
   - **Login:** e-mail ou celular (11 dígitos, apenas números).
   - **Senha:** senha fornecida pelo administrador.
3. Clique em **Entrar**.
4. Você será redirecionado para a sua **Vila**.

### Logout (sair)

No canto superior direito do menu, clique em **Sair**. Você será desconectado e voltará à tela de login.

### Sessão

Sua sessão expira após **30 minutos sem usar o jogo**. Se isso acontecer, você será desconectado automaticamente. Faça login novamente.

---

## 2. Primeiros passos (tutorial)

Seu objetivo é **vencer a Masmorra 1** para começar sua aventura.

### Passo 1: Conheça sua vila inicial

Ao entrar, você verá sua **Vila** com:
- **Prédios:** Centro da vila (N1), Armazém (N1), Fazenda (N1), Serraria (N1), Pedreira (N1).
- **Recursos:** 300 comida, 400 madeira, 300 pedra, 50 ferro.
- **Canteiros:** 1 canteiro plantado com Trigo (produz comida automaticamente).

Internamente os recursos são guardados em milésimos; a tela mostra unidades (300 comida = 300 000 milésimos).

### Passo 2: Produza recursos básicos

Recursos são produzidos **automaticamente**. Quanto mais altos os níveis da Serraria, Pedreira e Mina, mais rápido produzem. Para ver os efeitos, aguarde alguns segundos e atualize a página.

### Passo 3: Construa a Mina de ferro, a Forja e o Quartel (N1)

Você vai precisar de armas e tropas para vencer a masmorra. A Forja exige Mina de ferro N1; o Quartel exige Forja N1. Comece:

1. **Vá para a tela "Vila".**
2. **Melhore a Mina de ferro para N1** (a Forja exige Mina N1):
   - Clique no card da Mina de ferro.
   - Clique em **Melhorar**.
   - Confirme (o custo será debitado imediatamente; a ordem aparecerá na fila).
3. **Aguarde a conclusão** (no relógio da tela de Vila).
4. **Melhore a Forja para N1** (custa M120 P100 F40; o ferro inicial é 50, aguarde a produção da mina).
5. **Aguarde a conclusão.**
6. **Melhore o Quartel para N1** (custa M150 P120 F40).

### Passo 4: Crie uma arma na Forja

1. **Vá para "Forja".**
2. **Crie uma Espada N1:**
   - Selecione "Espada" como modelo.
   - Nível: 1.
   - Quantidade: 1.
   - Clique em **Forjar**.
3. **Aguarde a conclusão** (60 segundos).

### Passo 5: Crie uma armadura na Forja

1. **Vá para "Forja".**
2. **Crie uma Armadura de Couro N1:**
   - Selecione "Armadura de Couro".
   - Nível: 1.
   - Quantidade: 1.
   - Clique em **Forjar**.

### Passo 6: Treine Soldados em Lote no Quartel

1. **Vá para "Quartel".**
2. **Configura o treino:**
   - Selecione o tipo: **SOLDADO**.
   - Selecione o **Nível da Arma**: 1 (Espada N1 exigida para Soldado).
   - Selecione o **Modelo de Armadura**: "Couro" (recomendado) ou "Ferro".
   - Selecione o **Nível da Armadura**: 1.
   - **Quantidade**: Digite "1" para começar com 1 unidade.
   - Dica: Clique em **"Máx."** para calcular o máximo que você pode treinar (com os recursos atuais).
3. **Clique em "Treinar".**
4. **Aguarde a conclusão** (60 segundos por unidade, ajustado pela velocidade do jogo).

### Passo 6A: Conheça os Nomes das Suas Tropas

Cada soldado recebe um **nome** e **sobrenome** ao ser treinado:
- Exemplo: "Ana Silva", "Pedro Santos", "Maria Oliveira".
- **Nomes repetidos**: Se você treina outro "Ana Silva", ela será exibida como **"Ana Silva (2)"**, a próxima como **"Ana Silva (3)"** (sufixo ordinal).
- A contagem de nomes é **histórica e por vila**: mesmo que um soldado morra, o contador não volta (preserva a história da sua vila).

### Passo 6B: Veja os Detalhes de um Soldado

1. **Na lista de soldados do Quartel**, clique no **nome** de qualquer soldado.
2. **Abre a tela de detalhe** com:
   - **Nome exibido** (com sufixo se repetido): ex., "Ana Silva (2)".
   - **Atributos**: HP, Ataque, Defesa, Alcance, Movimento.
   - **9 Slots de Equipamento**: 
     - ARMA e ARMADURA (preenchidos com o que você selecionou).
     - CABEÇA, BOTA, LUVA, COLAR, ANEL_1, ANEL_2, ANEL_3 (vazios no futuro).
   - **Botão "Trocar"** em cada slot preenchido (Arma e Armadura) para substituir por outro item `DISPONIVEL` (exceto em masmorra).

### Passo 7: Entre na Masmorra 1

1. **Vá para "Masmorras".**
2. **Veja "Masmorra 1" (desbloqueada).**
3. **Selecione a tropa:**
   - No card da Masmorra 1, abra o seletor "Selecione até 4 unidades".
   - Escolha o Soldado que você treinou (limite: até 4).
4. **Clique em "Entrar".**

### Passo 8: Vença a batalha

Veja a seção §4 para detalhar as ações de combate. Em resumo:
- **Mova** seu soldado perto dos inimigos (Goblins).
- **Ataque** com distância Manhattan ≤ 1.
- **Defenda** se precisa resistir.
- **Encerre turno** para ativar a IA dos inimigos.

Quando todos os 3 Goblins morrerem, você **vence** e coleta o loot.

### Recompensa

Após vencer, você recebe:
- **Recursos garantidos:** 40 comida, 50 madeira, 50 pedra, 20 ferro.
- **2 rolagens de loot:** podem gerar sementes, ferro extra ou itens de masmorra.
- **Desbloqueio:** Masmorra 2 fica disponível.

---

## 3. Telas do jogo

### 3.1 Vila

**O que mostra:**
- Nome da vila e recursos flutuantes no topo.
- **Cards de prédios:** cada card exibe tipo, nível atual, custo para próximo nível e tempo de conclusão (se em construção).
- **Fila de ordens:** lista todas as construções em andamento, em ordem cronológica.

**Como usar:**
- Clique em um card para ver detalhes e **Melhorar** (se houver recursos suficientes).
- Ordens são processadas sequencialmente; apenas uma construção por vez.

### 3.2 Fazenda

**O que mostra:**
- **Título:** "Fazenda (nível N)" mostra quantos canteiros você tem (até 5).
- **Tabela de canteiros:** posição, cultivo atual, produção/hora e botões para selecionar novo cultivo e plantar.
- **Estoque de sementes:** lista quantidade de cada semente.

**Como usar:**
- Selecione um **novo cultivo** no dropdown (ex.: Milho).
- Clique **Plantar** (instantâneo; consome 1 semente se não for Trigo).
- Plantios subsequentes **preservam o progresso anterior** no canteiro.

**Cultivos disponíveis:**
- **Trigo:** 20 comida/h, sem semente (sempre disponível).
- **Milho:** 30 comida/h, exige semente (obtida em Masmorra N≥1).
- **Batata:** 45 comida/h, exige semente (obtida em Masmorra N≥2).
- **Abóbora dourada:** 70 comida/h, exige semente (obtida em Masmorra N≥4).

### 3.3 Forja

**O que mostra:**
- **Fila de ordens:** ordens de forja em andamento.
- **Criador de ordem:** dropdowns para modelo (Espada, Lança, Arco, Armadura de Couro, Armadura de Ferro), nível (1–5, limitado ao nível da Forja) e quantidade (1–5).

**Como usar:**
1. Selecione o modelo desejado.
2. Escolha o nível (máximo: nível da Forja).
3. Escolha a quantidade (1–5).
4. Clique **Forjar**.

**Tempo:** base do modelo × nível × quantidade ÷ velocidade, arredondado para cima.

### 3.4 Quartel

**O que mostra:**
- **Fila de treino:** ordens de treino em andamento.
- **Criador de ordem:** tipo de tropa (Soldado, Arqueiro, Lanceiro), seleção de arma (da categoria exigida) e armadura.
- **Unidades disponíveis:** lista de tropas já treinadas, com tipo, HP e status.

**Como usar:**
1. Selecione o tipo de tropa (limitado ao nível do Quartel).
2. Escolha a arma **exigida** (ex.: Soldado exige Espada).
3. Escolha a armadura.
4. Clique **Treinar**.

**Restrições:**
- Soldado (N1 quartel): exige Espada.
- Arqueiro (N2 quartel): exige Arco.
- Lanceiro (N3 quartel): exige Lança.

### 3.5 Masmorras

**O que mostra:**
- **Aviso:** se houver uma batalha em andamento, um card diz "Retomar batalha" com botão.
- **Cards de masmorras:** 5 cards (níveis 1–5), cada um mostrando:
  - Nível (e "Bloqueado" se não desbloqueado).
  - Inimigos que aparecem no nível.
  - Seletor de até 4 unidades.
  - Botão **Entrar** (ativado apenas se houver 1–4 unidades selecionadas).

**Como usar:**
1. Selecione um nível desbloqueado.
2. Selecione até 4 unidades no MultiSelect.
3. Clique **Entrar**.
4. Você será redirecionado para a **Batalha**.

### 3.6 Batalha

**O que mostra:**
- **Título:** "Masmorra N" e status da batalha (Turno X/30).
- **Mapa 8×8:** grid com células em preto (obstáculos) ou brancas, combatentes renderizados como peças (ex.: `J1`, `I1`).
- **Unidades e inimigos:**
  - `J1..J4`: suas unidades (ordem do esquadrão).
  - `I1..I5`: inimigos (ordem de spawn).
  - Cada um mostra HP / HP_máx.
- **Log:** histórico de ações.
- **Controles:** 5 botões para ações do turno.

**Como usar:**
1. **Clique em uma casa vazia para mover** a unidade selecionada.
2. **Clique em um inimigo para atacar** (se estiver ao alcance).
3. **"Defender"** dobra sua defesa até fim do turno.
4. **"Encerrar turno"** ativa a IA dos inimigos; eles se movem e atacam.
5. **"Render"** encerra a batalha em derrota.

**Mecânica:**
- Cada unidade pode se mover **OU** agir por turno.
- Distância é calculada em **Manhattan** (vertical + horizontal).
- Dano = `max(1, ataque - defesa_efetiva)`.
- Defendendo dobra sua defesa.
- Máximo 30 turnos; se não vencer até lá, você perde.

**Vitória:** todos os inimigos morrem.  
**Derrota:** todas as suas unidades morrem, você se rende, ou 30 turnos acabam.

---

## 4. Como fazer (procedimentos)

### Melhorar um prédio

1. Vá para **Vila**.
2. Clique no card do prédio (ex.: Serraria).
3. Clique **Melhorar** (se houver recursos suficientes).
4. A ordem aparecerá na fila.

**Tempo:** `base × 2^(N-1)` segundos (ex.: Serraria: 120 s para ir de N1 a N2, 240 s para N3 — N é o nível que se quer atingir).

### Plantar um cultivo

1. Vá para **Fazenda**.
2. Na tabela, escolha a posição.
3. Selecione o novo cultivo no dropdown (ex.: Milho).
4. Clique **Plantar** (se tiver semente para cultivos que exigem).

**Instantâneo.** Produção começa imediatamente.

### Forjar itens

1. Vá para **Forja**.
2. Selecione o modelo (ex.: Espada).
3. Selecione nível (1–5, máx. nível da Forja).
4. Selecione quantidade (1–5).
5. Clique **Forjar**.

**Fila:** apenas uma ordem por vez.

### Treinar tropas

1. Vá para **Quartel**.
2. Selecione o tipo (ex.: Soldado).
3. Selecione arma (ex.: Espada N1).
4. Selecione armadura (ex.: Armadura de Couro N1).
5. Clique **Treinar**.

**Tempo:** base do tipo ÷ velocidade (ex.: Soldado 60s).

### Montar esquadrão para masmorra

1. Vá para **Masmorras**.
2. No card do nível desejado, clique no MultiSelect de unidades.
3. Selecione até 4 unidades (1–4 obrigatórias).
4. Clique **Entrar**.

### Mover em combate

1. Clique em sua unidade no mapa.
2. Clique em uma célula vazia para mover até lá.

**Restrição:** máximo `movimento` casas por turno (Manhattan). Obstáculos bloqueiam.

### Atacar em combate

1. Clique em sua unidade.
2. Clique em um inimigo.

**Restrição:** alcance ≤ distância Manhattan. Ex.: espada (alcance 1) só ataca vizinhos; arco (alcance 3) ataca até 3 casas.

### Defender em combate

1. Clique em sua unidade.
2. Clique **Defender**.

**Efeito:** sua defesa é **dobrada** até fim do turno. Útil contra ataques numerosos.

### Encerrar turno

1. Clique **Encerrar turno**.

A IA dos inimigos se move e ataca. Seu turno volta.

### Render-se

1. Clique **Render**.

Derrota imediata. Você perde a batalha.

---

## 5. Mensagens de erro

Se uma ação falhar, um aviso em **vermelho (Toast)** aparecerá com a mensagem. Aqui estão os erros comuns:

| Código | Mensagem | O que fazer |
|---|---|---|
| `RECURSOS_INSUFICIENTES` | "Recursos insuficientes para esta ação." | Aguarde a produção ou melhore a economia. |
| `FILA_OCUPADA` | "Já existe uma ordem em andamento nesta categoria." | Aguarde a ordem anterior terminar. |
| `NIVEL_MAXIMO` | "Prédio já está no nível máximo (5)." | Não é possível melhorar mais. |
| `REQUISITO_NAO_ATENDIDO` | "Pré-requisito não atendido." | Construa/melhore o prédio necessário primeiro. |
| `CANTEIRO_INEXISTENTE` | "Canteiro não encontrado." | Verifique a posição (1–N). |
| `SEMENTE_INDISPONIVEL` | "Não há sementes deste cultivo." | Obtenha sementes em uma masmorra. |
| `ITEM_INDISPONIVEL` | "Item não encontrado ou já em uso." | Verifique o estoque ou espere a forja terminar. |
| `CAPACIDADE_EXERCITO` | "Capacidade do exército excedida." | Melhore o Quartel ou desfaça-se de unidades. |
| `MASMORRA_BLOQUEADA` | "Masmorra não desbloqueada." | Vença a masmorra anterior. |
| `BATALHA_EM_ANDAMENTO` | "Já existe uma batalha em andamento." | Termine ou retome a batalha. |
| `UNIDADE_INDISPONIVEL` | "Unidade em masmorra ou não existe." | A unidade está em combate. |
| `ACAO_INVALIDA` | "Ação não permitida (ex.: alvo fora de alcance)." | Verifique as restrições (distância, HP, turno). |
| `BATALHA_ENCERRADA` | "A batalha já terminou." | Reinicie ou retome a batalha. |
| `TURNO_DESATUALIZADO` | "Turno não corresponde ao turno atual (conflito)." | Atualize a página. |

---

## 6. Dicas de estratégia

### Economia inicial

1. **Melhore Serraria e Pedreira rápido:** N1 → N2 custa pouco e dobra produção.
2. **Capacidade:** não deixe recursos encherem (armazém cheia = produção parada).
3. **Ritmo:** construir e forjar levam tempo; planeje ordens com antecedência.

### Combate

1. **Posicionamento:** coloque unidades frágeis (Arqueiro) longe dos inimigos.
2. **Alcance:** use Arco (alcance 3) para atacar de longe; Espada (alcance 1) para dano próximo.
3. **Defesa:** Defendendo dobra sua defesa — use contra Trolls!
4. **Turno máximo:** 30 turnos é limite; não demore demais. Se for muito lento, melhore o Quartel e treine tropas melhores.

### Progressão de masmorras

- **Masmorra 1:** 2 Soldados N1 já vencem (3 Goblins).
- **Masmorra 2:** adicione 1 Arqueiro N1 (Esqueleto Arqueiro é ranged).
- **Masmorra 3+:** suba níveis de tropas e armas; use defensão contra Orcs.

### Sementes

- **Trigo:** sempre produz 20 comida/h (sem semente).
- **Milho:** 30/h, desbloqueado em Masmorra N≥1.
- **Batata:** 45/h, desbloqueado em Masmorra N≥2.
- **Abóbora:** 70/h, desbloqueado em Masmorra N≥4 (melhor comida/h).

Diversifique os canteiros para maximizar comida, pois treinar tropas consome recursos.

---

## 7. Perguntas frequentes

### "Meus recursos pararam de subir!"

**R:** Armazém cheia (capacidade atingida). Construa prédios ou treine tropas para consumir recursos. Capacidade aumenta com nível do Armazém.

### "Por que só posso ter uma ordem de construção?"

**R:** É intencional. Planeja-se com tempo. O jogo prioriza estratégia.

### "Posso cancelar uma ordem?"

**R:** Não. Uma vez iniciada, a ordem termina. Recursos já foram debitados.

### "O que acontece com minhas unidades mortas?"

**R:** Unidades que morrem em combate são perdidas permanentemente, junto com seus itens. Não há ressurreição.

### "Perdi uma batalha. E agora?"

**R:** Sem penalidade além das unidades mortas. Treine novas tropas e tente novamente.

### "Qual é a masmorra mais fácil?"

**R:** Masmorra 1 com 3 Goblins. Recomenda-se começar por lá.

### "Quanto tempo leva para vencer o jogo?"

**R:** Não há "fim" definido. O jogo é de progressão contínua. Melhore continuamente para acessar níveis mais altos.

---

## 8. Referências

- **Mecânicas de jogo completas:** [Game Design Document](12-gdd.md)
- **Estrutura técnica:** [Arquitetura](04-arquitetura.md)
- **Guia de desenvolvedor:** [Onboarding para novos devs](09-guia-desenvolvedor.md)

---

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.1.0 | 2026-09-27 | Change add-frontend-build implementada: SPA servida pelo backend em produção | Adiel, com apoio de agentes Claude |
| 1.0.1 | 2026-09-27 | Atualização para a change add-frontend-build (prevista, aberta) — endereço do servidor | Adiel, com apoio de agentes Claude |
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
