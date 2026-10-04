package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** O usuário já possui vila (409). */
public class VilaJaExisteException extends JogoException {

	public VilaJaExisteException(String mensagem) {
		super(HttpStatus.CONFLICT, CodigoErro.VILA_JA_EXISTE, mensagem);
	}

}
