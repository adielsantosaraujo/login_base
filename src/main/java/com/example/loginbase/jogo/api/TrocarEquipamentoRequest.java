package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.example.loginbase.jogo.catalogo.SlotEquipamento;

/**
 * Corpo de {@code POST /api/jogo/unidades/{id}/equipamento} (ver spec
 * game-army — Requirement: Troca de equipamento, e design.md, Decisão D12).
 * Um valor de {@code slot} que não corresponda a nenhum {@link SlotEquipamento}
 * falha na desserialização do JSON (400 via
 * {@code HttpMessageNotReadableException}); {@code slot}/{@code itemId} nulos
 * ou {@code itemId} não positivo são rejeitados por {@code @NotNull}/
 * {@code @Positive} (400 via {@code MethodArgumentNotValidException}).
 */
public record TrocarEquipamentoRequest(@NotNull SlotEquipamento slot, @NotNull @Positive Long itemId) {
}
