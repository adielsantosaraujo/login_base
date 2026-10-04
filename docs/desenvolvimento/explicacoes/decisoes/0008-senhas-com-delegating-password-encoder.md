---
titulo: Senhas com DelegatingPasswordEncoder
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - pom.xml
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - src/main/resources/db/migration/V1__controle_acesso.sql
---

# 0008 — Senhas com DelegatingPasswordEncoder

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

O Spring Security oferece vários algoritmos de hash de senha. É necessário escolher um que seja seguro atualmente, mas que permita transição futura para algoritmos mais modernos sem quebrar senhas existentes gravadas no banco.

## Decisão

Usar `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, que produz hashes com prefixo do algoritmo (ex.: `{bcrypt}...`). Algoritmo padrão é **BCrypt**. A coluna `senha` é `varchar(255)` para acomodar qualquer comprimento de hash futuro.

Na autenticação, o prefixo no banco orienta qual decodificador usar; no encode de nova senha, o algoritmo padrão (BCrypt) é usado automaticamente.

## Alternativas descartadas

- **BCrypt simples sem prefixo** — Rejeitada porque em caso de vulnerabilidade futura ou avanço em criptoanálise, não haveria forma de migrar hashes existentes para algoritmo mais forte sem quebrar logins.
- **Scrypt ou Argon2** — Rejeitada porque não trazem vantagem imediata sobre BCrypt; qualidade é equivalente, e BCrypt é mais testado. Argon2 fica como opção futura.

## Consequências

### Positivas

- **Flexibilidade futura:** trocar para Argon2, PBKDF2 ou outro sem quebrar hashes existentes.
- **Segurança contemporânea:** BCrypt com factor de trabalho padrão oferece proteção adequada contra força bruta.
- **Coluna flexível:** `varchar(255)` acomoda hashes de qualquer tamanho.

### Negativas

- **Tamanho em banco:** hashes com prefixo ocupam alguns bytes adicionais (8 caracteres para o prefixo `{bcrypt}`).
- **Migração futura exigida:** quando mudar o algoritmo, os usuários que não realizarem login continuarão com hash antigo até ressinhar.

