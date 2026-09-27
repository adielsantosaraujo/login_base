package com.example.loginbase.jogo.api;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Corpo de {@code POST /api/jogo/masmorras/{nivel}/batalhas} (ver spec
 * game-dungeon-combat e design.md, seções 10 e 17). {@code unidadeIds} MUST
 * ter de 1 a 4 ids (400 via {@code @Valid} quando vazio/nulo/maior que 4);
 * {@link com.example.loginbase.jogo.masmorra.MasmorraService} repete essa
 * validação (defesa em profundidade, incluindo distinção/disponibilidade das
 * unidades), lançando {@code RegraJogoException(ESQUADRAO_INVALIDO/UNIDADE_INDISPONIVEL)}
 * (422) caso alguém a chame fora do controller.
 */
public record IniciarBatalhaRequest(
		@NotEmpty(message = "Informe ao menos uma unidade.")
		@Size(max = 4, message = "O esquadrão deve ter no máximo 4 unidades.")
		List<Long> unidadeIds) {
}
