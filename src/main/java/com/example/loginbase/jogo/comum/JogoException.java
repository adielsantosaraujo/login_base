package com.example.loginbase.jogo.comum;

import org.springframework.http.HttpStatus;

/**
 * Exceção base das regras do jogo; carrega o status HTTP da resposta.
 */
public class JogoException extends RuntimeException {

	private final HttpStatus status;

	public JogoException(HttpStatus status, String mensagem) {
		super(mensagem);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
