---
titulo: Regras de acesso
publico: negocio
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/resources/db/migration/V2__perfil_admin.sql
  - src/main/java/com/example/loginbase/seguranca/UsuarioDetailsService.java
  - src/main/java/com/example/loginbase/seguranca/SessaoService.java
  - src/main/resources/application.properties
---

# Regras de acesso

Este documento descreve as regras de controle de acesso implementadas no sistema.

## Identificação

| Regra | Descrição |
|---|---|
| **E-mail único** | Cada usuário tem um e-mail único no sistema. O e-mail não diferencia maiúsculas de minúsculas. |
| **Celular único (opcional)** | Um usuário pode ter um celular cadastrado, mas é opcional. Se cadastrado, é único no sistema. O celular tem exatamente 11 dígitos (DDD + 9 dígitos) e é normalizado (apenas dígitos armazenados). |
| **Login por e-mail ou celular** | Nos dois casos, o sistema busca o usuário e valida a senha. O username registrado na sessão é sempre o e-mail, mesmo quando o login foi pelo celular. |
| **Mensagem genérica** | Se o e-mail/celular não existe ou a senha está errada, a mensagem é sempre "Usuário ou senha inválidos." (não diferencia os dois casos). Isso evita que atacantes enumerem usuários válidos. |

## Perfis e vigência

| Regra | Descrição |
|---|---|
| **Perfil necessário para login** | Um usuário só consegue entrar se tiver pelo menos um perfil com vigência ativa na data de hoje. Se a data de vigência do perfil já passou ou ainda não começou, o usuário não consegue entrar (recebe "Usuário ou senha inválidos."). |
| **Vigência controlada por datas** | Um perfil é considerado vigente quando a data de início é menor ou igual a hoje **e** a data de fim é nula (sem fim) **ou** maior ou igual a hoje. |
| **Perfil ADMIN pré-cadastrado** | O perfil "ADMIN" é criado automaticamente durante a inicialização do banco. O administrador inicial (usuário ADMIN) é criado na primeira execução com as variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_PASSWORD`. |
| **Permissões associadas a perfis** | As permissões são associadas aos perfis, não diretamente aos usuários. Se um usuário tem um perfil com permissão X, ele herda essa permissão. |

## Sessões e acessos

| Regra | Descrição |
|---|---|
| **Duração da sessão** | Uma sessão dura até 30 minutos sem atividade. Após 30 minutos sem requisição, o usuário é desconectado automaticamente. |
| **Registro de sessão** | Cada login bem-sucedido gera um registro na tabela `sessoes` com: data/hora de início, IP do cliente (sem tratamento de proxy), navegador/User-Agent (truncado em 500 caracteres), e um token (hash SHA-256 do ID da sessão). |
| **Encerramento de sessão** | A sessão é encerrada quando: o usuário faz logout (se houver botão) ou a sessão expira por inatividade. O horário de encerramento é registrado no banco apenas nestes casos. Fechar o navegador remove o cookie, mas a sessão no servidor continua ativa até expirar. |
| **Trocas de ID de sessão** | Após login bem-sucedido, o ID da sessão é trocado automaticamente. Isso evita ataque de fixação de sessão. |

## Auditoria

| Regra | Descrição |
|---|---|
| **Quem criou, quem alterou** | Toda mudança no sistema registra automaticamente quem fez (por e-mail) e quando. Se a mudança foi automática (ex.: encerramento de sessão), o autor é "sistema". |
| **Registra todas as tabelas** | Usuários, perfis, permissões, histórico de sessões — tudo tem `criado_em`, `criado_por`, `alterado_em`, `alterado_por`. |

## Dados pessoais

| Dados armazenados | Onde | Finalidade |
|---|---|---|
| Nome | Tabela `usuarios` | Identificação do usuário no sistema |
| E-mail | Tabela `usuarios` | Identificador único; login; auditoria |
| Celular | Tabela `usuarios` (opcional) | Login alternativo (opcional) |
| Senha (hash) | Tabela `usuarios` | Autenticação |
| IP do login | Tabela `sessoes` | Histórico de acessos; análise de anomalias |
| Navegador (User-Agent) | Tabela `sessoes` | Histórico de acessos; análise de anomalias |
| Data/hora de login | Tabela `sessoes` | Histórico de acessos; auditoria |

> **A confirmar com o responsável:**
> - Qual é a política de retenção de dados pessoais? Quanto tempo os registros de sessão são mantidos?
> - Há requisitos de conformidade com LGPD ou outro regulamento?
> - O que acontece quando um usuário pede para deletar seus dados?

## Veja também

- [Visão geral do produto](../explicacoes/visao-geral-do-produto.md) — capacidades e contexto
- [Limitações e próximos passos](../explicacoes/limitacoes-e-proximos-passos.md) — o que não está implementado
- [Glossário](glossario.md) — termos-chave definidos
