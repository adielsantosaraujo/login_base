package com.example.loginbase.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsável pelas páginas públicas e seguras de navegação:
 * a página de login e a página inicial protegida.
 */
@Controller
public class PaginaController {

    @GetMapping("/login")
    String login() {
        return "sistema/public/login";
    }

    @GetMapping("/")
    String index() {
        return "sistema/seguro/index";
    }
}
