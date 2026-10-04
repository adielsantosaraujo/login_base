package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** Nenhuma das regiões iniciais escolhidas é Urbana (400). */
public class UrbanaObrigatoriaException extends JogoException {

	public UrbanaObrigatoriaException(String mensagem) {
		super(HttpStatus.BAD_REQUEST, CodigoErro.SEM_REGIAO_URBANA, mensagem);
	}

}
