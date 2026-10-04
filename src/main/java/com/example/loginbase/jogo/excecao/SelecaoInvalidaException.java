package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** Seleção de regiões iniciais malformada: nula, fora de 3 itens, repetida ou fora de 1..16 (400). */
public class SelecaoInvalidaException extends JogoException {

	public SelecaoInvalidaException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, CodigoErro.SELECAO_INVALIDA, mensagem);
	}

}
