package com.example.loginbase.jogo.recurso;

import java.math.BigDecimal;
import java.util.List;

/** Estoque da vila: uma linha por recurso. Capacidade e percentual são nulos para recursos ilimitados (Ouro). */
public record EstoqueDTO(List<LinhaEstoque> recursos) {

	public record LinhaEstoque(String recurso, String nome, BigDecimal quantidade, BigDecimal capacidade,
			BigDecimal percentualUsado) {
	}

}
