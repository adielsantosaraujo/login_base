---
titulo: Adicionar um endpoint REST
publico: desenvolvimento
tipo: guia
atualizado_em: 2026-10-04
fontes:
  - src/main/java/com/example/loginbase/seguranca/SecurityConfig.java
  - src/main/java/com/example/loginbase/web/OpenApiConfig.java
  - src/main/resources/application.properties
  - src/test/java/com/example/loginbase/web/AutenticacaoWebMvcTest.java
---

# Adicionar um endpoint REST

## Quando usar

Use este guia para criar um novo endpoint REST (`@RestController`) sob `/api/**`, com autenticação automática por sessão e documentação no Swagger.

## Pré-requisitos

- Projeto clonado e rodando (veja [Primeiros passos](../tutoriais/primeiros-passos.md)).
- Familiaridade com Spring Boot, `@RestController` e JSON.
- Opcionalmente, leia [Modelo de dados](../referencia/modelo-de-dados.md) para entender as entidades existentes.

## Passos

1. **Crie uma classe `@RestController` sob `/api/**`.**

   Crie um arquivo em `src/main/java/com/example/loginbase/web/SeuEndpointController.java`:

   ```java
   package com.example.loginbase.web;

   import org.springframework.web.bind.annotation.*;
   import org.springframework.http.ResponseEntity;
   import java.util.Map;

   @RestController
   @RequestMapping("/api/seu-recurso")
   public class SeuEndpointController {

       @GetMapping
       public ResponseEntity<String> listar() {
           return ResponseEntity.ok("{ \"mensagem\": \"Olá, mundo!\" }");
       }

       @PostMapping
       public ResponseEntity<?> criar(@RequestBody Map<String, String> dados) {
           // Processa dados
           return ResponseEntity.ok(dados);
       }
   }
   ```

   > **Convenção:** prefixo `/api/` é obrigatório. Sem isso, não aparece no Swagger e fica fora das rotas de API.

2. **Requisições anônimas recebem 401.**

   O `SecurityConfig` força autenticação em `/api/**` usando `HttpStatusEntryPoint`:

   ```java
   .defaultAuthenticationEntryPointFor(
       new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), 
       new OrRequestMatcher(api, apiDocs))
   ```

   Se um cliente não-autenticado chamar seu endpoint:

   ```bash
   curl -X GET http://localhost:8080/api/seu-recurso
   ```

   Recebe:

   ```
   HTTP 401 Unauthorized
   ```

   Sem redirecionamento para `/login` (ao contrário de rotas MVC). O cliente é responsável por fazer login primeiro.

3. **Autenticação: login antes do endpoint.**

   O login é por formulário em `/login`. É necessário obter os cookies de sessão e o token CSRF:

   ```bash
   # 1. Obter cookies (JSESSIONID e XSRF-TOKEN)
   curl -X GET http://localhost:8080/login -c cookies.txt
   ```

   Depois, fazer login salvando os cookies:

   ```bash
   # 2. Fazer login (TOKEN vem do cookie XSRF-TOKEN obtido acima)
   curl -X POST http://localhost:8080/login \
     -b cookies.txt -c cookies.txt \
     -H "X-XSRF-TOKEN: <valor do cookie XSRF-TOKEN>" \
     -d "login=admin@loginbase.local&senha=sua-senha"
   ```

   Por fim, usar os cookies em requisições ao endpoint:

   ```bash
   # 3. Chamar o endpoint com autenticação
   curl -b cookies.txt \
     -H "X-XSRF-TOKEN: <valor do cookie XSRF-TOKEN>" \
     -X GET http://localhost:8080/api/seu-recurso
   ```

   > **Em um cliente navegador:** um `fetch()` com `credentials: 'include'` automaticamente envia cookies em requisições subsequentes e o framework JavaScript (ex.: Axios) extrai o token XSRF do cookie.

4. **CSRF: requisições que alteram estado.**

   Qualquer requisição POST, PUT, DELETE exige o cabeçalho `X-XSRF-TOKEN`:

   ```bash
   curl -X POST http://localhost:8080/api/seu-recurso \
     -b cookies.txt \
     -H "X-XSRF-TOKEN: <valor do cookie XSRF-TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"chave": "valor"}'
   ```

   O servidor envia o cookie `XSRF-TOKEN` em respostas (mesmo em `GET /login` anônimo). O cliente extrai o valor do cookie e o envia como cabeçalho `X-XSRF-TOKEN` em requisições que alteram estado.

   Sem o cabeçalho correto:

   ```
   HTTP 403 Forbidden
   ```

   > **No navegador:** uma biblioteca ou framework JavaScript (ex.: Axios, Fetch wrapper) automaticamente extrai `XSRF-TOKEN` do cookie e envia o cabeçalho.

5. **Injete dependências conforme necessário.**

   Exemplo com repositório:

   ```java
   @RestController
   @RequestMapping("/api/usuarios")
   public class UsuariosController {

       private final UsuarioRepository usuarioRepository;

       public UsuariosController(UsuarioRepository usuarioRepository) {
           this.usuarioRepository = usuarioRepository;
       }

       @GetMapping
       public ResponseEntity<List<UsuarioDTO>> listar() {
           return ResponseEntity.ok(usuarioRepository.findAll()
               .stream()
               .map(u -> new UsuarioDTO(u.getId(), u.getNome(), u.getEmail()))
               .toList());
       }
   }
   ```

   O Spring injeta automaticamente via construtor.

