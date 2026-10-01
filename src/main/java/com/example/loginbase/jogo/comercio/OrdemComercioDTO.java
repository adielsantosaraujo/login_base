package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;
import java.time.Instant;

/** Ordem de comércio exibida no histórico do Mercado. */
public record OrdemComercioDTO(Long id, int turno, String tipo, String recurso, int quantidade,
		BigDecimal precoUnitario, BigDecimal ouroTotal, Instant criadoEm) {

	public static OrdemComercioDTO de(OrdemComercio o) {
		return new OrdemComercioDTO(o.getId(), o.getTurno(), o.getTipo().name(), o.getRecurso().name(),
				o.getQuantidade(), o.getPrecoUnitario(), o.getOuroTotal(), o.getCriadoEm());
	}

}
