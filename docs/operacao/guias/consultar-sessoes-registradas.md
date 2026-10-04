---
titulo: Consultar sessões registradas
publico: operacao
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/main/resources/db/migration/V1__controle_acesso.sql
  - src/main/java/com/example/loginbase/seguranca/SessaoService.java
  - src/main/java/com/example/loginbase/seguranca/SessoesAbertasRunner.java
  - docker-compose.yml
---

# Consultar sessões registradas

## Quando usar

Use este guia para inspecionar o histórico e status das sessões de usuários:

- Verificar quem está logado no momento.
- Consultar histórico de logins (data, hora, IP, dispositivo).
- Rastrear atividades para auditoria.

## Pré-requisitos

- O banco de dados está em execução (`make up` já foi executado).
- Você tem acesso ao terminal WSL.
- Pelo menos um usuário realizou login na aplicação.

## Passos

1. Abra uma sessão no Postgres:

   ```bash
   docker compose exec db psql -U login_base login_base
   ```

2. Visualize todas as sessões (abertas e fechadas):

   ```sql
   SELECT id, usuario_id, data_inicio, data_fim, ip, dispositivo FROM sessoes ORDER BY data_inicio DESC;
   ```

3. Para ver apenas sessões abertas (ainda ativas):

   ```sql
   SELECT id, usuario_id, data_inicio, ip, dispositivo FROM sessoes WHERE data_fim IS NULL ORDER BY data_inicio DESC;
   ```

4. Para associar sessões com e-mails de usuários:

   ```sql
   SELECT s.id, u.email, s.data_inicio, s.data_fim, s.ip, s.dispositivo 
   FROM sessoes s 
   JOIN usuarios u ON s.usuario_id = u.id 
   ORDER BY s.data_inicio DESC;
   ```

5. Para ver as últimas N sessões de um usuário específico:

   ```sql
   SELECT s.id, s.data_inicio, s.data_fim, s.ip, s.dispositivo 
   FROM sessoes s 
   JOIN usuarios u ON s.usuario_id = u.id 
   WHERE u.email = 'seu-email@dominio.com' 
   ORDER BY s.data_inicio DESC 
   LIMIT 10;
   ```

6. Saia do Postgres:

   ```sql
   \q
   ```

## Informações sobre o campo `token`

O campo `token` na tabela `sessoes` armazena o **hash SHA-256** do identificador de sessão HTTP. Isto significa:

- O `token` não pode ser usado para sequestrar a sessão (não é o ID original).
- Serve apenas para rastrear e auditar, permitindo correlacionar um registro com a sessão sem expor o valor secreto.
- A sessão real é gerenciada pelo container HTTP da aplicação e armazenada em memória (nunca foi configurado Spring Session).

## Fechamento automático de sessões

Sessões abertas no início da aplicação são fechadas automaticamente pelo `SessoesAbertasRunner`:

- Quando a aplicação reinicia, as sessões HTTP em memória são perdidas.
- O evento de destruição da sessão não é publicado (pois a memória foi descartada).
- Portanto, o `SessoesAbertasRunner` executa na inicialização e fecha todos os registros com `data_fim` nula.

Você verá no log:

```
INFO ... — Fechados N registro(s) de sessão que estavam abertos ao iniciar a aplicação
```

## Limitações

- Não há interface de gerenciamento para forçar logout de uma sessão específica. Alterações devem ser feitas no banco se necessário.
- O IP registrado é `request.getRemoteAddr()`. Se a aplicação estiver atrás de um proxy ou load balancer, o IP será sempre o do proxy, não do cliente original. Para usar `X-Forwarded-For`, configure `server.forward-headers-strategy` em `application.properties`.

## Veja também

- [`docs/desenvolvimento/explicacoes/autenticacao-e-sessoes.md`](../../desenvolvimento/explicacoes/autenticacao-e-sessoes.md) — detalhes técnicos sobre como sessões funcionam.
- [`docs/usuario/guias/entrar-e-sair-do-sistema.md`](../../usuario/guias/entrar-e-sair-do-sistema.md) — como fazer login.
