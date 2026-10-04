package com.example.loginbase.jogo.comum;

import org.springframework.http.HttpStatus;

/**
 * Exceção base das regras do jogo; carrega o status HTTP da resposta.
 */
public class JogoException extends RuntimeException {

	private final HttpStatus status;

	private final String codigo;

	public JogoException(HttpStatus status, String mensagem) {
		this(status, null, mensagem);
	}

	public JogoException(HttpStatus status, String codigo, String mensagem) {
		super(mensagem);
		this.status = status;
		this.codigo = codigo;
	}

	/** Código estável de erro; {@code null} quando não definido. */
	public String getCodigo() {
		return codigo;
	}

	public HttpStatus getStatus() {
		return status;
	}

}
