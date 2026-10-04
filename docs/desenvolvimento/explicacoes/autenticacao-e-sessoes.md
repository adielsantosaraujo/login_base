---
titulo: Autenticação e sessões
publico: desenvolvimento
tipo: explicacao
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/seguranca/IdentificadorLogin.java
  - src/main/java/com/example/loginbase/seguranca/UsuarioDetailsService.java
  - src/main/java/com/example/loginbase/acesso/UsuarioPerfilRepository.java
  - src/main/java/com/example/loginbase/seguranca/RegistroSessaoSuccessHandler.java
  - src/main/java/com/example/loginbase/seguranca/SessaoService.java
  - src/main/java/com/example/loginbase/seguranca/SessaoEncerradaListener.java
  - src/main/java/com/example/loginbase/seguranca/SessoesAbertasRunner.java
  - src/main/resources/application.properties
---

# Autenticação e sessões

Explicação do fluxo de autenticação, validação de identidades e registro de sessões HTTP.

## Contexto

A aplicação utiliza autenticação baseada em **formulário HTML** e **sessões HTTP**. Não há JWT ou autenticação de API separada. Cada usuário que faz login recebe um cookie `JSESSIONID` e um registro na tabela `sessoes`.

A autenticação acontece em várias camadas:
1. **Interpretação** do identificador (e-mail ou celular).
2. **Validação** de credenciais contra o banco.
3. **Carregamento** de perfis e permissões vigentes.
4. **Registro** da sessão aberta.
5. **Encerramento** automático quando a sessão expira.

## Como funciona

O diagrama abaixo mostra o fluxo completo de autenticação e sessão:

```mermaid
sequenceDiagram
    participant U as Usuário
    participant B as Navegador
    participant S as Spring Boot
    participant BD as Banco de Dados
    participant R as RegistroSessaoSuccessHandler
    participant L as SessaoEncerradaListener
    
    U->>B: Preenche /login (e-mail/celular + senha)
    B->>S: POST /login com CSRF token
    activate S
    S->>S: IdentificadorLogin.interpretar(login)
    alt @ no input
        S->>S: Classifica como e-mail
    else Sem @
        S->>S: Tira não-dígitos; se 11 dígitos → celular
    else Outro
        S->>S: Entrada inválida
        S->>B: /login?error (mensagem genérica)
        deactivate S
    end
    
    S->>BD: Busca usuário por email/celular
    alt Não encontrado
        BD-->>S: null
        S->>B: /login?error (mensagem genérica)
        deactivate S
    else Encontrado
        BD-->>S: Usuario
    end
    
    S->>BD: findPerfisVigentesComPermissoes(usuario_id, hoje)
    BD-->>S: Lista[PerfilPermissaoVigente]
    
    alt Nenhum perfil vigente
        S->>S: UserDetails.disabled = true
        S->>B: /login?error (conta desabilitada → mensagem genérica)
        deactivate S
    else Tem perfil vigente
        S->>S: DelegatingPasswordEncoder valida senha
        alt Senha inválida
            S->>B: /login?error (mensagem genérica)
            deactivate S
        end
        S->>S: Cria UserDetails com authorities ROLE_*
        S->>S: changeSessionId() (muda ID de sessão)
        S->>R: onAuthenticationSuccess(...)
    end
    
    activate R
    R->>BD: SessaoService.registrarInicio(email, sessionId, ip, userAgent)
    R->>BD: INSERT INTO sessoes (usuario_id, token, ...)
    Note over BD: token = SHA-256(sessionId)
    R->>B: Redireciona para /app/index ou página anterior
    deactivate R
    deactivate S
    
    B->>S: GET /app/index (com JSESSIONID cookie)
    S->>B: Retorna SPA (index.html)
    
    par Navegação normal
        B->>B: Vue router mapeia rotas em /app/**
        B->>S: Fetch/POST (requisições da SPA)
        S->>B: Resposta JSON
    and Inatividade
        Note over B: 30 minutos de inatividade
        B->>S: (nenhuma requisição)
    end
    
    Note over S,BD: Sessão expira ou logout
    S->>L: HttpSessionDestroyedEvent
    L->>BD: SessaoService.registrarFim(sessionId)
    
    U->>B: Tenta acessar /app/index
    B->>S: GET /app/index (sem ou cookie expirado)
    S->>B: /login (redirecionamento)
```

## Identificação (e-mail ou celular)

O componente `IdentificadorLogin` interpreta o texto digitado no campo "E-mail ou celular":

| Input | Lógica | Resultado |
|---|---|---|
| `admin@exemplo.com` | Contém `@` | E-mail normalizado (minúsculas; espaços removidos das pontas) |
| `(11) 98765-4321` | Sem `@`; tira não-dígitos → `11987654321` (11 dígitos) | Celular (11 dígitos) |
| `11987654321` | Sem `@`; já em dígitos (11) | Celular |
| `119876543` | Sem `@`; apenas 9 dígitos | Inválido (rejeitado sem consultar banco) |
| `(vazio)` | Nulo ou em branco | Inválido |

