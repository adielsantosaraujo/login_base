# 0009 — Spring Data JPA Auditing (`@CreatedBy`, `@CreatedDate`)

| Campo | Valor |
|---|---|
| Status | Aceita |
| Data | 2026-09-24 |
| Decisores | Adiel (autor) com apoio de agentes Claude |
| Change de origem | [`add-user-authentication`](../../openspec/changes/archive/2026-09-25-add-user-authentication/) |

## Contexto e problema

Toda entidade precisa de auditoria: quem criou/modificou, quando. Há três estratégias: (1) campos manuais em cada entidade, (2) trigger SQL no banco, (3) framework de auditoria (Hibernate Envers, Spring Data JPA Auditing). Auditoria é transversal e deve ser baixa fricção. Valores precisam vir de contexto de segurança (usuário autenticado) ou "sistema" (processos automáticos).

## Direcionadores da decisão

- Auditoria automática: não exigir `usuario.setCriadoPor(email)` em cada save
- Rastreabilidade: `criado_em/por`, `alterado_em/por` em **todas** as tabelas
- Tipos de usuário: email (usuário real) ou "sistema" (runner, evento)
- Baixa fricção: superclasse JPA Auditable com anotações
- Campos não atualizáveis: `criado_em/por` não devem mudar

## Opções consideradas

| Opção | Descrição |
|---|---|
| **Spring Data JPA Auditing** | `@EnableJpaAuditing` + `AuditingEntityListener` em superclasse + `AuditorAware<String>`. Automático no persist/update. |
| `criado_por` como FK | `criado_por_usuario_id` referencia a tabela de usuários. Rejeitada: cria dependência circular (usuários auditam usuários), complica seed. |
| Sem auditoria automática | Campos manuais em cada entity. Rejeitada: alto custo de manutenção, esquecimentos. |
| Hibernate Envers | Alternativa potente (tables + revisions). Rejeitada: overhead adicional para necessidade simples. |

## Resultado da decisão

Adotou-se **Spring Data JPA Auditing**:

1. **Dependência**: `spring-boot-starter-data-jpa` (já no pom)

2. **Superclasse** (`auditoria.EntidadeAuditavel`):
   ```java
   @MappedSuperclass
   @EntityListeners(AuditingEntityListener.class)
   public class EntidadeAuditavel {
       @CreatedDate
       @Column(nullable = false, updatable = false)
       private OffsetDateTime criadoEm;
       
       @CreatedBy
       @Column(nullable = false, updatable = false, length = 150)
       private String criadoPor;
       
       @LastModifiedDate
       @Column(nullable = false)
       private OffsetDateTime alteradoEm;
       
       @LastModifiedBy
       @Column(nullable = false, length = 150)
       private String alteradoPor;
   }
   ```

3. **AuditorAware** (`auditoria.AuditorAware`):
   ```java
   @Component
   public class UsuarioAuditorAware implements AuditorAware<String> {
       @Override
       public Optional<String> getCurrentAuditor() {
           Authentication auth = SecurityContextHolder.getContext().getAuthentication();
           if (auth != null && auth.isAuthenticated()) {
               return Optional.of(auth.getName()); // e-mail
           }
           return Optional.of("sistema"); // process, runner
       }
   }
   ```

4. **Configuração**:
   - `@EnableJpaAuditing` no `@SpringBootApplication`
   - `@Column(nullable = false)` nas colunas de auditoria no SQL (V1, V2, V3)

### Consequências positivas
- **Automático**: nada de manual em saves
- **Consistente**: mesmo padrão em todas as tabelas
- **Rastreável**: e-mail do criador/modificador vem de `SecurityContext`
- **Simples**: não requer Envers ou triggers SQL
- **Testável**: `AuditorAware` pode ser mockado

### Consequências negativas
- **Timestamp do servidor**: usa `OffsetDateTime` (fuso configurable via `server.servlet.session.user-timezone`)
- **Sem histórico granular**: só último criador/modificador (não todas as mudanças)
- **Campo estático**: `criado_por` é string (não FK), mais flexível mas menos tipado

## Prós e contras das opções

| Opção | Prós | Contras |
|---|---|---|
| Spring Data JPA Auditing | Automático; simples; com `AuditorAware` flexível; testável. | Sem histórico granular; campo é string (não FK). |
| `criado_por` como FK | Tipado; referência a usuário. | Dependência circular; complica seed; difícil para "sistema". |
| Hibernate Envers | Histórico completo de cada mudança. | Overhead; tabelas extras; não pedido. |

## Mais informações

- **Design**: [`add-user-authentication/design.md` §Decisions 3](../../openspec/changes/archive/2026-09-25-add-user-authentication/design.md)
- **Código**: [`auditoria/EntidadeAuditavel.java`](../../src/main/java/com/example/loginbase/auditoria/EntidadeAuditavel.java)
- **AuditorAware**: [`auditoria/UsuarioAuditorAware.java`](../../src/main/java/com/example/loginbase/auditoria/UsuarioAuditorAware.java)
- **Migrações**: [`src/main/resources/db/migration/V1__controle_acesso.sql`](../../src/main/resources/db/migration/V1__controle_acesso.sql) — colunas não null

## Histórico de revisões

| Versão | Data | Descrição | Autor |
|---|---|---|---|
| 1.0.0 | 2026-09-27 | Versão inicial | Adiel, com apoio de agentes Claude |
