package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoTropa;

/**
 * Corpo de {@code POST /api/jogo/quartel/ordens} (ver spec game-army e
 * design.md, decisões D4 e D7). Seleção por configuração (nível da arma,
 * modelo/nível da armadura), não por id específico: {@code armaNivel} e
 * {@code armaduraNivel} MUST estar entre 1 e {@link ModeloItem#NIVEL_MAXIMO};
 * {@code quantidade} (tamanho do lote) MUST estar entre 1 e 15 — fora dessa
 * faixa, 400 {@code REQUISICAO_INVALIDA} via {@code @Valid}. A
 * disponibilidade real de armas/armaduras, a capacidade do exército e a
 * comida são validadas por {@link com.example.loginbase.jogo.quartel.QuartelService}.
 */
public record TreinarRequest(
		@NotNull TipoTropa tipo,
		@Min(1) @Max(ModeloItem.NIVEL_MAXIMO) int armaNivel,
		@NotNull ModeloItem armaduraModelo,
		@Min(1) @Max(ModeloItem.NIVEL_MAXIMO) int armaduraNivel,
		@Min(1) @Max(15) int quantidade) {
}
