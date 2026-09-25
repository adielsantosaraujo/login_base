package com.example.loginbase.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.loginbase.acesso.PerfilPermissaoVigente;
import com.example.loginbase.acesso.Usuario;
import com.example.loginbase.acesso.UsuarioPerfilRepository;
import com.example.loginbase.acesso.UsuarioRepository;

class UsuarioDetailsServiceTest {

	private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
	private final UsuarioPerfilRepository usuarioPerfilRepository = mock(UsuarioPerfilRepository.class);

	// componente real, sem contexto Spring, conforme pedido
	private final UsuarioDetailsService service = new UsuarioDetailsService(new IdentificadorLogin(),
			usuarioRepository, usuarioPerfilRepository);

	private Usuario usuario;

	@BeforeEach
	void configurarUsuario() {
		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNome("Ana");
		usuario.setEmail("ana@exemplo.com");
		usuario.setSenha("{bcrypt}hash");
	}

	@Test
	void authoritiesIncluemRolePerfilEPermissao() {
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", "USUARIO_LER")));

		UserDetails userDetails = service.loadUserByUsername("ana@exemplo.com");

		assertThat(userDetails.getUsername()).isEqualTo("ana@exemplo.com");
		assertThat(userDetails.getPassword()).isEqualTo("{bcrypt}hash");
		assertThat(userDetails.isEnabled()).isTrue();
		assertThat(userDetails.getAuthorities())
				.extracting(Object::toString)
				.containsExactlyInAnyOrder("ROLE_ADMIN", "USUARIO_LER");
	}

	@Test
	void perfilSemPermissaoResultaApenasNaRole() {
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", null)));

		UserDetails userDetails = service.loadUserByUsername("ana@exemplo.com");

		assertThat(userDetails.getAuthorities())
				.extracting(Object::toString)
				.containsExactly("ROLE_ADMIN");
	}

	@Test
	void semPerfilVigenteFicaDesabilitado() {
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of());

		UserDetails userDetails = service.loadUserByUsername("ana@exemplo.com");

		assertThat(userDetails.isEnabled()).isFalse();
		assertThat(userDetails.getAuthorities()).isEmpty();
	}

	@Test
	void emailComCaixaEEspacosEBuscadoNormalizado() {
		when(usuarioRepository.findByEmail("ana@exemplo.com")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", null)));

		UserDetails userDetails = service.loadUserByUsername(" Ana@Exemplo.COM ");

		assertThat(userDetails.getUsername()).isEqualTo("ana@exemplo.com");
	}

	@Test
	void loginPorCelularSemMascaraUsaEmailComoUsername() {
		usuario.setCelular("11987654321");
		when(usuarioRepository.findByCelular("11987654321")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", null)));

		UserDetails userDetails = service.loadUserByUsername("11987654321");

		assertThat(userDetails.getUsername()).isEqualTo("ana@exemplo.com");
	}

	@Test
	void loginPorCelularComMascaraUsaEmailComoUsername() {
		usuario.setCelular("11987654321");
		when(usuarioRepository.findByCelular("11987654321")).thenReturn(Optional.of(usuario));
		when(usuarioPerfilRepository.findPerfisVigentesComPermissoes(eq(1L), any(LocalDate.class)))
				.thenReturn(List.of(new PerfilPermissaoVigente("ADMIN", null)));

		UserDetails userDetails = service.loadUserByUsername("(11) 98765-4321");

		assertThat(userDetails.getUsername()).isEqualTo("ana@exemplo.com");
	}

	@ParameterizedTest
	@ValueSource(strings = { "1198765432", "119876543210" })
	void celularComQuantidadeDeDigitosErradaLancaExcecaoSemConsultarRepositorio(String celular) {
		assertThatThrownBy(() -> service.loadUserByUsername(celular))
				.isInstanceOf(UsernameNotFoundException.class);

		verifyNoInteractions(usuarioRepository, usuarioPerfilRepository);
	}

	@Test
	void emailInexistenteLancaExcecao() {
		when(usuarioRepository.findByEmail("naoexiste@exemplo.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("naoexiste@exemplo.com"))
				.isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void celularInexistenteLancaExcecao() {
		when(usuarioRepository.findByCelular("11987654321")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.loadUserByUsername("11987654321"))
				.isInstanceOf(UsernameNotFoundException.class);
	}

}
