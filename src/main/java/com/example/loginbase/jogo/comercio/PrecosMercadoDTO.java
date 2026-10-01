package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;
import java.util.List;

/** Preços atuais do Mercado: PE do melhor Comerciante, volume do turno e preço por recurso. */
public record PrecosMercadoDTO(boolean mercadoAtivo, int peMelhorComerciante, long volumeMaximo, long volumeUsado,
		long volumeRestante, List<LinhaPreco> precos) {

	public record LinhaPreco(String recurso, String nome, BigDecimal precoBase, BigDecimal precoVenda,
			BigDecimal precoCompra) {
	}

}
