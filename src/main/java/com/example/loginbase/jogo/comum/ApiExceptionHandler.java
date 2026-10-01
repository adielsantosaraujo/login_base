package com.example.loginbase.jogo.comum;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte exceções da API do jogo em respostas {@code { "erro": "..." }}.
 */
@RestControllerAdvice(basePackages = "com.example.loginbase.jogo")
public class ApiExceptionHandler {

	@ExceptionHandler(JogoException.class)
	public ResponseEntity<Map<String, String>> jogo(JogoException e) {
		return ResponseEntity.status(e.getStatus()).body(Map.of("erro", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.findFirst().orElse("Requisição inválida");
		return ResponseEntity.badRequest().body(Map.of("erro", msg));
	}

}
