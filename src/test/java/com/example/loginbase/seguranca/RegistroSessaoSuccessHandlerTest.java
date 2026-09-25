package com.example.loginbase.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class RegistroSessaoSuccessHandlerTest {

	private final SessaoService sessaoService = mock(SessaoService.class);
	private final RegistroSessaoSuccessHandler handler = new RegistroSessaoSuccessHandler(sessaoService);

	@Test
	void registraInicioDeSessaoERedirecionaParaRaiz() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setRemoteAddr("127.0.0.1");
		request.addHeader("User-Agent", "algum-agente");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated("ana@exemplo.com", null,
				List.of());

		handler.onAuthenticationSuccess(request, response, authentication);

		verify(sessaoService).registrarInicio("ana@exemplo.com", session.getId(), "127.0.0.1", "algum-agente");
		assertThat(response.getRedirectedUrl()).isEqualTo("/");
	}

	@Test
	void redirecionaMesmoQuandoRegistroDeSessaoFalha() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpSession session = new MockHttpSession();
		request.setSession(session);
		request.setRemoteAddr("127.0.0.1");
		request.addHeader("User-Agent", "algum-agente");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Authentication authentication = UsernamePasswordAuthenticationToken.authenticated("ana@exemplo.com", null,
				List.of());
		doThrow(new IllegalStateException("Usuário não encontrado")).when(sessaoService)
				.registrarInicio("ana@exemplo.com", session.getId(), "127.0.0.1", "algum-agente");

		handler.onAuthenticationSuccess(request, response, authentication);

		assertThat(response.getRedirectedUrl()).isEqualTo("/");
	}

}
