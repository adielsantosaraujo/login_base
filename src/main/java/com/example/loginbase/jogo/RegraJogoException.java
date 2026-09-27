package com.example.loginbase.jogo;

import lombok.Getter;

/**
 * Violação de regra de jogo, lançada por serviços do pacote {@code jogo} e
 * mapeada pelo {@code ErroApiHandler} para 422 (exceto
 * {@link CodigoErro#TURNO_DESATUALIZADO}, mapeado para 409).
 */
@Getter
public class RegraJogoException extends RuntimeException {

	private final CodigoErro codigo;

	public RegraJogoException(CodigoErro codigo, String mensagem) {
		super(mensagem);
		this.codigo = codigo;
	}

}
