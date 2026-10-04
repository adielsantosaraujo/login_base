package com.example.loginbase.jogo.cidadao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Estado da população inicial (GET /api/jogo/vila/populacao). */
public record PopulacaoDTO(boolean populacaoConfirmada, Map<Profissao, Integer> plano,
		Map<Profissao, Integer> minimos, LimitesDTO limites, Long familiaLiderSugeridaId, Long familiaLiderId,
		List<FamiliaDTO> familias) {

	public record LimitesDTO(int caracteristicasTotal, int profissoesTotal) {
	}

	public record FamiliaDTO(Long familiaId, String sobrenome, List<CidadaoDTO> cidadaos) {
	}

	public record CidadaoDTO(Long cidadaoId, String nome, Sexo sexo, int idadeAnos, PapelFamiliar papel,
			LinkedHashMap<Caracteristica, Integer> caracteristicas, LinkedHashMap<Profissao, Integer> profissoes) {
	}

}
