package com.example.loginbase.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelas páginas públicas e seguras de navegação:
 * a página de login, a SPA protegida em {@code /app/index} e o redirecionamento
 * de {@code /}, {@code /app} e {@code /app/} para ela (a barra final é mapeada
 * explicitamente, pois o Spring 6+ não a casa automaticamente).
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

    @GetMapping("/app/index")
    String index() {
        return "sistema/seguro/app/index";
    }
}
