package com.example.loginbase.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelas páginas públicas e seguras de navegação:
 * a página de login, a SPA protegida em {@code /app/index} (e nas rotas do
 * cliente vue-router sob {@code /app/...}) e o redirecionamento de {@code /},
 * {@code /app} e {@code /app/} para ela (a barra final é mapeada explicitamente,
 * pois o Spring 6+ não a casa automaticamente).
 */
@Controller
public class PaginaController {

    @GetMapping("/login")
    String login() {
        return "sistema/public/login";
    }

    @GetMapping({"/", "/app", "/app/"})
    String raiz() {
        return "redirect:/app/index";
    }

    /**
     * Serve a SPA em {@code /app/index} e em qualquer rota do cliente sob
     * {@code /app/...} (até 5 níveis) cujo último segmento não tenha ponto.
     * Caminhos com extensão (ex.: {@code /app/assets/x.js}) não casam e seguem
     * para os recursos estáticos. {@code {*...}} e {@code **} no meio do padrão
     * são rejeitados pelo PathPatternParser, e {@code /app/{*c}} sombrearia os
     * estáticos, daí os padrões explícitos por profundidade.
     */
    @GetMapping({ "/app/index",
            "/app/{s1:[^.]+}",
            "/app/{s1}/{s2:[^.]+}",
            "/app/{s1}/{s2}/{s3:[^.]+}",
            "/app/{s1}/{s2}/{s3}/{s4:[^.]+}",
            "/app/{s1}/{s2}/{s3}/{s4}/{s5:[^.]+}" })
    String index() {
        return "sistema/seguro/app/index";
    }
}
