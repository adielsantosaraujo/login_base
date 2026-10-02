package com.example.loginbase.jogo.item;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/** Implementação padrão: ninguém está em expedição. */
@Component
@ConditionalOnMissingBean(value = ConsultaTropas.class, ignored = ConsultaTropasVazia.class)
public class ConsultaTropasVazia implements ConsultaTropas {

	@Override
	public boolean emExpedicao(Long cidadaoId) {
		return false;
	}
}
