package com.example.loginbase.jogo.batalha;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Detalhe de uma batalha: resumo, participantes e rodadas agrupadas (para o replay). */
public record DetalheBatalhaDTO(Long id, int turno, Long tropaId, String tropaNome, Long masmorraId,
		int masmorraNivel, int regiaoIndice, ResultadoCombate resultado, int totalRodadas, Instant criadoEm,
		List<Participante> participantes, @JsonProperty("rodadas") List<Rodada> rodadasDetalhe, Map<String, Object> recompensas) {

	/** Combatente; identificado por (lado, id). */
	public record Participante(long id, LadoCombate lado, String nome, int pvMax, int pvFinal) {
	}

	public record Ref(long id, LadoCombate lado, String nome) {
	}

	public record Acao(Ref atacante, Ref alvo, int dano, boolean critico, int pvAntes, int pvDepois,
			boolean abatido) {
	}

	public record Rodada(int numero, List<Acao> acoes) {
	}
}
