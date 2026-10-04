---
titulo: Visão geral do produto
publico: negocio
tipo: explicacao
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - README.md
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
  - docker-compose.yml
---

# Visão geral do produto

## Contexto

O login-base é uma plataforma reutilizável de autenticação e controle de acesso. Ele fornece as capacidades fundamentais que qualquer aplicação web segura precisa: identificação de usuários, gerenciamento de sessões, auditoria de ações e histórico de acessos.

## Como funciona

A plataforma oferece as seguintes capacidades:

### Acesso e identificação

Os usuários entram fornecendo um identificador (e-mail ou celular) e uma senha. O sistema valida as credenciais e cria uma sessão segura. O e-mail é o identificador único no sistema, mesmo quando o login é feito pelo celular.

### Perfis e permissões

Cada usuário é associado a um ou mais perfis (ex.: Administrador). Cada perfil pode ter permissões específicas. Os perfis podem ter uma data de vigência — um usuário só consegue entrar se tiver pelo menos um perfil ativo na data atual.

### Histórico de acesso

O sistema registra cada login: data/hora, endereço IP do dispositivo e navegador utilizado. Esse histórico fica armazenado e disponível para consulta.

### Auditoria

Toda mudança no banco de dados — criação ou modificação de usuários, perfis, permissões — é registrada automaticamente com quem criou e quem alterou por último, junto com as datas correspondentes.

### Área logada

Após entrar, o usuário acessa uma área protegida da aplicação. Ela é uma interface web moderna, pronta para ser estendida com funcionalidades específicas do negócio.

## Por que é assim

A escolha de tecnologias e padrões está fundamentada em decisões de arquitetura. As principais são:

- **Padrões do Spring Framework** (o framework Java mais usado no mercado) para segurança e persistência
- **PostgreSQL** para confiabilidade e recursos avançados
- **Versionamento do banco com Flyway** para reprodutibilidade e rastreabilidade de mudanças
- **Identificação por e-mail ou celular** para flexibilidade de entrada do usuário

## Limitações e trade-offs

O sistema hoje é uma base. Algumas funcionalidades não estão implementadas:

- **Cadastro de usuários**, perfis e permissões — hoje só existe o administrador inicial
- **Botão de sair** — a sessão expira automaticamente após 30 minutos
- **Recuperação de senha** — o responsável do sistema precisa redefinir via banco
- **Bloqueio por tentativas** — não há proteção contra força bruta
- **Autenticação multifator (MFA)** — só existe a autenticação por senha
- **Limite de sessões simultâneas** — um mesmo usuário pode estar logado em vários navegadores ao mesmo tempo

Essas funcionalidades são candidatas a mudanças futuras e podem ser implementadas conforme o negócio exigir.

## Veja também

- [Limitações e próximos passos](limitacoes-e-proximos-passos.md) — detalhes de o que não está no escopo
- [Regras de acesso](../referencia/regras-de-acesso.md) — regras implementadas
- [Glossário](../referencia/glossario.md) — termos-chave definidos

> **A confirmar com o responsável:**
> - Qual é o produto final que será entregue aos usuários?
> - Qual é o público-alvo? (Há referências a "jogo" em testes e na configuração do ambiente.)
