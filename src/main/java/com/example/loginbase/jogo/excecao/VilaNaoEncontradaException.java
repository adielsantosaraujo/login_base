package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** O usuário ainda não possui vila (404). */
public class VilaNaoEncontradaException extends JogoException {

	public VilaNaoEncontradaException(String mensagem) {
		super(HttpStatus.NOT_FOUND, CodigoErro.VILA_NAO_ENCONTRADA, mensagem);
	}

}
