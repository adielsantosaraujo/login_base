package com.example.loginbase.jogo;

/**
 * Recurso de jogo (vila, prédio, batalha, etc.) não encontrado ou não
 * pertencente ao usuário atual, mapeada pelo {@code ErroApiHandler} para 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

	public RecursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}

}
