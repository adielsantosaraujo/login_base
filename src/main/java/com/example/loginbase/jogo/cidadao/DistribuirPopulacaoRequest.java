package com.example.loginbase.jogo.cidadao;

import java.util.List;
import java.util.Map;

/**
 * Confirmação da população: valores ABSOLUTOS por cidadão (chaves das características: VIT, FOR, VEL, INT, CAR; das
 * profissões: nomes do enum Profissao) e a família líder.
 */
public record DistribuirPopulacaoRequest(Long familiaLiderId, List<FamiliaEntradaDTO> familias) {

	public record FamiliaEntradaDTO(Long familiaId, List<CidadaoEntradaDTO> cidadaos) {
	}

	public record CidadaoEntradaDTO(Long cidadaoId, Map<String, Integer> caracteristicas,
			Map<String, Integer> profissoes) {
	}

}
