package com.example.loginbase.jogo.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.modelo.BonusRegiao;
import com.example.loginbase.jogo.modelo.TipoRegiao;

/** Resumo da vila (resposta do POST e do GET /api/jogo/vila). */
public record VilaResumoDTO(Long vilaId, String nome, Long semente, Integer turnoCriacao,
		List<Regiao> regioes, Map<String, BigDecimal> estoque, boolean populacaoConfirmada,
		Map<BonusRegiao, Integer> bonusRegiao) {

	public record Regiao(int indice, TipoRegiao tipo, boolean possuida, List<RegiaoBonusDTO> bonus) {
	}

}
