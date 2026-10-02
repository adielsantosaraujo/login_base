package com.example.loginbase.jogo.batalha;

/**
 * Atributos de combate calculados. Ataque/defesa em double (sem arredondamento intermediário);
 * crítico em pontos percentuais (ex.: 8.5 = 8,5%).
 */
public record AtributosCombate(int pvMax, double ataque, double defesa, int iniciativaBase, double criticoPp) {
}
