package com.example.loginbase.seguranca;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Registra a abertura da sessão em {@code sessoes} após uma autenticação bem
 * sucedida e delega o redirecionamento padrão (respeitando a página salva
 * pelo {@code RequestCache}). Falha no registro é logada e engolida: não
 * impede o login.
 */
@Component
@Slf4j
public class RegistroSessaoSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private final SessaoService sessaoService;

	public RegistroSessaoSuccessHandler(SessaoService sessaoService) {
		this.sessaoService = sessaoService;
		setDefaultTargetUrl("/");
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		try {
			HttpSession session = request.getSession(false);
			if (session != null) {
				sessaoService.registrarInicio(authentication.getName(), session.getId(), request.getRemoteAddr(),
						request.getHeader("User-Agent"));
			}
		} catch (RuntimeException e) {
			log.warn("Falha ao registrar início de sessão para o usuário {}", authentication.getName(), e);
		}

		super.onAuthenticationSuccess(request, response, authentication);
	}

}
