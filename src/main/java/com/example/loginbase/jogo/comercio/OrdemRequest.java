package com.example.loginbase.jogo.comercio;

/** Corpo do POST /api/jogo/mercado/ordens: recurso (nome do enum), tipo COMPRA|VENDA e quantidade inteira. */
public record OrdemRequest(String recurso, String tipo, Integer quantidade) {
}
