package com.example.loginbase.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelas páginas de navegação: a página de login e as
 * SPAs geradas pelo build do frontend. Cada SPA tem seus estáticos em
 * {@code /<nome>/...} e seu index em {@code sistema/<public|seguro>/<nome>/index}:
 * <ul>
 * <li>{@code cadastro_usuario}: área pública (sem login), em
 * {@code /cadastro_usuario/index} e nas rotas do cliente vue-router sob
 * {@code /cadastro_usuario/...};</li>
 * <li>{@code patrimonio}: área segura (exige login), em {@code /patrimonio/index}
 * e nas rotas do cliente sob {@code /patrimonio/...}.</li>
 * </ul>
 * {@code /} redireciona para {@link #PAGINA_INICIAL}; {@code /<nome>} e
 * {@code /<nome>/} redirecionam para {@code /<nome>/index} (a barra final é
 * mapeada explicitamente, pois o Spring 6+ não a casa automaticamente).
 *
 * <p>As rotas do cliente casam até 5 níveis cujo último segmento não tenha
 * ponto. Caminhos com extensão (ex.: {@code /patrimonio/assets/x.js}) não casam
 * e seguem para os recursos estáticos. {@code {*...}} e {@code **} no meio do
 * padrão são rejeitados pelo PathPatternParser, e {@code /<nome>/{*c}}
 * sombrearia os estáticos, daí os padrões explícitos por profundidade (os
 * valores de anotação precisam ser constantes, por isso os literais repetidos
 * por app).
 */
@Controller
public class PaginaController {

    /** Página para onde {@code /} e o pós-login redirecionam. */
    public static final String PAGINA_INICIAL = "/patrimonio/index";

    @GetMapping("/login")
    String login() {
        return "sistema/public/login";
    }

    @GetMapping("/")
    String raiz() {
        return "redirect:" + PAGINA_INICIAL;
    }

    @GetMapping({"/cadastro_usuario", "/cadastro_usuario/"})
    String raizCadastroUsuario() {
        return "redirect:/cadastro_usuario/index";
    }

    @GetMapping({"/patrimonio", "/patrimonio/"})
    String raizPatrimonio() {
        return "redirect:/patrimonio/index";
    }

    /** SPA pública {@code cadastro_usuario}: index e rotas do cliente. */
    @GetMapping({ "/cadastro_usuario/index",
            "/cadastro_usuario/{s1:[^.]+}",
            "/cadastro_usuario/{s1}/{s2:[^.]+}",
            "/cadastro_usuario/{s1}/{s2}/{s3:[^.]+}",
            "/cadastro_usuario/{s1}/{s2}/{s3}/{s4:[^.]+}",
            "/cadastro_usuario/{s1}/{s2}/{s3}/{s4}/{s5:[^.]+}" })
    String indexCadastroUsuario() {
        return "sistema/public/cadastro_usuario/index";
    }

    /** SPA segura {@code patrimonio}: index e rotas do cliente. */
    @GetMapping({ "/patrimonio/index",
            "/patrimonio/{s1:[^.]+}",
            "/patrimonio/{s1}/{s2:[^.]+}",
            "/patrimonio/{s1}/{s2}/{s3:[^.]+}",
            "/patrimonio/{s1}/{s2}/{s3}/{s4:[^.]+}",
            "/patrimonio/{s1}/{s2}/{s3}/{s4}/{s5:[^.]+}" })
    String indexPatrimonio() {
        return "sistema/seguro/patrimonio/index";
    }
}
