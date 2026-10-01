package com.example.loginbase.jogo.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.example.loginbase.jogo.modelo.TipoRegiao;
import com.example.loginbase.jogo.recurso.Recurso;

/** Resultado da anexação: região, estoque após o débito e custo pago. */
public record AnexacaoDTO(RegiaoAnexadaDTO regiao, Map<Recurso, BigDecimal> estoque, CustoAnexacaoDTO custo) {

	public record RegiaoAnexadaDTO(int indice, TipoRegiao tipo, boolean possuida) {
	}

}
