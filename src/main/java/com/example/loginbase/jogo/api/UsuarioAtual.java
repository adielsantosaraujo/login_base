package com.example.loginbase.jogo.api;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;

/**
 * Resolve o id do usuário autenticado a partir do {@link Authentication} da
 * requisição, para uso pelos controllers do jogo (cada vila só pode ser
 * acessada pelo próprio dono).
 *
 * <p>
 * Exposto como método <strong>estático</strong> ({@link #id(Authentication)})
 * para uso direto nos controllers (ex.: {@code UsuarioAtual.id(auth)}), sem
 * precisar declarar um campo injetado em cada um deles. Por depender de
 * {@link UsuarioRepository} (um bean Spring), a classe também é registrada
 * como {@code @Component}: sua construção (feita uma única vez pelo
 * container) grava a dependência num campo estático, que o método estático
 * então consulta. Isso exige que o container tenha instanciado
 * {@code UsuarioAtual} ao menos uma vez antes do primeiro uso (garantido em
 * produção pelo component scan; em testes de fatia web, via
 * {@code @Import(UsuarioAtual.class)}).
 */
@Component
public class UsuarioAtual {

	private static UsuarioRepository usuarioRepository;

	public UsuarioAtual(UsuarioRepository usuarioRepository) {
		UsuarioAtual.usuarioRepository = usuarioRepository;
	}

	/**
	 * @param auth autenticação da requisição atual ({@code auth.getName()} é o e-mail normalizado do usuário)
	 * @return id do usuário autenticado
	 * @throws RecursoNaoEncontradoException se o e-mail autenticado não corresponder a nenhum usuário
	 */
	public static Long id(Authentication auth) {
		Usuario usuario = usuarioRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + auth.getName()));
		return usuario.getId();
	}

}
