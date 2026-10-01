package com.example.loginbase.jogo.turno;

import java.time.Instant;

/**
 * Turno corrente do jogo. Sem nenhum turno ainda: {@code numero = 0},
 * {@code iniciadoEm}/{@code proximoEm} nulos e {@code segundosRestantes = 0}.
 * {@code segundosRestantes} nunca é negativo (0 se o próximo turno está atrasado).
 */
public record TurnoDTO(int numero, Instant iniciadoEm, Instant proximoEm, long segundosRestantes) {
}
