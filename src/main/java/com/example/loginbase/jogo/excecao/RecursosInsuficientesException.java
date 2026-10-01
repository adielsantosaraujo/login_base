package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.JogoException;

/** Estoque insuficiente para um débito (409). A mensagem é parametrizável. */
public class RecursosInsuficientesException extends JogoException {

	public static final String MENSAGEM_PADRAO = "Recursos insuficientes";

	public RecursosInsuficientesException() {
		this(MENSAGEM_PADRAO);
	}

	public RecursosInsuficientesException(String mensagem) {
		super(HttpStatus.CONFLICT, mensagem);
	}

}
