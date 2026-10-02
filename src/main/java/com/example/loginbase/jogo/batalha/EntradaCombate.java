package com.example.loginbase.jogo.batalha;

import com.example.loginbase.jogo.item.catalogo.ArmaCatalogo;

/**
 * Dados de um guerreiro para o cálculo dos atributos de combate. Características são totais
 * (base + itens); somas de bônus de itens em valores inteiros (percentuais em %, crítico em pp).
 *
 * @param arma arma equipada (null = desarmado, ataque de arma 0)
 * @param nivelArma nível L da arma
 * @param somaDefesaPecas soma de DEFpeça(L) das armaduras equipadas
 * @param iniciativaArmaduras soma da iniciativa extra das peças (Sapato +1)
 */
public record EntradaCombate(int vit, int forca, int vel, int inteligencia, int g, int somaVida, ArmaCatalogo arma,
		int nivelArma, double somaDefesaPecas, int iniciativaArmaduras, int somaAtkPct, int somaDefPct, int somaIni,
		int somaCrit) {
}
