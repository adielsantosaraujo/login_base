package com.example.loginbase.jogo.cidadao;

import java.util.List;
import java.util.Map;

/** Estado da população inicial (GET /api/jogo/vila/populacao). */
public record PopulacaoDTO(boolean populacaoConfirmada, Long familiaLiderId, List<FamiliaPop> familias) {

	public record FamiliaPop(Long id, String sobrenome, List<CidadaoPop> cidadaos) {
	}

	public record CidadaoPop(Long id, String nome, Sexo sexo, int idadeAnos, Map<String, Integer> caracteristicas,
			int pontosCarPendentes, int pontosProfPendentes, Map<String, Integer> profissoes) {
	}

}
