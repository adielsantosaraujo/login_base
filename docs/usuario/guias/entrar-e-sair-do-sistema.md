---
titulo: Entrar e sair do sistema
publico: usuario
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/templates/sistema/public/login.html
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - src/main/resources/application.properties
  - src/main/java/com/example/loginbase/web/PaginaController.java
---

# Entrar e sair do sistema

## Quando usar

Consulte este guia quando precisar fazer login, quando receber uma mensagem de erro ao entrar, ou quando precisar encerrar sua sessão.

## Pré-requisitos

- Você deve ter recebido um e-mail ou celular cadastrado no sistema
- Você deve ter uma senha válida
- A página de login deve estar acessível no navegador

## Passos

### Para entrar no sistema

1. Abra o navegador no endereço fornecido (ex.: `http://localhost:8080`).
   Você será redirecionado automaticamente para a página de login.

2. No campo "E-mail ou celular", digite seu identificador (veja os formatos em [Formatos de login aceitos](../referencia/mensagens-e-formatos-de-login.md)).

3. No campo "Senha", digite sua senha.

4. Clique em "Entrar".

5. Se o login foi bem-sucedido, você será redirecionado automaticamente para a tela principal do sistema (rota `/patrimonio/index`).

### Para sair do sistema

A sessão do sistema tem duração de 30 minutos sem atividade. Você pode:

- **Clicar em "Sair":** se houver botão de sair disponível na interface
- **Fechar o navegador ou a aba:** o cookie da sessão será removido do navegador, mas a sessão no servidor continuará ativa até expirar por inatividade
- **Aguardar a expiração:** após 30 minutos sem usar o sistema, sua sessão expirará automaticamente

> **A confirmar com o responsável:** haverá um botão "Sair" na interface?

Se a sessão expirar, você será redirecionado para a página de login.

## Como verificar

**Você entrou corretamente quando:**
- Você vê a tela principal do sistema
- O endereço na barra do navegador é `http://localhost:8080/patrimonio/index` (ou similar, conforme o app em uso)

**Você saiu corretamente quando:**
- Você é redirecionado para a página de login
- Você vê a mensagem "Você saiu do sistema." em azul (se acabou de sair manualmente)
- Você é levado para login automaticamente após 30 minutos sem usar o sistema (sem a mensagem azul)

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| "Usuário ou senha inválidos." | E-mail/celular ou senha incorretos | Verifique a ortografia, maiúsculas e espaços. Consulte [Formatos de login](../referencia/mensagens-e-formatos-de-login.md). |
| "Usuário ou senha inválidos." | Conta desabilitada | Contate o responsável do sistema. |
| Ficou na tela de login | Sessão expirou após 30 minutos | Faça login novamente. |

## Veja também

- [Fazer seu primeiro acesso](../tutoriais/primeiro-acesso.md) — guia passo a passo para o primeiro login
- [Formatos de login aceitos](../referencia/mensagens-e-formatos-de-login.md) — detalhes sobre e-mail e celular
