---
titulo: Glossário
publico: negocio
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/acesso/Usuario.java
  - src/main/java/com/example/loginbase/acesso/Perfil.java
  - src/main/java/com/example/loginbase/acesso/Permissao.java
  - src/main/java/com/example/loginbase/auditoria/UsuarioAuditorAware.java
---

# Glossário

Termos-chave usados no sistema de autenticação e controle de acesso.

| Termo | Definição |
|---|---|
| **Usuário** | Pessoa ou sistema que se autentica no aplicativo. Cada usuário tem um e-mail único, uma senha e opcionalmente um celular. |
| **Administrador inicial** | Primeiro usuário criado automaticamente durante a inicialização, com base nas variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_PASSWORD`. Tem o perfil ADMIN. |
| **Perfil** | Conjunto de permissões agrupadas por função ou papel. Exemplo implementado: Administrador. Exemplos hipotéticos: Editor, Leitor. Um usuário pode ter vários perfis, cada um com uma data de vigência. |
| **Permissão** | Autorização para executar uma ação específica no sistema. Exemplos: "ler_relatorios", "criar_usuario". As permissões são associadas aos perfis, não diretamente aos usuários. |
| **Vigência** | Período de validade de um perfil para um usuário, definido por uma data de início e opcionalmente uma data de fim. Um perfil só permite login se a data de hoje estiver dentro da vigência. |
| **Sessão** | Período durante o qual um usuário está autenticado e navegando o sistema. Uma sessão dura até 30 minutos sem atividade, após o qual o usuário é desconectado automaticamente. |
| **Token de sessão** | Hash SHA-256 do ID da sessão HTTP, armazenado na tabela de sessões para histórico e análise. Não pode ser usado para sequestrar a sessão porque é um hash irreversível. |
| **Auditoria** | Registro automático de quem criou ou alterou cada informação no sistema, junto com a data e hora. Garante rastreabilidade de todas as mudanças. |
| **SPA** | Single Page Application — a área logada do sistema é uma interface web moderna que não recarrega a página inteira a cada clique. |
| **Área logada** | Parte do aplicativo restrita a usuários autenticados. Começa após o usuário fazer login. |
| **Sistema** | Autor automático de ações que não foram iniciadas por um usuário (ex.: encerramento automático de sessões por timeout, ou criação do admin inicial). |

## Veja também

- [Visão geral do produto](../explicacoes/visao-geral-do-produto.md) — contexto geral
- [Regras de acesso](regras-de-acesso.md) — como as regras são implementadas
- [Limitações e próximos passos](../explicacoes/limitacoes-e-proximos-passos.md) — o que não está implementado
