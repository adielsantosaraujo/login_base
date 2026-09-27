package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.example.loginbase.jogo.catalogo.TipoTropa;

/**
 * Corpo de {@code POST /api/jogo/quartel/ordens} (ver spec game-army e
 * design.md, seções 8 e 17). {@code armaId}/{@code armaduraId} MUST ser
 * positivos (400 via {@code @Valid}); a existência e disponibilidade real
 * dos itens é validada por {@link QuartelService}.
 */
public record TreinarRequest(
		@NotNull TipoTropa tipo,
		@Min(1) long armaId,
		@Min(1) long armaduraId) {
}
