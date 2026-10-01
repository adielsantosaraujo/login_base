package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.JogoException;

/** A região já pertence à vila (400). */
public class RegiaoJaPossuidaException extends JogoException {

	public RegiaoJaPossuidaException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, mensagem);
	}

}
