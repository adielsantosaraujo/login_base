# Tela 02 — Distribuir a população

**Rota:** `/app/jogo/distribuir-populacao` · **Protótipo:** `../prototipos/Distribuir Populacao.dc.html` · **Regras:** `../regras/regras-populacao-v1.md` · **API:** `../api/contratos-api.md` §4–§5 · **Tokens:** `design-tokens.md`

## Layout
- Header comum (aba "Famílias" ativa, "TURNO 1").
- `main`: max-width 1320px, padding 36px 28px 48px, gap 28px.
  - **Topo**, em linha flex com quebra:
    - À esquerda:
      - etapas "01 Regiões ✓ — 02 População" (Mono 12px, com a etapa atual em `--accent`);
      - H1 "Distribuir a população";
      - parágrafo: "Os 16 cidadãos já vêm distribuídos segundo o plano inicial. Ajuste o que quiser: cada pessoa tem até 20 pontos de característica e 10 de profissão. Pontos não usados ficam pendentes."
    - À direita, os botões em pílula "↻ Redistribuir pelo plano" e "Zerar tudo".
  - Linha flex:
    - **Conteúdo** (flex 1 1 640px)
    - **Painel** (flex 1 1 320px, max 380px, sticky).

## Abas de família
- Grid `auto-fit minmax(150px,1fr)`, gap 8px.
- Cada aba é um botão (padding 12px 14px, raio 12px) com:
  - sobrenome (Bricolage 17px);
  - chip "LÍDER", se for a família líder;
  - status: "X pontos pendentes" em `--warn`, ou "Todos os pontos usados".
- Ativa: borda 2px `--accent` e fundo `--accent-bg`.

## Card do cidadão
- Grid `auto-fill minmax(300px,1fr)`, gap 12px; os 4 membros da família ativa.
- Card: padding 16px, raio 14px, fundo `--surface-2`.
- **Cabeçalho**:
  - nome (Bricolage 18px);
  - "40 anos · Pai" (12px);
  - à direita, o **seletor da profissão principal**: um select com aparência de chip (11px 600, maiúsculas, fundo `--accent-bg`, borda `--accent` a 35%, seta ▼) com as 12 profissões. Ao trocar, aplica a regra R8: refaz a distribuição da pessoa e ajusta o plano. Sem pontos de profissão, mostra "Sem profissão".
- **Características**:
  - rótulo e contador "X / 20" (em `--accent` quando está em 20);
  - 5 linhas no grid `34px | barra | 84px`: sigla (Mono 12px, com tooltip do nome completo), barra de 6px (largura = valor ÷ 20) e stepper.
- Divisor de 1px.
- **Profissões**:
  - rótulo e contador "X / 10";
  - grid de 2 colunas com as 12 profissões, cada uma com nome, **bônus R3**, stepper (botões de 20px) e valor;
  - **bônus R3** (cidadao.md): logo após o nome, em Mono 11px, `+N`, onde `N = Σ floor(característica ÷ 5)` das características ligadas à profissão. Fica em `--accent` se N > 0 e esmaecido se N = 0. O tooltip mostra a conta (ex.: "Bônus R3: FOR 8÷5 + VEL 7÷5"). Recalcula a cada mudança de característica. O total da R2 (PE efetivo) não é exibido;
  - com valor 0, o nome e o valor ficam esmaecidos (`--text-3`).
- Stepper: "+" fica desativado quando o total da pessoa chega ao limite; "−" fica desativado em 0.

## Painel
1. **Plano inicial**
   - Título e o total "16 / 16" (`--warn` se for diferente de 16).
   - Cabeçalho: PROFISSÃO | PLANO | ATUAL.
   - 12 linhas, cada uma com:
     - o nome, mais um chip "mín 2" para Construtor e Carregador;
     - um stepper com a quantidade (o "−" não desce abaixo do mínimo; o "+" para quando o total chega a 16);
     - "Atual": quantas pessoas têm a profissão como principal. Fica em `--error` abaixo do mínimo, em `--warn` se for diferente do plano, e neutro se for igual.
   - Nota: "“Atual” conta quem tem a profissão como principal (maior PE). Ajuste o plano e clique em Redistribuir."
2. **Família líder** ("+1% a cada 2 CAR")
   - 4 opções em estilo rádio. Cada uma mostra "Família X", "Líder Nome · CAR n" e o bônus "+n%" em Mono `--accent`.
   - Selecionada: borda `--accent` e fundo `--accent-bg`.
3. **Confirmação**
   - Checklist:
     - "Construtores principais n / 2"
     - "Carregadores principais n / 2"
     - "Pontos pendentes N" (aviso "!", não bloqueia) ou "Todos os pontos usados"
   - CTA:
     - Válido: "Confirmar população".
     - Inválido: "Ajuste os mínimos".
     - Ao clicar: `POST /populacao`, com spinner, e depois redireciona para `/app/jogo/mapa`.

## Estado (frontend)
- `cidadaos[16]`: `{ id, familia, papel, nome, idade, caracteristicas{5}, profissoes{12} }`
- `plano{12}`, `familiaAtiva`, `familiaLider`
- Derivados:
  - `principal(c)`: a profissão de maior PE (empate: a ordem da tabela);
  - `contagemPrincipais`;
  - `pendentes`;
  - `bonusLider = min(10, floor(CAR ÷ 2))`.
- Ações:
  - `editar(c, grupo, chave, ±1)`, respeitando o total;
  - `trocarPrincipal(c, prof)`;
  - `redistribuir()`, só com o plano somando 16;
  - `zerar()`;
  - `confirmar()`.
- O algoritmo de distribuição está em `../referencia/distribuicao-populacao.js`. Ele pode rodar no frontend (é determinístico) ou ser portado para o backend.

## Componentes Vue sugeridos
`DistribuirPopulacaoView.vue`, `FamiliaTabs.vue`, `CidadaoCard.vue`, `PontoStepper.vue`, `PlanoInicialPainel.vue`, `FamiliaLiderSelector.vue`, `ConfirmacaoPainel.vue`.
