package com.example.loginbase.auditoria;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolve o autor de auditoria a partir do usuário autenticado no
 * {@link SecurityContextHolder}. Quando não há usuário autenticado (processo
 * do sistema, carga inicial, autenticação anônima), o autor é {@link #SISTEMA}.
 */
@Component("auditorAware")
public class UsuarioAuditorAware implements AuditorAware<String> {

	public static final String SISTEMA = "sistema";

	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

		if (autenticacao == null
				|| !autenticacao.isAuthenticated()
				|| autenticacao instanceof AnonymousAuthenticationToken) {
			return Optional.of(SISTEMA);
		}

		return Optional.of(autenticacao.getName());
	}

}