6. **Anotações OpenAPI (opcionais).**

   Para melhorar a documentação no Swagger UI, use `@Operation` e `@Tag`:

   ```java
   @RestController
   @RequestMapping("/api/seu-recurso")
   @Tag(name = "Seu Recurso", description = "Operações sobre seu recurso")
   public class SeuEndpointController {

       @GetMapping
       @Operation(summary = "Listar todos", description = "Retorna uma lista de todos os recursos")
       public ResponseEntity<List<?>> listar() {
           // ...
       }

       @PostMapping
       @Operation(summary = "Criar um novo", description = "Cria um novo recurso a partir dos dados enviados")
       public ResponseEntity<?> criar(@RequestBody Map<String, String> dados) {
           // ...
       }
   }
   ```

   Elas são opcionais — até sem elas, seu endpoint aparecerá no Swagger com documentação básica.

7. **Teste seu endpoint.**

   Rode a aplicação:

   ```bash
   set -a && . ./.env && set +a
   ./mvnw spring-boot:run
   ```

   Teste via curl ou via Swagger UI:

   ```
   http://localhost:8080/swagger-ui.html
   ```

   Faça login via formulário HTML em `http://localhost:8080/login`.

   Depois, em Swagger, clique em seu endpoint. O cookie `JSESSIONID` é mantido. Clique em "Try it out" para testar.

8. **Atualize a documentação.**

   Após criar o endpoint, atualize `desenvolvimento/referencia/api-rest.md` com a lista de operações disponíveis. Veja [API REST](../referencia/api-rest.md).

## Exemplo: Criar um endpoint de produtos

**Controller:**

```java
package com.example.loginbase.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.loginbase.acesso.Produto;
import com.example.loginbase.acesso.ProdutoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Gerenciamento de produtos")
public class ProdutosController {

    private final ProdutoRepository produtoRepository;

    public ProdutosController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    @Operation(summary = "Listar produtos")
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(produtoRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter um produto")
    public ResponseEntity<Produto> obter(@PathVariable Long id) {
        return produtoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar um produto")
    public ResponseEntity<Produto> criar(@RequestBody Produto produto) {
        Produto criado = produtoRepository.save(produto);
        return ResponseEntity.ok(criado);
    }
}
```

> **Exemplo hipotético:** `Produto` e `ProdutoRepository` não existem no projeto. Para usar este exemplo, crie a entidade `Produto`, a interface `ProdutoRepository`, e uma migração Flyway correspondente (veja [Criar uma migração Flyway](./criar-uma-migracao-flyway.md)).

**Teste no Swagger:**

1. Acesse `http://localhost:8080/swagger-ui.html`.
2. Faça login em `http://localhost:8080/login`.
3. Volte ao Swagger. Você verá seu endpoint sob "Produtos".
4. Clique em "Try it out" e execute.

## Como verificar

Se seu endpoint está correto:

- ✓ Aparece na documentação Swagger (`http://localhost:8080/v3/api-docs` em JSON, ou `swagger-ui.html` visual).
- ✓ Requisições não-autenticadas recebem 401.
- ✓ Requisições autenticadas (com `JSESSIONID` válido) funcionam.
- ✓ POST/PUT/DELETE exigem o cabeçalho `X-XSRF-TOKEN`.
- ✓ Logs da aplicação não mostram erros ao chamar o endpoint.

## Problemas comuns

| Sintoma | Causa | Solução |
|---|---|---|
| Endpoint não aparece no Swagger | Não está sob `/api/**` ou classe não é `@RestController` | Renomeie o mapeamento para `/api/seu-endpoint` ou use `@RestController`. Reinicie a app. |
| `401 Unauthorized` mesmo com login | Cookie `JSESSIONID` não foi enviado ou expirou | No navegador, verifique o Storage/Cookies se tem `JSESSIONID`. Faça login novamente. |
| `403 Forbidden` em POST/PUT | Falta cabeçalho `X-XSRF-TOKEN` | Extraia o valor do cookie `XSRF-TOKEN` e o envie como cabeçalho. |
| `404 Not Found` | Spring não carregou sua classe ou o mapeamento está errado | Confirme que a classe é anotada com `@RestController` e o mapeamento começa com `/api/`. Reinicie a app. |
| Injeção de dependência falha | Classe não é gerenciada pelo Spring | Use `@Component`, `@Service`, `@Repository` ou `@RestController` na classe. Ou injete via construtor. |

## Veja também

- [Rotas e segurança](../referencia/rotas-e-seguranca.md) — mapeamento completo de rotas e autenticação.
- [API REST](../referencia/api-rest.md) — documentação da API (a ser atualizada com novos endpoints).
- [Autenticação e sessões](../explicacoes/autenticacao-e-sessoes.md) — como funciona o login e a sessão.
- [Executar os testes](./executar-os-testes.md) — como testar seu endpoint com testes unitários.
