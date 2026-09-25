package com.example.loginbase.seguranca;

import org.springframework.context.event.EventListener;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Preenche {@code data_fim} do registro de {@code sessoes} correspondente
 * quando a sessão HTTP é destruída, seja por logout, expiração por timeout ou
 * invalidação. Falha ao registrar o encerramento é logada e engolida.
 */
@Component
@Slf4j
public class SessaoEncerradaListener {

	private final SessaoService sessaoService;

	public SessaoEncerradaListener(SessaoService sessaoService) {
		this.sessaoService = sessaoService;
	}

	@EventListener
	public void aoDestruirSessao(HttpSessionDestroyedEvent event) {
		try {
			sessaoService.registrarFim(event.getId());
		} catch (RuntimeException e) {
			log.warn("Falha ao registrar fim de sessão para o id {}", event.getId(), e);
		}
	}

}
