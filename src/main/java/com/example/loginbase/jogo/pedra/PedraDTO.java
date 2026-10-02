package com.example.loginbase.jogo.pedra;

import java.util.List;

public record PedraDTO(Long id, TipoPedra qualidade, List<BonusPedra> bonus, int custoEngaste) {

	public static PedraDTO de(Pedra p) {
		return new PedraDTO(p.getId(), p.getQualidade(), List.copyOf(p.getBonus()),
				p.getQualidade().getCustoEngaste());
	}
}
