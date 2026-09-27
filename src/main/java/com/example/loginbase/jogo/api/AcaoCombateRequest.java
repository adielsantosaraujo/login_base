package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.NotNull;

import com.example.loginbase.jogo.masmorra.combate.AcaoCombate;

/**
 * Corpo de {@code POST /api/jogo/batalhas/{id}/acoes} (ver spec
 * game-dungeon-combat e design.md, seções 10 e 17). {@code tipo} nulo ou um
 * valor de {@code TipoAcao} inexistente é rejeitado com 400 ({@code @NotNull}
 * ou, para enum inválido no JSON, {@code HttpMessageNotReadableException} —
 * ambos tratados por {@link ErroApiHandler}); {@code turno} é obrigatório
 * (400 se ausente) — o motor de combate ({@link com.example.loginbase.jogo.masmorra.combate.MotorCombate})
 * rejeita com 409 {@code TURNO_DESATUALIZADO} se não corresponder ao turno
 * atual da batalha. {@code x}/{@code y} só se aplicam a {@code MOVER};
 * {@code alvoId} só a {@code ATACAR}; {@code combatenteId} é irrelevante para
 * {@code ENCERRAR_TURNO}/{@code RENDER}.
 */
public record AcaoCombateRequest(
		@NotNull AcaoCombate.TipoAcao tipo,
		String combatenteId,
		Integer x,
		Integer y,
		String alvoId,
		@NotNull Integer turno) {
}
