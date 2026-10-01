package com.example.loginbase.jogo.dto;

import com.example.loginbase.jogo.construcao.NivelConstrucao;
import com.example.loginbase.jogo.construcao.TipoConstrucao;
import com.example.loginbase.jogo.modelo.Jazida;

public record LadrilhoDTO(int x, int y, Jazida jazida, ConstrucaoLadrilhoDTO construcao) {

	public record ConstrucaoLadrilhoDTO(Long id, TipoConstrucao tipo, NivelConstrucao nivel, int tamanho) {
	}

}
