---
titulo: Registro de sessões em tabela própria
publico: desenvolvimento
tipo: adr
status: Aceita
data: 2026-09-24
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/java/com/example/loginbase/acesso/Sessao.java
  - src/main/java/com/example/loginbase/acesso/SessaoRepository.java
  - src/main/java/com/example/loginbase/seguranca/RegistroSessaoSuccessHandler.java
  - src/main/java/com/example/loginbase/seguranca/SessaoEncerradaListener.java
  - src/main/java/com/example/loginbase/seguranca/SessoesAbertasRunner.java
---

# 0009 — Registro de sessões em tabela própria

**Status:** Aceita · **Data:** 2026-09-24

## Contexto

A aplicação precisa registrar histórico de sessões abertas (quem entrou, quando, de qual IP, qual dispositivo) para auditoria e suporte. É necessário escolher: guardar os registros em tabela SQL própria, externalizar em Spring Session JDBC, ou não registrar.

## Decisão

Criar tabela `sessoes` em `V1__controle_acesso.sql` com colunas: `id`, `usuario_id` (FK), `data_inicio`, `data_fim` (nullable), `token` (SHA-256 hexadecimal do ID da sessão HTTP), `ip`, `dispositivo`, e colunas de auditoria padrão.

- **Abertura:** `AuthenticationSuccessHandler` (`RegistroSessaoSuccessHandler`) chamado após troca bem-sucedida de ID de sessão; calcula SHA-256 do ID e grava junto com IP (`request.getRemoteAddr()`) e User-Agent (truncado em 500 caracteres).
- **Encerramento:** `@EventListener` de `HttpSessionDestroyedEvent` preenche `data_fim`. Exceção: sessões abertas em crash/reinício ficam com `data_fim` nula; um `ApplicationRunner` (`SessoesAbertasRunner`) fecha-as na inicialização.
- **Token:** nunca é o ID em texto puro; é apenas o hash, impossibilitando sequestrar a sessão a partir do registro.

## Alternativas descartadas

- **Spring Session JDBC** — Rejeitada porque impõe esquema próprio (`SPRING_SESSION`) incompatível com a auditoria do projeto e exigiria consulta separada para extrair informações de auditoria.
- **Não registrar** — Rejeitada porque o requisito é auditoria de acessos; sem histórico de sessões há perda de rastreamento.

## Consequências

### Positivas

- **Auditoria completa:** data/hora, IP e dispositivo de cada acesso; `data_fim` registra logout ou expiração.
- **Compatível com auditoria do projeto:** colunas de criação/modificação seguem o padrão do banco.
- **Tabela sob controle:** versionada por Flyway; esquema é previsível e adaptável.

### Negativas

- **Uma instância só:** a sessão HTTP fica em memória (Java) e não suporta replicação direta; em múltiplas instâncias, cada uma teria seu próprio histórico de sessões fragmentado. Os registros ficam no banco (SQL) e são compartilhados. Mitigado com documentação de limitação.
- **Sessões em crash não encerram automaticamente:** `SessoesAbertasRunner` fecha na inicialização, acumulando possíveis "sessões órfãs" entre o crash e o restart.
- **IP real com proxy:** sem configuração explícita (`server.forward-headers-strategy`), o IP gravado é o do proxy, não o do cliente. Documentado como limitação.

