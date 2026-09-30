package com.example.loginbase.jogo.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.example.loginbase.jogo.catalogo.ModeloItem;

/**
 * Corpo de {@code POST /api/jogo/forja/ordens} (ver spec game-forge e
 * design.md, seções 7 e 17). {@code nivel} e {@code quantidade} MUST estar
 * entre 1–{@link ModeloItem#NIVEL_MAXIMO} (23) e 1–{@link ModeloItem#QUANTIDADE_MAXIMA_ORDEM}
 * (400 via {@code @Valid} quando fora da faixa); o nível máximo
 * efetivamente forjável depende do nível da FORJA (faixas, ver {@code TipoPredio#nivelMaximoForjavel}); {@link ForjaService}
 * repete essa validação (defesa em profundidade), lançando
 * {@code RegraJogoException(REQUISICAO_INVALIDA)} caso alguém a chame fora
 * do controller.
 */
public record ForjarRequest(
		@NotNull ModeloItem modelo,
		@Min(1) @Max(ModeloItem.NIVEL_MAXIMO) int nivel,
		@Min(1) @Max(ModeloItem.QUANTIDADE_MAXIMA_ORDEM) int quantidade) {
}