**Segurança:** Entradas inválidas não consultam o banco, reduzindo a janela de enumeração de usuários.

## Carregamento de perfis e permissões

Antes de validar a senha, `UsuarioDetailsService.loadUserByUsername()` carrega os perfis vigentes:

```java
List<PerfilPermissaoVigente> perfisVigentes = usuarioPerfilRepository
    .findPerfisVigentesComPermissoes(usuario.getId(), LocalDate.now());
```

**Vigência:** Um perfil é válido se:
- `data_inicial <= hoje` **E**
- (`data_final` é nula **OU** `data_final >= hoje`)

**Resultado:** Cada perfil gera uma authority `ROLE_<nome>` (ex.: `ROLE_ADMIN`); cada permissão gera uma authority com o nome dela (ex.: `ver_dashboard`, exemplo hipotético).

**Se nenhum perfil vigente:** O usuário fica marcado como `disabled = true` e não consegue logar. A mensagem de erro é genérica (mesma do resto).

## Registro de sessão

Imediatamente após login bem-sucedido (e **após** a mudança de ID de sessão), `RegistroSessaoSuccessHandler` chama:

```java
sessaoService.registrarInicio(
    authentication.getName(),   // sempre o e-mail, mesmo se login foi por celular
    session.getId(),            // ID da sessão HTTP (para calcular hash)
    request.getRemoteAddr(),    // IP (sem X-Forwarded-For por padrão)
    request.getHeader("User-Agent")  // User-Agent truncado em 500 chars
);
```

O `SessaoService` gera um hash SHA-256 do ID de sessão e insere um registro em `sessoes`:

```sql
INSERT INTO sessoes (usuario_id, token, ip, dispositivo, data_inicio, ...)
VALUES (1, 'a1b2c3...', '127.0.0.1', 'Mozilla/5.0 ...', now(), ...);
```

**Segurança:** O `token` armazenado é o hash, não o ID em si. Isso permite rastrear uma sessão sem guardar um valor reutilizável para sequestro.

## Encerramento de sessão

Existem três mecanismos:

### 1. Logout explícito

O usuário faz POST `/logout`:
```html
<form method="post" action="/logout">
  <input type="hidden" name="_csrf" value="...">
  <button>Sair</button>
</form>
```

A sesão HTTP é invalidada (`invalidateHttpSession(true)`) e o cookie `JSESSIONID` é apagado.

### 2. Expiração automática (timeout)

Se o usuário não fizer nenhuma requisição por 30 minutos (configurável em `SESSION_TIMEOUT`), o container descarta a sessão. O `SessaoEncerradaListener` escuta `HttpSessionDestroyedEvent` (publicado por `HttpSessionEventPublisher` do `SessaoEventosConfig`) e preenche `data_fim` tanto no logout quanto na expiração por timeout ou invalidação:

```java
@EventListener
public void aoDestruirSessao(HttpSessionDestroyedEvent event) {
    try {
        sessaoService.registrarFim(event.getId());
    } catch (RuntimeException e) {
        log.warn("Falha ao registrar fim de sessão para o id {}", event.getId(), e);
    }
}
```

### 3. Limpeza na inicialização

O `SessoesAbertasRunner` executa ao iniciar a aplicação e fecha qualquer registro de sessão ainda aberto (caso a aplicação tenha caído sem destruir as sessões):

```java
update Sessao s set s.dataFim = :agora, s.alteradoEm = :agora, s.alteradoPor = :por 
where s.dataFim is null
```

(JPQL via `SessaoRepository.fecharTodasAbertas()`)

## Mensagens de erro

Todas as falhas de autenticação exibem a mesma mensagem genérica:
> "Usuário ou senha inválidos."

Isso inclui:
- Usuário não encontrado.
- Senha incorreta.
- Conta desabilitada (nenhum perfil vigente).
- Identificador inválido (celular com dígitos a menos; sem validação de formato de e-mail no cliente).

**Benefício:** Impede enumeração de usuários. **Trade-off:** O usuário não sabe por que não consegue logar (fora do escopo; futuras features podem registrar tentativas ou enviar e-mail).

## Limitações

- **Uma instância só:** Registro de sessões em tabela local. Com múltiplas instâncias ou sessão externalizada, a abordagem precisa ser revisada.
- **IP real atrás de proxy:** Sem `server.forward-headers-strategy=native`, o IP registrado é o do proxy, não do usuário real.
- **Sem bloqueio por tentativas:** Vulnerável a força bruta com números sequenciais de celular.

## Veja também

- [Modelo de dados](../referencia/modelo-de-dados.md)
- [Auditoria](./auditoria.md)
- [Rotas e segurança](../referencia/rotas-e-seguranca.md)
- [ADR 0007 — Login por e-mail ou celular](./decisoes/0007-login-por-email-ou-celular-normalizados.md)
- [ADR 0009 — Registro de sessões em tabela](./decisoes/0009-registro-de-sessoes-em-tabela-propria.md)
