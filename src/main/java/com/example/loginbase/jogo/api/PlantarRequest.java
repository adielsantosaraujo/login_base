package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.NotNull;

import com.example.loginbase.jogo.catalogo.Cultivo;

/**
 * Corpo de {@code POST /api/jogo/canteiros/{posicao}/plantar} (ver spec
 * game-farming e design.md, seção 17). Um valor de {@code cultivo} que não
 * corresponda a nenhum {@link Cultivo} falha na desserialização do JSON
 * (tratado como 400 por {@link ErroApiHandler}, via
 * {@code HttpMessageNotReadableException}); {@code cultivo} nulo é rejeitado
 * por {@code @NotNull} (400 via {@code MethodArgumentNotValidException}).
 */
public record PlantarRequest(@NotNull Cultivo cultivo) {
}
