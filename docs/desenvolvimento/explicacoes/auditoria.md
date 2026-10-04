---
titulo: Auditoria
publico: desenvolvimento
tipo: explicacao
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/auditoria/EntidadeAuditavel.java
  - src/main/java/com/example/loginbase/auditoria/AuditoriaConfig.java
  - src/main/java/com/example/loginbase/auditoria/UsuarioAuditorAware.java
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/resources/db/migration/V2__perfil_admin.sql
  - src/main/java/com/example/loginbase/acesso/SessaoRepository.java
---

# Auditoria

Explicação de como a auditoria funciona: rastreamento de quem criou e alterou cada registro, quando e por quem.

## Contexto

A auditoria responde a três perguntas sobre cada registro:
1. **Quem criou?** E **quando?**
2. **Quem alterou por último?** E **quando?**

Todas as seis tabelas principais (`usuarios`, `perfis`, `permissoes`, `usuario_rel_perfis`, `perfis_rel_permissoes`, `sessoes`) possuem essas informações. Elas são preenchidas automaticamente por **Spring Data JPA Auditing** e rastreadas em quatro colunas:

```sql
criado_em      timestamptz NOT NULL  -- Quando foi criado
criado_por     varchar(150) NOT NULL -- Quem criou (e-mail do usuário ou "sistema")
alterado_em    timestamptz NOT NULL  -- Quando foi alterado por último
alterado_por   varchar(150) NOT NULL -- Quem alterou por último
```

## Como funciona

### 1. Definição das colunas (banco)

Na migração V1, cada tabela inclui:

```sql
CREATE TABLE usuarios (
    ...
    criado_em      timestamptz NOT NULL,
    criado_por     varchar(150) NOT NULL,
    alterado_em    timestamptz NOT NULL,
    alterado_por   varchar(150) NOT NULL,
    ...
);
```

No JPA, a anotação `updatable = false` em `criado_em` e `criado_por` (mapeadas nas entidades) garante que nunca sejam alteradas após a inserção.

### 2. Mapeamento na entidade

A superclasse `EntidadeAuditavel` (em `com.example.loginbase.auditoria`) define os campos no JPA:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeAuditavel {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 150)
    private String criadoPor;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant alteradoEm;

    @LastModifiedBy
    @Column(nullable = false, length = 150)
    private String alteradoPor;
}
```

Todas as entidades (`Usuario`, `Perfil`, `Permissao`, etc.) estendem `EntidadeAuditavel`.

### 3. Configuração (AuditoriaConfig)

A classe `AuditoriaConfig` ativa a auditoria:

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditoriaConfig {
}
```

Ela referencia um bean `auditorAware` que implementa `AuditorAware<String>`.

### 4. Provedor de usuário (UsuarioAuditorAware)

O `UsuarioAuditorAware` lê o contexto de segurança e devolve o usuário atual:

```java
@Component("auditorAware")
public class UsuarioAuditorAware implements AuditorAware<String> {
    public static final String SISTEMA = "sistema";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            return Optional.of(SISTEMA);
        }

        return Optional.of(autenticacao.getName());
    }
}
```

**Usuário autenticado:** o `name` da `Authentication` (sempre o e-mail, mesmo se o login foi por celular).

**Anônimo, sem autenticação ou autenticação anônima:** a string `"sistema"`.

## Fluxo de preenchimento

### Inserção

Quando um novo registro é criado:

1. A aplicação cria a entidade: `new Usuario()`.
2. Define os campos do domínio: `usuario.setNome(...)`, `usuario.setEmail(...)`.
3. Faz `usuarioRepository.save(usuario)`.
4. **JPA Auditing intercepta:**
   - `@CreatedDate` → `criadoEm = Instant.now()`.
   - `@CreatedBy` → `criadoPor = UsuarioAuditorAware.getCurrentAuditor()` (ex.: `"admin@exemplo.com"`).
   - `@LastModifiedDate` → `alteradoEm = Instant.now()` (mesma de `criadoEm` inicialmente).
   - `@LastModifiedBy` → `alteradoPor = UsuarioAuditorAware.getCurrentAuditor()`.

