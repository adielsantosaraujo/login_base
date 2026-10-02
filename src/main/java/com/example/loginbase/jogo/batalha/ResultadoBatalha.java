package com.example.loginbase.jogo.batalha;

import java.util.List;

/**
 * Resultado do motor.
 *
 * @param resultado VITORIA (todos os inimigos abatidos) ou DERROTA (tropa eliminada ou 30 rodadas sem vencedor)
 * @param rodadas rodadas disputadas (a última pode ser parcial se o combate acabou no meio dela)
 * @param log ações em ordem cronológica
 * @param abatidosTropa ids dos membros da tropa abatidos, na ordem em que caíram
 * @param pvFinal PV final de todos os combatentes (tropa primeiro, depois inimigos)
 */
public record ResultadoBatalha(ResultadoCombate resultado, int rodadas, List<AcaoBatalha> log,
		List<Long> abatidosTropa, List<PvFinal> pvFinal) {

	/** PV final de um combatente. */
	public record PvFinal(long id, LadoCombate lado, String nome, int pvMax, int pv) {
	}
}
