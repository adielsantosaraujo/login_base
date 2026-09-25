package com.example.loginbase.seguranca;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class SessoesAbertasRunnerTest {

	private final SessaoService sessaoService = mock(SessaoService.class);
	private final SessoesAbertasRunner runner = new SessoesAbertasRunner(sessaoService);

	@Test
	void delegaFechamentoDasSessoesAbertasAoServico() {
		when(sessaoService.fecharTodasAbertas()).thenReturn(3);

		runner.run(new DefaultApplicationArguments());

		verify(sessaoService).fecharTodasAbertas();
	}

}
