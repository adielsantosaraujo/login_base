package com.example.loginbase.jogo.dto;

import java.util.List;

import com.example.loginbase.jogo.modelo.TipoRegiao;

public record RegiaoResumoDTO(int indice, TipoRegiao tipo, boolean possuida, boolean masmorraAtiva,
		Integer nivelMasmorra, Long masmorraId,
		List<RegiaoBonusDTO> bonus) {
}
