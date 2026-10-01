# H-002 · Tarefa 002 — Interface de upgrade

**História:** [H-002 — Melhorar prédio de nível](h-002-melhorar-predio-de-nivel.md) · **Domínio:** [../construcoes.md](../construcoes.md) · **Depende de:** [h-002-tarefa-001-regra-e-api-de-upgrade.md](h-002-tarefa-001-regra-e-api-de-upgrade.md) · **Camada:** Frontend

## Objetivo

Implementar modal de upgrade mostrando preview da nova área, custos multiplicados e validações visuais.

## Contexto necessário

- [../construcoes.md](../construcoes.md) — Custos de upgrade (seção 4.1)
  > N1→N2 = 2,5×; N2→N3 = 5×.

## Backend

Não se aplica.

## Frontend

- **Modal `UpgradeModal.vue`**:
  - Exibe prédio atual (nível, posição, tamanho).
  - Opção para escolher novo nível (N2 ou N3, conforme atual).
  - Preview visual: grade com prédio antigo (transparente) e nova área (destaque).
  - Custos por nível: Madeira, Pedra, ... (2,5× ou 5×).
  - Validação: espaço livre ou ocupado (visual).
  - Botão "Confirmar upgrade" ativa API.

- **Integração em `RegiaoConstrucao.vue`**:
  - Click em prédio ATIVA → menu de ações (Upgrade, ...).
  - "Upgrade" → abre `UpgradeModal`.

- **Chamada de API**:
  - `POST /api/jogo/construcoes/{id}/upgrade`.
  - Sucesso: atualiza grade, notificação.
  - Erro: exibe mensagem.

## Arquivos prováveis

- [/frontend/src/components/UpgradeModal.vue](/frontend/src/components/UpgradeModal.vue) (novo)
- [/frontend/src/services/api/upgradeApi.ts](/frontend/src/services/api/upgradeApi.ts) (novo)

## Testes

- Modal exibe prédio atual.
- Custos multiplicados corretamente na UI.
- Preview visual de espaço novo.
- Botão desabilitado se espaço ocupado.
- POST chamado com dados corretos.

## Definição de pronto

- Critério CA1 coberto (UI clara).
- Build sem erros.
- Testes passando.

## Fora de escopo

- Animação durante upgrade.
