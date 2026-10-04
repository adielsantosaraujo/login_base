---
titulo: Fazer seu primeiro acesso
publico: usuario
tipo: tutorial
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/templates/sistema/public/login.html
  - frontend/src/views/HomeView.vue
---

# Fazer seu primeiro acesso

Neste tutorial você vai abrir o sistema pela primeira vez e entrar com suas credenciais. Ao final, você terá acesso à tela de boas-vindas.

## O que você precisa

- Um navegador web (Chrome, Firefox, Edge ou similar)
- Um endereço de acesso fornecido pelo responsável (ex.: `http://localhost:8080`)
- Suas credenciais: e-mail ou número de celular e uma senha

> **A confirmar com o responsável:** como novos usuários são cadastrados no sistema.

## Passo 1 — Abrir o navegador e ir ao endereço

Abra seu navegador favorito e digite o endereço que recebeu. Você será levado automaticamente para a página de login se não estiver autenticado.

Você deve ver:

```
Uma tela com o título "Login"
Um campo com rótulo "E-mail ou celular"
Um campo com rótulo "Senha"
Um botão azul escrito "Entrar"
```

## Passo 2 — Preencher o e-mail ou celular

No campo "E-mail ou celular", digite seu identificador:

- **Se for e-mail:** digite exatamente como foi cadastrado (sem diferença entre maiúsculas e minúsculas)
  - Exemplos: `ana@empresa.com`, `JOAO@EMPRESA.COM` (são aceitáveis)
- **Se for celular:** digite seu DDD + 9 dígitos, com ou sem máscara
  - Exemplos: `11987654321`, `(11) 98765-4321` (ambos funcionam)

## Passo 3 — Preencher a senha

No campo "Senha", digite a senha que recebeu.

## Passo 4 — Clicar em "Entrar"

Clique no botão "Entrar".

Você deve ver:

```
Uma tela com o card "Seja bem-vindo"
Você agora está dentro da área logada do sistema
```

## Resultado

Você fez login com sucesso e pode navegar pela aplicação. Caso você veja a mensagem "Usuário ou senha inválidos", verifique se digitou seu identificador e senha corretamente e tente novamente.

## Próximos passos

- [Entrar e sair do sistema](../guias/entrar-e-sair-do-sistema.md) — saiba como gerenciar sua sessão
- [Formatos de login aceitos](../referencia/mensagens-e-formatos-de-login.md) — aprenda os formatos exatos