### Atualização

Quando um registro existente é alterado:

1. A aplicação carrega a entidade: `usuario = usuarioRepository.findById(...)`.
2. Altera um campo: `usuario.setNome(novoNome)`.
3. Faz `usuarioRepository.save(usuario)`.
4. **JPA Auditing intercepta:**
   - `criadoEm`, `criadoPor` → **não mudam** (`updatable = false`).
   - `@LastModifiedDate` → `alteradoEm = Instant.now()`.
   - `@LastModifiedBy` → `alteradoPor = UsuarioAuditorAware.getCurrentAuditor()`.

### Seed SQL (migrações)

A migração V2 insere dados manualmente. Para preencher as colunas de auditoria, ela usa SQL explícito:

```sql
-- V2__perfil_admin.sql
INSERT INTO perfis (nome, descricao, criado_em, criado_por, alterado_em, alterado_por)
VALUES ('ADMIN', 'Administrador do sistema', now(), 'sistema', now(), 'sistema');
```

**Nota:** `now()` no Postgres retorna `CURRENT_TIMESTAMP` (tipo `timestamptz`).

## Operações em lote (bypass)

Algumas operações em lote **não** passam pelo JPA Auditing porque usam SQL direto. Exemplo em `SessaoRepository`:

```java
@Modifying(clearAutomatically = true)
@Query("update Sessao s set s.dataFim = :agora, s.alteradoEm = :agora, s.alteradoPor = :por where s.dataFim is null")
int fecharTodasAbertas(@Param("agora") Instant agora, @Param("por") String por);
```

Aqui, os campos são **preenchidos manualmente** na query (ex.: `SessaoService.fecharTodasAbertas()` chama com `UsuarioAuditorAware.SISTEMA`). Sem isso, o JPA Auditing não seria disparado (queries `@Modifying` não passam por listeners).

## Exemplo de ciclo completo

1. **Admin cria um usuário** (via API ou tela, a ser implementada):
   ```java
   Usuario u = new Usuario();
   u.setNome("Maria");
   u.setEmail("maria@exemplo.com");
   usuarioRepository.save(u);
   ```
   
   Resultado no banco:
   ```sql
   criado_em = 2026-10-04 10:30:00 (instant.now())
   criado_por = "admin@exemplo.com" (email do usuário logado)
   alterado_em = 2026-10-04 10:30:00 (mesmo de criado_em)
   alterado_por = "admin@exemplo.com"
   ```

2. **Outro admin altera o nome** (3 dias depois):
   ```java
   Usuario u = usuarioRepository.findByEmail("maria@exemplo.com").get();
   u.setNome("Maria Silva");
   usuarioRepository.save(u);
   ```
   
   Resultado (apenas colunas de auditoria):
   ```sql
   criado_em = 2026-10-04 10:30:00 (não muda)
   criado_por = "admin@exemplo.com" (não muda)
   alterado_em = 2026-10-07 14:45:00 (novo instant.now())
   alterado_por = "outro@exemplo.com" (email do usuário logado agora)
   ```

## Limitações

- **Auditoria não persiste histórico completo:** Apenas o último changeador é registrado. Para versionar todas as alterações, seria necessário um padrão como `@Audited` do Hibernate Envers (fora do escopo).
- **Tipos:** `criado_em` e `alterado_em` são `Instant` (UTC timezone-aware); `criado_por` e `alterado_por` são strings (e-mail ou `"sistema"`).
- **Sem retenção configurada:** A aplicação não apaga registros antigos (LGPD: "a confirmar").

## Veja também

- [Modelo de dados](../referencia/modelo-de-dados.md)
- [ADR 0006 — Auditoria com Spring Data JPA Auditing](./decisoes/0006-auditoria-com-spring-data-jpa-auditing.md)
