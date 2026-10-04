package com.example.loginbase.jogo.comum;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte exceções da API do jogo em respostas {@code { "erro": "...", "codigo": "..." }} (codigo só quando definido).
 */
@RestControllerAdvice(basePackages = "com.example.loginbase.jogo")
public class ApiExceptionHandler {

	@ExceptionHandler(JogoException.class)
	public ResponseEntity<Map<String, String>> jogo(JogoException e) {
		Map<String, String> corpo = new LinkedHashMap<>();
		corpo.put("erro", e.getMessage());
		if (e.getCodigo() != null) {
			corpo.put("codigo", e.getCodigo());
		}
		return ResponseEntity.status(e.getStatus()).body(corpo);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.findFirst().orElse("Requisição inválida");
		return ResponseEntity.badRequest().body(Map.of("erro", msg));
	}

}
