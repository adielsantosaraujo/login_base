package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** O usuário não tem prévia de mapa gerada (404). */
public class PreviaNaoEncontradaException extends JogoException {

	public PreviaNaoEncontradaException(String mensagem) {
		super(HttpStatus.NOT_FOUND, CodigoErro.PREVIA_NAO_ENCONTRADA, mensagem);
	}

}
