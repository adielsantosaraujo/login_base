package com.example.loginbase.jogo.construcao;

public record ConstrucaoDTO(Long id, TipoConstrucao tipo, NivelConstrucao nivel, int regiaoIndice, int x, int y,
		int tamanho, EstadoConstrucao estado, int poTotal, int poAtual) {

	public static ConstrucaoDTO de(Construcao c) {
		return new ConstrucaoDTO(c.getId(), c.getTipo(), c.getNivel(), c.getRegiaoIndice(), c.getX(), c.getY(),
				c.getTamanho(), c.getEstado(), c.getPoTotal(), c.getPoAtual());
	}

}
