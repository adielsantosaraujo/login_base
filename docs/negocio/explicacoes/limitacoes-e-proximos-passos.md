---
titulo: Limitações e próximos passos
publico: negocio
tipo: explicacao
atualizado_em: 2026-10-04
fontes:
  - frontend/seguro/patrimonio/src/views/HomeView.vue
---

# Limitações e próximos passos

## Contexto

O login-base foi concebido como uma base sólida e segura de autenticação. Intencionalmente, algumas funcionalidades foram deixadas de fora do escopo inicial para manter o foco em estabilidade e clareza. Este documento lista o que não está implementado, por que foi deixado de lado, e quais são os candidatos a mudanças futuras.

## Fora do escopo atual

As seguintes funcionalidades **não foram implementadas** nesta versão:

### Cadastro de usuários, perfis e permissões

Hoje, novos usuários só podem ser adicionados manualmente pelo responsável técnico, inserindo os dados diretamente no banco. Não há telas ou APIs para:
- Cadastrar um novo usuário
- Criar perfis e permissões
- Atribuir ou revogar perfis para usuários

> **A confirmar com o responsável:** quando será implementado o cadastro de usuários? Quem poderá cadastrar (administrador? qualquer usuário para si mesmo)?

### Botão "Sair" na interface

Não há um botão de logout na tela. A sessão é encerrada automaticamente após 30 minutos sem atividade, e o usuário pode fechar o navegador ou a aba.

> **A confirmar com o responsável:** será adicionado um botão de sair? Onde (menu, canto superior direito)?

### Recuperação de senha

Se um usuário esquecer a senha, o responsável técnico precisa redefinir manualmente no banco. Não há email de recuperação automática nem link de reset.

> **A confirmar com o responsável:** haverá suporte a recuperação de senha? Será por e-mail, SMS ou outro meio?

### Bloqueio por tentativas de login

Um atacante pode tentar adivinhar credenciais indefinidamente sem que o sistema bloqueie o acesso. A única proteção é a mensagem genérica que não revela se o usuário existe.

**Impacto:** risco de força bruta. **Risco mitigado parcialmente por:** mensagem genérica (não enumera usuários).

### Autenticação multifator (MFA)

O sistema usa apenas e-mail/celular + senha. Não há suporte a:
- Autenticadores de tempo (Google Authenticator, Authy)
- SMS/WhatsApp com código
- Biometria

### Limite de sessões simultâneas

Um mesmo usuário pode estar logado em múltiplos navegadores, computadores e dispositivos ao mesmo tempo. Não há limite configurável nem aviso ao criar uma nova sessão.

### Autorização fina por permissão

O sistema carrega as permissões do usuário durante o login, mas as rotas não verificam permissões individuais. Toda rota protegida exige apenas autenticação ("estar logado"). As permissões estão disponíveis no código para uso futuro.

### Autenticação de API por token

A API do sistema (endpoints `/api/**`) herda a autenticação por sessão HTTP (cookie). Não há suporte a tokens (JWT, OAuth2) para ferramentas e integrações.

## Trade-offs conhecidos

### Uma única instância

O sistema foi projetado para rodar em uma única instância da aplicação. O histórico de sessões é registrado no banco quando o login acontece (preenchido após login bem-sucedido), e o horário de encerramento é preenchido quando a sessão termina ou expira. Se houver múltiplas instâncias, cada uma mantém suas sessões em memória, e as instâncias não sincronizam — gerando comportamento inesperado quando uma mesma sessão tenta migrar entre elas.

**Alternativa descartada:** Spring Session JDBC, que externalizaria o armazenamento de sessões. Rejeitada porque impõe um esquema de banco próprio (`SPRING_SESSION`) incompatível com o modelo de auditoria do projeto, que exigiria manter dois sistemas de registro de sessões simultâneos.

### IP do usuário sem proxy confiável

O sistema registra o IP de cada login. Se a aplicação rodar atrás de um proxy ou load balancer, o IP gravado será o do proxy, não o do usuário final. Isso reduz a utilidade do histórico de sessões para detecção de anomalias.

**Mitigação:** o código permite habilitar leitura de `X-Forwarded-For` quando há proxy confiável, mas isso não está ativado por padrão em desenvolvimento.

### IP e navegador para análise, não para autenticação

O IP e User-Agent registrados são informativos. Não há verificação de dispositivos (ex.: "desconheço este navegador, confirme") nem bloqueio de IPs suspeitos.

## Roadmap

> **A confirmar com o responsável:** qual é a prioridade das funcionalidades abaixo?

Candidatos a próximas mudanças (ordem sugerida):

1. **Telas de cadastro de usuário** — permitir que administrador crie novos usuários e atribua perfis
2. **Botão de sair** — adicionar logout explícito na interface
3. **Recuperação de senha** — reset por e-mail ou código SMS
4. **Bloqueio por tentativas** — contar falhas e bloquear após N tentativas por um período
5. **Autenticação multifator (MFA)** — suporte a TOTP (Google Authenticator)
6. **Limite de sessões** — permitir no máximo N sessões simultâneas por usuário
7. **Autorização por permissão** — proteger rotas com `@PreAuthorize("hasAuthority(...)")`

## Veja também

- [Visão geral do produto](visao-geral-do-produto.md) — contexto geral e capacidades presentes
- [Regras de acesso](../referencia/regras-de-acesso.md) — regras implementadas hoje
- [Glossário](../referencia/glossario.md) — termos-chave definidos
