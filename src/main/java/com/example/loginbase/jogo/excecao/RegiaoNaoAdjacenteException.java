package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** Região escolhida não é adjacente a nenhuma já selecionada/possuída (400). */
public class RegiaoNaoAdjacenteException extends JogoException {

	public RegiaoNaoAdjacenteException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, CodigoErro.REGIAO_NAO_ADJACENTE, mensagem);
	}

}
