---
titulo: Criar uma migração Flyway
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/resources/db/migration/V2__perfil_admin.sql
  - src/main/java/com/example/loginbase/auditoria/EntidadeAuditavel.java
  - src/main/resources/application.properties
---

# Criar uma migração Flyway

## Quando usar

Use este guia quando precisar adicionar ou modificar tabelas, colunas ou índices no banco de dados PostgreSQL.

## Pré-requisitos

- Projeto clonado (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Familiarity com SQL e conceitos de banco relacional.
- `src/main/resources/db/migration/` acessível.

## Passos

1. **Identifique o número da próxima migração.**

   Verifique a pasta `src/main/resources/db/migration/`:

   ```bash
   ls -1 src/main/resources/db/migration/ | sort -V
   ```

   Você verá:

   ```
   V1__controle_acesso.sql
   V2__perfil_admin.sql
   ```

   A próxima é `V3__sua_descricao.sql`.

2. **Crie o arquivo com a convenção de nome.**

   Os arquivos devem seguir o padrão `V<N>__descricao.sql`, onde:

   - `V` é literal.
   - `<N>` é um inteiro sequencial (sem gaps).
   - `__` (dois underscores) separa o número da descrição.
   - `descricao` em snake_case sem acentos, descrevendo a mudança.

   Exemplos:

   ```
   V3__adicionar_tabela_usuarios_perfis.sql
   V4__adicionar_coluna_ativo.sql
   V5__criar_indice_email.sql
   ```

   Crie o arquivo:

   ```bash
   touch src/main/resources/db/migration/V3__sua_descricao.sql
   ```

3. **Defina tabelas com as convenções do projeto.**

   Toda tabela deve seguir:

   | Aspecto | Regra |
   |---|---|
   | **Nome** | Plural: `usuarios`, `perfis`, `permissoes`. |
   | **PK** | `id bigint generated always as identity` + `constraint pk_<tabela> primary key (id)` — nunca manual ou UUID. |
   | **Colunas de auditoria** | Se estender `EntidadeAuditavel`: `criado_em timestamptz not null`, `criado_por varchar(150) not null`, `alterado_em timestamptz not null`, `alterado_por varchar(150) not null`. **Não use `default`:** a aplicação preenche via JPA Auditing. |
   | **ForeignKey** | Colunas: `<tabela>_id` (ex.: `usuario_id`, `perfil_id`). Constraint: `constraint fk_<tabela-origem>_<tabela-destino> foreign key (<coluna>) references <tabela> (<coluna>)` (ex.: `fk_usuario_rel_perfis_usuario`). Índices em FK são criados manualmente (ex.: `ix_usuario_rel_perfis_usuario`). |
   | **Índices e Constraints** | Prefixo `pk_` (PK), `fk_` (constraint FK), `uk_` ou `ux_` (unique), `ck_` (check), `ix_` (índice comum). Ex.: `ux_usuarios_email_lower`, `ix_sessoes_usuario`. |
   | **Check** | Padrão: `ck_<tabela>_<coluna>` (ex.: `ck_usuarios_celular`). |

   Exemplo de migração:

   ```sql
   -- V3__adicionar_tabela_produtos.sql
   create table produtos (
       id                 bigint generated always as identity,
       nome               varchar(255) not null,
       descricao          text,
       preco              numeric(10, 2) not null check (preco > 0),
       usuario_id         bigint not null,
       criado_em          timestamptz not null,
       criado_por         varchar(150) not null,
       alterado_em        timestamptz not null,
       alterado_por       varchar(150) not null,
       constraint pk_produtos primary key (id),
       constraint fk_produtos_usuario foreign key (usuario_id) references usuarios (id),
       constraint uk_produtos_nome unique (nome),
       constraint ck_produtos_preco check (preco > 0)
   );

   create index ix_produtos_usuario on produtos (usuario_id);
   create index ix_produtos_nome_lower on produtos (lower(nome));
   ```

4. **Use `check` para restrições de domínio.**

   Checks reduzem erros e garantem integridade dos dados:

   ```sql
   -- Bom
   preco numeric(10, 2) not null check (preco > 0),
   celular varchar(20) check (celular ~ '^[0-9]{11}$')

   -- Evitar (validação só na app)
   preco numeric(10, 2) not null
   ```

   > **Auditoria:** não use `default now()` ou `default 'sistema'` para colunas de auditoria. A aplicação preenche `criado_em`, `criado_por`, `alterado_em` e `alterado_por` via JPA Auditing na criação e atualização. Exceção: dados de seed em migrações podem usar `now()` e `'sistema'`.

5. **Mapeie a entidade JPA correspondente.**

   Se criou uma tabela, crie (ou atualize) uma entidade Java em `src/main/java/com/example/loginbase/<pacote>/Entidade.java`:

   ```java
   @Entity
   @Table(name = "produtos")
   public class Produto extends EntidadeAuditavel {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(name = "nome", nullable = false)
       private String nome;

       @ManyToOne
       @JoinColumn(name = "usuario_id")
       private Usuario usuario;

       // getters, setters...
   }
   ```

   A entidade deve estender `EntidadeAuditavel` (em `com.example.loginbase.auditoria`), que fornece `criado_em`/`criado_por` e `alterado_em`/`alterado_por`.

6. **Nunca altere migrações já aplicadas.**

   Uma vez que uma migração foi rodada em algum banco (desenvolvimento, testes, produção), **nunca** a edite. Se precisar corrigir:

   - Crie uma nova migração `V(N+1)__corrigir_descricao.sql` com as alterações.
   - Nunca use `DROP` em produção sem backup.

7. **Rode a migração localmente.**

   Com o banco rodando (`make up PROFILE_FRONTEND=desativado`), inicie a aplicação:

   ```bash
   set -a && . ./.env && set +a
   ./mvnw spring-boot:run
   ```

   O Flyway aplica todas as migrações pendentes na inicialização:

   ```
   [INFO] Successfully validated 3 migrations
   [INFO] Migrating schema "public" to version "3 - sua_descricao"
   [INFO] Successfully applied 1 migration...
   ```

   Se houver erro na migração:

   ```
   org.flywaydb.core.api.FlywayException: Validate failed: Detected failed migration
   ```

   Verifique a sintaxe SQL no console. Como PostgreSQL tem DDL transacional, a migração que falhou não fica aplicada. Corrija a própria migração e reinicie a aplicação.

## Exemplo completo

Para adicionar uma tabela `articulos` com auditoria:

**src/main/resources/db/migration/V3__adicionar_tabela_articulos.sql:**

```sql
create table articulos (
    id                 bigint generated always as identity,
    nome               varchar(255) not null,
    descricao          text,
    preco              numeric(10, 2) not null check (preco >= 0),
    usuario_id         bigint not null,
    criado_em          timestamptz not null,
    criado_por         varchar(150) not null,
    alterado_em        timestamptz not null,
    alterado_por       varchar(150) not null,
    constraint pk_articulos primary key (id),
    constraint fk_articulos_usuario foreign key (usuario_id) references usuarios (id),
    constraint uk_articulos_nome unique (nome),
    constraint ck_articulos_nome_nao_vazio check (length(trim(nome)) > 0)
);

create index ix_articulos_usuario on articulos (usuario_id);
create index ix_articulos_nome_lower on articulos (lower(nome));
```

**src/main/java/com/example/loginbase/acesso/Articulo.java:**

```java
import jakarta.persistence.*;
import com.example.loginbase.auditoria.EntidadeAuditavel;
import java.math.BigDecimal;

@Entity
@Table(name = "articulos")
public class Articulo extends EntidadeAuditavel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // getters, setters...
}
```

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| `Detected failed migration` na startup | Sintaxe SQL errada ou constraint violado | Verifique o console da aplicação ou os logs do Spring Boot. Corrija a migração que falhou e reinicie. |
| `Column "X" not found` ao rodar a app | Migração não foi aplicada ou entidade referencia coluna inexistente | Confirme que o `.sql` está em `src/main/resources/db/migration/` com nome `V*.sql`. Reinicie a aplicação Spring. |
| `ddl-auto=validate error` | Entidade JPA diverge do banco | Edite a entidade ou crie nova migração para sincronizar. |
| Constraints muito restritivas | Não permitindo inserção de dados legacy ou seed | Use `check` com lógica clara. Considere migração intermediária se mudar regra existente. |

## Veja também

- [Modelo de dados](../referencia/modelo-de-dados.md) — diagrama das tabelas atuais.
- [Autenticação e sessões](../explicacoes/autenticacao-e-sessoes.md) — detalhes sobre tabelas `usuarios`, `perfis`, `sessoes`.
- [Primeiros passos](../tutoriais/primeiros-passos.md) — como subir o banco.
