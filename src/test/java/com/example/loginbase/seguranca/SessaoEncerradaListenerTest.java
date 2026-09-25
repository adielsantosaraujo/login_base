package com.example.loginbase.seguranca;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;

class SessaoEncerradaListenerTest {

	private final SessaoService sessaoService = mock(SessaoService.class);
	private final SessaoEncerradaListener listener = new SessaoEncerradaListener(sessaoService);

	@Test
	void registraFimDeSessaoComIdDoEvento() {
		MockHttpSession session = new MockHttpSession();
		HttpSessionDestroyedEvent event = new HttpSessionDestroyedEvent(session);

		listener.aoDestruirSessao(event);

		verify(sessaoService).registrarFim(event.getId());
	}

	@Test
	void engoleExcecaoDoServicoAoRegistrarFim() {
		MockHttpSession session = new MockHttpSession();
		HttpSessionDestroyedEvent event = new HttpSessionDestroyedEvent(session);
		doThrow(new IllegalStateException("falha")).when(sessaoService).registrarFim(event.getId());

		listener.aoDestruirSessao(event);

		verify(sessaoService).registrarFim(event.getId());
	}

}
