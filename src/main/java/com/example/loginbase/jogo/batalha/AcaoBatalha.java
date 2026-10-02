package com.example.loginbase.jogo.batalha;

/**
 * Uma ação (ataque) registrada no log da batalha.
 *
 * @param rodada rodada (1..30)
 * @param atacanteId id do atacante (cidadão ou índice do inimigo)
 * @param atacanteLado lado do atacante
 * @param alvoId id do alvo
 * @param alvoLado lado do alvo
 * @param dano dano aplicado (já com crítico)
 * @param critico true se foi crítico (x1,5)
 * @param pvAntes PV do alvo antes do golpe
 * @param pvDepois PV do alvo depois do golpe (mínimo 0)
 * @param abatido true se o alvo chegou a 0 PV
 */
public record AcaoBatalha(int rodada, long atacanteId, LadoCombate atacanteLado, long alvoId, LadoCombate alvoLado,
		int dano, boolean critico, int pvAntes, int pvDepois, boolean abatido) {
}
