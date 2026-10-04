package com.example.loginbase.jogo.dto;

import com.example.loginbase.jogo.construcao.EstadoConstrucao;
import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.TipoTerreno;

public record LadrilhoDTO(int x, int y, TipoTerreno terreno, int bonusBase, int bonusAdjacente, int bonusTotal,
		ConstrucaoLadrilhoDTO construcao) {

	public record ConstrucaoLadrilhoDTO(Long id, TipoConstrucao tipo, NivelConstrucao nivel, int tamanho,
			EstadoConstrucao estado, int poAtual, int poTotal) {
	}

}
