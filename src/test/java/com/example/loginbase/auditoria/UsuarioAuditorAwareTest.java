package com.example.loginbase.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class UsuarioAuditorAwareTest {

	private final UsuarioAuditorAware auditorAware = new UsuarioAuditorAware();

	@AfterEach
	void limparContextoSeguranca() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void deveRetornarEmailDoUsuarioAutenticado() {
		SecurityContextHolder.getContext().setAuthentication(
				UsernamePasswordAuthenticationToken.authenticated("ana@exemplo.com", null, List.of()));

		Optional<String> autor = auditorAware.getCurrentAuditor();

		assertThat(autor).contains("ana@exemplo.com");
	}

	@Test
	void deveRetornarSistemaQuandoAutenticacaoAnonima() {
		SecurityContextHolder.getContext().setAuthentication(
				new AnonymousAuthenticationToken("chave", "anonimo", List.of(() -> "ROLE_ANONYMOUS")));

		Optional<String> autor = auditorAware.getCurrentAuditor();

		assertThat(autor).contains(UsuarioAuditorAware.SISTEMA);
	}

	@Test
	void deveRetornarSistemaQuandoNaoHaAutenticacao() {
		SecurityContextHolder.clearContext();

		Optional<String> autor = auditorAware.getCurrentAuditor();

		assertThat(autor).contains(UsuarioAuditorAware.SISTEMA);
	}

}
