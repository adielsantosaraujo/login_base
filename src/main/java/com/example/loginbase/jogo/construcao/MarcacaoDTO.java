package com.example.loginbase.jogo.construcao;

public record MarcacaoDTO(Long id, Long construcaoId, int x, int y) {

	public static MarcacaoDTO de(ConstrucaoMarcacao m) {
		return new MarcacaoDTO(m.getId(), m.getConstrucaoId(), m.getX(), m.getY());
	}

}
