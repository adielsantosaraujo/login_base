package com.example.loginbase.jogo.servico;

import org.springframework.stereotype.Component;

import com.example.loginbase.jogo.comum.UsuarioAtual;
import com.example.loginbase.jogo.excecao.VilaNaoEncontradaException;
import com.example.loginbase.jogo.modelo.Vila;
import com.example.loginbase.jogo.repositorio.VilaRepository;

/**
 * Resolve a vila do usuário autenticado.
 */
@Component
public class VilaAtual {

	private final UsuarioAtual usuarioAtual;
	private final VilaRepository vilaRepository;

	public VilaAtual(UsuarioAtual usuarioAtual, VilaRepository vilaRepository) {
		this.usuarioAtual = usuarioAtual;
		this.vilaRepository = vilaRepository;
	}

	/**
	 * @throws VilaNaoEncontradaException 404 se o usuário logado ainda não tem vila
	 */
	public Vila obter() {
		return vilaRepository.findByUsuarioId(usuarioAtual.idUsuario())
				.orElseThrow(() -> new VilaNaoEncontradaException("Vila não encontrada"));
	}

}
