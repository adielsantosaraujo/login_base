package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.JogoException;

/** Região com masmorra ativa não pode ser anexada (400). */
public class RegiaoComMasmorraException extends JogoException {

	public RegiaoComMasmorraException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, mensagem);
	}

}
