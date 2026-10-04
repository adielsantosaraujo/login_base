---
titulo: Auditoria com Spring Data JPA Auditing
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/auditoria/EntidadeAuditavel.java
  - src/main/java/com/example/loginbase/auditoria/AuditoriaConfig.java
  - src/main/java/com/example/loginbase/auditoria/UsuarioAuditorAware.java
  - src/main/resources/db/migration
---

# 0006 — Auditoria com Spring Data JPA Auditing

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

Toda entidade precisa registrar quando foi criada, quem criou, quando foi alterada e quem alterou. É necessário escolher: implementar auditoria manualmente em cada entidade ou usar o mecanismo de Spring Data JPA Auditing.

## Decisão

- **Spring Data JPA Auditing** via `@EnableJpaAuditing` + `@EntityListeners(AuditingEntityListener.class)` na superclasse `EntidadeAuditavel`.
- Campos: `@CreatedDate @CreatedBy` (não atualizável) e `@LastModifiedDate @LastModifiedBy` no banco como `criado_em`, `criado_por`, `alterado_em`, `alterado_por`.
- **Autor como texto** (e-mail ou string): implementar `AuditorAware<String>` que lê o `SecurityContextHolder` e devolve o nome do usuário autenticado (e-mail) ou a string `"sistema"` quando anônimo/ausente.
- **Exceção:** updates em lote (ex.: `fecharTodasAbertas` em `SessaoRepository`) preenchem `data_fim`, `alterado_em` e `alterado_por` manualmente no JPQL.
- **Configuração fora da classe principal:** `AuditoriaConfig` isola a anotação `@EnableJpaAuditing` para evitar problemas com testes de fatia (`@WebMvcTest`) que carregam a configuração parcialmente.

## Alternativas descartadas

- **`criado_por` e `alterado_por` como FK para `usuarios.id`** — Rejeitada porque:
  - Cria dependência circular: usuários auditados por usuários.
  - Complica o seed SQL (precisaria de usuários antes de registrar transações do sistema).
  - Impede registrar ações de processos do sistema (runners, imports) quando não há usuário.

## Consequências

### Positivas

- **Automático:** a auditoria é preenchida sem código explícito em cada entidade.
- **Consistente:** o Spring garante que todos os campos sejam preenchidos no insert/update.
- **Texto simples:** guardar o e-mail ou `"sistema"` é simples e suficiente para rastreamento.
- **Sem foreign key:** não há constraint quebrada quando um usuário é deletado (raro, mas possível em testes).

### Negativas

- **Menos rastreável:** guardar só o e-mail significa que se o usuário for deletado, o histórico fica órfão. Mitigado pela política de não deletar usuários em produção (usar soft delete se necessário no futuro).
- **Isolamento em `AuditoriaConfig`:** `@EnableJpaAuditing` fica em classe separada para que `@WebMvcTest` e outros testes de fatia não carreguem a configuração de auditoria acidentalmente.
- **Updates em lote:** não são interceptados pelo listener de auditoria. Necessário preencher manualmente (documentado).

