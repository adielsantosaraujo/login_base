package com.example.loginbase.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de apoio usado apenas por {@link ApiSegurancaWebMvcTest} para
 * exercitar o tratamento de segurança dado a {@code /api/**} pelo
 * {@code SecurityConfig} (Task 1.1), sem depender de nenhum endpoint real do
 * jogo.
 */
@RestController
public class ControladorTesteApi {

	@GetMapping("/api/teste")
	public String get() {
		return "ok";
	}

	@PostMapping("/api/teste")
	public String post() {
		return "ok";
	}

}
