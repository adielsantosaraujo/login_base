---
titulo: Mensagens e formatos de login
publico: usuario
tipo: referencia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/templates/sistema/public/login.html
  - src/main/java/com/example/loginbase/seguranca/IdentificadorLogin.java
  - src/main/java/com/example/loginbase/acesso/NormalizacaoContato.java
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
---

# Mensagens e formatos de login

Esta página lista as mensagens que você pode ver na página de login e os formatos aceitos para e-mail e celular.

## Mensagens

| Mensagem | Quando aparece | O que fazer |
|---|---|---|
| "Usuário ou senha inválidos." | Você digitou um e-mail/celular ou senha incorretos, ou sua conta não tem um perfil ativo. | Verifique se digitou corretamente (veja os formatos aceitos abaixo). Contate o responsável se o e-mail/celular ou senha estão certos mas a mensagem persiste. |
| "Você saiu do sistema." | Você acabou de clicar em sair (se houver botão). | Faça login novamente. |
| Nenhuma mensagem | Você acabou de entrar na página de login. | Preencha seus dados e clique em "Entrar". |

## Formatos aceitos

### E-mail

**Formato:** qualquer texto com um `@` no meio.

**Regras:**
- A diferença entre maiúsculas e minúsculas **não importa** (ex.: `Ana@Empresa.com` e `ana@empresa.com` são tratados como o mesmo)
- Espaços no início ou final são ignorados (ex.: ` ana@empresa.com ` é aceitável)
- Não há validação adicional de formato (ex.: `usuario@` é aceito)

**Exemplos aceitos:**
- `ana@empresa.com`
- `JOAO@EMPRESA.COM`
- ` maria@empresa.com ` (com espaços nas pontas)

**Exemplos rejeitados:**
- `ana @empresa.com` (espaço no meio)
- E-mail que não foi cadastrado no sistema

### Celular

**Formato:** DDD (2 dígitos) + 9 dígitos, totalizando 11 dígitos.

**Regras:**
- O sistema aceita qualquer combinação de caracteres, desde que contenha exatamente 11 dígitos
- Caracteres não-dígitos (parênteses, hífen, espaços, pontos, etc.) são automaticamente removidos
- Após a remoção, devem restar **exatamente 11 dígitos**
- 10 dígitos ou 12 dígitos são rejeitados

**Exemplos aceitos:**
- `11987654321` (sem máscara)
- `(11) 98765-4321` (com máscara padrão)
- `(11)98765-4321` (sem espaço dentro)
- `11 98765-4321` (com espaço)
- `11.98765.4321` (com pontos)
- `11 9 8765-4321` (múltiplos espaços)

**Exemplos rejeitados:**
- `1198765432` (10 dígitos — falta um)
- `119876543210` (12 dígitos — um a mais)
- `+55 11 98765-4321` (inclui o código do país — 13 dígitos)
- Celular que não foi cadastrado no sistema

## Veja também

- [Entrar e sair do sistema](../guias/entrar-e-sair-do-sistema.md) — como fazer login e logout
- [Fazer seu primeiro acesso](../tutoriais/primeiro-acesso.md) — passo a passo para o primeiro login
