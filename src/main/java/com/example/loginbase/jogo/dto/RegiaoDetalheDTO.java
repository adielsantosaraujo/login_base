package com.example.loginbase.jogo.dto;

import java.util.List;

import com.example.loginbase.jogo.modelo.TipoRegiao;

public record RegiaoDetalheDTO(RegiaoDTO regiao, List<LadrilhoDTO> ladrilhos) {

	public record RegiaoDTO(Long id, int indice, TipoRegiao tipo, boolean possuida,
			List<RegiaoBonusDTO> bonus) {
	}

}
