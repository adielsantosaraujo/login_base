package com.example.loginbase.jogo.batalha;

import com.example.loginbase.jogo.item.catalogo.Alcance;

/**
 * Participante da batalha (guerreiro ou inimigo), imutável. O PV atual é controlado pelo motor.
 *
 * @param id identificador estável para desempate (cidadão = id; inimigo = índice)
 * @param vel VEL total (desempate de iniciativa); 0 para inimigos
 * @param alcance alcance da arma (inimigos: FRENTE = corpo a corpo, RETAGUARDA = distância)
 * @param ignoraDefesa25 true para Besta e Xamã orc (defesa efetiva = defesa x 0,75)
 */
public record Combatente(long id, LadoCombate lado, String nome, LinhaCombate linha, int pvMax, double ataque,
		double defesa, int iniciativaBase, double criticoPp, int vel, Alcance alcance, boolean ignoraDefesa25) {
}
