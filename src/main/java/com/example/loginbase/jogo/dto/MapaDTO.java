package com.example.loginbase.jogo.dto;

import java.util.List;

public record MapaDTO(VilaResumoDTO vila, List<RegiaoResumoDTO> regioes) {

	public record VilaResumoDTO(Long id, String nome) {
	}

}
