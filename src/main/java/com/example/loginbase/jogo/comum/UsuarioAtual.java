package com.example.loginbase.jogo.comum;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.loginbase.acesso.NormalizacaoContato;
import com.example.loginbase.acesso.UsuarioRepository;

/**
 * Resolve o usuário autenticado da requisição atual. O {@code username} do
 * principal é o e-mail do usuário.
 */
@Component
public class UsuarioAtual {

	private final UsuarioRepository usuarioRepository;

	public UsuarioAtual(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * @throws JogoException 401 se não houver usuário autenticado ou ele não existir
	 */
	public Long idUsuario() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()
				|| auth.getName() == null) {
			throw naoAutenticado();
		}
		return usuarioRepository.findByEmail(NormalizacaoContato.email(auth.getName()))
				.map(u -> u.getId())
				.orElseThrow(UsuarioAtual::naoAutenticado);
	}

	private static JogoException naoAutenticado() {
		return new JogoException(HttpStatus.UNAUTHORIZED, "Não autenticado");
	}

}
