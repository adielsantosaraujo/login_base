package com.example.loginbase.jogo.cidadao;

import java.util.List;
import java.util.Map;

/**
 * Distribuição inicial: pontos a aplicar por cidadão (chaves das características: VIT, FOR, VEL, INT, CAR;
 * das profissões: nomes do enum Profissao) e a família líder.
 */
public record DistribuirPopulacaoRequest(Long familiaLiderId, List<DistribuicaoCidadao> cidadaos) {

	public record DistribuicaoCidadao(Long cidadaoId, Map<String, Integer> caracteristicas,
			Map<String, Integer> profissoes) {
	}

}
