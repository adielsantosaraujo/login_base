package com.example.loginbase.seguranca;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.acesso.PerfilPermissaoVigente;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioPerfilRepository;
import com.example.loginbase.acesso.UsuarioRepository;
import com.example.loginbase.seguranca.IdentificadorLogin.Identificador;
import com.example.loginbase.seguranca.IdentificadorLogin.Tipo;

/**
 * Carrega o usuário para autenticação a partir do identificador digitado no
 * login (e-mail ou celular). O {@code username} do {@link UserDetails}
 * devolvido é sempre o e-mail do usuário, mesmo quando o login foi feito
 * pelo celular, para que auditoria e registro de sessões tenham um
 * identificador único (ver design.md, decisão 5).
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final IdentificadorLogin identificadorLogin;
	private final UsuarioRepository usuarioRepository;
	private final UsuarioPerfilRepository usuarioPerfilRepository;

	public UsuarioDetailsService(IdentificadorLogin identificadorLogin, UsuarioRepository usuarioRepository,
			UsuarioPerfilRepository usuarioPerfilRepository) {
		this.identificadorLogin = identificadorLogin;
		this.usuarioRepository = usuarioRepository;
		this.usuarioPerfilRepository = usuarioPerfilRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Identificador identificador = identificadorLogin.interpretar(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

		Usuario usuario = buscar(identificador)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

		List<PerfilPermissaoVigente> perfisVigentes = usuarioPerfilRepository
				.findPerfisVigentesComPermissoes(usuario.getId(), LocalDate.now());

		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		for (PerfilPermissaoVigente item : perfisVigentes) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + item.perfil()));
			if (item.permissao() != null) {
				authorities.add(new SimpleGrantedAuthority(item.permissao()));
			}
		}

		return User.withUsername(usuario.getEmail())
				.password(usuario.getSenha())
				.authorities(authorities)
				.disabled(perfisVigentes.isEmpty())
				.build();
	}

	private Optional<Usuario> buscar(Identificador identificador) {
		if (identificador.tipo() == Tipo.EMAIL) {
			return usuarioRepository.findByEmail(identificador.valor());
		}
		return usuarioRepository.findByCelular(identificador.valor());
	}

}
