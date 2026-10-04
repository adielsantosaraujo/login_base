package com.example.loginbase.jogo.excecao;

import org.springframework.http.HttpStatus;

import com.example.loginbase.jogo.comum.CodigoErro;
import com.example.loginbase.jogo.comum.JogoException;

/** O previaId informado não é mais o da prévia vigente (409). */
public class PreviaExpiradaException extends JogoException {

	public PreviaExpiradaException(String mensagem) {
		super(HttpStatus.CONFLICT, CodigoErro.PREVIA_EXPIRADA, mensagem);
	}

}
