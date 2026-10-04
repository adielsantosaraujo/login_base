package com.example.loginbase.jogo.dto;

import java.util.List;
import java.util.Map;

import com.example.loginbase.jogo.modelo.BonusRegiao;

public record MapaDTO(VilaResumoDTO vila, List<RegiaoResumoDTO> regioes) {

	public record VilaResumoDTO(Long id, String nome, Map<BonusRegiao, Integer> bonusRegiao) {
	}

}
