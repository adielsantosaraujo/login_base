package com.example.loginbase.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Publica os eventos de ciclo de vida da sessão HTTP do container
 * ({@link jakarta.servlet.http.HttpSessionEvent}) como eventos do Spring,
 * permitindo que {@link SessaoEncerradaListener} seja notificado quando uma
 * sessão é destruída (logout, expiração ou invalidação).
 */
@Configuration
public class SessaoEventosConfig {

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

}
