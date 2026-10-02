package com.example.loginbase.jogo.batalha;

import java.time.Instant;

/** Item da listagem de batalhas da vila. */
public record ResumoBatalhaDTO(Long id, int turno, Long tropaId, String tropaNome, Long masmorraId,
		int masmorraNivel, int regiaoIndice, ResultadoCombate resultado, int rodadas, Instant criadoEm) {

	public static ResumoBatalhaDTO de(Batalha b) {
		return new ResumoBatalhaDTO(b.getId(), b.getTurno(), b.getTropaId(), b.getTropaNome(), b.getMasmorraId(),
				b.getMasmorraNivel(), b.getRegiaoIndice(), b.getResultado(), b.getRodadas(), b.getCriadoEm());
	}
}
