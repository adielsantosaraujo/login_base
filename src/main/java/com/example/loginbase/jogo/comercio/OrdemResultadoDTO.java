package com.example.loginbase.jogo.comercio;

import java.math.BigDecimal;

public record OrdemResultadoDTO(boolean sucesso, String tipo, String recurso, int quantidade,
		BigDecimal precoUnitario, BigDecimal ouroTotal, BigDecimal recursoNovo, BigDecimal ouroNovo,
		long volumeUsado, long volumeMaximo) {
}
