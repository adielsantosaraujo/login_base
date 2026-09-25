package com.example.loginbase.seguranca;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Fecha, na inicialização da aplicação, os registros de {@code sessoes} que
 * ficaram abertos ({@code data_fim} nula) em decorrência de reinício ou
 * queda: as sessões HTTP em memória não sobrevivem ao restart, então seus
 * eventos de destruição nunca são publicados.
 */
@Component
@Slf4j
public class SessoesAbertasRunner implements ApplicationRunner {

	private final SessaoService sessaoService;

	public SessoesAbertasRunner(SessaoService sessaoService) {
		this.sessaoService = sessaoService;
	}

	@Override
	public void run(ApplicationArguments args) {
		int n = sessaoService.fecharTodasAbertas();
		log.info("Fechados {} registro(s) de sessão que estavam abertos ao iniciar a aplicação", n);
	}

}
