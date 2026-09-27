# 0011 — Senhas com DelegatingPasswordEncoder (BCrypt)

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

Senhas precisam ser hasheadas, não armazenadas em texto plano. Spring Security oferece multiple opções: BCrypt (padrão seguro), PBKDF2, Scrypt, Argon2. O `DelegatingPasswordEncoder` permite migração gradual: armazena o nome do algoritmo no hash (`{bcrypt}...`, `{argon2}...`) e reconhece qualquer um na validação, permitindo trocar o padrão sem invalidar senhas antigas.

## Direcionadores da decisão

- Segurança: BCrypt é padrão consolidado, adaptável (custo parametrizável)
- Flexibilidade: `DelegatingPasswordEncoder` permite evolução (Argon2, Scrypt) sem quebrar senhas existentes
- Simplicidade: uma linha de config
- Compatibilidade: Spring Security 7 traz `PasswordEncoderFactories.createDelegatingPasswordEncoder()`

## Opções consideradas

| Opção | Descrição |
|---|---|
| **DelegatingPasswordEncoder (BCrypt padrão)** | Bean `PasswordEncoder` via `PasswordEncoderFactories.createDelegatingPasswordEncoder()`. Hash com prefixo `{bcrypt}`. |
| BCrypt explícito (sem delegação) | `new BCryptPasswordEncoder()` diretamente. Rejeitada: sem flexibilidade futura. |
| MD5 ou SHA | Criptográficamente quebrados. Rejeitados imediatamente. |
| Sem hash (texto plano) | Rejeitado: impossível de aceitar. |

## Resultado da decisão

Adotou-se **`DelegatingPasswordEncoder`**:

1. **Bean** (em `seguranca.SecurityConfig`):
   ```java
   @Bean
   public PasswordEncoder passwordEncoder() {
       return PasswordEncoderFactories.createDelegatingPasswordEncoder();
   }
   ```

2. **Coluna no banco** (`senha varchar(255)`):
   - Armazena hash com prefixo: `{bcrypt}$2a$10$...`
   - Futura evolução para `{argon2}...` será possível

3. **Autenticação**: `DaoAuthenticationProvider` (padrão) reconhece o prefixo e aplica o encoder correspondente

### Consequências positivas
- **Seguro**: BCrypt com salt aleatório, adaptável (custo aumenta com poder computacional)
- **Flexível**: trocar para Argon2 sem invalidar senhas antigas
- **Simples**: uma linha de código
- **Padrão**: amplamente usado, bem auditado

### Consequências negativas
- **Nenhuma mitigação de força bruta**: rate limiting não está incluído (Non-Goal)
- **Custo de hash**: BCrypt é lento propositalmente (mitigado: bom tradeoff segurança/performance)

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| DelegatingPasswordEncoder | Flexível; seguro; simples; padronizado. | Nenhum. |
| BCrypt direto | Simples. | Sem flexibilidade futura. |
| Senha + salt armazenados | Controle total. | Complexo; erro propenso. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 6](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Código**: [`seguranca/SecurityConfig.java`](../../src/main/java/com/example/loginbase/seguranca/SecurityConfig.java) — bean `passwordEncoder()`
- **Teste**: [`seguranca/UsuarioDetailsServiceTest.java`](../../src/test/java/com/example/loginbase/seguranca/UsuarioDetailsServiceTest.java) — senha hasheada

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
